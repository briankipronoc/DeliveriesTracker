@file:OptIn(androidx.camera.core.ExperimentalGetImage::class)

package com.kiprono.mamambogaqrapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.time.LocalDate
import java.time.LocalTime
import java.util.concurrent.Executors
import kotlinx.coroutines.delay

class QrCodeScannerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { AppTheme { QrCodeScannerScreen() } }
    }
}

@Composable
fun QrCodeScannerScreen() {
    val context = LocalContext.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    var qrProcessed by remember { mutableStateOf(false) }

    // ticket data
    var orderId by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Completed") }
    var price by remember { mutableStateOf<Double?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(modifier = Modifier.fillMaxSize(), factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
            val selector = CameraSelector.DEFAULT_BACK_CAMERA
            val scanner = BarcodeScanning.getClient()
            val analysis = ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build()

            val executor = Executors.newSingleThreadExecutor()
            analysis.setAnalyzer(executor) { imageProxy ->
                val mediaImage = imageProxy.image
                if (mediaImage != null) {
                    val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                    scanner.process(inputImage)
                        .addOnSuccessListener { barcodes ->
                            for (barcode in barcodes) {
                                val raw = barcode.rawValue ?: continue
                                if (!qrProcessed) {
                                    qrProcessed = true
                                    // expected format: ID|Description|Status|Price
                                    val parts = raw.split("|")
                                    orderId = parts.getOrNull(0) ?: "AUTO-${System.currentTimeMillis() % 10000}"
                                    description = parts.getOrNull(1) ?: "Scanned order"
                                    status = parts.getOrNull(2) ?: "Completed"
                                    price = parts.getOrNull(3)?.toDoubleOrNull()

                                    // Save into store ✅ fixed
                                    UserStore.addDelivery(
                                        UserStore.Delivery(
                                            id = orderId,
                                            userId = UserStore.getCurrentUser()?.id ?: "guest",
                                            customerName = description,
                                            totalAmount = price ?: 0.0,
                                            date = LocalDate.now(),
                                            time = LocalTime.now(),
                                            status = status
                                        )
                                    )
                                }
                            }
                        }
                        .addOnFailureListener { Log.e("QRScanner", "Error", it) }
                        .addOnCompleteListener { imageProxy.close() }
                } else {
                    imageProxy.close()
                }
            }
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(ctx as ComponentActivity, selector, preview, analysis)
            } catch (e: Exception) {
                Log.e("QRScanner", "Camera binding failed", e)
            }
            previewView
        })

        // ticket overlay, appears on top when processed
        AnimatedVisibility(visible = qrProcessed, enter = fadeIn() + scaleIn(), exit = fadeOut() + scaleOut()) {
            DeliveryTicket(
                orderId = orderId,
                description = description,
                status = status,
                price = price,
                onComplete = {
                    // after animation - navigate to Deliveries screen
                    context.startActivity(Intent(context, DeliveriesActivity::class.java))
                    // finish scanner activity
                    if (context is ComponentActivity) context.finish()
                }
            )
        }
    }
}

@Composable
fun DeliveryTicket(orderId: String, description: String, status: String, price: Double?, onComplete: () -> Unit) {
    LaunchedEffect(orderId) {
        // show for 2.2s then complete
        delay(2200)
        onComplete()
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(0.86f),
            elevation = CardDefaults.cardElevation(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2ECC71), modifier = Modifier.size(68.dp))
                Spacer(Modifier.height(10.dp))
                Text("Order scanned", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("ID: $orderId", style = MaterialTheme.typography.bodyLarge)
                Text(description, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                Text("Status: $status", style = MaterialTheme.typography.bodyMedium)
                price?.let { Text("KSH ${it.toInt()}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) }
            }
        }
    }
}