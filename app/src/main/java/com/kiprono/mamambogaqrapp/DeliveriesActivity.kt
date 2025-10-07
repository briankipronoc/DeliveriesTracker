package com.kiprono.mamambogaqrapp

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.format.DateTimeFormatter

@Composable
fun DeliveriesScreen() {
    val user = UserStore.getCurrentUser()
    var deliveries by remember { mutableStateOf(user?.let { UserStore.getDeliveryHistory(it.id) } ?: emptyList()) }

    DisposableEffect(Unit) {
        UserStore.onDeliveriesChanged = {
            deliveries = user?.let { UserStore.getDeliveryHistory(it.id) } ?: emptyList()
        }
        onDispose { UserStore.onDeliveriesChanged = null }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("My Deliveries", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(bottom = 12.dp))

        if (deliveries.isEmpty()) {
            Text("No deliveries yet.", style = MaterialTheme.typography.bodyLarge)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
                items(deliveries) { d ->
                    DeliveryCard(d)
                }
            }
        }
    }
}

@Composable
fun DeliveryCard(delivery: UserStore.Delivery) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Customer: ${delivery.customerName}", style = MaterialTheme.typography.titleMedium)
            Text("Amount: ${delivery.totalAmount}", style = MaterialTheme.typography.bodyMedium)
            Text("Date: ${delivery.date.format(DateTimeFormatter.ISO_DATE)} ${delivery.time.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))}",
                style = MaterialTheme.typography.bodySmall)
            Text("Status: ${delivery.status}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}