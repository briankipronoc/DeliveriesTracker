package com.kiprono.mamambogaqrapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kiprono.mamambogaqrapp.ui.theme.AppThemeState
import com.kiprono.mamambogaqrapp.ui.theme.MamaMbogaQRAppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class DeliveriesActivity : ComponentActivity() {
    private lateinit var qrScannerLauncher: ActivityResultLauncher<Intent>
    private var deliveryIdToComplete: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        qrScannerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val qrCode = result.data?.getStringExtra("qr_code_result")
                if (qrCode != null) {
                    if (deliveryIdToComplete != null) {
                        UserStore.completeDelivery(deliveryIdToComplete!!, qrCode)
                        Toast.makeText(this, "Delivery completed!", Toast.LENGTH_SHORT).show()
                        deliveryIdToComplete = null
                    } else {
                        UserStore.startDeliveryFromQR(qrCode)
                        Toast.makeText(this, "Delivery started!", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "QR Scan cancelled.", Toast.LENGTH_SHORT).show()
            }
        }

        setContent {
            MamaMbogaQRAppTheme(darkTheme = AppThemeState.isDark.value) {
                DeliveriesScreen(
                    onLaunchScanner = { launchQRScanner() },
                    onCompleteDelivery = { deliveryId -> launchQRScanner(deliveryId) },
                    onNavigate = { route -> Toast.makeText(this, "Navigating to $route", Toast.LENGTH_SHORT).show() }
                )
            }
        }
    }

    private fun launchQRScanner(deliveryId: String? = null) {
        this.deliveryIdToComplete = deliveryId
        val intent = Intent(this, ScannerActivity::class.java)
        qrScannerLauncher.launch(intent)
    }
