package com.example.smartcard

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.smartcard.data.remote.Product
import com.example.smartcard.ml.MlRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

private const val TAG = "CameraScreenML"

@OptIn(ExperimentalGetImage::class)
@Composable
fun CameraScreen(
    onBack: () -> Unit,
    onProductFoundGoCart: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val db = remember { FirebaseFirestore.getInstance() }

    var productName by remember { mutableStateOf<String?>(null) }
    var statusMessage by remember { mutableStateOf("Наведите камеру на товар") }
    var barcodeFallbackMode by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }
    var productAdded by remember { mutableStateOf(false) }
    var lastMlRequestTime by remember { mutableLongStateOf(0L) }

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            Log.d(TAG, "Camera permission result: $granted")
            hasPermission = granted
        }

    LaunchedEffect(Unit) {
        Log.d(TAG, "CameraScreen started, hasPermission=$hasPermission")
        if (!hasPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(12.dp),
        verticalArrangement = Arrangement.Top
    ) {
        TextButton(onClick = onBack) {
            Text("Back", color = Color.White)
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (!hasPermission) {
            Text("Camera permission required", color = Color.White)
            return@Column
        }

        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(16.dp)),
            factory = { ctx ->
                Log.d(TAG, "AndroidView factory started")

                val previewView = PreviewView(ctx)
                previewView.scaleType = PreviewView.ScaleType.FILL_CENTER

                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
                val barcodeScanner = BarcodeScanning.getClient()

                cameraProviderFuture.addListener({
                    Log.d(TAG, "cameraProviderFuture listener called")
                    try {
                        val cameraProvider = cameraProviderFuture.get()
                        Log.d(TAG, "CameraProvider obtained")

                        val preview = Preview.Builder()
                            .build()
                            .also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }

                        val analyzer = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()
                            .also { analysis ->
                                analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                                    try {
                                        if (productAdded) {
                                            imageProxy.close()
                                            return@setAnalyzer
                                        }

                                        if (isProcessing) {
                                            imageProxy.close()
                                            return@setAnalyzer
                                        }

                                        if (!barcodeFallbackMode) {
                                            handleMlModeWithLogs(
                                                context = context,
                                                imageProxy = imageProxy,
                                                db = db,
                                                scope = scope,
                                                lastMlRequestTime = lastMlRequestTime,
                                                onLastMlRequestTimeChanged = { lastMlRequestTime = it },
                                                onProcessingChanged = { isProcessing = it },
                                                onStatusChanged = { statusMessage = it },
                                                onProductNameChanged = { productName = it },
                                                onEnableBarcodeFallback = { barcodeFallbackMode = true },
                                                onProductAdded = { productAdded = true },
                                                onGoCart = onProductFoundGoCart
                                            )
                                        } else {
                                            handleBarcodeModeWithLogs(
                                                imageProxy = imageProxy,
                                                barcodeScanner = barcodeScanner,
                                                db = db,
                                                scope = scope,
                                                onProcessingChanged = { isProcessing = it },
                                                onStatusChanged = { statusMessage = it },
                                                onProductNameChanged = { productName = it },
                                                onProductAdded = { productAdded = true },
                                                onGoCart = onProductFoundGoCart
                                            )
                                        }
                                    } catch (e: Exception) {
                                        Log.e(TAG, "Analyzer crash", e)
                                        imageProxy.close()
                                    }
                                }
                            }

                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            analyzer
                        )
                        Log.d(TAG, "Camera bound successfully")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error binding camera", e)
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = statusMessage, color = Color.Black)

                productName?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Product: $it", color = Color.Black)
                }

                if (isProcessing) {
                    Spacer(modifier = Modifier.height(8.dp))
                    CircularProgressIndicator()
                }
            }
        }
    }
}

