package com.kiprono.mamambogaqrapp

import android.app.Application
import com.jakewharton.threetenabp.AndroidThreeTen
import com.kiprono.mamambogaqrapp.data.local.AppDatabase
import com.kiprono.mamambogaqrapp.data.repository.AchievementRepository
import com.kiprono.mamambogaqrapp.data.repository.OrderRepository

class MamaMbogaApp : Application() {

    // Database instance
    val database by lazy { AppDatabase.getDatabase(this) }

    // Repositories
    val orderRepository by lazy { OrderRepository(database.orderDao()) }
    val achievementRepository by lazy { AchievementRepository(database.achievementDao()) }

    override fun onCreate() {
        super.onCreate()
        // Initialize ThreeTenABP for LocalDate/DateTime APIs
        AndroidThreeTen.init(this)
    }
}