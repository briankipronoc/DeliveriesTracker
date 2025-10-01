package com.kiprono.mamambogaqrapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val achievedDate: Long // store as timestamp, converted with Converters if needed
)