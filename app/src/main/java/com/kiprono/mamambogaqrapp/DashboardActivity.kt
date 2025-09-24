package com.kiprono.mamambogaqrapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.kiprono.mamambogaqrapp.ui.theme.MamaMbogaQRAppTheme
import java.util.*

class DashboardActivity : ComponentActivity() {

    private val CAMERA_PERMISSION_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val riderName = "Brian"
        setContent {
            MamaMbogaQRAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DashboardScreen(
                        riderName = riderName,
                        onScanClick = {
                            checkAndRequestCameraPermission()
                        },
                        onDeliveriesClick = {
                            startActivity(Intent(this, DeliveriesActivity::class.java))
                        },
                        onProfileClick = {
                            startActivity(Intent(this, ProfileActivity::class.java))
                        }
                    )
                }
            }
        }
    }

    private fun checkAndRequestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE
            )
        } else {
            startQRScanner()
        }
    }

    private fun startQRScanner() {
        startActivity(Intent(this, MainActivity::class.java))
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_PERMISSION_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    startQRScanner()
                } else {
                    Toast.makeText(this, "Camera permission is required to scan QR codes.", Toast.LENGTH_SHORT).show()
                }
                return
            }
        }
    }
}

@Composable
fun DashboardScreen(
    riderName: String,
    onScanClick: () -> Unit,
    onDeliveriesClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    // Corrected mock data for progress tracking
    val dailyCompleted = 7
    val weeklyCompleted = 35
    val monthlyCompleted = 140
    val dailyTarget = 10
    val weeklyTarget = 50
    val monthlyTarget = 200

    val greeting = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
        in 0..11 -> "Good Morning"
        in 12..17 -> "Good Afternoon"
        else -> "Good Evening"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "$greeting, $riderName 👋🏿",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Progress Rings Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProgressRing("Daily", dailyCompleted, dailyTarget)
            ProgressRing("Weekly", weeklyCompleted, weeklyTarget)
            ProgressRing("Monthly", monthlyCompleted, monthlyTarget)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ButtonWithIcon("Scan QR Code", Icons.Default.QrCodeScanner, onScanClick)
            Spacer(modifier = Modifier.height(16.dp))
            ButtonWithIcon("View Deliveries", Icons.Default.Assessment, onDeliveriesClick)
            Spacer(modifier = Modifier.height(16.dp))
            ButtonWithIcon("Profile", Icons.Default.AccountCircle, onProfileClick)
        }
    }
}

@Composable
fun ProgressRing(title: String, completed: Int, target: Int) {
    val progress = if (target > 0) completed.toFloat() / target.toFloat() else 0f
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = progress,
                modifier = Modifier.size(60.dp),
                strokeWidth = 6.dp
            )
            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(title, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
fun ButtonWithIcon(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        Icon(icon, contentDescription = text, modifier = Modifier.padding(end = 8.dp))
        Text(text)
    }
}