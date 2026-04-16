package com.naturalmotion.customstreetrac.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naturalmotion.customstreetrac.logic.present.AppEvent
import com.naturalmotion.customstreetrac.logic.present.AppUiState
import com.naturalmotion.customstreetrac.logic.present.StartDestination
import com.naturalmotion.customstreetrac.storage.ScoreRepo
import com.naturalmotion.customstreetrac.ui.componets.PlayerBuilder
import com.naturalmotion.customstreetrac.ui.componets.ScoreDestinationResolver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.nsk.kstatemachine.state.initialState
import ru.nsk.kstatemachine.state.invoke
import ru.nsk.kstatemachine.state.onEntry
import ru.nsk.kstatemachine.state.state
import ru.nsk.kstatemachine.state.transition
import ru.nsk.kstatemachine.statemachine.StateMachine
import ru.nsk.kstatemachine.statemachine.createStateMachine

class AppStartViewModel(
    private val scoreRepo: ScoreRepo,
    private val playerBuilder: PlayerBuilder,
    private val destinationResolver: ScoreDestinationResolver
) : ViewModel() {

    private val _uiState = MutableStateFlow<AppUiState>(AppUiState.Loading)
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    private var currentScore: String? = null

    private lateinit var machine: StateMachine
    private var isFromStorage = false
    private var isStarted = false

    fun startIfNeeded() {
        if (isStarted) return
        isStarted = true

        viewModelScope.launch {
            createMachine()
            startFlow()
        }
    }
    private suspend fun createMachine() {
        machine = createStateMachine(
            scope = viewModelScope,
            start = false
        ) {
            val loading = initialState("Loading")
            val checkingSavedScore = state("CheckingSavedScore")
            val buildingPlayer = state("BuildingPlayer")
            val resolvingDestination = state("ResolvingDestination")
            val openBoard = state("OpenBoard")
            val openScreen1 = state("OpenScreen1")
            val openScreen2 = state("OpenScreen2")
            val openScreen3 = state("OpenScreen3")
            val error = state("Error")

            loading {
                onEntry {
                    _uiState.value = AppUiState.Loading
                }

                transition<AppEvent.Start>(targetState = checkingSavedScore)
            }

            checkingSavedScore {
                onEntry {
                    viewModelScope.launch {
                        runCatching { scoreRepo.getSavedScore() }
                            .onSuccess { savedLink ->
//                                Log.d("MYTAG", "savedLink: $savedLink")

                                if (savedLink.isNullOrBlank()) {
                                    isFromStorage = false
                                    machine.processEvent(AppEvent.NoSavedPlayer)
                                } else {
                                    isFromStorage = true
                                    currentScore = savedLink
                                    machine.processEvent(AppEvent.SavedPlayerFound(savedLink))
                                }
                            }
                            .onFailure { throwable ->
                                machine.processEvent(
                                    AppEvent.Failure(
                                        throwable.message ?: "Failed to read saved link"
                                    )
                                )
                            }
                    }
                }

                transition<AppEvent.SavedPlayerFound>(targetState = resolvingDestination)
                transition<AppEvent.NoSavedPlayer>(targetState = buildingPlayer)
                transition<AppEvent.Failure>(targetState = error)
            }

            buildingPlayer {
                onEntry {
                    viewModelScope.launch {
                        runCatching { playerBuilder.build() }
                            .onSuccess { builtLink ->
//                                Log.d("MYTAG", "builtLink: $builtLink")

                                isFromStorage = false
                                currentScore = builtLink

                                machine.processEvent(AppEvent.PlayerBuilt(builtLink))
                            }
                            .onFailure { throwable ->
                                machine.processEvent(
                                    AppEvent.Failure(
                                        throwable.message ?: "Failed to build link"
                                    )
                                )
                            }
                    }
                }

                transition<AppEvent.PlayerBuilt>(targetState = resolvingDestination)
                transition<AppEvent.Failure>(targetState = error)
            }

            resolvingDestination {

                onEntry {

                    val link = currentScore

                    if (link.isNullOrBlank()) {
                        machine.processEvent(AppEvent.Failure("Link is missing"))
                        return@onEntry
                    }

                    if (!isFromStorage) {
                        machine.processEvent(AppEvent.GoToBoard)
                        return@onEntry
                    }

                    when (destinationResolver.resolve(link)) {

                        is StartDestination.Player -> {
                            machine.processEvent(AppEvent.GoToBoard)
                        }

                        StartDestination.Screen1 -> {
                            machine.processEvent(AppEvent.GoToScreen1)
                        }

                        StartDestination.Screen2 -> {
                            machine.processEvent(AppEvent.GoToScreen2)
                        }

                        StartDestination.Screen3 -> {
                            machine.processEvent(AppEvent.GoToScreen3)
                        }
                    }
                }

                transition<AppEvent.GoToBoard>(targetState = openBoard)
                transition<AppEvent.GoToScreen1>(targetState = openScreen1)
                transition<AppEvent.GoToScreen2>(targetState = openScreen2)
                transition<AppEvent.GoToScreen3>(targetState = openScreen3)
                transition<AppEvent.Failure>(targetState = error)
            }

            openBoard {
                onEntry {
                    val link = currentScore ?: return@onEntry
                    _uiState.value = AppUiState.Ready(StartDestination.Player(link))
                }
            }

            openScreen1 {
                onEntry {
                    _uiState.value = AppUiState.Ready(StartDestination.Screen1)
                }
            }

            openScreen2 {
                onEntry {
                    _uiState.value = AppUiState.Ready(StartDestination.Screen2)
                }
            }

            openScreen3 {
                onEntry {
                    _uiState.value = AppUiState.Ready(StartDestination.Screen3)
                }
            }

            error {
                onEntry {
                    _uiState.value = AppUiState.Error("Something went wrong")
                }
            }
        }
    }

    private fun startFlow() {
        viewModelScope.launch {
            machine.start()
            machine.processEvent(AppEvent.Start)
        }
    }
}