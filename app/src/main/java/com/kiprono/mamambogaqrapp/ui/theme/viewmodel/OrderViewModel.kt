package com.kiprono.mamambogaqrapp.ui.theme.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kiprono.mamambogaqrapp.data.local.OrderEntity
import com.kiprono.mamambogaqrapp.data.repository.OrderRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for managing Orders
 */
class OrderViewModel(private val repository: OrderRepository) : ViewModel() {

    // Expose all orders as a StateFlow
    val allOrders: StateFlow<List<OrderEntity>> =
        repository.getAllOrders()
            .stateIn(viewModelScope, SharingStarted.Companion.Lazily, emptyList())

    fun insertOrder(order: OrderEntity) {
        viewModelScope.launch {
            repository.insertOrder(order)
        }
    }

    fun getOrdersByStatus(status: String): StateFlow<List<OrderEntity>> {
        return repository.getOrdersByStatus(status)
            .stateIn(viewModelScope, SharingStarted.Companion.Lazily, emptyList())
    }
}