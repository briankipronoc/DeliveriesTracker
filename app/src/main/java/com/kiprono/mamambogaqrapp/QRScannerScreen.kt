package com.kiprono.mamambogaqrapp

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider

import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRScannerScreen(
    lifecycleOwner: LifecycleOwner,
    onQrScanned: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var scannedCode by remember { mutableStateOf<String?>(null) }
    val scannerColor = MaterialTheme.colorScheme.primary
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan Customer QR", fontWeight = FontWeight.SemiBold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
        ) {
            // 1. Camera Preview (AndroidView)
            CameraPreview(
                lifecycleOwner = lifecycleOwner,
                onQrCodeScanned = { qrResult ->
                    if (qrResult != null && qrResult != scannedCode) {
                        scannedCode = qrResult

                        // Haptic Feedback
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
                        } else {
                            @Suppress("DEPRECATION")
                            vibrator.vibrate(100)
                        }

                        // Add delivery to data store immediately
                        ContextCompat.getMainExecutor(context).execute {
                            // Assuming UserStore.getCurrentUser() and UserStore.addDelivery() are defined elsewhere
                            val userId = "guest" // Placeholder for actual UserStore access
                            // UserStore.getCurrentUser()?.id ?: "guest"
                            // UserStore.addDelivery(userId, "QR: $qrResult", 500.0)
                            Toast.makeText(context, "✅ Delivery added via QR: $qrResult", Toast.LENGTH_LONG).show()

                            // Trigger the external function to finish the Activity
                            onQrScanned(qrResult)
                        }
                    }
                }
            )

            // 2. Scanner Overlay and Guidance Text
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.weight(0.2f))

                // Guidance Text
                Text(
                    text = "Center the customer's QR code below",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(16.dp)
                )

                // Scanner Window
                ScannerOverlay(
                    modifier = Modifier.weight(1f),
                    color = scannerColor
                )

                // Status/Instruction
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(bottom = 24.dp, top = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = scannerColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (scannedCode == null) "Scanning live..." else "Scanned! Delivery Added!",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth()
                    )
                }
            }
        }
    }
}

// ====================================================================
// Custom Composable Helper Functions
// ====================================================================

@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
@Composable
private fun CameraPreview(
    lifecycleOwner: LifecycleOwner,
    onQrCodeScanned: (String?) -> Unit
) {
    AndroidView(factory = { ctx ->
        val previewView = PreviewView(ctx)
        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().apply {
                // FIX: Changed property assignment (surfaceProvider = ...)
                // to the explicit setter method (setSurfaceProvider(...))
                setSurfaceProvider(previewView.surfaceProvider)
            }

            val scanner = BarcodeScanning.getClient(
                BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE).build()
            )

            val analysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build().apply {
                    setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
                        processImage(scanner, imageProxy, onQrCodeScanned)
                    }
                }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    analysis
                )
            } catch (e: Exception) {
                Log.e("CameraPreview", "Camera binding failed", e)
                Toast.makeText(ctx, "Error: Camera access required.", Toast.LENGTH_LONG).show()
            }
        }, ContextCompat.getMainExecutor(ctx))
        previewView
    }, modifier = Modifier.fillMaxSize())
}

@Composable
private fun ScannerOverlay(modifier: Modifier = Modifier, color: Color) {
    Canvas(modifier = modifier.fillMaxWidth()) {
        val squareSize = size.width * 0.75f

        // Draw the dark background overlay
        drawRect(
            color = Color.Black.copy(alpha = 0.5f),
            size = size,
            topLeft = Offset.Zero,
            style = Fill
        )

        // Draw the four corner guides (primary color)
        val cornerLength = 50f
        val strokeWidth = 10f
        val offset = strokeWidth / 2f
        val startX = (size.width - squareSize) / 2f
        val startY = (size.height - squareSize) / 2f // Vertically centered
        val endX = startX + squareSize
        val endY = startY + squareSize

        // Top-Left
        drawLine(color, Offset(startX + offset, startY + offset), Offset(startX + cornerLength, startY + offset), strokeWidth)
        drawLine(color, Offset(startX + offset, startY + offset), Offset(startX + offset, startY + cornerLength), strokeWidth)

        // Top-Right
        drawLine(color, Offset(endX - offset, startY + offset), Offset(endX - cornerLength, startY + offset), strokeWidth)
        drawLine(color, Offset(endX - offset, startY + offset), Offset(endX - offset, startY + cornerLength), strokeWidth)

        // Bottom-Left
        drawLine(color, Offset(startX + offset, endY - offset), Offset(startX + cornerLength, endY - offset), strokeWidth)
        drawLine(color, Offset(startX + offset, endY - offset), Offset(startX + offset, endY - cornerLength), strokeWidth)

        // Bottom-Right
        drawLine(color, Offset(endX - offset, endY - offset), Offset(endX - cornerLength, endY - offset), strokeWidth)
        drawLine(color, Offset(endX - offset, endY - offset), Offset(endX - offset, endY - cornerLength), strokeWidth)
    }
}

@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
private fun processImage(scanner: BarcodeScanner, imageProxy: ImageProxy, onResult: (String?) -> Unit) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        scanner.process(image)
            .addOnSuccessListener { barcodes -> onResult(barcodes.firstOrNull()?.rawValue) }
            .addOnFailureListener { Log.e("QRScannerScreen", "Scan failed", it) }
            .addOnCompleteListener { imageProxy.close() }
    } else {
        imageProxy.close()
    }
}
