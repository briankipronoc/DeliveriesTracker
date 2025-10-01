package com.kiprono.mamambogaqrapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        AchievementEntity::class,
        OrderEntity::class,
        DeliveryEntity::class,
        DeliveryUser::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun achievementDao(): AchievementDao
    abstract fun orderDao(): OrderDao
    abstract fun deliveryDao(): DeliveryDao
    abstract fun deliveryUserDao(): DeliveryUserDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mama_mboga_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}