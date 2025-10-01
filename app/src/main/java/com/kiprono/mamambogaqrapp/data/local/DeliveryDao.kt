package com.kiprono.mamambogaqrapp.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DeliveryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDelivery(delivery: DeliveryEntity)

    @Query("SELECT * FROM deliveries")
    fun getAllDeliveries(): Flow<List<DeliveryEntity>>

    @Query("SELECT * FROM deliveries WHERE id = :id LIMIT 1")
    suspend fun getDeliveryById(id: Int): DeliveryEntity?

    @Delete
    suspend fun deleteDelivery(delivery: DeliveryEntity)
}