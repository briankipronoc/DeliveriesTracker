// MainActivity.kt (FINAL FIX)
package com.kiprono.mamambogaqrapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
// ✅ FIX: Import the Text Composable function
import androidx.compose.material3.Text
import com.kiprono.mamambogaqrapp.ui.theme.MamaMbogaQRAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Option 1: Redirect to DeliveriesActivity immediately (Cleanest for your architecture)
        startActivity(Intent(this, DeliveriesActivity::class.java))
        finish()

        // Option 2: Show a simple loading screen (ONLY if you need MainActivity to run)
        // setContent {
        //     MamaMbogaQRAppTheme {
        //         Text("App Loading...")
        //     }
        // }
    }
}