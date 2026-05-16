package com.lagless.booster.ui.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    // ── Individual animatables for staggered entrance ─────────
    val iconScale  = remember { Animatable(0f) }
    val iconAlpha  = remember { Animatable(0f) }
    val lagOffsetX = remember { Animatable(-80f) }
    val lagAlpha   = remember { Animatable(0f) }
    val lessOffsetX= remember { Animatable(80f) }
    val lessAlpha  = remember { Animatable(0f) }
    val subtitleAlpha = remember { Animatable(0f) }
    val scanlineAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Icon springs in
        launch { iconAlpha.animateTo(1f, tween(350)) }
        launch {
            iconScale.animateTo(
                1f,
                spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
            )
        }
        delay(300)

        // "LAG" slides in from left, "LESS" from right
        launch { lagAlpha.animateTo(1f, tween(300)) }
        launch { lagOffsetX.animateTo(0f, tween(350, easing = EaseOutCubic)) }
        delay(80)
        launch { lessAlpha.animateTo(1f, tween(300)) }
        launch { lessOffsetX.animateTo(0f, tween(350, easing = EaseOutCubic)) }
        delay(200)

        // Subtitle fades in
        launch { subtitleAlpha.animateTo(1f, tween(400)) }
        launch { scanlineAlpha.animateTo(1f, tween(400)) }

        delay(1500)
        onFinished()
    }

    // ── Pulsing sonar rings (continuous) ──────────────────────
    val pulse = rememberInfiniteTransition(label = "sonar")

    val r1Scale by pulse.animateFloat(0.8f, 1.9f, infiniteRepeatable(tween(1600, easing = FastOutSlowInEasing)), "r1s")
    val r1Alpha by pulse.animateFloat(0.5f, 0f,   infiniteRepeatable(tween(1600)), "r1a")

    val r2Scale by pulse.animateFloat(0.8f, 1.9f, infiniteRepeatable(tween(1600, easing = FastOutSlowInEasing), initialStartOffset = StartOffset(530)), "r2s")
    val r2Alpha by pulse.animateFloat(0.5f, 0f,   infiniteRepeatable(tween(1600), initialStartOffset = StartOffset(530)), "r2a")

    val r3Scale by pulse.animateFloat(0.8f, 1.9f, infiniteRepeatable(tween(1600, easing = FastOutSlowInEasing), initialStartOffset = StartOffset(1060)), "r3s")
    val r3Alpha by pulse.animateFloat(0.5f, 0f,   infiniteRepeatable(tween(1600), initialStartOffset = StartOffset(1060)), "r3a")

    // Scanline travel
    val scanX by pulse.animateFloat(-1f, 2f, infiniteRepeatable(tween(1100)), "scanX")

    Box(
        modifier         = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors  = listOf(BackgroundCard.copy(alpha = 0.8f), BackgroundPrimary),
                    radius  = 1400f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Grid-line background subtle effect
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, NeonGreen.copy(alpha = 0.02f), Color.Transparent)
                    )
                )
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            // ── Icon + sonar rings ─────────────────────────────
            Box(
                modifier         = Modifier
                    .size(120.dp)
                    .alpha(iconAlpha.value),
                contentAlignment = Alignment.Center
            ) {
                // Sonar rings
                listOf(r1Scale to r1Alpha, r2Scale to r2Alpha, r3Scale to r3Alpha).forEach { (s, a) ->
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .scale(s)
                            .clip(CircleShape)
                            .border(1.5.dp, NeonGreen.copy(alpha = a), CircleShape)
                    )
                }

                // Icon glow base
                Box(
                    modifier         = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    NeonGreen.copy(alpha = 0.25f),
                                    NeonCyan.copy(alpha = 0.08f),
                                    Color.Transparent
                                )
                            )
                        )
                        .scale(iconScale.value),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector        = Icons.Filled.Bolt,
                        contentDescription = null,
                        tint               = NeonGreen,
                        modifier           = Modifier.size(52.dp)
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            // ── Staggered title ────────────────────────────────
            Text(
                text          = "LAG",
                fontSize      = 52.sp,
                fontWeight    = FontWeight.ExtraBold,
                color         = NeonGreen,
                letterSpacing = 8.sp,
                modifier      = Modifier
                    .alpha(lagAlpha.value)
                    .offset(x = lagOffsetX.value.dp)
            )
            Text(
                text          = "LESS",
                fontSize      = 52.sp,
                fontWeight    = FontWeight.ExtraBold,
                color         = NeonCyan,
                letterSpacing = 8.sp,
                modifier      = Modifier
                    .alpha(lessAlpha.value)
                    .offset(x = lessOffsetX.value.dp)
            )

            Spacer(Modifier.height(16.dp))

            // ── Subtitle ───────────────────────────────────────
            Text(
                text          = "GAMER-GRADE OPTIMIZATION",
                fontSize      = 11.sp,
                color         = TextSecondary,
                textAlign     = TextAlign.Center,
                letterSpacing = 3.sp,
                modifier      = Modifier.alpha(subtitleAlpha.value)
            )

            Spacer(Modifier.height(40.dp))

            // ── Scanline progress bar ──────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.55f)
                    .height(2.dp)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(1.dp))
                    .background(NeonGreen.copy(alpha = 0.1f))
                    .alpha(scanlineAlpha.value)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.4f)
                        .height(2.dp)
                        .offset(x = (scanX * 200).dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color.Transparent, NeonGreen, NeonCyan, Color.Transparent)
                            )
                        )
                )
            }
        }
    }
}
