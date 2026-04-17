package com.example.smartcard

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.smartcard.localization.LocalAppStrings
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.concurrent.Executors

@OptIn(ExperimentalGetImage::class)
@Composable
fun ScanCartQrScreen(
    onBack: () -> Unit,
    onConnected: (String) -> Unit
) {
    val texts = LocalAppStrings.current
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    var scanning by remember { mutableStateOf(true) }
    var message by remember { mutableStateOf<String?>(null) }

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

        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(16.dp)),
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
                                                    event = "CART_QR_SCAN_RAW",
                                                    "raw" to value
                                                )

                                                val scanType = ScanPayloadClassifier.classify(value)
                                                QrFlowPhoneLog.d(
                                                    event = "CART_QR_SCAN_CLASSIFIED",
                                                    "kind" to scanType,
                                                    "raw" to value
                                                )

                                                if (scanType == ScanPayloadType.PRODUCT_BARCODE) {
                                                    Log.d(SmartCartLogTags.QR, "cart_qr_rejected_product_barcode")
                                                    QrFlowPhoneLog.d(
                                                        event = "CART_QR_SCAN_INVALID_QR_TYPE",
                                                        "raw" to value,
                                                        "reason" to "product_barcode_in_cart_qr_scanner"
                                                    )
                                                    message = texts.productBarcodeMismatch
                                                    scanning = true
                                                    break
                                                }

                                                scanning = false

                                                val fallbackTraceId = "trace_" + UUID.randomUUID()
                                                    .toString()
                                                    .replace("-", "")
                                                    .take(8)
                                                val parsed = QrPayloadParser.parse(value, fallbackTraceId)

                                                when {
                                                    !parsed.sessionId.isNullOrBlank() -> {
                                                        scope.launch {
                                                            QrFlowPhoneLog.d(
                                                                event = "CART_QR_SCAN_CONNECT_STARTED",
                                                                "mode" to if (parsed.localUrl != null) "local_session" else "cloud_session",
                                                                "traceId" to parsed.traceId,
                                                                "sessionId" to parsed.sessionId
                                                            )
                                                            Log.d(
                                                                SmartCartLogTags.QR,
                                                                "confirm_start traceId=${parsed.traceId} sessionId=${parsed.sessionId} mode=${if (parsed.localUrl != null) "local" else "cloud"}"
                                                            )

                                                            val result = if (parsed.localUrl != null) {
                                                                QrSessionRepository.confirmLocalSession(
                                                                    traceId = parsed.traceId,
                                                                    sessionId = parsed.sessionId,
                                                                    tabletBaseUrl = parsed.localUrl
                                                                )
                                                            } else {
                                                                QrSessionRepository.confirmSession(
                                                                    traceId = parsed.traceId,
                                                                    sessionId = parsed.sessionId
                                                                )
                                                            }

                                                            val cartId = result.getOrNull()
                                                            if (cartId.isNullOrBlank()) {
                                                                val error = result.exceptionOrNull()?.message ?: "Failed to confirm session"
                                                                Log.e(
                                                                    SmartCartLogTags.QR,
                                                                    "confirm_failed traceId=${parsed.traceId} sessionId=${parsed.sessionId} error=$error"
                                                                )
                                                                QrFlowPhoneLog.d(
                                                                    event = "CART_QR_SCAN_CONNECT_FAILED",
                                                                    "traceId" to parsed.traceId,
                                                                    "sessionId" to parsed.sessionId,
                                                                    "error" to error
                                                                )
                                                                message = error
                                                                scanning = true
                                                                return@launch
                                                            }

                                                            CartConnectionSession.updateConnection(
                                                                cartId = cartId,
                                                                sessionId = parsed.sessionId
                                                            )
                                                            Log.d(
                                                                SmartCartLogTags.QR,
                                                                "confirm_success traceId=${parsed.traceId} sessionId=${parsed.sessionId} cartId=$cartId"
                                                            )
                                                            message = "${texts.connectedToCart} $cartId"
                                                            onConnected(cartId)
                                                        }
                                                    }

                                                    !parsed.cartId.isNullOrBlank() -> {
                                                        Log.d(SmartCartLogTags.QR, "legacy_cart_qr_rejected cartId=${parsed.cartId}")
                                                        QrFlowPhoneLog.d(
                                                            event = "CART_QR_SCAN_INVALID_QR_TYPE",
                                                            "raw" to value,
                                                            "reason" to "legacy_cart_qr_not_supported",
                                                            "cartId" to parsed.cartId
                                                        )
                                                        message = texts.legacyQrNotSupported
                                                        scanning = true
                                                    }

                                                    else -> {
                                                        Log.d(SmartCartLogTags.QR, "invalid_cart_qr_payload")
                                                        QrFlowPhoneLog.d(
                                                            event = "CART_QR_SCAN_INVALID_QR_TYPE",
                                                            "raw" to value,
                                                            "reason" to "no_session_or_cart_payload"
                                                        )
                                                        message = texts.invalidCartQr
                                                        scanning = true
                                                    }
                                                }
                                                break
                                            }
                                        }
                                    }
                                    .addOnFailureListener { err ->
                                        Log.e("QR_FLOW_PHONE", "cart_qr_scan_failure", err)
                                        Log.e(SmartCartLogTags.QR, "scan_failure", err)
                                        message = err.message ?: "QR scan failed"
                                        scanning = true
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
                    } catch (_: Exception) {
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        message?.let {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = AppColors.Surface)
            ) {
                Text(
                    text = it,
                    modifier = Modifier.padding(16.dp),
                    color = AppColors.TextDark,
                    fontSize = 16.sp
                )
            }
        }
    }
}