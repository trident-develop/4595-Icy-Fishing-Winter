package com.naturalmotion.customstreetrac.navigation

import android.annotation.SuppressLint
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.naturalmotion.customstreetrac.LoadingActivity
import com.naturalmotion.customstreetrac.MainActivity
import com.naturalmotion.customstreetrac.logic.present.AppUiState
import com.naturalmotion.customstreetrac.logic.present.StartDestination
import com.naturalmotion.customstreetrac.model.AppStartViewModel
import com.naturalmotion.customstreetrac.screens.ConnectScreen
import com.naturalmotion.customstreetrac.screens.LoadingScreen
import com.naturalmotion.customstreetrac.screens.components.privacy.ThirdBoard
import com.naturalmotion.customstreetrac.screens.isFlowersConnected
import kotlinx.coroutines.delay

@SuppressLint("ContextCastToActivity")
@Composable
fun LoadingGraph(
    viewModel: AppStartViewModel,
    web: ThirdBoard
) {

    val navController = rememberNavController()
    val context = LocalContext.current as LoadingActivity
    var mainOpened by remember { mutableStateOf(false) }

    fun openMainIfNeeded() {
        if (mainOpened) return
        mainOpened = true

        val intent = Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        context.startActivity(intent)
        context.finish()
    }

    NavHost(
        navController = navController,
        startDestination = if (context.isFlowersConnected()) Routes.LOADING else Routes.CONNECT
    ) {
        composable(Routes.LOADING) {

            val route = rememberRouteToken()

            when {
                route.isLoading() -> LoadingScreen({})
                route.isGame() -> {
                    LaunchedEffect(Unit) {
                        openMainIfNeeded()
                    }
                }
                route.isRules() -> {  }
            }

            LoadingScreenRoute(
                viewModel = viewModel,
                onOpenPlayer = { url ->
                    web.loadUrl(url)
                },
                onOpenScreen1 = {
                    openMainIfNeeded()
                },
                onOpenScreen2 = {
                    openMainIfNeeded()
                },
                onOpenScreen3 = {
                    openMainIfNeeded()
                },
                onError = {
                    openMainIfNeeded()
                }
            )
        }

        composable(Routes.CONNECT) {
            ConnectScreen(navController)
        }
    }
}

@Composable
fun LoadingScreenRoute(
    viewModel: AppStartViewModel,
    onOpenPlayer: (String) -> Unit,
    onOpenScreen1: () -> Unit,
    onOpenScreen2: () -> Unit,
    onOpenScreen3: () -> Unit,
    onError: () -> Unit
) {
    val uiState = viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.startIfNeeded()
    }


    when (val state = uiState.value) {
        AppUiState.Loading -> LoadingScreen({})

        is AppUiState.Ready -> {

            LaunchedEffect(state.destination) {
                when (val destination = state.destination) {
                    is StartDestination.Player -> onOpenPlayer(destination.score)
                    StartDestination.Screen1 -> onOpenScreen1()
                    StartDestination.Screen2 -> onOpenScreen2()
                    StartDestination.Screen3 -> onOpenScreen3()
                }
            }
        }

        is AppUiState.Error -> {
            onError()
        }
    }
}