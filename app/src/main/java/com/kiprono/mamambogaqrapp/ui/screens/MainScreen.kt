package com.kiprono.mamambogaqrapp.ui.screens

import androidx.compose.runtime.*
import com.kiprono.mamambogaqrapp.data.model.*

@Composable
fun MainScreen() {
    var showScanner by remember { mutableStateOf(false) }
    val deliveries = remember {
        mutableStateListOf(
            Delivery("1", "Brian Kiprono", "Mama Leah"),
            Delivery("2", "Mercy Cherono", "Mama Wanjiku"),
            Delivery("3", "John Chebet", "Mama Njeri")
        )
    }
    var selectedDelivery by remember { mutableStateOf<Delivery?>(null) }

    if (showScanner && selectedDelivery != null) {
        QRScannerScreen { qrResult ->
            selectedDelivery?.status = when (selectedDelivery?.status) {
                DeliveryStatus.PENDING -> DeliveryStatus.PICKED_UP
                DeliveryStatus.PICKED_UP -> DeliveryStatus.DELIVERED
                else -> DeliveryStatus.DELIVERED
            }
            showScanner = false
        }
    } else {
        DeliveryListScreen(
            deliveries = deliveries,
            onScanClicked = { delivery ->
                selectedDelivery = delivery
                showScanner = true
            }
        )
    }
}