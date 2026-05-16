package com.lagless.booster.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lagless.booster.ui.theme.BackgroundCard
import com.lagless.booster.ui.theme.BackgroundElevated
import com.lagless.booster.ui.theme.BackgroundPrimary
import com.lagless.booster.ui.theme.Divider
import com.lagless.booster.ui.theme.NeonCyan
import com.lagless.booster.ui.theme.NeonGreen
import com.lagless.booster.ui.theme.TextPrimary
import com.lagless.booster.ui.theme.TextSecondary

// ── Shimmer ───────────────────────────────────────────────────

@Composable
fun shimmerBrush(highlightColor: Color = Color(0xFF2A2A50)): Brush {
    val shimmerColors = listOf(
        BackgroundCard,
        BackgroundElevated,
        highlightColor,
        BackgroundElevated,
        BackgroundCard,
    )
    val transition = rememberInfiniteTransition(label = "shimmer")
    val x by transition.animateFloat(
        initialValue  = -600f,
        targetValue   = 1200f,
        animationSpec = infiniteRepeatable(tween(1100, easing = LinearEasing)),
        label         = "shimmerX"
    )
    return Brush.linearGradient(
        colors = shimmerColors,
        start  = Offset(x, x * 0.3f),
        end    = Offset(x + 600f, x * 0.3f + 300f)
    )
}

@Composable
fun ShimmerCard(
    modifier: Modifier = Modifier,
    height: Dp = 90.dp,
    cornerRadius: Dp = 16.dp
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(cornerRadius))
            .background(shimmerBrush())
    )
}

@Composable
fun ShimmerRow(modifier: Modifier = Modifier, width: Dp = 120.dp, height: Dp = 14.dp) {
    Box(
        modifier = modifier
            .width(width)
            .height(height)
            .clip(RoundedCornerShape(7.dp))
            .background(shimmerBrush())
    )
}

@Composable
fun ShimmerLoadingDashboard() {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Hero card skeleton
        ShimmerCard(height = 160.dp, cornerRadius = 20.dp)
        Spacer(Modifier.height(20.dp))

        // Section header shimmer
        ShimmerRow(width = 100.dp, height = 16.dp)
        Spacer(Modifier.height(10.dp))

        // Two stat cards
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ShimmerCard(modifier = Modifier.weight(1f), height = 70.dp)
            ShimmerCard(modifier = Modifier.weight(1f), height = 70.dp)
        }
        Spacer(Modifier.height(20.dp))

        // Section header shimmer
        ShimmerRow(width = 110.dp, height = 16.dp)
        Spacer(Modifier.height(10.dp))

        // RAM + battery row
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ShimmerCard(modifier = Modifier.weight(1f), height = 80.dp)
            ShimmerCard(modifier = Modifier.weight(1f), height = 80.dp)
        }
        Spacer(Modifier.height(20.dp))

        // Quick actions
        ShimmerRow(width = 120.dp, height = 16.dp)
        Spacer(Modifier.height(10.dp))
        ShimmerCard(height = 100.dp)
    }
}

// ── Neon glow modifier ────────────────────────────────────────

fun Modifier.neonGlow(color: Color, cornerRadius: Dp = 16.dp): Modifier =
    this.drawBehind {
        val cr = cornerRadius.toPx()
        listOf(0.30f to 3f, 0.14f to 7f, 0.06f to 14f, 0.02f to 22f).forEach { (alpha, spread) ->
            drawRoundRect(
                color        = color.copy(alpha = alpha),
                size         = Size(size.width + spread * 2, size.height + spread * 2),
                topLeft      = Offset(-spread, -spread),
                cornerRadius = CornerRadius(cr + spread)
            )
        }
    }

fun Modifier.pulsingNeonGlow(color: Color, intensity: Float, cornerRadius: Dp = 16.dp): Modifier =
    this.drawBehind {
        val cr = cornerRadius.toPx()
        listOf(0.4f to 4f, 0.18f to 10f, 0.07f to 18f).forEach { (alpha, spread) ->
            drawRoundRect(
                color        = color.copy(alpha = alpha * intensity),
                size         = Size(size.width + spread * 2, size.height + spread * 2),
                topLeft      = Offset(-spread, -spread),
                cornerRadius = CornerRadius(cr + spread)
            )
        }
    }

// ── Animated stat counter ─────────────────────────────────────

@Composable
fun AnimatedStatValue(
    target   : Float,
    format   : (Float) -> String,
    color    : Color,
    modifier : Modifier = Modifier,
    style    : TextStyle = MaterialTheme.typography.headlineMedium
) {
    val anim by animateFloatAsState(
        targetValue   = target,
        animationSpec = tween(durationMillis = 1300, easing = FastOutSlowInEasing),
        label         = "statAnim"
    )
    Text(
        text       = format(anim),
        color      = color,
        style      = style,
        fontWeight = FontWeight.ExtraBold,
        modifier   = modifier
    )
}

