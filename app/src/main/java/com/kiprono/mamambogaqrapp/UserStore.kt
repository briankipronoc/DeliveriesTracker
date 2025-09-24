package com.kiprono.mamambogaqrapp

// Central in-memory user store (temporary replacement for a real DB/API)
object UserStore {
    // Data class to hold user-specific information
    data class RiderData(
        val password: String,
        var dailyTarget: Int = 0 // New field for user-set daily target
    )

    private val users = mutableMapOf<String, RiderData>() // email -> RiderData

    fun addUser(email: String, password: String) {
        users[email] = RiderData(password)
    }

    fun validateUser(email: String, password: String): Boolean {
        return users[email]?.password == password
    }

    fun getUserData(email: String): RiderData? {
        return users[email]
    }
}