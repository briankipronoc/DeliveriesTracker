package com.kiprono.mamambogaqrapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.kiprono.mamambogaqrapp.data.model.Delivery
import com.kiprono.mamambogaqrapp.data.model.DeliveryStatus
import com.kiprono.mamambogaqrapp.ui.screens.DeliveryListScreen
import com.kiprono.mamambogaqrapp.ui.screens.QRScannerScreen
import com.kiprono.mamambogaqrapp.ui.theme.MamaMbogaQRAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MamaMbogaQRAppTheme {
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
                        // Simulate status change after scan
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
        }
    }
}