// ── Top App Bar ───────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LagLessTopBar(
    title   : String,
    onBack  : (() -> Unit)? = null,
    actions : @Composable () -> Unit = {}
) {
    TopAppBar(
        title = {
            Text(text = title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = NeonGreen)
                }
            }
        },
        actions = { actions() },
        colors  = TopAppBarDefaults.topAppBarColors(
            containerColor             = BackgroundPrimary,
            titleContentColor          = TextPrimary,
            navigationIconContentColor = NeonGreen
        )
    )
}

// ── Neon Card ─────────────────────────────────────────────────

@Composable
fun NeonCard(
    modifier    : Modifier = Modifier,
    accentColor : Color = NeonGreen,
    onClick     : (() -> Unit)? = null,
    glowing     : Boolean = false,
    content     : @Composable () -> Unit
) {
    val glowMod = if (glowing) Modifier.neonGlow(accentColor) else Modifier
    val mod = modifier
        .then(glowMod)
        .clip(RoundedCornerShape(16.dp))
        .background(BackgroundCard)
        .border(
            width = 1.dp,
            brush = Brush.linearGradient(listOf(accentColor.copy(0.55f), Color.Transparent)),
            shape = RoundedCornerShape(16.dp)
        )
        .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)

    Box(modifier = mod.padding(16.dp)) { content() }
}

// ── Pulsing Neon Card (for hero elements) ─────────────────────

@Composable
fun PulsingNeonCard(
    modifier    : Modifier = Modifier,
    accentColor : Color = NeonGreen,
    onClick     : (() -> Unit)? = null,
    content     : @Composable () -> Unit
) {
    val transition = rememberInfiniteTransition(label = "cardPulse")
    val glowIntensity by transition.animateFloat(
        initialValue  = 0.4f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label         = "glowIntensity"
    )
    val mod = modifier
        .pulsingNeonGlow(accentColor, glowIntensity)
        .clip(RoundedCornerShape(16.dp))
        .background(BackgroundCard)
        .border(
            width = 1.dp,
            brush = Brush.linearGradient(listOf(accentColor.copy(glowIntensity * 0.8f), Color.Transparent)),
            shape = RoundedCornerShape(16.dp)
        )
        .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)

    Box(modifier = mod.padding(16.dp)) { content() }
}

// ── Stat Row ──────────────────────────────────────────────────

@Composable
fun StatRow(label: String, value: String, valueColor: Color = TextPrimary) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(label, color = TextSecondary, fontSize = 13.sp)
        Text(value, color = valueColor, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
    }
}

// ── Neon Progress Bar ─────────────────────────────────────────

@Composable
fun NeonProgressBar(
    progress   : Float,
    modifier   : Modifier = Modifier,
    color      : Color = NeonGreen,
    trackColor : Color = Divider
) {
    val anim by animateFloatAsState(
        targetValue   = progress.coerceIn(0f, 1f),
        animationSpec = tween(900),
        label         = "progress"
    )
    LinearProgressIndicator(
        progress   = { anim },
        modifier   = modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
        color      = color,
        trackColor = trackColor,
        strokeCap  = StrokeCap.Round
    )
}

// ── Storage Ring ──────────────────────────────────────────────

@Composable
fun StorageRing(
    usedFraction  : Float,
    size          : Dp = 160.dp,
    strokeWidth   : Dp = 14.dp,
    color         : Color = NeonGreen,
    trackColor    : Color = Divider,
    centerContent : @Composable () -> Unit = {}
) {
    val anim by animateFloatAsState(
        targetValue   = usedFraction.coerceIn(0f, 1f),
        animationSpec = tween(1100),
        label         = "ring"
    )
    Box(modifier = Modifier.size(size), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            progress    = { anim },
            modifier    = Modifier.size(size),
            color       = color,
            trackColor  = trackColor,
            strokeWidth = strokeWidth,
            strokeCap   = StrokeCap.Round
        )
        centerContent()
    }
}

// ── Quick Action Button ───────────────────────────────────────

@Composable
fun QuickActionButton(
    icon    : ImageVector,
    label   : String,
    color   : Color = NeonGreen,
    onClick : () -> Unit
) {
    val scale = remember { Animatable(1f) }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier            = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(12.dp)
            .scale(scale.value)
    ) {
        Box(
            modifier         = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f))
                .border(1.dp, color.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, label, tint = color, modifier = Modifier.size(26.dp))
        }
        Spacer(Modifier.height(6.dp))
        Text(label, color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)
    }
}

// ── Neon Button ───────────────────────────────────────────────

@Composable
fun NeonButton(
    text     : String,
    onClick  : () -> Unit,
    modifier : Modifier = Modifier,
    color    : Color = NeonGreen,
    enabled  : Boolean = true
) {
    Button(
        onClick  = onClick,
        modifier = modifier.fillMaxWidth(),
        enabled  = enabled,
        colors   = ButtonDefaults.buttonColors(
            containerColor         = color.copy(alpha = 0.18f),
            contentColor           = color,
            disabledContainerColor = Divider,
            disabledContentColor   = TextSecondary
        ),
        shape  = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = if (enabled) 0.6f else 0.2f))
    ) {
        Text(text, fontWeight = FontWeight.Bold, fontSize = 15.sp)
    }
}

