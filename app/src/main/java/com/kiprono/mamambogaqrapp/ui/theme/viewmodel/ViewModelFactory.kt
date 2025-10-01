package com.kiprono.mamambogaqrapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kiprono.mamambogaqrapp.data.repository.AchievementRepository
import com.kiprono.mamambogaqrapp.data.repository.OrderRepository
import com.kiprono.mamambogaqrapp.ui.theme.viewmodel.AchievementViewModel
import com.kiprono.mamambogaqrapp.ui.theme.viewmodel.OrderViewModel

/**
 * Factory to provide ViewModels with repositories.
 */
@Suppress("UNCHECKED_CAST")
class ViewModelFactory(
    private val orderRepository: OrderRepository? = null,
    private val achievementRepository: AchievementRepository? = null
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(OrderViewModel::class.java) -> {
                requireNotNull(orderRepository)
                OrderViewModel(orderRepository) as T
            }
            modelClass.isAssignableFrom(AchievementViewModel::class.java) -> {
                requireNotNull(achievementRepository)
                AchievementViewModel(achievementRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}