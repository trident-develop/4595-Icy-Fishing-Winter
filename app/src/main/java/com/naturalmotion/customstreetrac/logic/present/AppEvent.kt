package com.naturalmotion.customstreetrac.logic.present

import ru.nsk.kstatemachine.event.Event

sealed class AppEvent : Event {

    data object Start : AppEvent()

    data object NoSavedPlayer : AppEvent()

    data class SavedPlayerFound(val player: String) : AppEvent()

    data class PlayerBuilt(val player: String) : AppEvent()

    data class Failure(val message: String) : AppEvent()

    data object GoToBoard : AppEvent()

    data object GoToScreen1 : AppEvent()

    data object GoToScreen2 : AppEvent()

    data object GoToScreen3 : AppEvent()
}