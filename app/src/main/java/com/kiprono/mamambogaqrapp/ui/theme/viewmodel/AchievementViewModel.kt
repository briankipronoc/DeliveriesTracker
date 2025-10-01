package com.kiprono.mamambogaqrapp.ui.theme.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kiprono.mamambogaqrapp.data.local.AchievementEntity
import com.kiprono.mamambogaqrapp.data.repository.AchievementRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for managing Achievements
 */
class AchievementViewModel(private val repository: AchievementRepository) : ViewModel() {

    val allAchievements: StateFlow<List<AchievementEntity>> =
        repository.getAllAchievements()
            .stateIn(viewModelScope, SharingStarted.Companion.Lazily, emptyList())

    fun insertAchievement(achievement: AchievementEntity) {
        viewModelScope.launch {
            repository.insertAchievement(achievement)
        }
    }
}