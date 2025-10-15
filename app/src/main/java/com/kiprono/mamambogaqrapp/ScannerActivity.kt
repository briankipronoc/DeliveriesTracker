// ScannerActivity.kt (Create this file)
package com.kiprono.mamambogaqrapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.LifecycleOwner
import com.kiprono.mamambogaqrapp.ui.theme.MamaMbogaQRAppTheme

class ScannerActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MamaMbogaQRAppTheme {
                val lifecycleOwner: LifecycleOwner = this

                QRScannerScreen(
                    lifecycleOwner = lifecycleOwner,
                    onQrScanned = { qrResult ->
                        // Pass the result back to the calling Activity
                        val resultIntent = Intent().apply {
                            putExtra("qr_code_result", qrResult)
                        }
                        setResult(Activity.RESULT_OK, resultIntent)
                        finish() // Close the scanner activity
                    },
                    onBack = {
                        // Handle back press/cancellation
                        setResult(Activity.RESULT_CANCELED)
                        finish()
                    }
                )
            }
        }
    }
}