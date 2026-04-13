package com.naturalmotion.customstreetrac

import android.os.Bundle
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.compose.rememberNavController
import com.naturalmotion.customstreetrac.audio.SoundManager
import com.naturalmotion.customstreetrac.navigation.AppNavGraph
import com.naturalmotion.customstreetrac.storage.GamePreferences
import com.naturalmotion.customstreetrac.ui.theme.IcyFishingWinterTheme

class MainActivity : ComponentActivity() {
    private lateinit var prefs: GamePreferences
    private lateinit var soundManager: SoundManager
    private val windowController by lazy {
        WindowInsetsControllerCompat(window, window.decorView)
    }
    private var multiTouchDetected = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        prefs = GamePreferences(this)
        soundManager = SoundManager(this, prefs)

        lifecycle.addObserver(LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> soundManager.resumeIfNeeded()
                Lifecycle.Event.ON_PAUSE -> soundManager.pauseIfPlaying()
                Lifecycle.Event.ON_DESTROY -> soundManager.releaseMusic()
                else -> {}
            }
        })

        setContent {
            IcyFishingWinterTheme {
                val navController = rememberNavController()
                AppNavGraph(
                    navController = navController,
                    prefs = prefs,
                    soundManager = soundManager,
                    onExitApp = { finish() }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        windowController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowController.hide(WindowInsetsCompat.Type.systemBars())
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.pointerCount > 1) {
            if (!multiTouchDetected) {
                multiTouchDetected = true
                val cancelEvent = MotionEvent.obtain(ev)
                cancelEvent.action = MotionEvent.ACTION_CANCEL
                super.dispatchTouchEvent(cancelEvent)
                cancelEvent.recycle()
            }
            return true
        }
        if (multiTouchDetected) {
            if (ev.actionMasked == MotionEvent.ACTION_UP ||
                ev.actionMasked == MotionEvent.ACTION_CANCEL
            ) {
                multiTouchDetected = false
            }
            return true
        }
        return super.dispatchTouchEvent(ev)
    }
}