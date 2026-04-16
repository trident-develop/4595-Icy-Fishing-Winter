package com.naturalmotion.customstreetrac

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.naturalmotion.customstreetrac.logic.anim.ParamProvider1
import com.naturalmotion.customstreetrac.logic.anim.ParamProvider2
import com.naturalmotion.customstreetrac.logic.anim.ParamProvider3
import com.naturalmotion.customstreetrac.logic.anim.ParamProvider4
import com.naturalmotion.customstreetrac.logic.anim.ParamProvider5
import com.naturalmotion.customstreetrac.logic.anim.ParamProvider6
import com.naturalmotion.customstreetrac.logic.players.Players
import com.naturalmotion.customstreetrac.model.AppStartViewModel
import com.naturalmotion.customstreetrac.model.AppStartViewModelFactory
import com.naturalmotion.customstreetrac.navigation.LoadingGraph
import com.naturalmotion.customstreetrac.screens.components.privacy.ThirdBoard
import com.naturalmotion.customstreetrac.storage.ScoreRepoImpl
import com.naturalmotion.customstreetrac.storage.winterStore
import com.naturalmotion.customstreetrac.ui.componets.PlayerBuilder
import com.naturalmotion.customstreetrac.ui.componets.ScoreDestinationResolver

class LoadingActivity : ComponentActivity() {
    private var controller: WindowInsetsControllerCompat? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        controller = WindowInsetsControllerCompat(window, window.decorView)
        controller?.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller?.hide(WindowInsetsCompat.Type.systemBars())
        enableEdgeToEdge()

        val scoreRepo = ScoreRepoImpl(winterStore)

        val playerBuilder = PlayerBuilder(
            baseName = Players.getFullRealPlayer(),
            providers = listOf(
                ParamProvider1(),
                ParamProvider2(),
                ParamProvider3(),
                ParamProvider4(),
                ParamProvider5(),
                ParamProvider6()
            ),
            context = applicationContext
        )

        val destinationResolver = ScoreDestinationResolver()

        val thirdBoard = ThirdBoard(this, scoreRepo)
        setContent {
            val factory = remember {
                AppStartViewModelFactory(
                    scoreRepo = scoreRepo,
                    playerBuilder = playerBuilder,
                    destinationResolver = destinationResolver
                )
            }
            val viewModel: AppStartViewModel = viewModel(
                factory = factory
            )
            LoadingGraph(viewModel, thirdBoard)
        }
    }

    override fun onResume() {
        super.onResume()
        controller?.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller?.hide(WindowInsetsCompat.Type.systemBars())
    }
}