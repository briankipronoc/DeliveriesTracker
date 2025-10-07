@file:OptIn(androidx.camera.core.ExperimentalGetImage::class)

package com.kiprono.mamambogaqrapp.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.kiprono.mamambogaqrapp.UserStore
import java.util.concurrent.Executors

/**
 * QRScannerScreen - Composable that shows camera preview and reads QR codes.
 */
@Composable
fun QRScannerScreen(onQrScanned: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
    var scannedCode by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()

                    // Set up camera preview
                    val preview = Preview.Builder().build().apply {
                        setSurfaceProvider(previewView.surfaceProvider)
                    }

                    // ML Kit barcode scanner configuration
                    val barcodeOptions = BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                        .build()

                    val scanner = BarcodeScanning.getClient(barcodeOptions)

                    val analysisUseCase = ImageAnalysis.Builder()
                        .build()
                        .also { analysis ->
                            analysis.setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
                                processImageProxy(scanner, imageProxy) { qrResult ->
                                    if (qrResult != null && qrResult != scannedCode) {
                                        scannedCode = qrResult
                                        ContextCompat.getMainExecutor(ctx).execute {
                                            Toast.makeText(ctx, "Scanned: $qrResult", Toast.LENGTH_SHORT).show()

                                            // Save to UserStore
                                            val userId = UserStore.getCurrentUser()?.id ?: "guest"
                                            UserStore.addDelivery(
                                                userId = userId,
                                                customerName = "QR: $qrResult",
                                                amount = 500.0
                                            )

                                            // Return result to MainActivity
                                            onQrScanned(qrResult)
                                        }
                                    }
                                }
                            }
                        }

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            analysisUseCase
                        )
                    } catch (exc: Exception) {
                        Log.e("QRScannerScreen", "Camera binding failed", exc)
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            }
        )

        Text(text = "Align QR code within frame", modifier = Modifier)
    }
}

/**
 * Helper to analyze each camera frame and decode QR result.
 */
private fun processImageProxy(
    scanner: com.google.mlkit.vision.barcode.BarcodeScanner,
    imageProxy: ImageProxy,
    onResult: (String?) -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    onResult(barcode.rawValue)
                }
            }
            .addOnFailureListener { Log.e("QRScannerScreen", "Scan failed", it) }
            .addOnCompleteListener { imageProxy.close() }
    } else {
        imageProxy.close()
    }
}