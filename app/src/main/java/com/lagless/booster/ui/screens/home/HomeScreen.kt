package com.lagless.booster.ui.screens.home

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lagless.booster.ui.components.EmptyState
import com.lagless.booster.ui.components.LoadingOverlay
import com.lagless.booster.ui.components.NeonCard
import com.lagless.booster.ui.components.NeonProgressBar
import com.lagless.booster.ui.components.PulsingDot
import com.lagless.booster.ui.components.QuickActionButton
import com.lagless.booster.ui.components.SectionHeader
import com.lagless.booster.ui.components.StatRow
import com.lagless.booster.ui.theme.BackgroundCard
import com.lagless.booster.ui.theme.BackgroundElevated
import com.lagless.booster.ui.theme.BackgroundPrimary
import com.lagless.booster.ui.theme.NeonCyan
import com.lagless.booster.ui.theme.NeonGreen
import com.lagless.booster.ui.theme.NeonOrange
import com.lagless.booster.ui.theme.NeonPurple
import com.lagless.booster.ui.theme.TextPrimary
import com.lagless.booster.ui.theme.TextSecondary
import com.lagless.booster.utils.formatters.Formatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateStorage  : () -> Unit,
    onNavigateCleaner  : () -> Unit,
    onNavigateAppManager: () -> Unit,
    onNavigateGamer    : () -> Unit,
    onNavigateOptimize : () -> Unit,
    onNavigateSettings : () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Bolt,
                            contentDescription = null,
                            tint = NeonGreen,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "LagLess",
                            color = TextPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            letterSpacing = 1.sp
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateSettings) {
                        Icon(
                            Icons.Outlined.Settings,
                            contentDescription = "Settings",
                            tint = TextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundPrimary)
            )
        },
        containerColor = BackgroundPrimary
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            when {
                state.isLoading -> LoadingOverlay("Loading dashboard…")
                state.error != null -> EmptyState(
                    icon     = Icons.Filled.Android,
                    title    = "Couldn't load data",
                    subtitle = state.error ?: "",
                    accentColor = NeonOrange
                )
                else -> {
                    // ── Storage hero card ─────────────────────────────────
                    StorageHeroCard(state = state)

                    Spacer(Modifier.height(20.dp))

                    // ── Quick stats row ───────────────────────────────────
                    SectionHeader("Quick Stats")
                    Spacer(Modifier.height(10.dp))
                    QuickStatsRow(state = state)

                    Spacer(Modifier.height(20.dp))

                    // ── Quick action buttons ──────────────────────────────
                    SectionHeader("Quick Actions")
                    Spacer(Modifier.height(12.dp))
                    QuickActionsGrid(
                        onScanJunk      = onNavigateCleaner,
                        onAnalyzeStorage = onNavigateStorage,
                        onGamerMode     = onNavigateGamer,
                        onAppManager    = onNavigateAppManager,
                        onOptimize      = onNavigateOptimize
                    )

                    Spacer(Modifier.height(20.dp))

                    // ── Biggest app shortcut ──────────────────────────────
                    state.biggestApp?.let { app ->
                        SectionHeader("Biggest App", accentColor = NeonOrange)
                        Spacer(Modifier.height(10.dp))
                        NeonCard(accentColor = NeonOrange, onClick = onNavigateAppManager) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(NeonOrange.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Filled.Apps,
                                        contentDescription = null,
                                        tint = NeonOrange,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        app.appName,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        Formatters.formatBytes(app.sizeBytes),
                                        color = NeonOrange,
                                        fontSize = 13.sp
                                    )
                                }
                                Text(
                                    "View →",
                                    color = NeonOrange,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun StorageHeroCard(state: HomeUiState) {
    val storage     = state.storageInfo
    val usedFrac    = storage?.usedPercent ?: 0f
    val accentColor = when {
        usedFrac > 0.85f -> NeonOrange
        usedFrac > 0.6f  -> NeonCyan
        else             -> NeonGreen
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(BackgroundElevated, BackgroundCard)
                )
            )
            .padding(20.dp)
    ) {
        Column {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Column {
                    Text("Storage", color = TextSecondary, fontSize = 12.sp, letterSpacing = 1.sp)
                    Text(
                        text       = Formatters.formatPercent(usedFrac) + " used",
                        color      = accentColor,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 28.sp
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PulsingDot(color = accentColor)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text  = if (storage != null) "Live" else "—",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            NeonProgressBar(progress = usedFrac, color = accentColor)
            Spacer(Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StorageStat(
                    label = "Used",
                    value = if (storage != null) Formatters.formatBytes(storage.usedBytes) else "—",
                    color = accentColor
                )
                StorageStat(
                    label = "Free",
                    value = if (storage != null) Formatters.formatBytes(storage.freeBytes) else "—",
                    color = NeonGreen
                )
                StorageStat(
                    label = "Total",
                    value = if (storage != null) Formatters.formatBytes(storage.totalBytes) else "—",
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun StorageStat(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = color, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text(label, color = TextSecondary, fontSize = 11.sp)
    }
}

@Composable
private fun QuickStatsRow(state: HomeUiState) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        NeonCard(modifier = Modifier.weight(1f), accentColor = NeonCyan) {
            Column {
                Text("Junk Est.", color = TextSecondary, fontSize = 11.sp)
                Spacer(Modifier.height(4.dp))
                Text(
                    Formatters.formatBytes(state.junkEstimateBytes),
                    color = NeonCyan,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
        NeonCard(modifier = Modifier.weight(1f), accentColor = NeonPurple) {
            Column {
                Text("Unused Apps", color = TextSecondary, fontSize = 11.sp)
                Spacer(Modifier.height(4.dp))
                Text(
                    if (state.unusedAppsCount > 0) "${state.unusedAppsCount}" else "—",
                    color = NeonPurple,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
private fun QuickActionsGrid(
    onScanJunk: () -> Unit,
    onAnalyzeStorage: () -> Unit,
    onGamerMode: () -> Unit,
    onAppManager: () -> Unit,
    onOptimize: () -> Unit
) {
    NeonCard {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            QuickActionButton(
                icon    = Icons.Filled.CleaningServices,
                label   = "Scan Junk",
                color   = NeonGreen,
                onClick = onScanJunk
            )
            QuickActionButton(
                icon    = Icons.Filled.Storage,
                label   = "Storage",
                color   = NeonCyan,
                onClick = onAnalyzeStorage
            )
            QuickActionButton(
                icon    = Icons.Filled.SportsEsports,
                label   = "Gamer Mode",
                color   = NeonPurple,
                onClick = onGamerMode
            )
            QuickActionButton(
                icon    = Icons.Filled.Apps,
                label   = "Apps",
                color   = NeonOrange,
                onClick = onAppManager
            )
        }
    }
}