@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun DeliveriesScreen(
    onLaunchScanner: () -> Unit,
    onCompleteDelivery: (String) -> Unit, // New callback
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val user = UserStore.getCurrentUser() ?: return
    val scope = rememberCoroutineScope()

    var deliveries by remember { mutableStateOf(UserStore.getDeliveryHistory(user.id)) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var isRefreshing by remember { mutableStateOf(false) }
    var selectedStatusFilter by remember { mutableStateOf("All") }
    val currentRoute by remember { mutableStateOf("deliveries") }

    DisposableEffect(Unit) {
        val currentUserId = user.id
        UserStore.onDeliveriesChanged = {
            deliveries = UserStore.getDeliveryHistory(currentUserId)
        }
        onDispose { UserStore.onDeliveriesChanged = null }
    }

    val completedDays = remember(deliveries) { deliveries.filter { it.status == "Completed" }.map { it.date.dayOfMonth }.toSet() }
    val deliveriesForSelectedDate = remember(deliveries, selectedDate) { UserStore.getDeliveriesForUserOnDate(user.id, selectedDate) }
    val filteredDeliveries = remember(deliveriesForSelectedDate, selectedStatusFilter) {
        deliveriesForSelectedDate.filter { d ->
            if (selectedStatusFilter == "All") true else d.status.equals(selectedStatusFilter, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Deliveries", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface, titleContentColor = MaterialTheme.colorScheme.onSurface),
                actions = {
                    IconButton(onClick = {
                        scope.launch {
                            isRefreshing = true
                            delay(800)
                            deliveries = UserStore.getDeliveryHistory(user.id)
                            isRefreshing = false
                        }
                    }) {
                        val rotation by animateFloatAsState(targetValue = if (isRefreshing) 360f else 0f, label = "Refresh Animation")
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.rotate(rotation))
                    }
                }
            )
        },
        floatingActionButton = { if (selectedStatusFilter == "All") { FloatingActionButton(onClick = onLaunchScanner, containerColor = MaterialTheme.colorScheme.primary, modifier = Modifier.shadow(8.dp, CircleShape)) { Icon(Icons.Default.QrCode2, contentDescription = "Start New Delivery", tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(32.dp)) } } },
        floatingActionButtonPosition = FabPosition.Center,
        bottomBar = { BottomNavigationBar(currentRoute = currentRoute, onNavigate = onNavigate) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // Date Picker Row
            DatePickerRow(
                selectedDate = selectedDate,
                onDateChange = { selectedDate = it },
                completedDays = completedDays
            )

            // Status Filter Row
            StatusFilterRow(
                selectedStatus = selectedStatusFilter,
                onSelectStatus = { selectedStatusFilter = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Animated delivery list
            AnimatedContent(
                targetState = filteredDeliveries.hashCode(),
                label = "DeliveryListAnimation"
            ) {
                if (filteredDeliveries.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No ${selectedStatusFilter.lowercase()} deliveries on this date.",
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(32.dp)
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item(key = "summary") {
                            DeliverySummaryBar(deliveriesForSelectedDate)
                        }
                        items(filteredDeliveries, key = { it.id }) { delivery ->
                            DeliveryCard(
                                delivery = delivery,
                                onComplete = { onCompleteDelivery(delivery.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusFilterRow(selectedStatus: String, onSelectStatus: (String) -> Unit) {
    val statuses = listOf("All", "Completed", "Ongoing", "Cancelled")
    Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        statuses.forEach { status ->
            FilterChip(
                selected = selectedStatus == status,
                onClick = { onSelectStatus(status) },
                label = { Text(status, fontWeight = FontWeight.Medium) },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primary, selectedLabelColor = MaterialTheme.colorScheme.onPrimary, containerColor = MaterialTheme.colorScheme.surfaceContainerHigh, labelColor = MaterialTheme.colorScheme.onSurfaceVariant),
                border = FilterChipDefaults.filterChipBorder(borderColor = MaterialTheme.colorScheme.outlineVariant, selectedBorderColor = Color.Transparent, enabled = true, selected = selectedStatus == status)
            )
        }
    }
}

@Composable
fun DeliverySummaryBar(deliveries: List<UserStore.Delivery>) {
    val totalCount = deliveries.size
    val completedCount = deliveries.count { it.status == "Completed" }
    val totalAmount = deliveries.filter { it.status == "Completed" }.sumOf { it.totalAmount }
    val completionPercentage = if (totalCount > 0) (completedCount.toFloat() / totalCount * 100).toInt() else 0

    Row(modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surfaceContainerLow, RoundedCornerShape(8.dp)).padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        SummaryItem(label = "Total Trips", value = totalCount.toString(), color = MaterialTheme.colorScheme.onSurface)
        VerticalDivider()
        SummaryItem(label = "Done", value = "$completedCount ($completionPercentage%)", color = MaterialTheme.colorScheme.primary)
        VerticalDivider()
        SummaryItem(label = "Total Sales", value = String.format(Locale.getDefault(), "Ksh %,.0f", totalAmount), color = MaterialTheme.colorScheme.secondary)
    }
}

@Composable
fun RowScope.SummaryItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f).padding(horizontal = 4.dp)) {
        Text(text = value, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = color, maxLines = 1)
        Spacer(Modifier.height(2.dp))
        Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f), maxLines = 1)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerRow(selectedDate: LocalDate, onDateChange: (LocalDate) -> Unit, completedDays: Set<Int>) {
    var showDatePicker by remember { mutableStateOf(false) }
    val highlightColor = MaterialTheme.colorScheme.primary
    val isCompletedDay = completedDays.contains(selectedDate.dayOfMonth)
    val today = LocalDate.now()
    val todayLabel = if (selectedDate == today) " (Today)" else ""

    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.clickable { showDatePicker = true }) {
            Text("Selected Date", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("${selectedDate.format(DateTimeFormatter.ISO_DATE)}$todayLabel", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = if (isCompletedDay) highlightColor else MaterialTheme.colorScheme.onSurface)
        }
        IconButton(onClick = { showDatePicker = true }, modifier = Modifier.border(2.dp, if (isCompletedDay) highlightColor else MaterialTheme.colorScheme.outline, CircleShape).size(48.dp)) {
            Icon(Icons.Default.CalendarToday, contentDescription = "Pick Date", tint = highlightColor)
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli())
        DatePickerDialog(onDismissRequest = { showDatePicker = false }, confirmButton = { TextButton(onClick = { datePickerState.selectedDateMillis?.let { onDateChange(java.time.Instant.ofEpochMilli(it).atZone(java.time.ZoneId.systemDefault()).toLocalDate()) }; showDatePicker = false }) { Text("OK") } }, dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }) { DatePicker(state = datePickerState) }
    }
}

@Composable
fun DeliveryCard(delivery: UserStore.Delivery, onComplete: () -> Unit) {
    val (bgColor, textColor, statusColor) = when (delivery.status) {
        "Completed" -> Triple(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer, MaterialTheme.colorScheme.primary)
        "Cancelled" -> Triple(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f), MaterialTheme.colorScheme.onErrorContainer, MaterialTheme.colorScheme.error)
        else -> Triple(MaterialTheme.colorScheme.surfaceContainerHigh, MaterialTheme.colorScheme.onSurface, MaterialTheme.colorScheme.secondary)
    }
    val timeFormat = remember { DateTimeFormatter.ofPattern("hh:mm a") }

    Card(modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(12.dp)), colors = CardDefaults.cardColors(containerColor = bgColor)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Ksh ${String.format(Locale.getDefault(), "%,.0f", delivery.totalAmount)}", fontWeight = FontWeight.ExtraBold, fontSize = 19.sp, color = textColor)
                    Spacer(Modifier.height(4.dp))
                    Text(delivery.status, color = statusColor, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 2.dp))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(delivery.customerName, fontWeight = FontWeight.SemiBold, fontSize = 17.sp, color = textColor)
                    Spacer(Modifier.height(4.dp))
                    Text("Picked up at: ${delivery.scanTime.format(timeFormat)}", color = textColor.copy(alpha = 0.8f), fontSize = 12.sp)
                    if (delivery.deliveryTime != null) {
                        Text("Delivered at: ${delivery.deliveryTime!!.format(timeFormat)}", color = textColor.copy(alpha = 0.8f), fontSize = 12.sp)
                    }
                }
            }
            if (delivery.status == "Ongoing") {
                Spacer(Modifier.height(12.dp))
                Button(onClick = onComplete, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Complete Delivery (Scan Buyer QR)")
                }
            }
        }
    }
}

@Composable
fun VerticalDivider(modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.outlineVariant, thickness: Dp = 1.dp) {
    Box(modifier = modifier.height(36.dp).width(thickness).background(color = color))
}

@Composable
fun BottomNavigationBar(currentRoute: String, onNavigate: (String) -> Unit) {
    BottomAppBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 8.dp) {
        val navItems = listOf("dashboard" to Icons.Default.Home, "deliveries" to Icons.Default.LocalShipping, "profile" to Icons.Default.Person)
        navItems.forEach { (route, icon) ->
            NavigationBarItem(selected = currentRoute == route, onClick = { onNavigate(route) }, icon = { Icon(icon, contentDescription = route) }, label = { Text(route.replaceFirstChar { it.uppercase() }) }, colors = NavigationBarItemDefaults.colors(selectedIconColor = MaterialTheme.colorScheme.primary, selectedTextColor = MaterialTheme.colorScheme.primary, indicatorColor = MaterialTheme.colorScheme.primaryContainer), modifier = Modifier.weight(1f))
        }
    }
}
}
