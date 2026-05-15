package com.lagless.booster.ui.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lagless.booster.ui.theme.BackgroundCard
import com.lagless.booster.ui.theme.BackgroundPrimary
import com.lagless.booster.ui.theme.NeonCyan
import com.lagless.booster.ui.theme.NeonGreen
import com.lagless.booster.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val alpha = remember { Animatable(0f) }
    val scale = remember { Animatable(0.6f) }

    LaunchedEffect(Unit) {
        alpha.animateTo(1f, animationSpec = tween(600, easing = EaseOutCubic))
        scale.animateTo(1f, animationSpec = tween(600, easing = EaseOutCubic))
        delay(1200)
        onFinished()
    }

    Box(
        modifier         = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors  = listOf(BackgroundCard, BackgroundPrimary),
                    radius  = 1200f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier            = Modifier
                .alpha(alpha.value)
                .scale(scale.value)
        ) {
            // Glowing icon
            Box(
                modifier         = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                NeonGreen.copy(alpha = 0.35f),
                                NeonCyan.copy(alpha = 0.1f),
                                BackgroundPrimary.copy(alpha = 0f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = Icons.Filled.Bolt,
                    contentDescription = null,
                    tint               = NeonGreen,
                    modifier           = Modifier.size(56.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text       = "LAG",
                fontSize   = 48.sp,
                fontWeight = FontWeight.ExtraBold,
                color      = NeonGreen,
                letterSpacing = 6.sp
            )
            Text(
                text       = "LESS",
                fontSize   = 48.sp,
                fontWeight = FontWeight.ExtraBold,
                color      = NeonCyan,
                letterSpacing = 6.sp
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text      = "Gamer-grade optimization",
                fontSize  = 13.sp,
                color     = TextSecondary,
                textAlign = TextAlign.Center,
                letterSpacing = 2.sp
            )
        }
    }
}
