package com.kiprono.mamambogaqrapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class DeliveryRole {
    BIKE_RIDER,
    TRUCK_DRIVER
}

@Entity(tableName = "delivery_users")
data class DeliveryUser(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phone: String,
    val password: String,
    val role: DeliveryRole,
    val vehicleBrand: String,
    val vehiclePlate: String,
    val bikeCC: Int? = null,
    val capacityTons: Double? = null,
    val dailyTarget: Int = 0
)