private fun handleMlModeWithLogs(
    context: Context,
    imageProxy: ImageProxy,
    db: FirebaseFirestore,
    scope: kotlinx.coroutines.CoroutineScope,
    lastMlRequestTime: Long,
    onLastMlRequestTimeChanged: (Long) -> Unit,
    onProcessingChanged: (Boolean) -> Unit,
    onStatusChanged: (String) -> Unit,
    onProductNameChanged: (String?) -> Unit,
    onEnableBarcodeFallback: () -> Unit,
    onProductAdded: () -> Unit,
    onGoCart: () -> Unit
) {
    val now = System.currentTimeMillis()

    if (now - lastMlRequestTime < 1500) {
        imageProxy.close()
        return
    }

    onLastMlRequestTimeChanged(now)
    onProcessingChanged(true)
    onStatusChanged("Распознаем товар...")
    Log.d(TAG, "ML mode started")

    val file = try {
        imageProxyToJpegFile(context, imageProxy)
    } catch (e: Exception) {
        Log.e(TAG, "imageProxyToJpegFile failed", e)
        onProcessingChanged(false)
        onStatusChanged("Ошибка подготовки кадра")
        imageProxy.close()
        return
    }

    scope.launch {
        try {
            Log.d(TAG, "Sending frame to ML: ${file.absolutePath}")

            val mlResult = withContext(Dispatchers.IO) {
                MlRepository.sendFrameToMl(
                    file = file,
                    barcodeProduct = "x"
                )
            }

            Log.d(TAG, "ML response = $mlResult")

            val bestDetection = mlResult?.detections?.maxByOrNull { it.yolo_confidence ?: 0.0 }
            val yoloClassRaw = bestDetection?.yolo_class?.trim()?.lowercase()
            val yoloConfidence = bestDetection?.yolo_confidence ?: 0.0
            val yoloClass = normalizeYoloLabel(yoloClassRaw)

            Log.d(TAG, "yoloClassRaw=$yoloClassRaw, yoloClass=$yoloClass, yoloConfidence=$yoloConfidence")

            if (yoloClass != null && yoloConfidence >= 0.65) {
                onStatusChanged("Найдено: $yoloClass")
                Log.d(TAG, "YOLO confident enough, searching Firestore by ml_label")

                val result = db.collection("products")
                    .whereEqualTo("ml_label", yoloClass)
                    .get()
                    .await()

                Log.d(TAG, "Firestore ml_label success, empty=${result.isEmpty}")

                if (!result.isEmpty) {
                    val document = result.documents[0]

                    val product = Product(
                        name = document.getString("name") ?: "",
                        brand = document.getString("brand") ?: "",
                        price = document.getDouble("price") ?: 0.0,
                        barcode = document.getString("barcode") ?: ""
                    )

                    Log.d(TAG, "Product from Firestore: $product")

                    addProductToCartWithLogs(
                        product = product,
                        onSuccess = {
                            Log.d(TAG, "Product added to cart successfully")
                            onProductNameChanged(product.name)
                            onStatusChanged("Товар распознан и добавлен")
                            onProductAdded()
                            onProcessingChanged(false)
                            onGoCart()
                        },
                        onError = { error ->
                            Log.e(TAG, "addProductToCart error: $error")
                            onStatusChanged(error)
                            onProcessingChanged(false)
                            onEnableBarcodeFallback()
                        }
                    )
                } else {
                    Log.d(TAG, "No product found by ml_label=$yoloClass")
                    onStatusChanged("Товар не найден в базе. Сканируйте штрихкод")
                    onProcessingChanged(false)
                    onEnableBarcodeFallback()
                }
            } else {
                Log.d(TAG, "YOLO confidence too low or yoloClass null")
                onStatusChanged("Товар не распознан. Сканируйте штрихкод")
                onProcessingChanged(false)
                onEnableBarcodeFallback()
            }
        } catch (e: Exception) {
            Log.e(TAG, "ML mode exception", e)
            onStatusChanged("Ошибка ML: ${e.message}")
            onProcessingChanged(false)
            onEnableBarcodeFallback()
        } finally {
            try {
                if (file.exists()) file.delete()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete temp file", e)
            }
            imageProxy.close()
        }
    }
}

@OptIn(ExperimentalGetImage::class)
private fun handleBarcodeModeWithLogs(
    imageProxy: ImageProxy,
    barcodeScanner: BarcodeScanner,
    db: FirebaseFirestore,
    scope: kotlinx.coroutines.CoroutineScope,
    onProcessingChanged: (Boolean) -> Unit,
    onStatusChanged: (String) -> Unit,
    onProductNameChanged: (String?) -> Unit,
    onProductAdded: () -> Unit,
    onGoCart: () -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage == null) {
        Log.d(TAG, "Barcode mode: mediaImage is null")
        imageProxy.close()
        return
    }

    val image = InputImage.fromMediaImage(
        mediaImage,
        imageProxy.imageInfo.rotationDegrees
    )

    barcodeScanner.process(image)
        .addOnSuccessListener { barcodes ->
            val value = barcodes.firstOrNull()?.rawValue
            Log.d(TAG, "Barcode scan result = $value")

            if (value != null) {
                onProcessingChanged(true)
                onStatusChanged("Штрихкод найден, ищем товар...")

                scope.launch {
                    try {
                        val result = db.collection("products")
                            .whereEqualTo("barcode", value)
                            .get()
                            .await()

                        Log.d(TAG, "Firestore barcode success, empty=${result.isEmpty}")

                        if (!result.isEmpty) {
                            val document = result.documents[0]

                            val product = Product(
                                name = document.getString("name") ?: "",
                                brand = document.getString("brand") ?: "",
                                price = document.getDouble("price") ?: 0.0,
                                barcode = document.getString("barcode") ?: ""
                            )

                            Log.d(TAG, "Product by barcode: $product")

                            addProductToCartWithLogs(
                                product = product,
                                onSuccess = {
                                    Log.d(TAG, "Product added by barcode")
                                    onProductNameChanged(product.name)
                                    onStatusChanged("Товар добавлен по штрихкоду")
                                    onProductAdded()
                                    onProcessingChanged(false)
                                    onGoCart()
                                },
                                onError = { error ->
                                    Log.e(TAG, "addProductToCart barcode error: $error")
                                    onStatusChanged(error)
                                    onProcessingChanged(false)
                                }
                            )
                        } else {
                            Log.d(TAG, "No product found by barcode")
                            onStatusChanged("Товар по штрихкоду не найден")
                            onProcessingChanged(false)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Firestore barcode query failed", e)
                        onStatusChanged("Ошибка базы: ${e.message}")
                        onProcessingChanged(false)
                    }
                }
            }
        }
        .addOnFailureListener { e ->
            Log.e(TAG, "Barcode scanner failed", e)
            onStatusChanged("Ошибка сканирования штрихкода")
        }
        .addOnCompleteListener {
            imageProxy.close()
        }
}

