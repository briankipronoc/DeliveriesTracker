package com.kiprono.mamambogaqrapp.data.repository

import com.kiprono.mamambogaqrapp.data.local.AchievementDao
import com.kiprono.mamambogaqrapp.data.local.AchievementEntity
import kotlinx.coroutines.flow.Flow

class AchievementRepository(private val achievementDao: AchievementDao) {

    suspend fun insertAchievement(achievement: AchievementEntity) {
        achievementDao.insertAchievement(achievement)
    }

    fun getAllAchievements(): Flow<List<AchievementEntity>> {
        return achievementDao.getAllAchievements()
    }
}