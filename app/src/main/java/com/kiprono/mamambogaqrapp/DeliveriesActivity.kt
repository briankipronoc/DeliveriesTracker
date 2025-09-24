// DeliveriesActivity.kt

package com.kiprono.mamambogaqrapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kiprono.mamambogaqrapp.ui.theme.MamaMbogaQRAppTheme
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp

data class Delivery(
    val id: String,
    val status: String,
    val price: Double
)

class DeliveriesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Mock data for testing
        // Add a new completed delivery to see the analytics update
        val deliveries = listOf(
            Delivery("1", "Ongoing", 0.0),
            Delivery("2", "Completed", 500.0),
            Delivery("3", "Completed", 350.0),
            Delivery("4", "Cancelled", 0.0),
            Delivery("5", "Completed", 450.0),
            Delivery("6", "Ongoing", 0.0),
            Delivery("7", "Completed", 600.0) // New delivery added for testing
        )

        setContent {
            MamaMbogaQRAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DeliveriesScreen(deliveries)
                }
            }
        }
    }
}

@Composable
fun DeliveriesScreen(deliveries: List<Delivery>) {
    val completedCount = deliveries.count { it.status == "Completed" }
    val ongoingCount = deliveries.count { it.status == "Ongoing" }
    val cancelledCount = deliveries.count { it.status == "Cancelled" }
    val totalDeliveries = deliveries.size.toFloat()

    val completedProgress = if (totalDeliveries > 0) completedCount.toFloat() / totalDeliveries else 0f
    val ongoingProgress = if (totalDeliveries > 0) ongoingCount.toFloat() / totalDeliveries else 0f
    val cancelledProgress = if (totalDeliveries > 0) cancelledCount.toFloat() / totalDeliveries else 0f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Spacer(Modifier.height(48.dp))

        Text("Your Deliveries", style = MaterialTheme.typography.headlineSmall)

        Spacer(Modifier.height(16.dp))

        // Three-ring analytics widget
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Completed Ring
                AnalyticsRing(
                    title = "Completed",
                    value = completedCount,
                    progress = completedProgress,
                    color = Color.Green
                )
                // Ongoing Ring
                AnalyticsRing(
                    title = "Ongoing",
                    value = ongoingCount,
                    progress = ongoingProgress,
                    color = Color.Yellow
                )
                // Cancelled Ring
                AnalyticsRing(
                    title = "Cancelled",
                    value = cancelledCount,
                    progress = cancelledProgress,
                    color = Color.Red
                )
            }
        }

        // List
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(deliveries) { delivery ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
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
fun AnalyticsRing(title: String, value: Int, progress: Float, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = progress,
                modifier = Modifier.size(60.dp),
                strokeWidth = 6.dp,
                color = color
            )
            Text(
                text = "$value",
                fontSize = 16.sp,
                style = MaterialTheme.typography.bodyLarge
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(title, style = MaterialTheme.typography.labelMedium)
    }
}