private fun addProductToCartWithLogs(
    product: Product,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    try {
        Log.d(TAG, "addProductToCart called with product=$product")

        val emoji = when (product.barcode) {
            "1234567890123" -> "🥛"
            "1234567890179" -> "🧼"
            "1234567890155" -> "🥔"
            else -> "🛍️"
        }

        val connectedCartId = CartConnectionSession.connectedCartId
        Log.d(TAG, "connectedCartId=$connectedCartId")

        if (!connectedCartId.isNullOrBlank()) {
            RemoteCartRepository.addProductToRemoteCart(
                cartId = connectedCartId,
                product = product.copy(),
                onSuccess = {
                    Log.d(TAG, "RemoteCartRepository success")
                    onSuccess()
                },
                onError = { error ->
                    Log.e(TAG, "RemoteCartRepository error: $error")
                    onError("Error: $error")
                }
            )
        } else {
            CartSession.addOrIncrement(
                CartItem(
                    barcode = product.barcode,
                    name = product.name,
                    brand = product.brand,
                    price = product.price.toInt().toDouble(),
                    imageEmoji = emoji
                )
            )
            Log.d(TAG, "CartSession.addOrIncrement success")
            onSuccess()
        }
    } catch (e: Exception) {
        Log.e(TAG, "addProductToCart crashed", e)
        onError("Ошибка корзины: ${e.message}")
    }
}

private fun imageProxyToJpegFile(context: Context, imageProxy: ImageProxy): File {
    val yBuffer = imageProxy.planes[0].buffer
    val uBuffer = imageProxy.planes[1].buffer
    val vBuffer = imageProxy.planes[2].buffer

    val ySize = yBuffer.remaining()
    val uSize = uBuffer.remaining()
    val vSize = vBuffer.remaining()

    val nv21 = ByteArray(ySize + uSize + vSize)

    yBuffer.get(nv21, 0, ySize)
    vBuffer.get(nv21, ySize, vSize)
    uBuffer.get(nv21, ySize + vSize, uSize)

    val yuvImage = YuvImage(
        nv21,
        ImageFormat.NV21,
        imageProxy.width,
        imageProxy.height,
        null
    )

    val out = ByteArrayOutputStream()
    yuvImage.compressToJpeg(
        Rect(0, 0, imageProxy.width, imageProxy.height),
        90,
        out
    )

    val file = File(context.cacheDir, "ml_frame_${System.currentTimeMillis()}.jpg")
    file.writeBytes(out.toByteArray())

    Log.d(TAG, "imageProxyToJpegFile created: ${file.absolutePath}, size=${file.length()}")
    return file
}

private fun normalizeYoloLabel(label: String?): String? {
    if (label == null) return null

    return when (label.lowercase()) {

        // 🍎 Фрукты
        "apple" -> "apple"
        "banana" -> "banana"
        "orange" -> "orange"

        // 🥤 Coca-Cola
        "coca-cola_can" -> "coca-cola can"
        "coca-cola_box" -> "coca-cola carton"
        "coca-cola_bottle" -> "coca-cola bottle"

        // 🥤 Fanta
        "fanta_box" -> "fanta carton"
        "fanta_bottle" -> "fanta bottle"

        // 🥤 Fuse Tea
        "fusetea_box" -> "fuse tea carton"
        "fusetea_bottle" -> "fuse tea bottle"

        // 🥤 Sprite
        "sprite_box" -> "sprite carton"
        "sprite_bottle" -> "sprite bottle"

        else -> {
            // fallback если вдруг новый класс появится
            label.replace("_", " ")
        }
    }
}