package com.naturalmotion.customstreetrac.storage

interface ScoreRepo {
    suspend fun getSavedScore(): String?
    suspend fun saveScore(score: String)
    suspend fun isNotifyShown(): Boolean

    suspend fun markNotifyShown()
}