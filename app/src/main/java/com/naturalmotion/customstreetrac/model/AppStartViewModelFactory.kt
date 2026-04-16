package com.naturalmotion.customstreetrac.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.naturalmotion.customstreetrac.storage.ScoreRepo
import com.naturalmotion.customstreetrac.ui.componets.PlayerBuilder
import com.naturalmotion.customstreetrac.ui.componets.ScoreDestinationResolver

class AppStartViewModelFactory(
    private val scoreRepo: ScoreRepo,
    private val playerBuilder: PlayerBuilder,
    private val destinationResolver: ScoreDestinationResolver
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppStartViewModel::class.java)) {
            return AppStartViewModel(
                scoreRepo = scoreRepo,
                playerBuilder = playerBuilder,
                destinationResolver = destinationResolver
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}