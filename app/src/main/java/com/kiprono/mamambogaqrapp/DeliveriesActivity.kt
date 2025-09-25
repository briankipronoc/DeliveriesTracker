package com.kiprono.mamambogaqrapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.LocalDate

class DeliveriesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                DeliveriesScreen()
            }
        }
    }
}

@Composable
fun DeliveriesScreen() {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    val deliveries = remember {
        listOf(
            Delivery("D123", LocalDate.now(), "Completed", 350.0),
            Delivery("D124", LocalDate.now(), "Pending", null),
            Delivery("D125", LocalDate.now().plusDays(1), "Completed", 500.0)
        )
    }

    val deliveriesByDate = deliveries.groupBy { it.date }
    val todayDeliveries = deliveriesByDate[selectedDate] ?: emptyList()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Deliveries", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(16.dp))

        CalendarView(
            deliveriesByDate = deliveriesByDate,
            selectedDate = selectedDate,
            onDateSelected = { selectedDate = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        DeliveryList(deliveries = todayDeliveries)
    }
}

@Composable
fun DeliveryList(
    deliveries: List<Delivery>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth()
    ) {
        if (deliveries.isEmpty()) {
            item {
                Text(
                    "No deliveries found for this day.",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            items(deliveries) { delivery ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text("Delivery ID: ${delivery.id}")
                        Text("Status: ${delivery.status}")
                        if (delivery.status == "Completed") {
                            Text("Price: KES ${delivery.price}")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarView(
    deliveriesByDate: Map<LocalDate, List<Delivery>>,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    val currentMonth = remember { LocalDate.now().withDayOfMonth(1) }
    val daysInMonth = currentMonth.lengthOfMonth()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("📅 Calendar goes here")
            Text("Month has $daysInMonth days")
        }
    }
}

data class Delivery(
    val id: String,
    val date: LocalDate,
    val status: String,
    val price: Double?
)