// ProfileActivity.kt

package com.kiprono.mamambogaqrapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import com.kiprono.mamambogaqrapp.ui.theme.MamaMbogaQRAppTheme
import java.lang.NumberFormatException

class ProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MamaMbogaQRAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ProfileScreen(
                        riderName = "Brian Kiprono",
                        email = "brian@email.com",
                        bikeBrand = "Honda",
                        ccs = "150cc",
                        idNumber = "12345678",
                        phone = "0712345678",
                        onLogout = {
                            val intent = Intent(this, LoginActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileScreen(
    riderName: String,
    email: String,
    bikeBrand: String,
    ccs: String,
    idNumber: String,
    phone: String,
    onLogout: () -> Unit
) {
    val userData = remember { UserStore.getUserData(email) }
    var dailyTarget by remember { mutableStateOf(userData?.dailyTarget?.toString() ?: "0") }
    val context = LocalContext.current
    val completedDeliveries by remember { mutableStateOf(15) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item { Spacer(Modifier.height(48.dp)) }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Weekly Target",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(16.dp))
                    Box(contentAlignment = Alignment.Center) {
                        val numericTarget = try { dailyTarget.toInt() } catch (e: NumberFormatException) { 0 }
                        val progress = if (numericTarget > 0) completedDeliveries.toFloat() / (numericTarget * 7) else 0f
                        CircularProgressIndicator(
                            progress = progress,
                            modifier = Modifier.size(100.dp),
                            strokeWidth = 10.dp
                        )
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            fontSize = 20.sp,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("Daily Goal:", style = MaterialTheme.typography.bodyLarge)
                    OutlinedTextField(
                        value = dailyTarget,
                        onValueChange = { newValue ->
                            dailyTarget = newValue
                            val newTarget = try { newValue.toInt() } catch (e: NumberFormatException) { 0 }
                            userData?.dailyTarget = newTarget
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = {
                            Toast.makeText(context, "Target saved. You'll crush it! 💪", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save Target")
                    }
                }
            }
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Profile", style = MaterialTheme.typography.headlineSmall)
                Text("Name: $riderName")
                Text("Email: $email")
                Text("Bike: $bikeBrand ($ccs)")
                Text("ID: $idNumber")
                Text("Phone: $phone")
            }
        }

        item {
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { showLogoutDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Log Out")
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(text = "Leaving so soon? 🏃‍♂️") },
            text = { Text(text = "Are you sure you want to log out?") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    }
                ) {
                    Text("Log Out")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showLogoutDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}