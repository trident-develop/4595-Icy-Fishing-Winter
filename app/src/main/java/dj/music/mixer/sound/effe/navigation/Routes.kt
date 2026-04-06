package dj.music.mixer.sound.effe.navigation

object Routes {
    const val MENU = "menu"
    const val LEVELS = "levels"
    const val GAME = "game/{level}"
    const val SETTINGS = "settings"
    const val HOW_TO_PLAY = "how_to_play"
    const val PRIVACY_POLICY = "privacy_policy"
    const val LEADERBOARD = "leaderboard"
    const val LOADING = "loading"
    const val CONNECT = "connect"

    fun game(level: Int) = "game/$level"
}