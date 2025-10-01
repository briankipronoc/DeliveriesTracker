package com.kiprono.mamambogaqrapp.data.repository

import com.kiprono.mamambogaqrapp.data.local.DeliveryDao
import com.kiprono.mamambogaqrapp.data.local.DeliveryEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing Deliveries data.
 */
class DeliveryRepository(private val deliveryDao: DeliveryDao) {

    suspend fun insertDelivery(delivery: DeliveryEntity) {
        deliveryDao.insertDelivery(delivery)
    }

    fun getAllDeliveries(): Flow<List<DeliveryEntity>> {
        return deliveryDao.getAllDeliveries()
    }
}