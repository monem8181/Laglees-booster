package com.lagless.booster.ui.screens.folders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import com.lagless.booster.ui.theme.BackgroundPrimary
import com.lagless.booster.ui.theme.Divider
import com.lagless.booster.ui.theme.NeonCyan
import com.lagless.booster.ui.theme.NeonGreen
import com.lagless.booster.ui.theme.NeonOrange
import com.lagless.booster.ui.theme.TextPrimary
import com.lagless.booster.ui.theme.TextSecondary
import com.lagless.booster.utils.formatters.Formatters

@Composable
fun FolderScannerScreen(onBack: () -> Unit, viewModel: FolderScannerViewModel = viewModel()) {
    val state = viewModel.uiState.collectAsState().value

    Scaffold(
        topBar = {
            LagLessTopBar(
                title   = "Folder Scanner",
                onBack  = onBack,
                actions = {
                    IconButton(onClick = { viewModel.scan() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Rescan", tint = NeonGreen)
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
        ) {
            when {
                state.isLoading -> LoadingOverlay("Scanning folders…")
                state.error != null -> EmptyState(
                    icon     = Icons.Filled.FolderOff,
                    title    = "Scan Failed",
                    subtitle = state.error ?: "",
                    accentColor = NeonOrange
                )
                state.folders.isEmpty() -> EmptyState(
                    icon     = Icons.Filled.Folder,
                    title    = "No Folders Found",
                    subtitle = "Grant storage permission and try again.",
                    accentColor = NeonCyan
                )
                else -> {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        SectionHeader("Common Folders")
                        Spacer(Modifier.height(4.dp))
                        InfoBanner(
                            text  = "Android 11+ restricts access to most folders. Sizes shown are for accessible content only.",
                            color = NeonCyan
                        )
                    }
                    LazyColumn(
                        modifier       = Modifier.fillMaxSize(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(state.folders, key = { it.path }) { folder ->
                            FolderDetailRow(folder = folder)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FolderDetailRow(folder: FolderInfo) {
    val accent = if (folder.isAccessible) NeonCyan else Divider
    NeonCard(accentColor = accent) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                Icons.Filled.Folder,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.padding(end = 12.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(folder.name, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(
                    text = if (folder.isAccessible)
                        "${folder.fileCount} files • ${folder.path.substringAfter("/storage/emulated/0/")}"
                    else "Not accessible on this Android version",
                    color    = TextSecondary,
                    fontSize = 11.sp
                )
            }
            Text(
                text = if (folder.isAccessible) Formatters.formatBytes(folder.sizeBytes)
                       else "—",
                color      = if (folder.isAccessible) NeonCyan else TextSecondary,
                fontWeight = FontWeight.Bold,
                fontSize   = 14.sp
            )
        }
    }
}
