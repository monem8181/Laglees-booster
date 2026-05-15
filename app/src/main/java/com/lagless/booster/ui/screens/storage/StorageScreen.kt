package com.lagless.booster.ui.screens.storage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lagless.booster.data.model.FolderInfo
import com.lagless.booster.ui.components.EmptyState
import com.lagless.booster.ui.components.InfoBanner
import com.lagless.booster.ui.components.LagLessTopBar
import com.lagless.booster.ui.components.LoadingOverlay
import com.lagless.booster.ui.components.NeonCard
import com.lagless.booster.ui.components.SectionHeader
import com.lagless.booster.ui.components.StatRow
import com.lagless.booster.ui.components.StorageRing
import com.lagless.booster.ui.theme.BackgroundPrimary
import com.lagless.booster.ui.theme.Divider
import com.lagless.booster.ui.theme.NeonCyan
import com.lagless.booster.ui.theme.NeonGreen
import com.lagless.booster.ui.theme.NeonOrange
import com.lagless.booster.ui.theme.TextPrimary
import com.lagless.booster.ui.theme.TextSecondary
import com.lagless.booster.utils.formatters.Formatters

@Composable
fun StorageScreen(onBack: () -> Unit, viewModel: StorageViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            LagLessTopBar(
                title  = "Storage Analyzer",
                onBack = onBack,
                actions = {
                    IconButton(onClick = { viewModel.load() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Refresh", tint = NeonGreen)
                    }
                }
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
                state.isLoading -> LoadingOverlay("Analyzing storage…")
                state.error != null -> EmptyState(
                    icon     = Icons.Filled.Storage,
                    title    = "Analysis Failed",
                    subtitle = state.error ?: "Unknown error",
                    accentColor = NeonOrange
                )
                state.storageInfo != null -> {
                    val storage = state.storageInfo!!
                    val usedFrac = storage.usedPercent
                    val accentColor = when {
                        usedFrac > 0.85f -> NeonOrange
                        usedFrac > 0.6f  -> NeonCyan
                        else             -> NeonGreen
                    }

                    // Ring visual
                    NeonCard(accentColor = accentColor) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            StorageRing(
                                usedFraction = usedFrac,
                                color        = accentColor,
                                size         = 170.dp,
                                strokeWidth  = 16.dp
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        Formatters.formatPercent(usedFrac),
                                        color = accentColor,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 26.sp
                                    )
                                    Text("Used", color = TextSecondary, fontSize = 12.sp)
                                }
                            }

                            Spacer(Modifier.height(20.dp))

                            StatRow("Total Storage", Formatters.formatBytes(storage.totalBytes))
                            Spacer(Modifier.height(6.dp))
                            StatRow("Used", Formatters.formatBytes(storage.usedBytes), valueColor = accentColor)
                            Spacer(Modifier.height(6.dp))
                            StatRow("Free", Formatters.formatBytes(storage.freeBytes), valueColor = NeonGreen)
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    InfoBanner(
                        text  = "Sizes shown are estimates based on system storage stats. Some system folders are not directly accessible due to Android security restrictions.",
                        color = NeonCyan
                    )

                    Spacer(Modifier.height(20.dp))

                    SectionHeader("Common Folders")
                    Spacer(Modifier.height(10.dp))

                    if (state.folders.isEmpty()) {
                        EmptyState(
                            icon        = Icons.Filled.FolderOff,
                            title       = "No Folders Found",
                            subtitle    = "Grant storage permission to scan common folders.",
                            accentColor = NeonOrange
                        )
                    } else {
                        state.folders.forEach { folder ->
                            FolderRow(folder = folder)
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FolderRow(folder: FolderInfo) {
    NeonCard(accentColor = if (folder.isAccessible) NeonCyan else Divider) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    folder.name,
                    color = if (folder.isAccessible) TextPrimary else TextSecondary,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
                Text(
                    text = if (folder.isAccessible)
                        "${folder.fileCount} files"
                    else "Not accessible",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    Formatters.formatBytes(folder.sizeBytes),
                    color = if (folder.isAccessible) NeonCyan else TextSecondary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                if (!folder.isAccessible) {
                    Text("No access", color = NeonOrange, fontSize = 11.sp)
                }
            }
        }
    }
}
