package com.kiprono.mamambogaqrapp.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DeliveryUserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeliveryUser(user: DeliveryUser)

    @Query("SELECT * FROM delivery_users")
    fun getAllDeliveryUsers(): Flow<List<DeliveryUser>>

    @Query("SELECT * FROM delivery_users WHERE phone = :phone LIMIT 1")
    suspend fun getUserByPhone(phone: String): DeliveryUser?

    @Delete
    suspend fun deleteDeliveryUser(user: DeliveryUser)
}