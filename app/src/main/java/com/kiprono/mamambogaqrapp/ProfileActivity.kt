package com.kiprono.mamambogaqrapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kiprono.mamambogaqrapp.data.local.SessionManager
import com.kiprono.mamambogaqrapp.ui.theme.AppThemeState
import com.kiprono.mamambogaqrapp.ui.theme.MamaMbogaQRAppTheme
import java.text.NumberFormat
import java.util.Locale

// NOTE: Assumes R.drawable.ic_profile exists and UserStore logic is available.

class ProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Removed systemBarsPadding() from Surface here to let Scaffold handle it
            // and ensure the full screen is covered by the background color.
            MamaMbogaQRAppTheme(darkTheme = AppThemeState.isDark.value) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ProfileScreen()
                }
            }
        }
    }
}

// ---------------------------------------------------------------------
// MAIN SCREEN COMPOSABLE
// ---------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen() {
    val context = LocalContext.current
    var user by remember { mutableStateOf(UserStore.getCurrentUser()) }

    // Logic to update user state after updates
    DisposableEffect(Unit) {
        val userChangeListener: (UserStore.User?) -> Unit = { updatedUser -> user = updatedUser }
        UserStore.onUserChanged = userChangeListener
        onDispose { UserStore.onUserChanged = null }
    }

    if (user == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No user logged in.", style = MaterialTheme.typography.bodyLarge)
        }
        return
    }

    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeactivateDialog by remember { mutableStateOf(false) }
    var showEditProfile by remember { mutableStateOf(false) }
    var showEditTarget by remember { mutableStateOf(false) }

    val achievements = remember { mutableStateListOf<String>() }
    LaunchedEffect(user!!.id) {
        achievements.clear()
        achievements.addAll(UserStore.getAchievements(user!!.id))
    }

    val streakDays = UserStore.getStreakDays()
    val allDeliveries = UserStore.getDeliveryHistory(user!!.id)
    val totalRevenue = allDeliveries.sumOf { it.totalAmount }

    // Use a standard formatter for good practice
    // Using Locale "en", "KE" (Kenyan Shilling) as a placeholder for regional currency
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("en", "KE")) }

    val motivational = remember(streakDays) {
        when {
            streakDays <= 0 -> "Let's build a streak — start by completing today's target!"
            streakDays in 1..2 -> "Nice! You're on a $streakDays-day streak. Keep going!"
            streakDays in 3..6 -> "Great momentum — $streakDays days in a row! Don’t break the chain."
            streakDays in 7..29 -> "Amazing consistency — $streakDays-day streak! You're a top performer."
            else -> "Legendary! $streakDays days straight. You're unstoppable 🔥"
        }
    }

    // --- Content ---
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Profile", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background // Ensure Scaffold uses the correct background color
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                // Header (Profile Info) - Enhanced to look more like a card
                ProfileHeader(user = user!!, onEditClick = { showEditProfile = true })
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))

                // Summary cards - Metrics (Daily Target, Streak, Total Revenue)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MetricCard(
                        title = "Target",
                        value = "${user!!.dailyTarget}",
                        unit = "Deliveries",
                        modifier = Modifier.weight(1f),
                        icon = Icons.Filled.Adjust,
                        onClick = { showEditTarget = true }
                    )
                    MetricCard(
                        title = "Streak",
                        value = "$streakDays",
                        unit = "Days",
                        modifier = Modifier.weight(1f),
                        icon = Icons.Filled.Star
                    )
                    MetricCard(
                        title = "Revenue",
                        // Format the number to reduce length in the card
                        value = currencyFormat.format(totalRevenue).replace("KSh", "K"), // Example simplification
                        unit = "Total Sales",
                        modifier = Modifier.weight(1f),
                        icon = Icons.Filled.MonetizationOn
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Motivational message - Using tertiary container for a distinct look
                MotivationCard(motivational = motivational)

                Spacer(modifier = Modifier.height(20.dp))
            }

            // Achievements
            if (achievements.isNotEmpty()) {
                item {
                    AchievementsCard(achievements = achievements)
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            // Actions - Using the refined list-item style
            item {
                Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surface)) {
                    ProfileActionButton(
                        text = "Edit Daily Target",
                        icon = Icons.Filled.Adjust,
                        onClick = { showEditTarget = true }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ProfileActionButton(
                        text = "Change Password",
                        icon = Icons.Filled.Lock,
                        onClick = { Toast.makeText(context, "Password change coming soon!", Toast.LENGTH_SHORT).show() }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ProfileActionButton(
                        text = "Terms & Conditions",
                        icon = Icons.Filled.Description,
                        onClick = { context.startActivity(Intent(context, TermsActivity::class.java)) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surface)) {
                    ProfileActionButton(
                        text = "Logout",
                        icon = Icons.AutoMirrored.Filled.Logout,
                        color = MaterialTheme.colorScheme.error,
                        onClick = { showLogoutDialog = true }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ProfileActionButton(
                        text = "Deactivate Account",
                        icon = Icons.Filled.DeleteForever,
                        color = MaterialTheme.colorScheme.error,
                        onClick = { showDeactivateDialog = true }
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // --- Dialogs ---
    if (showEditProfile) {
        EditProfileDialog(
            user = user!!,
            onDismiss = { showEditProfile = false },
            onSave = { updated ->
                UserStore.updateUser(updated)
                UserStore.onUserChanged?.invoke(updated)
                showEditProfile = false
                Toast.makeText(context, "Profile updated", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (showEditTarget) {
        EditTargetDialog(
            onDismiss = { showEditTarget = false },
            onSave = { newTarget ->
                UserStore.updateDailyTarget(user!!.id, newTarget)
                val updatedUser = UserStore.getUserData(user!!.id)
                UserStore.onUserChanged?.invoke(updatedUser)
                showEditTarget = false
                Toast.makeText(context, "Daily target updated", Toast.LENGTH_SHORT).show()
            }
        )
    }

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

    if (showDeactivateDialog) {
        ConfirmationDialog(
            title = "Deactivate account",
            message = "This will remove your account from this device. Continue?",
            onConfirm = {
                Toast.makeText(context, "Account deletion not implemented yet", Toast.LENGTH_SHORT).show()
                showDeactivateDialog = false
            },
            onDismiss = { showDeactivateDialog = false }
        )
    }
}

// ---------------------------------------------------------------------
// ENHANCED COMPONENTS
// ---------------------------------------------------------------------

@Composable
fun ProfileHeader(user: UserStore.User, onEditClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_profile),
                contentDescription = "Profile avatar",
                modifier = Modifier
                    .size(64.dp) // Slightly smaller avatar
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(4.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(user.name, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                Text(user.email, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Role: ${user.role}", fontSize = 12.sp, fontStyle = FontStyle.Italic, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = onEditClick) {
                Icon(Icons.Filled.Edit, contentDescription = "Edit Profile", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileDialog(user: UserStore.User, onDismiss: () -> Unit, onSave: (UserStore.User) -> Unit) {
    // ... (Dialog implementation remains largely the same)
    var name by remember { mutableStateOf(TextFieldValue(user.name)) }
    var email by remember { mutableStateOf(TextFieldValue(user.email)) }
    var phone by remember { mutableStateOf(TextFieldValue(user.phone)) }
    var plate by remember { mutableStateOf(TextFieldValue(user.plate)) }
    var bikeBrand by remember { mutableStateOf(TextFieldValue(user.bikeBrand)) }
    var bikeCC by remember { mutableStateOf(TextFieldValue(user.bikeCC)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Profile") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item { OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") }) }
                item { OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }) }
                item { OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone Number") }) }
                item { OutlinedTextField(value = plate, onValueChange = { plate = it }, label = { Text("License Plate") }) }
                item { OutlinedTextField(value = bikeBrand, onValueChange = { bikeBrand = it }, label = { Text("Bike Brand") }) }
                item { OutlinedTextField(value = bikeCC, onValueChange = { bikeCC = it }, label = { Text("Bike CC") }) }
            }
        },
        confirmButton = {
            Button(onClick = {
                val updatedUser = user.copy(
                    name = name.text,
                    email = email.text,
                    phone = phone.text,
                    plate = plate.text,
                    bikeBrand = bikeBrand.text,
                    bikeCC = bikeCC.text
                )
                onSave(updatedUser)
            }) {
                Text("Save")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTargetDialog(onDismiss: () -> Unit, onSave: (Int) -> Unit) {
    // ... (Dialog implementation remains largely the same)
    var target by remember { mutableStateOf(TextFieldValue("")) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Daily Target") },
        text = {
            OutlinedTextField(
                value = target,
                onValueChange = { target = it },
                label = { Text("New target (e.g., 10)") },
                singleLine = true
            )
        },
        confirmButton = {
            Button(onClick = { onSave(target.text.toIntOrNull() ?: 0) }) {
                Text("Save")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}


@Composable
fun ConfirmationDialog(title: String, message: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    // ... (Dialog implementation remains largely the same)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Confirm")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    unit: String,
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector = Icons.Filled.Star,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .height(90.dp) // Explicit height for consistency
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = title, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                value,
                fontSize = 16.sp, // Slightly smaller font for better fit
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
            Text(
                unit,
                fontSize = 10.sp, // Smaller unit text
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun AchievementsCard(achievements: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.WorkspacePremium, contentDescription = "Achievements", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Achievements", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(modifier = Modifier.height(10.dp))
            if (achievements.isEmpty()) {
                Text("No achievements unlocked yet. Keep working!", fontStyle = FontStyle.Italic, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                achievements.take(3).forEach { achievement ->
                    Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.clip(CircleShape).size(6.dp).background(MaterialTheme.colorScheme.secondary))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(achievement, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
    }
}

@Composable
fun MotivationCard(motivational: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.TipsAndUpdates, contentDescription = "Motivation", tint = MaterialTheme.colorScheme.onTertiaryContainer, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(motivational, color = MaterialTheme.colorScheme.onTertiaryContainer, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun ProfileActionButton(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color = MaterialTheme.colorScheme.onSurface, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(text, color = color, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.weight(1f))
        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = color.copy(alpha = 0.6f))
    }

    @Composable
    fun BottomNavigationBar(onNavigate: (String) -> Unit) {
        NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 8.dp) {
            NavigationBarItem(
                selected = true,
                onClick = {},
                icon = { Icon(painterResource(R.drawable.ic_dashboard), contentDescription = "Dashboard") },
                label = { Text("Dashboard") }
            )
            NavigationBarItem(
                selected = false,
                onClick = { onNavigate("deliveries") },
                icon = { Icon(painterResource(R.drawable.ic_delivery), contentDescription = "Deliveries") },
                label = { Text("Deliveries") }
            )
            NavigationBarItem(
                selected = false,
                onClick = { onNavigate("profile") },
                icon = { Icon(painterResource(R.drawable.ic_prof), contentDescription = "Profile") },
                label = { Text("Profile") }
            )
        }
    }
}