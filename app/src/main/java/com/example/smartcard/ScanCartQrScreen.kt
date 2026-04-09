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
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var scanning by remember { mutableStateOf(true) }
    var message by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

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
            .background(Color.Black)
            .padding(12.dp)
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
                                            if (!value.isNullOrBlank()) {
                                                Log.d(
                                                    "QR_PHONE_SCAN",
                                                    "raw_scanned_text=${value}"
                                                )
                                                QrFlowPhoneLog.d(
                                                    event = "qr_scan_success",
                                                    "qrRawValue" to value
                                                )

                                                val fallbackTraceId = "trace_" + UUID.randomUUID().toString()
                                                    .replace("-", "")
                                                    .take(8)

                                                val parsed = QrPayloadParser.parse(value, fallbackTraceId)
                                                Log.d(
                                                    "QR_PHONE_CLASSIFIER",
                                                    "parse_result traceId=${parsed.traceId} strategy=${parsed.parseStrategy} sessionId=${parsed.sessionId ?: ""} cartId=${parsed.cartId ?: ""}"
                                                )
                                                QrFlowPhoneLog.d(
                                                    event = "qr_parse_result",
                                                    "parseStrategy" to parsed.parseStrategy,
                                                    "parsedSessionId" to parsed.sessionId,
                                                    "parsedCartId" to parsed.cartId,
                                                    "traceId" to parsed.traceId
                                                )

                                                scanning = false

                                                val legacyCartId = parsed.cartId
                                                val sessionId = parsed.sessionId
                                                val traceId = parsed.traceId

                                                if (!sessionId.isNullOrBlank()) {
                                                    scope.launch {
                                                        Log.d(
                                                            "QR_PHONE_CLASSIFIER",
                                                            "classified_as_session reason=sessionId_present traceId=$traceId sessionId=$sessionId"
                                                        )
                                                        Log.d(
                                                            "QR_FLOW_PHONE",
                                                            "qr_scan_session_mode traceId=$traceId sessionId=$sessionId"
                                                        )

                                                        val result = QrSessionRepository.confirmSession(
                                                            traceId = traceId,
                                                            sessionId = sessionId
                                                        )

                                                        val cartId = result.getOrNull()
                                                        if (cartId.isNullOrBlank()) {
                                                            message = result.exceptionOrNull()?.message
                                                                ?: "Failed to confirm session"
                                                            scanning = true
                                                            return@launch
                                                        }

                                                        Log.d(
                                                            "QR_FLOW_SESSION",
                                                            "session_confirm_success traceId=$traceId sessionId=$sessionId cartId=$cartId"
                                                        )
                                                        Log.d(
                                                            "QR_FLOW_CART",
                                                            "defer_cart_load_until_after_confirmation traceId=$traceId sessionId=$sessionId cartId=$cartId"
                                                        )

                                                        CartConnectionSession.connectedCartId = cartId
                                                        Log.d(
                                                            "QR_FLOW_CART",
                                                            "phone_local_cart_binding_set traceId=$traceId cartId=$cartId"
                                                        )

                                                        message = "Session confirmed for $cartId"
                                                        onConnected(cartId)
                                                    }
                                                } else if (!legacyCartId.isNullOrBlank()) {
                                                    Log.d(
                                                        "QR_PHONE_CLASSIFIER",
                                                        "classified_as_legacy_cart reason=no_sessionId_but_cartId_present traceId=$traceId cartId=$legacyCartId strategy=${parsed.parseStrategy}"
                                                    )
                                                    Log.d(
                                                        "QR_FLOW_PHONE",
                                                        "legacy_cart_qr_scanned traceId=$traceId cartId=$legacyCartId parseStrategy=${parsed.parseStrategy}"
                                                    )

                                                    Log.d(
                                                        "QR_FLOW_CART",
                                                        "legacy_cart_connect_path_blocked wouldHaveCalled=CartConnectionRepository.connectToCart cartId=$legacyCartId"
                                                    )
                                                    message = "Legacy cart QR is not supported. Scan tablet session QR."
                                                    scanning = true
                                                } else {
                                                    Log.d(
                                                        "QR_PHONE_CLASSIFIER",
                                                        "classified_as_invalid reason=no_sessionId_and_no_cartId traceId=$traceId strategy=${parsed.parseStrategy}"
                                                    )
                                                    QrFlowPhoneLog.d(
                                                        event = "qr_scan_failure",
                                                        "reason" to "empty_parsed_values",
                                                        "qrRawValue" to value,
                                                        "traceId" to traceId
                                                    )
                                                    Log.d(
                                                        "QR_FLOW_PHONE",
                                                        "qr_scan_invalid traceId=$traceId reason=empty_parsed_values"
                                                    )
                                                    message = "Invalid QR"
                                                    scanning = true
                                                }
                                                break
                                            }
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        QrFlowPhoneLog.e(
                                            event = "qr_scan_failure",
                                            throwable = e,
                                            "where" to "mlkit_barcode_scan"
                                        )
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
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Text(
                    text = it,
                    modifier = Modifier.padding(16.dp),
                    color = Color.Black,
                    fontSize = 16.sp
                )
            }
        }
    }
}