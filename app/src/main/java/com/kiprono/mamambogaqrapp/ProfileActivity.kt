package com.kiprono.mamambogaqrapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kiprono.mamambogaqrapp.data.local.SessionManager

class ProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme(darkTheme = true) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .systemBarsPadding(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ProfileScreen()
                }
            }
        }
    }
}

@Composable
fun ProfileScreen() {
    val context = LocalContext.current
    val user = remember { mutableStateOf(UserStore.getCurrentUser()) }
    if (user.value == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No user logged in.")
        }
        return
    }

    // Dialog states
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeactivateDialog by remember { mutableStateOf(false) }
    var showEditProfile by remember { mutableStateOf(false) }
    var showEditTarget by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- Profile Image ---
            Image(
                painter = painterResource(id = R.drawable.ic_profile_placeholder),
                contentDescription = "Profile Image",
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "My Profile",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- Info Card ---
            InfoCard(
                title = "Personal Information",
                content = {
                    Text("Name: ${user.value!!.name}")
                    Text("Phone: ${user.value!!.phone}")
                    Text("Email: ${user.value!!.email}")
                },
                onEdit = { showEditProfile = true }
            )

            Spacer(modifier = Modifier.height(16.dp))
            InfoCard(
                title = "Daily Target",
                content = {
                    Text("Current Target: ${user.value!!.dailyTarget}")
                },
                onEdit = { showEditTarget = true }
            )

            Spacer(modifier = Modifier.height(16.dp))
            InfoCard(
                title = "Achievements",
                content = {
                    val achievements = UserStore.getAchievements(user.value!!.id)
                    if (achievements.isEmpty()) Text("No achievements yet.")
                    else achievements.forEach { Text("🏆 $it") }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { showLogoutDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Logout")
            }

            OutlinedButton(
                onClick = { context.startActivity(Intent(context, TermsActivity::class.java)) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Terms & Conditions")
            }

            OutlinedButton(
                onClick = { showDeactivateDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Deactivate Account")
            }
        }

        // --- Edit Profile Dialog ---
        AnimatedVisibility(visible = showEditProfile) {
            EditProfileDialog(
                user = user.value!!,
                onDismiss = { showEditProfile = false },
                onSave = { updated ->
                    UserStore.updateUser(updated)
                    user.value = updated
                    showEditProfile = false
                    Toast.makeText(context, "Profile updated!", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // --- Edit Target Dialog ---
        AnimatedVisibility(visible = showEditTarget) {
            EditTargetDialog(
                userId = user.value!!.id,
                onDismiss = { showEditTarget = false },
                onSave = {
                    Toast.makeText(context, "Target updated!", Toast.LENGTH_SHORT).show()
                    showEditTarget = false
                }
            )
        }

        // --- Logout Confirmation ---
        if (showLogoutDialog) {
            ConfirmationDialog(
                title = "Logout?",
                message = "Are you sure you want to log out?",
                onConfirm = {
                    SessionManager.logout(context)
                    UserStore.logout()
                    (context as? Activity)?.finish()
                    context.startActivity(Intent(context, LoginActivity::class.java))
                },
                onDismiss = { showLogoutDialog = false }
            )
        }

        // --- Deactivate Confirmation ---
        if (showDeactivateDialog) {
            ConfirmationDialog(
                title = "Deactivate Account?",
                message = "Are you sure you want to delete your account permanently?",
                onConfirm = {
                    Toast.makeText(context, "Account deactivated (feature coming soon)", Toast.LENGTH_SHORT).show()
                    showDeactivateDialog = false
                },
                onDismiss = { showDeactivateDialog = false }
            )
        }

        // --- Blur Background when dialogs active ---
        if (showEditProfile || showEditTarget || showLogoutDialog || showDeactivateDialog) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .alpha(0.7f)
            )
        }
    }
}

@Composable
fun InfoCard(title: String, content: @Composable ColumnScope.() -> Unit, onEdit: (() -> Unit)? = null) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                if (onEdit != null) {
                    TextButton(onClick = onEdit) {
                        Text("Edit", color = MaterialTheme.colorScheme.tertiary)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
fun EditProfileDialog(user: UserStore.User, onDismiss: () -> Unit, onSave: (UserStore.User) -> Unit) {
    var name by remember { mutableStateOf(TextFieldValue(user.name)) }
    var phone by remember { mutableStateOf(TextFieldValue(user.phone)) }
    var email by remember { mutableStateOf(TextFieldValue(user.email)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Profile") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") })
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(user.copy(name = name.text, phone = phone.text, email = email.text))
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EditTargetDialog(userId: String, onDismiss: () -> Unit, onSave: () -> Unit) {
    var target by remember { mutableStateOf("") }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Daily Target") },
        text = {
            OutlinedTextField(
                value = target,
                onValueChange = { target = it },
                label = { Text("New Target") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = {
                val newTarget = target.toIntOrNull()
                if (newTarget != null) {
                    UserStore.updateDailyTarget(userId, newTarget)
                    onSave()
                } else {
                    Toast.makeText(context, "Enter a valid number", Toast.LENGTH_SHORT).show()
                }
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ConfirmationDialog(title: String, message: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = {
                onConfirm()
                onDismiss()
            }) {
                Text("Yes", color = MaterialTheme.colorScheme.primary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}