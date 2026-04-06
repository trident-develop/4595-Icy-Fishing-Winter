package dj.music.mixer.sound.effe.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import dj.music.mixer.sound.effe.audio.SoundManager
import dj.music.mixer.sound.effe.screens.GameScreen
import dj.music.mixer.sound.effe.screens.HowToPlayScreen
import dj.music.mixer.sound.effe.screens.LeaderboardScreen
import dj.music.mixer.sound.effe.screens.LevelsScreen
import dj.music.mixer.sound.effe.screens.MenuScreen
import dj.music.mixer.sound.effe.screens.PrivacyPolicyScreen
import dj.music.mixer.sound.effe.screens.SettingsScreen
import dj.music.mixer.sound.effe.storage.GamePreferences

@Composable
fun AppNavGraph(
    navController: NavHostController,
    prefs: GamePreferences,
    soundManager: SoundManager,
    onExitApp: () -> Unit
) {
    NavHost(navController = navController, startDestination = Routes.MENU) {
        composable(Routes.MENU) {
            MenuScreen(
                onPlay = { navController.navigate(Routes.LEVELS) },
                onSettings = { navController.navigate(Routes.SETTINGS) },
                onLeaderboard = { navController.navigate(Routes.LEADERBOARD) },
                onExit = onExitApp
            )
        }
        composable(Routes.LEVELS) {
            LevelsScreen(
                prefs = prefs,
                onBack = { navController.popBackStack() },
                onHome = { navController.popBackStack(Routes.MENU, inclusive = false) },
                onLevelSelected = { level -> navController.navigate(Routes.game(level)) }
            )
        }
        composable(
            route = Routes.GAME,
            arguments = listOf(navArgument("level") { type = NavType.IntType })
        ) { backStackEntry ->
            val level = backStackEntry.arguments?.getInt("level") ?: 1
            GameScreen(
                level = level,
                prefs = prefs,
                soundManager = soundManager,
                onHome = { navController.popBackStack(Routes.MENU, inclusive = false) },
                onBack = { navController.popBackStack() },
                onNextLevel = { nextLevel ->
                    navController.popBackStack()
                    navController.navigate(Routes.game(nextLevel))
                },
                onReplay = {
                    navController.popBackStack()
                    navController.navigate(Routes.game(level))
                }
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                prefs = prefs,
                soundManager = soundManager,
                onBack = { navController.popBackStack() },
                onHowToPlay = { navController.navigate(Routes.HOW_TO_PLAY) },
                onPrivacyPolicy = { navController.navigate(Routes.PRIVACY_POLICY) }
            )
        }
        composable(Routes.HOW_TO_PLAY) {
            HowToPlayScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.PRIVACY_POLICY) {
            PrivacyPolicyScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.LEADERBOARD) {
            LeaderboardScreen(
                prefs = prefs,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
