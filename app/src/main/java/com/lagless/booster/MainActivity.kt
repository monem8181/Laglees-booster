package com.lagless.booster

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.lagless.booster.navigation.LagLessNavGraph
import com.lagless.booster.ui.theme.BackgroundPrimary
import com.lagless.booster.ui.theme.LagLessTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Install the system splash screen (shows the icon before Compose is ready)
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LagLessTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color    = BackgroundPrimary
                ) {
                    val navController = rememberNavController()
                    LagLessNavGraph(navController = navController)
                }
            }
        }
    }
}
