package com.kiprono.mamambogaqrapp.data.model

// Enum to represent delivery states
enum class DeliveryStatus {
    PENDING,
    PICKED_UP,
    DELIVERED
}

// Data class for each delivery
data class Delivery(
    val id: String,
    val buyerName: String,
    val mamaMbogaName: String,
    var status: DeliveryStatus = DeliveryStatus.PENDING
)