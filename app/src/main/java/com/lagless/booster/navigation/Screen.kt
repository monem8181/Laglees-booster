package com.lagless.booster.navigation

sealed class Screen(val route: String) {
    data object Splash       : Screen("splash")
    data object Home         : Screen("home")
    data object Storage      : Screen("storage")
    data object Cleaner      : Screen("cleaner")
    data object AppManager   : Screen("app_manager")
    data object UnusedApps   : Screen("unused_apps")
    data object FolderScanner: Screen("folder_scanner")
    data object GamerMode    : Screen("gamer_mode")
    data object Optimize     : Screen("optimize")
    data object Settings     : Screen("settings")
    data object About        : Screen("about")
}
