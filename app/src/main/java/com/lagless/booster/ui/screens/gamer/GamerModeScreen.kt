package com.lagless.booster.ui.screens.gamer

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoNotDisturb
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lagless.booster.data.model.InstalledAppInfo
import com.lagless.booster.ui.components.InfoBanner
import com.lagless.booster.ui.components.LagLessTopBar
import com.lagless.booster.ui.components.LoadingOverlay
import com.lagless.booster.ui.components.NeonCard
import com.lagless.booster.ui.components.SectionHeader
import com.lagless.booster.ui.theme.BackgroundCard
import com.lagless.booster.ui.theme.BackgroundPrimary
import com.lagless.booster.ui.theme.NeonCyan
import com.lagless.booster.ui.theme.NeonGreen
import com.lagless.booster.ui.theme.NeonOrange
import com.lagless.booster.ui.theme.NeonPurple
import com.lagless.booster.ui.theme.NeonYellow
import com.lagless.booster.ui.theme.TextPrimary
import com.lagless.booster.ui.theme.TextSecondary
import com.lagless.booster.utils.appinfo.AppInfoUtils

@Composable
fun GamerModeScreen(onBack: () -> Unit, viewModel: GamerModeViewModel = viewModel()) {
    val state   = viewModel.uiState.collectAsState().value
    val context = LocalContext.current
    var showPickerDialog by remember { mutableStateOf(false) }

    if (showPickerDialog) {
        GamePickerDialog(
            apps       = state.allUserApps,
            onSelect   = { app ->
                viewModel.setFavoriteGame(app.packageName, app.appName)
                showPickerDialog = false
            },
            onDismiss  = { showPickerDialog = false }
        )
    }

    Scaffold(
        topBar         = { LagLessTopBar("Gamer Mode", onBack = onBack) },
        containerColor = BackgroundPrimary
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            if (state.isLoading) {
                LoadingOverlay("Loading gamer data…")
                return@Column
            }

            // ── Favorite game card ────────────────────────────────
            SectionHeader("Favorite Game", accentColor = NeonPurple)
            Spacer(Modifier.height(10.dp))

            NeonCard(
                accentColor = NeonPurple,
                modifier    = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(NeonPurple.copy(0.12f), BackgroundCard)
                        ),
                        RoundedCornerShape(16.dp)
                    )
            ) {
                if (state.favoriteGamePackage.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val icon: ImageBitmap? = remember(state.favoriteGamePackage) {
                            AppInfoUtils.getAppIcon(context, state.favoriteGamePackage)
                        }
                        if (icon != null) {
                            Image(
                                bitmap             = icon,
                                contentDescription = state.favoriteGameName,
                                modifier           = Modifier.size(52.dp).clip(RoundedCornerShape(12.dp))
                            )
                        } else {
                            Icon(
                                Icons.Filled.SportsEsports,
                                contentDescription = null,
                                tint = NeonPurple,
                                modifier = Modifier.size(52.dp)
                            )
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                state.favoriteGameName,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text("Favorite Game", color = NeonPurple, fontSize = 12.sp)
                        }
                        Row {
                            IconButton(onClick = {
                                val intent = context.packageManager
                                    .getLaunchIntentForPackage(state.favoriteGamePackage)
                                if (intent != null) context.startActivity(intent)
                            }) {
                                Icon(Icons.Filled.Bolt, contentDescription = "Launch", tint = NeonGreen)
                            }
                            IconButton(onClick = { viewModel.clearFavoriteGame() }) {
                                Icon(Icons.Filled.Close, contentDescription = "Remove", tint = TextSecondary)
                            }
                        }
                    }
                } else {
                    Column(
                        modifier            = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Filled.SportsEsports,
                            contentDescription = null,
                            tint = NeonPurple,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text("No favorite game set", color = TextSecondary, fontSize = 14.sp)
                        Spacer(Modifier.height(12.dp))
                        TextButton(onClick = { showPickerDialog = true }) {
                            Icon(Icons.Filled.Add, contentDescription = null, tint = NeonPurple)
                            Spacer(Modifier.width(4.dp))
                            Text("Choose Favorite Game", color = NeonPurple, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Gaming quick settings ─────────────────────────────
            SectionHeader("Gaming Quick Settings", accentColor = NeonCyan)
            Spacer(Modifier.height(10.dp))

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GamingShortcutCard(
                    icon       = Icons.Filled.DoNotDisturb,
                    label      = "Do Not\nDisturb",
                    color      = NeonCyan,
                    modifier   = Modifier.weight(1f),
                    onClick    = {
                        context.startActivity(Intent(Settings.ACTION_ZEN_MODE_PRIORITY_SETTINGS)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    }
                )
                GamingShortcutCard(
                    icon     = Icons.Filled.BatteryChargingFull,
                    label    = "Battery\nOptimize",
                    color    = NeonYellow,
                    modifier = Modifier.weight(1f),
                    onClick  = {
                        context.startActivity(Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    }
                )
                GamingShortcutCard(
                    icon     = Icons.Filled.Wifi,
                    label    = "Network\nSettings",
                    color    = NeonGreen,
                    modifier = Modifier.weight(1f),
                    onClick  = {
                        context.startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    }
                )
            }

            Spacer(Modifier.height(20.dp))

            // ── Network placeholder card ──────────────────────────
            SectionHeader("Network", accentColor = NeonGreen)
            Spacer(Modifier.height(10.dp))

            NeonCard(accentColor = NeonGreen) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.NetworkCheck, contentDescription = null, tint = NeonGreen)
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text("Network Status", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                            Text(
                                "Open Wireless Settings for network info",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Gaming tips ───────────────────────────────────────
            SectionHeader("Performance Tips", accentColor = NeonOrange)
            Spacer(Modifier.height(10.dp))

            val tips = listOf(
                "Enable DND mode to block calls and notifications during gaming.",
                "Lower screen brightness slightly to reduce battery drain.",
                "Close background apps before launching your game.",
                "Enable Game Mode in phone settings if supported by your device.",
                "Keep your device charged above 20% for consistent performance.",
                "Use Wi-Fi instead of mobile data for lower latency online gaming."
            )
            tips.forEachIndexed { i, tip ->
                NeonCard(accentColor = NeonOrange) {
                    Row(verticalAlignment = Alignment.Top) {
                        Text(
                            "${i + 1}",
                            color      = NeonOrange,
                            fontWeight = FontWeight.Bold,
                            fontSize   = 13.sp,
                            modifier   = Modifier.width(20.dp)
                        )
                        Text(tip, color = TextSecondary, fontSize = 13.sp, lineHeight = 18.sp)
                    }
                }
                Spacer(Modifier.height(6.dp))
            }

            Spacer(Modifier.height(20.dp))

            // ── Detected games list ───────────────────────────────
            if (state.recentGames.isNotEmpty()) {
                SectionHeader("Detected Games", accentColor = NeonPurple)
                Spacer(Modifier.height(10.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(state.recentGames.take(10), key = { it.packageName }) { game ->
                        GameChip(
                            app     = game,
                            context = context,
                            onSetFavorite = { viewModel.setFavoriteGame(game.packageName, game.appName) }
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun GamingShortcutCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    NeonCard(modifier = modifier, accentColor = color, onClick = onClick) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(28.dp))
            Spacer(Modifier.height(6.dp))
            Text(label, color = TextPrimary, fontSize = 11.sp, textAlign = TextAlign.Center, lineHeight = 15.sp)
        }
    }
}

@Composable
private fun GameChip(
    app: InstalledAppInfo,
    context: android.content.Context,
    onSetFavorite: () -> Unit
) {
    val icon: ImageBitmap? = remember(app.packageName) {
        AppInfoUtils.getAppIcon(context, app.packageName)
    }
    Column(
        modifier            = Modifier
            .width(80.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(BackgroundCard)
            .clickable {
                context.packageManager.getLaunchIntentForPackage(app.packageName)
                    ?.let { context.startActivity(it) }
            }
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box {
            if (icon != null) {
                Image(icon, contentDescription = app.appName, modifier = Modifier.size(48.dp).clip(RoundedCornerShape(10.dp)))
            } else {
                Icon(Icons.Filled.Android, contentDescription = null, tint = NeonPurple, modifier = Modifier.size(48.dp))
            }
            IconButton(
                onClick  = onSetFavorite,
                modifier = Modifier.size(18.dp).align(Alignment.TopEnd)
            ) {
                Icon(Icons.Filled.Star, contentDescription = "Set Favorite", tint = NeonYellow, modifier = Modifier.size(14.dp))
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            app.appName,
            color     = TextPrimary,
            fontSize  = 10.sp,
            maxLines  = 1,
            overflow  = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun GamePickerDialog(
    apps: List<InstalledAppInfo>,
    onSelect: (InstalledAppInfo) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose Favorite Game", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.height(320.dp).verticalScroll(rememberScrollState())) {
                apps.take(50).forEach { app ->
                    Row(
                        modifier          = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(app) }
                            .padding(vertical = 10.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val ctx  = LocalContext.current
                        val icon = remember(app.packageName) { AppInfoUtils.getAppIcon(ctx, app.packageName) }
                        if (icon != null) {
                            Image(icon, contentDescription = null, modifier = Modifier.size(36.dp).clip(CircleShape))
                        } else {
                            Icon(Icons.Filled.Android, contentDescription = null, tint = NeonPurple, modifier = Modifier.size(36.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(app.appName, color = TextPrimary, fontSize = 14.sp)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = NeonGreen) }
        },
        containerColor = BackgroundCard
    )
}
