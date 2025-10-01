package com.kiprono.mamambogaqrapp.data.local

import androidx.room.TypeConverter
import java.util.Date

class Converters {

    // Date <-> Long
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    // DeliveryRole <-> String
    @TypeConverter
    fun fromRole(value: String?): DeliveryRole? {
        return value?.let { DeliveryRole.valueOf(it) }
    }

    @TypeConverter
    fun roleToString(role: DeliveryRole?): String? {
        return role?.name
    }
}