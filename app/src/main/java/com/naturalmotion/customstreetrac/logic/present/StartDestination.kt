package com.naturalmotion.customstreetrac.logic.present

sealed class StartDestination {
    data class Player(val score: String) : StartDestination()
    data object Screen1 : StartDestination()
    data object Screen2 : StartDestination()
    data object Screen3 : StartDestination()
}