package com.kiprono.mamambogaqrapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val customerName: String,
    val items: String,
    val totalPrice: Double,
    val date: Date,
    val status: String
)