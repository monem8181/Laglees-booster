package com.lagless.booster.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.lagless.booster.ui.screens.about.AboutScreen
import com.lagless.booster.ui.screens.apps.AppManagerScreen
import com.lagless.booster.ui.screens.cleaner.CleanerScreen
import com.lagless.booster.ui.screens.folders.FolderScannerScreen
import com.lagless.booster.ui.screens.gamer.GamerModeScreen
import com.lagless.booster.ui.screens.home.HomeScreen
import com.lagless.booster.ui.screens.optimize.OptimizeScreen
import com.lagless.booster.ui.screens.settings.SettingsScreen
import com.lagless.booster.ui.screens.shizuku.ShizukuSetupScreen
import com.lagless.booster.ui.screens.splash.SplashScreen
import com.lagless.booster.ui.screens.storage.StorageScreen
import com.lagless.booster.ui.screens.unused.UnusedAppsScreen

@Composable
fun LagLessNavGraph(navController: NavHostController) {

    NavHost(
        navController    = navController,
        startDestination = Screen.Splash.route,
        enterTransition  = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300)) + fadeIn(tween(300))
        },
        exitTransition   = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300)) + fadeOut(tween(300))
        },
        popEnterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300)) + fadeIn(tween(300))
        },
        popExitTransition  = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300)) + fadeOut(tween(300))
        }
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(onFinished = {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            })
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateStorage    = { navController.navigate(Screen.Storage.route) },
                onNavigateCleaner    = { navController.navigate(Screen.Cleaner.route) },
                onNavigateAppManager = { navController.navigate(Screen.AppManager.route) },
                onNavigateGamer      = { navController.navigate(Screen.GamerMode.route) },
                onNavigateOptimize   = { navController.navigate(Screen.Optimize.route) },
                onNavigateSettings   = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(Screen.Storage.route) {
            StorageScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.Cleaner.route) {
            CleanerScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.AppManager.route) {
            AppManagerScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.UnusedApps.route) {
            UnusedAppsScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.FolderScanner.route) {
            FolderScannerScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.GamerMode.route) {
            GamerModeScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.Optimize.route) {
            OptimizeScreen(
                onBack        = { navController.popBackStack() },
                onGoToCleaner = { navController.popBackStack(); navController.navigate(Screen.Cleaner.route) }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack         = { navController.popBackStack() },
                onAbout        = { navController.navigate(Screen.About.route) },
                onShizukuSetup = { navController.navigate(Screen.ShizukuSetup.route) }
            )
        }

        composable(Screen.About.route) {
            AboutScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.ShizukuSetup.route) {
            ShizukuSetupScreen(onBack = { navController.popBackStack() })
        }
    }
}
