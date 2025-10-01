package com.kiprono.mamambogaqrapp.data.repository

import com.kiprono.mamambogaqrapp.data.local.OrderDao
import com.kiprono.mamambogaqrapp.data.local.OrderEntity
import kotlinx.coroutines.flow.Flow

class OrderRepository(private val orderDao: OrderDao) {

    suspend fun insertOrder(order: OrderEntity) {
        orderDao.insertOrder(order)
    }

    fun getAllOrders(): Flow<List<OrderEntity>> {
        return orderDao.getAllOrders()
    }

    fun getOrdersByStatus(status: String): Flow<List<OrderEntity>> {
        return orderDao.getOrdersByStatus(status)
    }
}