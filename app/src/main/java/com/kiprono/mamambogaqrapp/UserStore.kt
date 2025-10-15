package com.kiprono.mamambogaqrapp

import androidx.compose.runtime.mutableStateListOf
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

object UserStore {

    // ------------------ MODELS (REFACTORED) ------------------
    data class User(
        val id: String = UUID.randomUUID().toString(),
        val username: String,
        val password: String,
        val name: String,
        val phone: String,
        val email: String,
        val bikeBrand: String = "",
        val bikeCC: String = "",
        val plate: String = "",
        var dailyTarget: Int = 5,
        val role: String = "Rider"
    )

    data class Delivery(
        val id: String = UUID.randomUUID().toString(),
        val userId: String,
        val customerName: String,
        val totalAmount: Double,
        val date: LocalDate,
        var status: String, // "Ongoing", "Completed", "Cancelled"
        val scanTime: LocalTime, // Time rider scanned Mama Mboga QR
        var deliveryTime: LocalTime? = null // Time rider scanned Buyer QR
    )

    // ------------------ DATA STORAGE ------------------
    private val users = mutableListOf<User>()
    private val deliveries = mutableStateListOf<Delivery>()
    private val achievements = mutableMapOf<String, MutableList<String>>()
    private val userStreaks = mutableMapOf<String, Pair<Int, LocalDate>>()

    var onDeliveriesChanged: (() -> Unit)? = null
    var onUserChanged: ((User?) -> Unit)? = null

    var currentLoggedInUser: User? = null
        private set

    init {
        val mockUser = User(id = "mock_user_1", username = "rider", password = "123", name = "Kiprono", phone = "0712345678", email = "rider@smartrider.com")
        users.add(mockUser)
        login("rider", "123")

        deliveries.add(Delivery(userId = mockUser.id, customerName = "Asha", totalAmount = 1200.0, date = LocalDate.now(), status = "Ongoing", scanTime = LocalTime.now().minusHours(1)))
        deliveries.add(Delivery(userId = mockUser.id, customerName = "Ben", totalAmount = 850.0, date = LocalDate.now(), status = "Completed", scanTime = LocalTime.now().minusHours(2), deliveryTime = LocalTime.now().minusHours(1)))
    }

    // ------------------ USER AUTH ------------------
    fun addUser(user: User) {
        users.add(user)
        currentLoggedInUser = user
        onUserChanged?.invoke(user)
    }

    fun login(username: String, password: String): Boolean {
        val found = users.find { it.username == username && it.password == password }
        currentLoggedInUser = found
        onUserChanged?.invoke(found)
        return found != null
    }

    fun logout() {
        currentLoggedInUser = null
        onUserChanged?.invoke(null)
    }

    fun getCurrentUser(): User? = currentLoggedInUser
    fun getUserData(userId: String): User? = users.find { it.id == userId }

    fun updateUser(updatedUser: User) {
        val index = users.indexOfFirst { it.id == updatedUser.id }
        if (index != -1) {
            users[index] = updatedUser
            if (currentLoggedInUser?.id == updatedUser.id) currentLoggedInUser = updatedUser
            onUserChanged?.invoke(updatedUser)
        }
    }

    fun updateDailyTarget(userId: String, newTarget: Int) {
        users.find { it.id == userId }?.let { user ->
            user.dailyTarget = newTarget
            onUserChanged?.invoke(user)
        }
    }

    // ------------------ DELIVERY LOGIC (REFACTORED) ------------------

    fun getDeliveryHistory(userId: String): List<Delivery> = deliveries.filter { it.userId == userId }.sortedByDescending { it.date.atTime(it.scanTime) }

    fun getDeliveriesForUserOnDate(userId: String, date: LocalDate): List<Delivery> = deliveries.filter { it.userId == userId && it.date == date }

    fun startDeliveryFromQR(qrCode: String) {
        val userId = currentLoggedInUser?.id ?: return
        val parts = qrCode.split("|")
        val customerName = if (parts.isNotEmpty()) parts[0] else "Unknown Customer"
        val amount = if (parts.size > 1) parts[1].toDoubleOrNull() ?: 0.0 else 0.0

        val newDelivery = Delivery(
            userId = userId,
            customerName = customerName,
            totalAmount = amount,
            date = LocalDate.now(),
            status = "Ongoing",
            scanTime = LocalTime.now()
        )
        deliveries.add(0, newDelivery)
        onDeliveriesChanged?.invoke()
    }

    fun completeDelivery(deliveryId: String, buyerQr: String) {
        val delivery = deliveries.find { it.id == deliveryId }
        if (delivery != null && delivery.status == "Ongoing") {
            delivery.status = "Completed"
            delivery.deliveryTime = LocalTime.now()
            // Optional: You could validate the buyerQr here
            onDeliveriesChanged?.invoke()
        }
    }

    // ------------------ ACHIEVEMENTS & STREAKS ------------------

    fun markAchievement(userId: String, achievement: String): Boolean {
        val userAchievements = achievements.getOrPut(userId) { mutableListOf() }
        return if (!userAchievements.contains(achievement)) {
            userAchievements.add(achievement)
            true
        } else {
            false
        }
    }

    fun getAchievements(userId: String): List<String> = achievements[userId] ?: emptyList()

    fun getStreakDays(): Int {
        val userId = currentLoggedInUser?.id ?: return 0
        val (count, lastActive) = userStreaks.getOrElse(userId) { return 0 }
        return if (lastActive >= LocalDate.now().minusDays(1)) count else 0
    }
}