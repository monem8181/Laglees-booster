package com.lagless.booster.ui.screens.shizuku

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import com.lagless.booster.data.model.ShizukuStatus
import com.lagless.booster.data.shizuku.ShizukuManager
import com.lagless.booster.data.shizuku.ShizukuManager.displaySubtitle
import com.lagless.booster.data.shizuku.ShizukuManager.displayTitle
import com.lagless.booster.ui.components.InfoBanner
import com.lagless.booster.ui.components.LagLessTopBar
import com.lagless.booster.ui.components.NeonButton
import com.lagless.booster.ui.components.NeonCard
import com.lagless.booster.ui.components.PulsingDot
import com.lagless.booster.ui.components.SectionHeader
import com.lagless.booster.ui.theme.BackgroundPrimary
import com.lagless.booster.ui.theme.Divider
import com.lagless.booster.ui.theme.NeonCyan
import com.lagless.booster.ui.theme.NeonGreen
import com.lagless.booster.ui.theme.NeonOrange
import com.lagless.booster.ui.theme.NeonPurple
import com.lagless.booster.ui.theme.NeonRed
import com.lagless.booster.ui.theme.NeonYellow
import com.lagless.booster.ui.theme.TextPrimary
import com.lagless.booster.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun ShizukuSetupScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var status  by remember { mutableStateOf(ShizukuStatus.NOT_INSTALLED) }
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        status = ShizukuManager.computeStatus(context)
        delay(100)
        visible = true
    }

    val statusColor = when (status) {
        ShizukuStatus.CONNECTED         -> NeonGreen
        ShizukuStatus.PERMISSION_DENIED -> NeonOrange
        ShizukuStatus.NOT_RUNNING       -> NeonYellow
        ShizukuStatus.NOT_INSTALLED     -> NeonRed
    }

    Scaffold(
        topBar         = { LagLessTopBar("Advanced Mode Setup", onBack = onBack) },
        containerColor = BackgroundPrimary
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            AnimatedVisibility(visible, enter = fadeIn() + expandVertically()) {
                Column {
                    // ── Current status card ────────────────────────────────
                    NeonCard(accentColor = statusColor, glowing = status == ShizukuStatus.CONNECTED) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier.size(48.dp).clip(CircleShape)
                                    .background(statusColor.copy(alpha = 0.15f))
                                    .border(1.dp, statusColor.copy(alpha = 0.4f), CircleShape),
                                Alignment.Center
                            ) {
                                Icon(Icons.Filled.Terminal, null, tint = statusColor, modifier = Modifier.size(24.dp))
                            }
                            Spacer(Modifier.width(14.dp))
                            Column(Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(status.displayTitle(), color = statusColor,
                                        fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    if (status == ShizukuStatus.CONNECTED) {
                                        Spacer(Modifier.width(8.dp))
                                        PulsingDot(statusColor, 8.dp)
                                    }
                                }
                                Text(status.displaySubtitle(), color = TextSecondary,
                                    fontSize = 12.sp, lineHeight = 17.sp)
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Refresh button
                    NeonButton(
                        text    = "Refresh Status",
                        onClick = { status = ShizukuManager.computeStatus(context) },
                        color   = NeonCyan
                    )

                    if (status == ShizukuStatus.PERMISSION_DENIED) {
                        Spacer(Modifier.height(8.dp))
                        NeonButton(
                            text    = "Request Permission",
                            onClick = { ShizukuManager.requestPermission(); status = ShizukuManager.computeStatus(context) },
                            color   = NeonOrange
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    // ── What is Advanced Mode ──────────────────────────────
                    SectionHeader("What is Advanced Mode?", accentColor = NeonPurple)
                    Spacer(Modifier.height(10.dp))
                    NeonCard(accentColor = NeonPurple) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                "Advanced Mode uses Shizuku — an ADB-level permission bridge — to unlock deeper " +
                                "Android features without requiring root access.",
                                color = TextSecondary, fontSize = 13.sp, lineHeight = 19.sp
                            )
                            Spacer(Modifier.height(4.dp))
                            AdvancedFeatureRow(Icons.Filled.Terminal,          "Force stop apps via ADB shell",              NeonGreen)
                            AdvancedFeatureRow(Icons.Filled.AdminPanelSettings, "Detailed per-app storage breakdown",        NeonCyan)
                            AdvancedFeatureRow(Icons.Filled.Info,              "Package dump diagnostics",                   NeonPurple)
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // ── Setup steps ────────────────────────────────────────
                    SectionHeader("Setup Guide", accentColor = NeonCyan)
                    Spacer(Modifier.height(10.dp))

                    SetupStep(
                        number  = 1,
                        icon    = Icons.Filled.Download,
                        title   = "Install Shizuku",
                        desc    = "Download Shizuku (free) from Google Play. It is an official open-source tool by RikkaApps.",
                        color   = NeonGreen,
                        done    = status != ShizukuStatus.NOT_INSTALLED,
                        action  = "Open Play Store"
                    ) {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=moe.shizuku.privileged.api"))
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    SetupStep(
                        number  = 2,
                        icon    = Icons.Filled.Wifi,
                        title   = "Start the Shizuku Service",
                        desc    = "Open Shizuku and tap 'Pairing' under Wireless Debugging (Android 11+), or connect via USB debugging. Follow the in-app instructions.",
                        color   = NeonCyan,
                        done    = status == ShizukuStatus.CONNECTED || status == ShizukuStatus.PERMISSION_DENIED,
                        action  = "Open Shizuku"
                    ) {
                        context.packageManager
                            .getLaunchIntentForPackage("moe.shizuku.privileged.api")
                            ?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            ?.let { context.startActivity(it) }
                    }

                    Spacer(Modifier.height(8.dp))

                    SetupStep(
                        number  = 3,
                        icon    = Icons.Filled.Lock,
                        title   = "Grant Permission to LagLess",
                        desc    = "The first time LagLess uses Shizuku, a permission dialog will appear. Tap Allow. You can also grant it from the Shizuku app.",
                        color   = NeonOrange,
                        done    = status == ShizukuStatus.CONNECTED,
                        action  = if (status == ShizukuStatus.PERMISSION_DENIED) "Grant Now" else null
                    ) { ShizukuManager.requestPermission() }

                    Spacer(Modifier.height(24.dp))

                    // ── Honesty card ───────────────────────────────────────
                    SectionHeader("Limitations", accentColor = NeonOrange)
                    Spacer(Modifier.height(10.dp))
                    InfoBanner(
                        text = "Advanced Mode uses ADB-level shell access — the same as Android's wireless debugging.\n\n" +
                               "• Root access is never used or required\n" +
                               "• Shizuku stops when you reboot (restart it after each reboot)\n" +
                               "• Some OEMs (Samsung, MIUI) may restrict certain ADB commands\n" +
                               "• Android sandbox still applies — app user data is protected\n" +
                               "• System apps can be force-stopped but may restart automatically",
                        color = NeonOrange
                    )

                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun SetupStep(
    number : Int,
    icon   : ImageVector,
    title  : String,
    desc   : String,
    color  : Color,
    done   : Boolean,
    action : String?,
    onAction: () -> Unit
) {
    NeonCard(accentColor = if (done) NeonGreen else color) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                Modifier.size(36.dp).clip(CircleShape)
                    .background((if (done) NeonGreen else color).copy(alpha = 0.15f))
                    .border(1.dp, (if (done) NeonGreen else color).copy(alpha = 0.4f), CircleShape),
                Alignment.Center
            ) {
                if (done) {
                    Icon(Icons.Filled.CheckCircle, null, tint = NeonGreen, modifier = Modifier.size(20.dp))
                } else {
                    Text("$number", color = color, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = if (done) NeonGreen else TextPrimary,
                    fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Spacer(Modifier.height(4.dp))
                Text(desc, color = TextSecondary, fontSize = 12.sp, lineHeight = 18.sp)
                if (action != null && !done) {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier  = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        androidx.compose.material3.TextButton(onClick = onAction) {
                            Text(action, color = color, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.width(4.dp))
                            Icon(Icons.Filled.OpenInNew, null, tint = color, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdvancedFeatureRow(icon: ImageVector, label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = color, modifier = Modifier.size(15.dp))
        Spacer(Modifier.width(8.dp))
        Text(label, color = TextSecondary, fontSize = 12.sp)
    }
}
