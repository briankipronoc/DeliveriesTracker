package com.kiprono.mamambogaqrapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kiprono.mamambogaqrapp.data.model.Delivery

@Composable
fun DeliveryListScreen(
    deliveries: List<Delivery>,
    onScanClicked: (Delivery) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        items(deliveries) { delivery ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable { onScanClicked(delivery) },
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Buyer: ${delivery.buyerName}", style = MaterialTheme.typography.bodyLarge)
                    Text("Mama Mboga: ${delivery.mamaMbogaName}", style = MaterialTheme.typography.bodyMedium)
                    Text("Status: ${delivery.status}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}