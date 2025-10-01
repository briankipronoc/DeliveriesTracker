package com.kiprono.mamambogaqrapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalDate

class DeliveriesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DeliveriesScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveriesScreen() {
    val user = UserStore.getCurrentUser()
    var customerName by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var deliveries by remember { mutableStateOf(UserStore.getDeliveryHistory(user?.id ?: "")) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Deliveries") })
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
            ) {
                OutlinedTextField(
                    value = customerName,
                    onValueChange = { customerName = it },
                    label = { Text("Customer Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Total Amount") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val amount = amountText.toDoubleOrNull()
                        if (user != null && amount != null) {
                            val delivery = UserStore.Delivery(
                                userId = user.id,
                                customerName = customerName,
                                totalAmount = amount,
                                date = LocalDate.now()
                            )
                            UserStore.addDelivery(delivery)
                            deliveries = UserStore.getDeliveryHistory(user.id)

                            customerName = ""
                            amountText = ""
                        } else {
                            Toast.makeText(
                                null,
                                "Invalid input",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add Delivery")
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text("Delivery History", style = MaterialTheme.typography.titleMedium)

                LazyColumn {
                    items(deliveries) { delivery ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Customer: ${delivery.customerName}")
                                Text("Amount: ${delivery.totalAmount}")
                                Text("Date: ${delivery.date}")
                                Text("Status: ${delivery.status}")
                            }
                        }
                    }
                }
            }
        }
    )
}