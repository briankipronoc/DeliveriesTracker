package com.kiprono.mamambogaqrapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kiprono.mamambogaqrapp.ui.screens.QRScannerScreen
import com.kiprono.mamambogaqrapp.ui.screens.DeliveryListScreen
import com.kiprono.mamambogaqrapp.ProfileActivity
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.time.LocalDate
import java.util.*
import java.util.concurrent.TimeUnit

class DashboardActivity : ComponentActivity() {
    private val CAMERA_PERMISSION_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val email = intent.getStringExtra("email")

        setContent {
            AppTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "dashboard") {

                    composable("dashboard") {
                        DashboardScreen(
                            email,
                            onScanClick = {
                                if (checkCameraPermission()) {
                                    navController.navigate("qr")
                                } else {
                                    requestCameraPermission()
                                }
                            },
                            onDeliveriesClick = { navController.navigate("deliveries") },
                            onProfileClick = { navController.navigate("profile") }
                        )
                    }

                    // ✅ QR scanner screen
                    composable("qr") {
                        val context = this@DashboardActivity  // ✅ get Activity context properly

                        QRScannerScreen(onQrScanned = { result ->
                            Toast.makeText(context, "QR Scanned: $result", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        })
                    }

                    composable("deliveries") { DeliveriesScreen() }
                    composable("profile") { ProfileScreen() }
                }
            }
        }
    }

    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Camera permission granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Camera permission is required to scan QR codes", Toast.LENGTH_LONG).show()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    email: String?,
    onScanClick: () -> Unit,
    onDeliveriesClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    val current = UserStore.getCurrentUser()
    val userEmail = email ?: UserStore.currentLoggedInUser?.email ?: current?.email ?: "test@rider.com"

    val rider = current ?: UserStore.getUserDataByEmail(userEmail)
    val riderName = rider?.name ?: "Rider"
    val dailyTarget = rider?.dailyTarget ?: 5

    val dailyCompleted = getDeliveriesCountForLastNDays(rider?.id ?: "", 1)
    val weeklyCompleted = getDeliveriesCountForLastNDays(rider?.id ?: "", 7)
    val daysInMonth = LocalDate.now().lengthOfMonth()
    val weeklyTarget = dailyTarget * 7
    val monthlyTarget = dailyTarget * daysInMonth
    val monthlyCompleted = getDeliveriesCountForLastNDays(rider?.id ?: "", daysInMonth)

    val greeting = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
        in 0..11 -> "🌞 Good Morning"
        in 12..17 -> "🌤️ Good Afternoon"
        else -> "🌙 Good Evening"
    }

    val goalReached = dailyCompleted >= dailyTarget
    var confettiVisible by remember { mutableStateOf(goalReached) }
    LaunchedEffect(dailyCompleted, dailyTarget) {
        if (dailyCompleted >= dailyTarget && rider != null) {
            confettiVisible = true
            UserStore.markAchievement(rider.id, "Daily Target Reached")
        }
    }

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("$greeting, $riderName") },
            actions = {
                IconButton(onClick = { AppThemeState.isDark.value = !AppThemeState.isDark.value }) {
                    Icon(Icons.Default.DarkMode, contentDescription = "Theme")
                }
            })
    }) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Progress card section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(6.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ProgressCircleWithLabel(
                            label = "Daily",
                            valueText = "$dailyCompleted",
                            subLabel = "$dailyTarget target",
                            progress = if (dailyTarget > 0) (dailyCompleted.toFloat() / dailyTarget).coerceAtMost(1f) else 0f
                        )
                        ProgressCircleWithLabel(
                            label = "Weekly",
                            valueText = "${((weeklyCompleted.toFloat() / weeklyTarget.coerceAtLeast(1)).coerceAtMost(1f) * 100).toInt()}%",
                            subLabel = "$weeklyCompleted / $weeklyTarget",
                            progress = if (weeklyTarget > 0) (weeklyCompleted.toFloat() / weeklyTarget).coerceAtMost(1f) else 0f
                        )
                        ProgressCircleWithLabel(
                            label = "Monthly",
                            valueText = "${((monthlyCompleted.toFloat() / monthlyTarget.coerceAtLeast(1)).coerceAtMost(1f) * 100).toInt()}%",
                            subLabel = "$monthlyCompleted / $monthlyTarget",
                            progress = if (monthlyTarget > 0) (monthlyCompleted.toFloat() / monthlyTarget).coerceAtMost(1f) else 0f
                        )
                    }
                }

                // Action buttons
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ButtonWithIcon("Scan QR Code", Icons.Default.QrCodeScanner, onScanClick)
                    ButtonWithIcon("View Deliveries", Icons.Default.Assessment, onDeliveriesClick)
                    ButtonWithIcon("Profile", Icons.Default.AccountCircle, onProfileClick)
                }

                // Achievement celebration
                if (confettiVisible) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🎉 Congrats — daily goal achieved!", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }

            if (confettiVisible) {
                KonfettiView(
                    modifier = Modifier.fillMaxSize(),
                    parties = listOf(
                        Party(
                            speed = 0f,
                            maxSpeed = 40f,
                            damping = 0.9f,
                            spread = 360,
                            colors = listOf(
                                0xfff59e0b.toInt(),
                                0xffef4444.toInt(),
                                0xff60a5fa.toInt(),
                                0xff34d399.toInt()
                            ),
                            emitter = Emitter(duration = 2, TimeUnit.SECONDS).perSecond(75),
                            position = Position.Relative(0.5, 0.0)
                        )
                    )
                )
            }
        }
    }
}

private fun getDeliveriesCountForLastNDays(userId: String, days: Int): Int {
    val today = LocalDate.now()
    var count = 0
    for (i in 0 until days) {
        val d = today.minusDays(i.toLong())
        count += UserStore.getDeliveriesForUserOnDate(userId, d).count { it.status.equals("Completed", true) }
    }
    return count
}

@Composable
fun ProgressCircleWithLabel(label: String, valueText: String, subLabel: String, progress: Float) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = androidx.compose.animation.core.spring()
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = animatedProgress,
                strokeWidth = 6.dp,
                modifier = Modifier.size(86.dp),
                color = if (progress >= 1f) MaterialTheme.colorScheme.secondary
                else MaterialTheme.colorScheme.primary
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(valueText, style = MaterialTheme.typography.titleSmall)
                Text(label, style = MaterialTheme.typography.labelSmall)
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(subLabel, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun ButtonWithIcon(text: String, icon: ImageVector, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = CircleShape
    ) {
        Icon(icon, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text(text)
    }
}