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
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.smartcard.localization.LocalAppStrings
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartcard.viewmodel.ScanViewModel
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.delay
import java.util.concurrent.Executors

@OptIn(ExperimentalGetImage::class)
@Composable
fun CameraScreen(
    onBack: () -> Unit,
    onProductFoundGoCart: () -> Unit
) {
    val vm: ScanViewModel = viewModel()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val texts = LocalAppStrings.current
    val scanning by vm.scanning.collectAsState()
    val productName by vm.productMessage.collectAsState()
    val navigateToCart by vm.navigateToCart.collectAsState()

    LaunchedEffect(texts) {
        vm.updateStrings(texts)
    }

    LaunchedEffect(navigateToCart) {
        if (navigateToCart) {
            delay(600)
            onProductFoundGoCart()
            vm.onNavigationHandled()
        }
    }

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
            .background(AppColors.DarkSurface)
            .padding(12.dp)
    ) {
        TextButton(onClick = onBack) {
            Text(texts.back, color = AppColors.OnDark)
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (!hasPermission) {
            Text(texts.cameraPermissionRequired, color = AppColors.OnDark)
            return@Column
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
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
                                                val value = barcode.rawValue?.trim()
                                                if (!value.isNullOrBlank()) {
                                                    QrFlowPhoneLog.d(
                                                        event = "PRODUCT_SCAN_RAW",
                                                        "raw" to value
                                                    )

                                                    when (ScanPayloadClassifier.classify(value)) {
                                                        ScanPayloadType.CART_QR_OR_SESSION -> {
                                                            QrFlowPhoneLog.d(
                                                                event = "PRODUCT_SCAN_CLASSIFIED",
                                                                "kind" to "CART_QR_OR_SESSION",
                                                                "raw" to value
                                                            )
                                                            vm.showMessage(texts.productBarcodeMismatch, resumeScanning = true)
                                                            break
                                                        }

                                                        ScanPayloadType.PRODUCT_BARCODE -> {
                                                            QrFlowPhoneLog.d(
                                                                event = "PRODUCT_SCAN_CLASSIFIED",
                                                                "kind" to "PRODUCT_BARCODE",
                                                                "raw" to value
                                                            )
                                                        }
                                                    }

                                                    vm.onBarcodeScanned(value)

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
////////////

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

            Image(
                painter = painterResource(id = R.drawable.ic_scan_overlay),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(0.88f)
                    .padding(horizontal = 8.dp)
            )

            if (!scanning && !navigateToCart) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color.Black.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = AppColors.OnDark)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = texts.loading,
                            color = AppColors.OnDark,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            if (navigateToCart) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color.Black.copy(alpha = 0.45f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF163A2B)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Added to cart",
                                color = Color(0xFF6DFF9A)
                            )
                            productName?.takeIf { it.isNotBlank() }?.let { message ->
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = message,
                                    color = AppColors.OnDark,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = texts.cameraPointToProduct,
            modifier = Modifier.fillMaxWidth(),
            color = AppColors.OnDark,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        productName?.let {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = AppColors.Surface)
            ) {
                Text(
                    text = "${texts.productLabel}: $it",
                    modifier = Modifier.padding(16.dp),
                    color = AppColors.TextDark
                )
            }
        }
    }
}