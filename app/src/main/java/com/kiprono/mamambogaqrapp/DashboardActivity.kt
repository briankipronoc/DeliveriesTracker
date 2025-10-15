package com.kiprono.mamambogaqrapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.kiprono.mamambogaqrapp.ui.theme.AppThemeState
import com.kiprono.mamambogaqrapp.ui.theme.MamaMbogaQRAppTheme
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

// KONFETTI IMPORTS
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.*
import nl.dionsegijn.konfetti.core.emitter.Emitter
import nl.dionsegijn.konfetti.core.models.Shape
import java.util.concurrent.TimeUnit
// END KONFETTI IMPORTS

// Placeholder/Mock classes (assuming they exist in the actual project)
// NOTE: I am not including the actual implementation of these external classes
// but their usage implies their existence.
// (e.g., DeliveriesActivity, ProfileActivity, LoginActivity, R.drawable, UserStore, AppThemeState)

class DashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MamaMbogaQRAppTheme(darkTheme = AppThemeState.isDark.value) {
                DashboardScreen(activity = this, onNavigate = { destination ->
                    when (destination) {
                        "deliveries" -> {
                            try {
                                startActivity(Intent(this, DeliveriesActivity::class.java))
                            } catch (e: Exception) {
                                Toast.makeText(
                                    this,
                                    "Deliveries screen not available.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        "profile" -> {
                            try {
                                startActivity(Intent(this, ProfileActivity::class.java))
                            } catch (e: Exception) {
                                Toast.makeText(
                                    this,
                                    "Profile screen not available.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(activity: ComponentActivity, onNavigate: (String) -> Unit) {
    val context = LocalContext.current
    val user by rememberUpdatedState(UserStore.getCurrentUser())
    val isDarkTheme = AppThemeState.isDark.value

    // ADDED: Scroll State for the main content
    val scrollState = rememberScrollState()

    // --- Login/Empty State Logic ---
    if (user == null) {
        var showLoginPrompt by remember { mutableStateOf(true) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Welcome to SmartRider",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Please log in to view your dashboard.", color = Color.Gray)
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = {
                    try {
                        context.startActivity(Intent(context, LoginActivity::class.java))
                    } catch (e: Exception) {
                        Toast.makeText(context, "Login screen not available.", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text("Login Now")
                }
            }
        }

        if (showLoginPrompt) {
            AlertDialog(
                onDismissRequest = { showLoginPrompt = false },
                title = { Text("Not signed in") },
                text = { Text("You're not signed in. Please log in to view your deliveries and progress.") },
                confirmButton = {
                    TextButton(onClick = {
                        try {
                            context.startActivity(Intent(context, LoginActivity::class.java))
                            activity.finish()
                        } catch (e: Exception) {
                            Toast.makeText(context, "Login screen not available.", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Text("Go to Login", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLoginPrompt = false }) {
                        Text("Cancel")
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface
            )
        }
        return
    }
    // --- End Login/Empty State Logic ---

    // ---------------- Main Dashboard Logic ----------------
    val today = LocalDate.now()
    val allDeliveriesForUser by remember(user!!.id) {
        derivedStateOf { UserStore.getDeliveryHistory(user!!.id) }
    }

    // Calculate 7-day performance data for the chart
    val dailyTarget = user!!.dailyTarget.coerceAtLeast(1)
    val lastSevenDaysData = remember(allDeliveriesForUser) {
        List(7) { i ->
            val date = today.minusDays(i.toLong())
            val completed = allDeliveriesForUser.count { it.date == date && it.status.equals("Completed", true) }
            completed
        }.reversed() // Result is: [Day-6, Day-5, ..., Today]
    }

    val weeklyTarget = dailyTarget * 7
    val monthlyTarget = dailyTarget * today.lengthOfMonth()

    val completedToday = lastSevenDaysData.last()
    val completedWeek = lastSevenDaysData.sum()
    val completedMonth = allDeliveriesForUser.count {
        it.date.month == today.month && it.status.equals("Completed", true)
    }

    // Progress Calculations
    val dailyProgress = (completedToday / dailyTarget.toFloat()).coerceAtMost(1f)
    val weeklyProgress = (completedWeek / weeklyTarget.toFloat()).coerceAtMost(1f)
    val monthlyProgress = (completedMonth / monthlyTarget.toFloat()).coerceAtMost(1f)

    var selectedTab by remember { mutableStateOf("Daily") }
    val progressForTab = when (selectedTab) {
        "Weekly" -> weeklyProgress
        "Monthly" -> monthlyProgress
        else -> dailyProgress
    }
    val targetCompleted = when (selectedTab) {
        "Weekly" -> completedWeek
        "Monthly" -> completedMonth
        else -> completedToday
    }
    val targetTotal = when (selectedTab) {
        "Weekly" -> weeklyTarget
        "Monthly" -> monthlyTarget
        else -> dailyTarget
    }

    val animatedProgress by animateFloatAsState(targetValue = progressForTab, animationSpec = tween(800))
    val streakDays = UserStore.getStreakDays()

    var showConfetti by remember { mutableStateOf(false) }
    val celebrated = remember { mutableStateListOf<String>() }

    LaunchedEffect(animatedProgress, streakDays) {
        // Trigger confetti for target hit
        if (animatedProgress >= 0.999f && !celebrated.contains("target_${selectedTab}_${today}")) {
            celebrated.add("target_${selectedTab}_${today}")
            showConfetti = true
            delay(1500)
            showConfetti = false
        }
        // Trigger confetti for streak milestones (3, 7, 30 days)
        if (streakDays in listOf(3, 7, 30) && !celebrated.contains("streak_$streakDays")) {
            celebrated.add("streak_$streakDays")
            showConfetti = true
            delay(1500)
            showConfetti = false
        }
    }

    // DYNAMIC MOTIVATIONAL MESSAGE
    val motivational = remember(streakDays, animatedProgress, completedToday, dailyTarget) {
        when {
            animatedProgress >= 0.999f && selectedTab == "Daily" -> "Target reached — great job! Keep the momentum going for your streak!"
            animatedProgress >= 0.999f && streakDays >= 7 -> "Legendary streak + target hit — you’re unstoppable 🔥"
            streakDays >= 7 -> "7+ days in a row — consistency wins!"
            streakDays >= 3 -> "You’re on a $streakDays-day streak — keep going!"
            dailyProgress >= 0.75f -> "Almost there for today — just ${dailyTarget - completedToday} more deliveries!"
            else -> "Complete ${(dailyTarget - completedToday).coerceAtLeast(0)} more to reach your daily goal!"
        }
    }

    Scaffold(
        // NEW: Top Bar for theme toggle
        topBar = {
            TopAppBar(
                title = { Text("Dashboard", fontWeight = FontWeight.SemiBold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                actions = {
                    IconButton(
                        onClick = { AppThemeState.isDark.value = !AppThemeState.isDark.value },
                        modifier = Modifier.semantics {
                            contentDescription = if (isDarkTheme) "Switch to Light Mode" else "Switch to Dark Mode"
                        }
                    ) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                            contentDescription = null // Handled by parent IconButton
                        )
                    }
                }
            )
        },
        bottomBar = { BottomNavigationBar(onNavigate) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) { // Use Box to layer content and confetti

            // ADDED: verticalScroll modifier to enable scrolling
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(horizontal = 16.dp)
                    .fillMaxSize()
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                GreetingHeader(user, motivational)
                Spacer(modifier = Modifier.height(18.dp))

                SummaryTabs(selectedTab = selectedTab, onSelect = { selectedTab = it })

                SummaryCardCentered(
                    title = when (selectedTab) {
                        "Weekly" -> "Weekly Progress"
                        "Monthly" -> "Monthly Goal"
                        else -> "Daily Target"
                    },
                    animatedProgress = animatedProgress,
                    mainStat = if (selectedTab == "Daily") {
                        "$targetCompleted/$targetTotal"
                    } else {
                        "${(progressForTab * 100).toInt()}%"
                    },
                    subtitle = when (selectedTab) {
                        "Weekly" -> "($targetCompleted / $targetTotal deliveries)"
                        "Monthly" -> "($targetCompleted / $targetTotal deliveries)"
                        else -> "deliveries today"
                    }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)

                // NEW: Lifetime Statistics Card
                LifetimeStatsCard(allDeliveriesForUser)
                Spacer(modifier = Modifier.height(16.dp))

                DailyPerformanceChart(lastSevenDaysData, dailyTarget)
                Spacer(modifier = Modifier.height(16.dp))

                // UPDATED: Combined Motivational/Streak Section in a Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StreakSection(streakDays, Modifier.weight(1f)) // Pass modifier
                    MotivationalCard(motivational, Modifier.weight(1f)) // Pass modifier
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Confetti Overlay positioned at the end of the Box to ensure it is on top (higher zIndex)
            if (showConfetti) {
                ConfettiOverlay()
            }
        }
    }
}
// END OF DASHBOARDSCREEN


// ------------------------------------------------------------------------------------
// NEW: LIFETIME STATISTICS CARD
// ------------------------------------------------------------------------------------

@Composable
private fun LifetimeStatsCard(allDeliveries: List<UserStore.Delivery>) {
    val completedDeliveries = remember(allDeliveries) {
        allDeliveries.count { it.status.equals("Completed", true) }
    }

    // Calculate average daily deliveries based on the time since the first delivery
    val averageDaily = remember(allDeliveries) {
        if (allDeliveries.isEmpty()) {
            "0.0"
        } else {
            val firstDate = allDeliveries.minOf { it.date }
            // Calculate days, minimum 1 day to avoid division by zero if all deliveries were today
            val daysBetween = ChronoUnit.DAYS.between(firstDate, LocalDate.now()).toInt().coerceAtLeast(1)
            val avg = completedDeliveries.toFloat() / daysBetween
            String.format(Locale.getDefault(), "%.1f", avg)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            StatItem(label = "Total Lifetime Deliveries", value = completedDeliveries.toString())
            StatItem(label = "Avg. Daily Deliveries", value = averageDaily)
        }
    }
}

@Composable
private fun RowScope.StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
        Text(
            text = value,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.secondary
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}


// ------------------------------------------------------------------------------------
// DYNAMIC COLOR LOGIC FOR STREAK SECTION (MODIFIED TO ACCEPT MODIFIER)
// ------------------------------------------------------------------------------------

@Composable
private fun getStreakColor(streakDays: Int): Color {
    val initialColor = MaterialTheme.colorScheme.surface
    val level1 = MaterialTheme.colorScheme.tertiaryContainer // 3 days: Light Tertiary
    val level2 = Color(0xFFB39DDB) // 7 days: Lavender (medium intensity)
    val level3 = Color(0xFFFDD835) // 30+ days: Gold/Yellow (high intensity)

    val targetColor = when {
        streakDays >= 30 -> level3
        streakDays >= 7 -> level2
        streakDays >= 3 -> level1
        else -> initialColor
    }

    return animateColorAsState(targetColor, animationSpec = tween(1000)).value
}

@Composable
private fun StreakSection(streakDays: Int, modifier: Modifier = Modifier) {
    val cardColor = getStreakColor(streakDays)

    // Determine text color based on the background for contrast
    val textColor = if (cardColor == MaterialTheme.colorScheme.surface) {
        MaterialTheme.colorScheme.onSurface
    } else {
        // Use a darker color for light level backgrounds
        MaterialTheme.colorScheme.onBackground
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = cardColor) // DYNAMIC COLOR
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "🔥 Streak",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            Text(
                "$streakDays day${if (streakDays == 1) "" else "s"}",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary // Keep primary color for emphasis
            )
            Text(
                "Keep going — daily consistency wins!",
                color = textColor.copy(alpha = 0.8f),
                fontSize = 12.sp
            )
        }
    }
}


// ------------------------------------------------------------------------------------
// THEMATIC CONFETTI OVERLAY (FIXED Z-INDEX)
// ------------------------------------------------------------------------------------

@Composable
private fun ConfettiOverlay() {
    // Colors inspired by Mama Mboga (vegetables/fruit)
    val greenColor = Color(0xFF66BB6A)
    val orangeColor = Color(0xFFFF9800)
    val redColor = Color(0xFFE53935)
    val yellowColor = Color(0xFFFFEE58)

    val konfettiColors = listOf(greenColor.toArgb(), orangeColor.toArgb(), redColor.toArgb(), yellowColor.toArgb())

    // ADDED: zIndex to ensure it overlays all other content
    Box(modifier = Modifier.fillMaxSize().zIndex(10f), contentAlignment = Alignment.Center) {
        // Konfetti View (creates a double-blast effect)
        KonfettiView(
            modifier = Modifier.fillMaxSize(),
            parties = listOf(
                // Burst 1: Shoots upwards from the bottom center (fountain)
                Party(
                    speed = 10f,
                    maxSpeed = 50f,
                    damping = 0.9f,
                    angle = Angle.TOP,
                    spread = 45,
                    timeToLive = 1500L,
                    fadeOutEnabled = true,
                    shapes = listOf(Shape.Square, Shape.Circle),
                    colors = konfettiColors,
                    emitter = Emitter(duration = 100, TimeUnit.MILLISECONDS).max(50),
                    position = Position.Relative(0.5, 1.0)
                ),
                // Burst 2: Explodes out from the center (fireworks)
                Party(
                    speed = 0f,
                    maxSpeed = 30f,
                    damping = 0.9f,
                    angle = Angle.TOP,
                    spread = 360,
                    timeToLive = 2000L,
                    fadeOutEnabled = true,
                    shapes = listOf(Shape.Square, Shape.Circle),
                    colors = konfettiColors,
                    emitter = Emitter(duration = 500, TimeUnit.MILLISECONDS).max(100),
                    position = Position.Relative(0.5, 0.5)
                )
            )
        )

        // Bold, Animated celebratory text
        val scale = remember { androidx.compose.animation.core.Animatable(0.2f) }
        LaunchedEffect(Unit) {
            scale.animateTo(1.3f, animationSpec = spring(dampingRatio = 0.4f, stiffness = 200f))
            scale.animateTo(1f, animationSpec = tween(200))
        }

        Text(
            text = "🔥 TARGET SMASHED! 🔥",
            fontWeight = FontWeight.Black,
            fontSize = 30.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .scale(scale.value)
                .background(Color.White.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

// ------------------------------------------------------------------------------------
// UPDATED GREETING HEADER (ACCEPTS DYNAMIC MESSAGE)
// ------------------------------------------------------------------------------------

@Composable
fun GreetingHeader(currentUser: UserStore.User?, dynamicMessage: String) { // NOTE: Added dynamicMessage
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(20.dp))
            .background(
                brush = Brush.horizontalGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Good Afternoon, ${currentUser?.name ?: "Friend"} 👋", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                // DYNAMIC MESSAGE USED HERE
                Text(dynamicMessage, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f))
            }
            Image(
                painter = painterResource(id = R.drawable.avatar),
                contentDescription = "Profile",
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }
    }
}


// ------------------------------------------------------------------------------------
// UNMODIFIED SUPPORTING COMPOSABLES (Remaining)
// ------------------------------------------------------------------------------------

@Composable
private fun DailyPerformanceChart(lastSevenDaysData: List<Int>, dailyTarget: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Last 7 Days Performance",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Chart area
            val maxDeliveries = lastSevenDaysData.maxOrNull()?.coerceAtLeast(dailyTarget) ?: dailyTarget
            val targetLineY = if (maxDeliveries > 0) dailyTarget.toFloat() / maxDeliveries else 0f

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.Bottom
            ) {
                lastSevenDaysData.forEachIndexed { index, deliveries ->
                    val date = LocalDate.now().minusDays((6 - index).toLong())
                    val barHeight = if (maxDeliveries > 0) deliveries.toFloat() / maxDeliveries else 0f

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // Value label
                        Text(
                            text = deliveries.toString(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))

                        // Chart Bar
                        Box(
                            modifier = Modifier
                                .fillMaxHeight(barHeight)
                                .width(16.dp)
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(
                                    if (deliveries >= dailyTarget) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
                                )
                        )
                        // Date Label
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (index == 6) "Today" else date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            // Draw the target line and label outside the bar loop
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                )
                Text(
                    " Daily Target: $dailyTarget ",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun SummaryCardCentered(title: String, animatedProgress: Float, mainStat: String, subtitle: String) {
    // Resolve colors in the Composable scope
    val resolvedPrimaryColor = MaterialTheme.colorScheme.primary
    val trackColor = resolvedPrimaryColor.copy(alpha = 0.15f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .shadow(6.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = resolvedPrimaryColor)
            Spacer(modifier = Modifier.height(14.dp))

            Box(contentAlignment = Alignment.Center) {
                // Progress Ring
                val progressDescription = "$title, $mainStat completed. Progress bar at ${animatedProgress * 100} percent."
                Canvas(
                    modifier = Modifier
                        .size(140.dp)
                        .semantics { contentDescription = progressDescription }
                ) {
                    // Background track
                    drawArc(
                        color = trackColor,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = 18f, cap = StrokeCap.Round)
                    )
                    // Progress arc
                    drawArc(
                        color = resolvedPrimaryColor,
                        startAngle = -90f,
                        sweepAngle = 360f * animatedProgress,
                        useCenter = false,
                        style = Stroke(width = 18f, cap = StrokeCap.Round)
                    )
                }
                // Text overlay
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(mainStat, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = resolvedPrimaryColor)
                    Text(subtitle, color = Color.Gray, fontSize = 13.sp, textAlign = TextAlign.Center)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("${(animatedProgress * 100).toInt()}% achieved", color = Color.Gray, fontSize = 13.sp)
        }
    }
}

@Composable
private fun MotivationalCard(text: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun SummaryTabs(selectedTab: String, onSelect: (String) -> Unit) {
    val tabs = listOf("Daily", "Weekly", "Monthly")
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        tabs.forEach { tab ->
            FilterChip(
                selected = selectedTab == tab,
                onClick = { onSelect(tab) },
                label = { Text(tab) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
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