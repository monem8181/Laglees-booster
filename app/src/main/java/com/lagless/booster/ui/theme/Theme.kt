package com.lagless.booster.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val GamerDarkColorScheme = darkColorScheme(
    primary            = NeonGreen,
    onPrimary          = Color(0xFF00200F),
    primaryContainer   = Color(0xFF003D1F),
    onPrimaryContainer = NeonGreen,

    secondary            = NeonCyan,
    onSecondary          = Color(0xFF003040),
    secondaryContainer   = Color(0xFF004D60),
    onSecondaryContainer = NeonCyan,

    tertiary            = NeonPurple,
    onTertiary          = Color(0xFF20004A),
    tertiaryContainer   = Color(0xFF380080),
    onTertiaryContainer = NeonPurple,

    background  = BackgroundPrimary,
    onBackground = TextPrimary,

    surface         = BackgroundSurface,
    onSurface       = TextPrimary,
    surfaceVariant  = BackgroundCard,
    onSurfaceVariant = TextSecondary,

    outline        = Divider,
    outlineVariant = Color(0xFF1E1E30),

    error    = NeonRed,
    onError  = Color(0xFF3A0010),

    scrim = Color(0xAA000000)
)

@Composable
fun LagLessTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = GamerDarkColorScheme,
        typography  = LagLessTypography,
        content     = content
    )
}