// ── Section Header ────────────────────────────────────────────

@Composable
fun SectionHeader(title: String, accentColor: Color = NeonGreen) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.width(3.dp).height(18.dp).background(accentColor, RoundedCornerShape(2.dp)))
        Spacer(Modifier.width(8.dp))
        Text(title, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
    }
}

// ── Loading Overlay ───────────────────────────────────────────

@Composable
fun LoadingOverlay(message: String = "Scanning…") {
    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = NeonCyan, strokeWidth = 3.dp)
            Spacer(Modifier.height(16.dp))
            Text(message, color = TextSecondary, fontSize = 14.sp)
        }
    }
}

// ── Empty State (with bouncing icon) ─────────────────────────

@Composable
fun EmptyState(
    icon        : ImageVector,
    title       : String,
    subtitle    : String,
    accentColor : Color = NeonGreen,
    action      : (@Composable () -> Unit)? = null
) {
    val transition = rememberInfiniteTransition(label = "iconBounce")
    val bounceY by transition.animateFloat(
        initialValue  = 0f,
        targetValue   = -10f,
        animationSpec = infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label         = "bounce"
    )
    Column(
        modifier            = Modifier.fillMaxWidth().padding(vertical = 48.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier         = Modifier
                .size(80.dp)
                .offset(y = bounceY.dp)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.12f))
                .border(1.dp, accentColor.copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = accentColor, modifier = Modifier.size(40.dp))
        }
        Spacer(Modifier.height(20.dp))
        Text(title, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 17.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(subtitle, color = TextSecondary, fontSize = 14.sp, textAlign = TextAlign.Center, lineHeight = 20.sp)
        if (action != null) { Spacer(Modifier.height(24.dp)); action() }
    }
}

// ── Pulsing Dot ───────────────────────────────────────────────

@Composable
fun PulsingDot(color: Color = NeonGreen, size: Dp = 10.dp) {
    val transition = rememberInfiniteTransition(label = "dot")
    val alpha by transition.animateFloat(0.3f, 1f, infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Reverse), "dotAlpha")
    Box(Modifier.size(size).clip(CircleShape).background(color.copy(alpha = alpha)))
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
        Text(text, color = color, fontSize = 12.sp, modifier = Modifier.padding(12.dp), lineHeight = 18.sp)
    }
}

// ── Scanning Progress Bar (animated scanline) ─────────────────

@Composable
fun ScanlineBar(color: Color = NeonGreen, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "scan")
    val x by transition.animateFloat(
        initialValue  = -1f,
        targetValue   = 2f,
        animationSpec = infiniteRepeatable(tween(1000, easing = LinearEasing)),
        label         = "scanX"
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(3.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(Divider)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.35f)
                .height(3.dp)
                .offset(x = (x * 300).dp)
                .background(Brush.horizontalGradient(listOf(Color.Transparent, color, Color.Transparent)))
        )
    }
}

// ── Pulsing Rings (sonar effect) ─────────────────────────────

@Composable
fun SonarRings(color: Color = NeonGreen, size: Dp = 120.dp) {
    val transition = rememberInfiniteTransition(label = "sonar")

    val scale1 by transition.animateFloat(0.6f, 1.6f, infiniteRepeatable(tween(1600, easing = FastOutSlowInEasing)), "s1")
    val alpha1 by transition.animateFloat(0.5f, 0f, infiniteRepeatable(tween(1600)), "a1")

    val scale2 by transition.animateFloat(0.6f, 1.6f,
        infiniteRepeatable(tween(1600, easing = FastOutSlowInEasing), initialStartOffset = StartOffset(530)), "s2")
    val alpha2 by transition.animateFloat(0.5f, 0f,
        infiniteRepeatable(tween(1600), initialStartOffset = StartOffset(530)), "a2")

    val scale3 by transition.animateFloat(0.6f, 1.6f,
        infiniteRepeatable(tween(1600, easing = FastOutSlowInEasing), initialStartOffset = StartOffset(1060)), "s3")
    val alpha3 by transition.animateFloat(0.5f, 0f,
        infiniteRepeatable(tween(1600), initialStartOffset = StartOffset(1060)), "a3")

    Box(modifier = Modifier.size(size), contentAlignment = Alignment.Center) {
        listOf(scale1 to alpha1, scale2 to alpha2, scale3 to alpha3).forEach { (s, a) ->
            Box(
                modifier = Modifier
                    .size(size)
                    .scale(s)
                    .clip(CircleShape)
                    .background(Color.Transparent)
                    .border((1.5).dp, color.copy(alpha = a), CircleShape)
            )
        }
    }
}
