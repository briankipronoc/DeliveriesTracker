package com.kiprono.mamambogaqrapp

import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

// Centralized store for managing users and deliveries
object UserStore {

    // ------------------ MODELS ------------------
    data class User(
        val id: String = UUID.randomUUID().toString(),
        val username: String,
        val password: String,
        val name: String,
        val phone: String,
        val email: String,
        val bikeBrand: String,
        val bikeCC: String,
        val plate: String,
        val dailyTarget: Int = 5, // Example target
        val role: String = "Rider" // ✅ Added role so ProfileActivity compiles
    )

    data class Delivery(
        val id: String = UUID.randomUUID().toString(),
        val userId: String,
        val customerName: String,
        val totalAmount: Double,
        val date: LocalDate = LocalDate.now(),
        val time: LocalTime = LocalTime.now(),
        val status: String = "Pending"
    )

    // ------------------ STORE ------------------
    private val users = mutableListOf<User>()
    private val deliveries = mutableListOf<Delivery>()

    // Keep track of logged-in user
    var currentLoggedInUser: User? = null
        private set

    // ------------------ AUTH ------------------
    fun addUser(user: User) {
        users.add(user)
        currentLoggedInUser = user
    }

    fun login(username: String, password: String): Boolean {
        val found = users.find { it.username == username && it.password == password }
        currentLoggedInUser = found
        return found != null
    }

    fun logout() {
        currentLoggedInUser = null
    }

    fun getCurrentUser(): User? = currentLoggedInUser

    fun getUserData(userId: String): User? = users.find { it.id == userId }

    // ✅ New function to fix DashboardActivity
    fun getUserDataByEmail(email: String): User? = users.find { it.email == email }

    // ✅ Needed for ProfileActivity
    fun getAllUsers(): List<User> = users.toList()

    // ------------------ DELIVERIES ------------------
    fun addDelivery(delivery: Delivery) {
        deliveries.add(delivery)
    }

    fun getDeliveryHistory(userId: String): List<Delivery> {
        return deliveries.filter { it.userId == userId }
    }

    fun getDeliveriesForUserOnDate(userId: String, date: LocalDate): List<Delivery> {
        return deliveries.filter { it.userId == userId && it.date == date }
    }

    // ------------------ ACHIEVEMENTS ------------------
    fun markAchievement(userId: String, achievement: String): Boolean {
        // For now, just print achievement unlock
        println("Achievement unlocked for $userId: $achievement")
        return true
    }
}