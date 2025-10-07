package com.kiprono.mamambogaqrapp

import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

// Single consolidated UserStore
object UserStore {

    // ------------------ MODELS ------------------
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
        var dailyTarget: Int = 5, // default target
        val role: String = "Rider"
    )

    data class Delivery(
        val id: String = UUID.randomUUID().toString(),
        val userId: String,
        val customerName: String,
        val totalAmount: Double,
        val date: LocalDate = LocalDate.now(),
        val time: LocalTime = LocalTime.now(),
        var status: String = "Pending"
    )

    // ------------------ STORE ------------------
    private val users = mutableListOf<User>()
    private val deliveries = mutableListOf<Delivery>()

    // Optional UI update callback
    var onDeliveriesChanged: (() -> Unit)? = null

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

    fun getUserDataByEmail(email: String): User? = users.find { it.email == email }

    fun getAllUsers(): List<User> = users.toList()

    // ------------------ DAILY TARGET ------------------
    fun updateDailyTarget(userId: String, newTarget: Int) {
        users.find { it.id == userId }?.let { existing ->
            val updated = existing.copy(dailyTarget = newTarget)
            users[users.indexOf(existing)] = updated
            if (currentLoggedInUser?.id == userId) {
                currentLoggedInUser = updated
            }
        }
    }

    // ------------------ DELIVERIES ------------------
    // Add a Delivery object
    fun addDelivery(delivery: Delivery) {
        deliveries.add(delivery)
        onDeliveriesChanged?.invoke()
    }

    // Overload: quick creation used by the scanner
    fun addDelivery(userId: String, customerName: String, amount: Double) {
        val d = Delivery(
            userId = userId,
            customerName = customerName,
            totalAmount = amount,
            status = "Completed"
        )
        addDelivery(d)
    }

    fun getDeliveryHistory(userId: String): List<Delivery> {
        return deliveries.filter { it.userId == userId }.sortedByDescending { it.date }
    }

    fun getDeliveriesForUserOnDate(userId: String, date: LocalDate): List<Delivery> {
        return deliveries.filter { it.userId == userId && it.date == date }
    }

    // ---- Order marking with parameters (for manual entry) ----
    fun markOrderOngoing(userId: String, customerName: String, amount: Double) {
        val delivery = Delivery(userId = userId, customerName = customerName, totalAmount = amount, status = "Ongoing")
        addDelivery(delivery)
    }

    fun markOrderCompleted(userId: String, customerName: String, amount: Double) {
        val delivery = Delivery(userId = userId, customerName = customerName, totalAmount = amount, status = "Completed")
        addDelivery(delivery)
    }

    fun markOrderCancelled(userId: String, customerName: String, amount: Double) {
        val delivery = Delivery(userId = userId, customerName = customerName, totalAmount = amount, status = "Cancelled")
        addDelivery(delivery)
    }

    // ---- Overloads for QR Scanner (auto use current user) ----
    fun markOrderOngoing() {
        val user = currentLoggedInUser ?: return
        val delivery = Delivery(
            userId = user.id,
            customerName = "Mama Mboga Order",
            totalAmount = 250.0,
            status = "Ongoing"
        )
        addDelivery(delivery)
    }

    fun markOrderCompleted() {
        val user = currentLoggedInUser ?: return
        val delivery = Delivery(
            userId = user.id,
            customerName = "Customer Pickup",
            totalAmount = 250.0,
            status = "Completed"
        )
        addDelivery(delivery)
    }

    fun markOrderCancelled() {
        val user = currentLoggedInUser ?: return
        val delivery = Delivery(
            userId = user.id,
            customerName = "Order Cancelled",
            totalAmount = 0.0,
            status = "Cancelled"
        )
        addDelivery(delivery)
    }

    // ------------------ ACHIEVEMENTS ------------------
    private val achievements = mutableMapOf<String, MutableList<String>>()

    fun getAchievements(userId: String): List<String> {
        return achievements[userId] ?: emptyList()
    }

    fun markAchievement(userId: String, achievement: String): Boolean {
        val list = achievements.getOrPut(userId) { mutableListOf() }
        if (!list.contains(achievement)) list.add(achievement)
        onDeliveriesChanged?.invoke()
        println("Achievement unlocked for $userId: $achievement")
        return true
    }
    fun updateUser(updatedUser: User) {
        val index = users.indexOfFirst { it.id == updatedUser.id }
        if (index != -1) {
            users[index] = updatedUser
            if (currentLoggedInUser?.id == updatedUser.id) {
                currentLoggedInUser = updatedUser
            }
        }
    }
}