package com.lagless.booster.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lagless.booster.ui.theme.BackgroundCard
import com.lagless.booster.ui.theme.BackgroundPrimary
import com.lagless.booster.ui.theme.Divider
import com.lagless.booster.ui.theme.NeonCyan
import com.lagless.booster.ui.theme.NeonGreen
import com.lagless.booster.ui.theme.TextPrimary
import com.lagless.booster.ui.theme.TextSecondary

// ── Top App Bar ───────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LagLessTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    actions: @Composable () -> Unit = {}
) {
    TopAppBar(
        title = {
            Text(
                text       = title,
                color      = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize   = 18.sp
            )
        },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint               = NeonGreen
                    )
                }
            }
        },
        actions     = { actions() },
        colors      = TopAppBarDefaults.topAppBarColors(
            containerColor         = BackgroundPrimary,
            titleContentColor      = TextPrimary,
            navigationIconContentColor = NeonGreen
        )
    )
}

// ── Neon Card ─────────────────────────────────────────────────

@Composable
fun NeonCard(
    modifier: Modifier = Modifier,
    accentColor: Color = NeonGreen,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val mod = modifier
        .clip(RoundedCornerShape(16.dp))
        .background(BackgroundCard)
        .border(
            width = 1.dp,
            brush = Brush.linearGradient(
                colors = listOf(accentColor.copy(alpha = 0.5f), Color.Transparent)
            ),
            shape = RoundedCornerShape(16.dp)
        )
        .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)

    Box(modifier = mod.padding(16.dp)) {
        content()
    }
}

// ── Stat Row (label + value) ──────────────────────────────────

@Composable
fun StatRow(label: String, value: String, valueColor: Color = TextPrimary) {
    Row(
        modifier            = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment   = Alignment.CenterVertically
    ) {
        Text(text = label, color = TextSecondary, fontSize = 13.sp)
        Text(text = value, color = valueColor, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
    }
}

// ── Neon Progress Bar ─────────────────────────────────────────

@Composable
fun NeonProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = NeonGreen,
    trackColor: Color = Divider
) {
    val animatedProgress by animateFloatAsState(
        targetValue  = progress.coerceIn(0f, 1f),
        animationSpec = tween(800),
        label        = "progress"
    )
    LinearProgressIndicator(
        progress      = { animatedProgress },
        modifier      = modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp)),
        color         = color,
        trackColor    = trackColor,
        strokeCap     = StrokeCap.Round
    )
}

// ── Circular Progress (storage ring) ─────────────────────────

@Composable
fun StorageRing(
    usedFraction: Float,
    size: Dp = 160.dp,
    strokeWidth: Dp = 14.dp,
    color: Color = NeonGreen,
    trackColor: Color = Divider,
    centerContent: @Composable () -> Unit = {}
) {
    val animatedFraction by animateFloatAsState(
        targetValue  = usedFraction.coerceIn(0f, 1f),
        animationSpec = tween(1000),
        label        = "ring"
    )
    Box(
        modifier       = Modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            progress      = { animatedFraction },
            modifier      = Modifier.size(size),
            color         = color,
            trackColor    = trackColor,
            strokeWidth   = strokeWidth,
            strokeCap     = StrokeCap.Round
        )
        centerContent()
    }
}

// ── Quick Action Button ───────────────────────────────────────

@Composable
fun QuickActionButton(
    icon: ImageVector,
    label: String,
    color: Color = NeonGreen,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier            = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Box(
            modifier         = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f))
                .border(1.dp, color.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = label,
                tint               = color,
                modifier           = Modifier.size(26.dp)
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text      = label,
            color     = TextSecondary,
            fontSize  = 11.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
}

// ── Neon Button ───────────────────────────────────────────────

@Composable
fun NeonButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = NeonGreen,
    enabled: Boolean = true
) {
    Button(
        onClick  = onClick,
        modifier = modifier.fillMaxWidth(),
        enabled  = enabled,
        colors   = ButtonDefaults.buttonColors(
            containerColor = color.copy(alpha = 0.2f),
            contentColor   = color,
            disabledContainerColor = Divider,
            disabledContentColor   = TextSecondary
        ),
        shape    = RoundedCornerShape(12.dp),
        border   = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = if (enabled) 0.6f else 0.2f))
    ) {
        Text(text = text, fontWeight = FontWeight.Bold, fontSize = 15.sp)
    }
}

// ── Section Header ────────────────────────────────────────────

@Composable
fun SectionHeader(title: String, accentColor: Color = NeonGreen) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(18.dp)
                .background(accentColor, RoundedCornerShape(2.dp))
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text       = title,
            color      = TextPrimary,
            fontWeight = FontWeight.SemiBold,
            fontSize   = 15.sp
        )
    }
}

// ── Loading Overlay ───────────────────────────────────────────

@Composable
fun LoadingOverlay(message: String = "Scanning…") {
    Box(
        modifier         = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = NeonCyan, strokeWidth = 3.dp)
            Spacer(Modifier.height(16.dp))
            Text(text = message, color = TextSecondary, fontSize = 14.sp)
        }
    }
}

// ── Empty State ───────────────────────────────────────────────

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String,
    accentColor: Color = NeonGreen,
    action: (@Composable () -> Unit)? = null
) {
    Column(
        modifier            = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier         = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = null,
                tint               = accentColor,
                modifier           = Modifier.size(40.dp)
            )
        }
        Spacer(Modifier.height(20.dp))
        Text(
            text       = title,
            color      = TextPrimary,
            fontWeight = FontWeight.SemiBold,
            fontSize   = 17.sp,
            textAlign  = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text      = subtitle,
            color     = TextSecondary,
            fontSize  = 14.sp,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
        if (action != null) {
            Spacer(Modifier.height(24.dp))
            action()
        }
    }
}

// ── Pulsing Neon Dot ──────────────────────────────────────────

@Composable
fun PulsingDot(color: Color = NeonGreen, size: Dp = 10.dp) {
    val transition = rememberInfiniteTransition(label = "pulse")
    val alpha by transition.animateFloat(
        initialValue  = 0.3f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(
            animation  = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(color.copy(alpha = alpha))
    )
}

// ── Info Banner ───────────────────────────────────────────────

@Composable
fun InfoBanner(text: String, color: Color = NeonCyan) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(10.dp),
        color    = color.copy(alpha = 0.08f),
        border   = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.25f))
    ) {
        Text(
            text     = text,
            color    = color,
            fontSize = 12.sp,
            modifier = Modifier.padding(12.dp),
            lineHeight = 18.sp
        )
    }
}
