package com.example.smartcard

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

@OptIn(ExperimentalGetImage::class)
@Composable
fun CameraScreen(
    onBack: () -> Unit,
    onProductFoundGoCart: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var productName by remember { mutableStateOf<String?>(null) }
    var scanning by remember { mutableStateOf(true) }

    val db = FirebaseFirestore.getInstance()

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
            hasPermission = it
        }

    LaunchedEffect(Unit) {
        if (!hasPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A1A))
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AppBackButton(onClick = onBack)
            Spacer(Modifier.width(12.dp))
            Text(
                "Scan Product",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (!hasPermission) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Camera permission required",
                    color = Color.White,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
            }
            return@Column
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            AndroidView(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(AppRadius.Large),
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    val executor = Executors.newSingleThreadExecutor()

                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()

                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                        val analyzer = ImageAnalysis.Builder().build().also {
                            it.setAnalyzer(executor) { imageProxy ->

                                if (!scanning) {
                                    imageProxy.close()
                                    return@setAnalyzer
                                }

                                val mediaImage = imageProxy.image
                                if (mediaImage != null) {
                                    val image = InputImage.fromMediaImage(
                                        mediaImage,
                                        imageProxy.imageInfo.rotationDegrees
                                    )

                                    val scanner = BarcodeScanning.getClient()

                                    scanner.process(image)
                                        .addOnSuccessListener { barcodes ->
                                            for (barcode in barcodes) {
                                                val value = barcode.rawValue
                                                if (value != null) {
                                                    scanning = false

                                                    db.collection("products")
                                                        .whereEqualTo("barcode", value)
                                                        .get()
                                                        .addOnSuccessListener { result ->
                                                            if (!result.isEmpty) {
                                                                val document = result.documents[0]

                                                                val product = Product(
                                                                    name = document.getString("name")
                                                                        ?: "",
                                                                    brand = document.getString("brand")
                                                                        ?: "",
                                                                    price = document.getDouble("price")
                                                                        ?: 0.0,
                                                                    barcode = document.getString("barcode")
                                                                        ?: ""
                                                                )

                                                                val emoji = when (product.barcode) {
                                                                    "1234567890123" -> "🥛"
                                                                    "1234567890179" -> "🧼"
                                                                    "1234567890155" -> "🥔"
                                                                    else -> "🛍️"
                                                                }

                                                                val connectedCartId = CartConnectionSession.connectedCartId

                                                                if (!connectedCartId.isNullOrBlank()) {
                                                                    RemoteCartRepository.addProductToRemoteCart(
                                                                        cartId = connectedCartId,
                                                                        product = product.copy(), // если надо, можно просто product
                                                                        onSuccess = {
                                                                            productName = product.name
                                                                            onProductFoundGoCart()
                                                                        },
                                                                        onError = { error ->
                                                                            productName = "Error: $error"
                                                                            scanning = true
                                                                        }
                                                                    )
                                                                } else {
                                                                    CartSession.addOrIncrement(
                                                                        CartItem(
                                                                            barcode = product.barcode,
                                                                            name = product.name,
                                                                            brand = product.brand,
                                                                            price = product.price.toInt()
                                                                                .toDouble(),
                                                                            imageEmoji = emoji
                                                                        )
                                                                    )

                                                                    productName = product.name
                                                                    onProductFoundGoCart()
                                                                }
                                                            } else {
                                                                productName = "Not found"
                                                                scanning = true
                                                            }
                                                        }
                                                        .addOnFailureListener {
                                                            productName = "Error: ${it.message}"
                                                            scanning = true
                                                        }

                                                    break
                                                }
                                            }
                                        }
                                        .addOnFailureListener {
                                            imageProxy.close()
                                        }
                                        .addOnCompleteListener {
                                            imageProxy.close()
                                        }
                                } else {
                                    imageProxy.close()
                                }
                            }
                        }

                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                CameraSelector.DEFAULT_BACK_CAMERA,
                                preview,
                                analyzer
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                }
            )

            // Scan frame overlay
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .align(Alignment.Center)
                    .border(
                        width = 2.dp,
                        color = AppColors.Primary,
                        shape = AppRadius.Large
                    )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Point the camera at a product barcode",
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        productName?.let { name ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(AppRadius.Large)
                    .background(AppColors.Surface)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = if (name.startsWith("Error") || name == "Not found") AppColors.Error else AppColors.Success,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = name,
                    color = AppColors.TextDark,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}