package com.lagless.booster.ui.screens.apps

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lagless.booster.data.model.AppSortOrder
import com.lagless.booster.data.model.AppStorageStats
import com.lagless.booster.data.model.InstalledAppInfo
import com.lagless.booster.data.model.ShizukuStatus
import com.lagless.booster.ui.components.EmptyState
import com.lagless.booster.ui.components.LagLessTopBar
import com.lagless.booster.ui.components.NeonCard
import com.lagless.booster.ui.components.NeonProgressBar
import com.lagless.booster.ui.components.ShimmerRow
import com.lagless.booster.ui.components.ShimmerLoadingDashboard
import com.lagless.booster.ui.theme.BackgroundCard
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
import com.lagless.booster.utils.appinfo.AppInfoUtils
import com.lagless.booster.utils.formatters.Formatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppManagerScreen(onBack: () -> Unit, viewModel: AppManagerViewModel = viewModel()) {
    val state   = viewModel.uiState.collectAsState().value
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    var showSortMenu by remember { mutableStateOf(false) }

    // Show force-stop result as snackbar
    LaunchedEffect(state.forceStopResult) {
        state.forceStopResult?.let {
            snackbar.showSnackbar(it)
            viewModel.clearForceStopResult()
        }
    }

    // Force-stop confirmation dialog
    state.forceStopTarget?.let { app ->
        AlertDialog(
            onDismissRequest = { viewModel.cancelForceStop() },
            title = {
                Text("Force Stop ${app.appName}?", color = TextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        "This will immediately terminate the app and may cause it to lose unsaved work.",
                        color = TextSecondary, fontSize = 13.sp, lineHeight = 19.sp
                    )
                    if (app.isSystemApp) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "⚠ This is a system app — force-stopping it may affect device stability.",
                            color = NeonOrange, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, lineHeight = 18.sp
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmForceStop() }) {
                    Text("Force Stop", color = NeonRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelForceStop() }) {
                    Text("Cancel", color = NeonGreen)
                }
            },
            containerColor = BackgroundCard
        )
    }

    Scaffold(
        topBar = {
            LagLessTopBar(
                title  = "App Manager",
                onBack = onBack,
                actions = {
                    // Sort menu
                    Box {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(Icons.Filled.Sort, "Sort", tint = if (state.sortOrder != AppSortOrder.LARGEST_APP) NeonGreen else TextSecondary)
                        }
                        DropdownMenu(
                            expanded         = showSortMenu,
                            onDismissRequest = { showSortMenu = false },
                            containerColor   = BackgroundCard
                        ) {
                            AppSortOrder.entries.forEach { order ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            order.label,
                                            color = if (order == state.sortOrder) NeonGreen else TextPrimary,
                                            fontWeight = if (order == state.sortOrder) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        viewModel.setSortOrder(order)
                                        showSortMenu = false
                                    },
                                    colors = MenuDefaults.itemColors(textColor = TextPrimary)
                                )
                            }
                        }
                    }
                    IconButton(onClick = { viewModel.toggleSystemApps() }) {
                        Icon(Icons.Filled.FilterList, "Toggle System Apps",
                            tint = if (state.showSystemApps) NeonGreen else TextSecondary)
                    }
                    IconButton(onClick = { viewModel.load() }) {
                        Icon(Icons.Filled.Refresh, "Refresh", tint = TextSecondary)
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(snackbar) { data ->
                Snackbar(
                    snackbarData  = data,
                    containerColor = BackgroundCard,
                    contentColor  = NeonGreen
                )
            }
        },
        containerColor = BackgroundPrimary
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Search bar ─────────────────────────────────────
            OutlinedTextField(
                value         = state.query,
                onValueChange = { viewModel.onQueryChange(it) },
                modifier      = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder   = { Text("Search apps…", color = TextSecondary) },
                leadingIcon   = { Icon(Icons.Filled.Search, null, tint = TextSecondary) },
                singleLine    = true,
                shape         = RoundedCornerShape(12.dp),
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = NeonGreen,
                    unfocusedBorderColor = Divider,
                    focusedTextColor     = TextPrimary,
                    unfocusedTextColor   = TextPrimary,
                    cursorColor          = NeonGreen
                )
            )

            // ── Shizuku status badge (only when installed) ─────
            if (state.shizukuStatus != ShizukuStatus.NOT_INSTALLED) {
                ShizukuStatusBanner(
                    status           = state.shizukuStatus,
                    onRequestPerm    = { viewModel.requestShizukuPermission() },
                    onRefresh        = { viewModel.refreshShizukuStatus() },
                    modifier         = Modifier.padding(horizontal = 16.dp).padding(bottom = 4.dp)
                )
            }

            // ── Usage-access hint ──────────────────────────────
            if (!state.isLoading && !state.storageStatsAvailable && state.allApps.isNotEmpty()) {
                NeonCard(
                    accentColor = NeonCyan,
                    modifier    = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Lock, null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Grant Usage Access in Settings for detailed storage breakdown.",
                            color = TextSecondary, fontSize = 11.sp, modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // ── Sort + count row ───────────────────────────────
            if (!state.isLoading && state.filteredApps.isNotEmpty()) {
                Row(
                    modifier          = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("${state.filteredApps.size} apps", color = TextSecondary, fontSize = 12.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Sort: ", color = TextSecondary, fontSize = 11.sp)
                        Text(state.sortOrder.label, color = NeonGreen, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        if (state.isLoadingStorage) {
                            Spacer(Modifier.width(8.dp))
                            ShimmerRow(width = 60.dp, height = 11.dp)
                        }
                    }
                }
            }

            // ── Main content ───────────────────────────────────
            when {
                state.isLoading -> ShimmerLoadingDashboard()

                state.error != null -> EmptyState(
                    icon        = Icons.Filled.Apps,
                    title       = "Failed to load apps",
                    subtitle    = state.error,
                    accentColor = NeonOrange
                )

                state.filteredApps.isEmpty() -> EmptyState(
                    icon     = Icons.Filled.Apps,
                    title    = "No Apps Found",
                    subtitle = "Try a different search term or toggle system apps.",
                    accentColor = NeonGreen
                )

                else -> {
                    LazyColumn(
                        modifier       = Modifier.fillMaxSize(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(state.filteredApps, key = { it.packageName }) { app ->
                            AppItemRow(
                                app             = app,
                                storageStats    = state.storageStats[app.packageName],
                                isLoadingStats  = state.isLoadingStorage,
                                isAdvancedMode  = state.shizukuStatus == ShizukuStatus.CONNECTED,
                                onOpen          = {
                                    context.packageManager
                                        .getLaunchIntentForPackage(app.packageName)
                                        ?.let { context.startActivity(it) }
                                },
                                onSettings      = {
                                    context.startActivity(
                                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                            .setData(Uri.parse("package:${app.packageName}"))
                                    )
                                },
                                onUninstall     = if (!app.isSystemApp) ({
                                    context.startActivity(
                                        Intent(Intent.ACTION_DELETE)
                                            .setData(Uri.parse("package:${app.packageName}"))
                                    )
                                }) else null,
                                onForceStop     = { viewModel.promptForceStop(app) }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Shizuku status banner ─────────────────────────────────────

@Composable
private fun ShizukuStatusBanner(
    status       : ShizukuStatus,
    onRequestPerm: () -> Unit,
    onRefresh    : () -> Unit,
    modifier     : Modifier = Modifier
) {
    val (color, icon, label) = when (status) {
        ShizukuStatus.NOT_RUNNING      -> Triple(NeonYellow, Icons.Filled.Terminal, "Shizuku not running")
        ShizukuStatus.PERMISSION_DENIED -> Triple(NeonOrange, Icons.Filled.Lock,    "Tap to grant Shizuku permission")
        ShizukuStatus.CONNECTED        -> Triple(NeonGreen,  Icons.Filled.Terminal, "Advanced Mode active")
        else                           -> Triple(NeonRed,    Icons.Filled.Block,    "Shizuku unavailable")
    }

    NeonCard(accentColor = color, modifier = modifier,
        onClick = if (status == ShizukuStatus.PERMISSION_DENIED) onRequestPerm else onRefresh
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text(label, color = color, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            if (status == ShizukuStatus.CONNECTED) {
                Box(
                    Modifier.size(8.dp).clip(CircleShape).background(NeonGreen)
                )
            }
        }
    }
}

// ── App row ───────────────────────────────────────────────────

@Composable
private fun AppItemRow(
    app            : InstalledAppInfo,
    storageStats   : AppStorageStats?,
    isLoadingStats : Boolean,
    isAdvancedMode : Boolean,
    onOpen         : () -> Unit,
    onSettings     : () -> Unit,
    onUninstall    : (() -> Unit)?,
    onForceStop    : () -> Unit
) {
    val context  = LocalContext.current
    val icon: ImageBitmap? = remember(app.packageName) {
        AppInfoUtils.getAppIcon(context, app.packageName)
    }
    val accentColor = when {
        app.isSystemApp -> Divider
        isAdvancedMode  -> NeonPurple
        else            -> NeonCyan
    }

    NeonCard(accentColor = accentColor) {
        Column {
            // ── Header row ─────────────────────────────────────
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) {
                    Image(icon, app.appName,
                        modifier = Modifier.size(44.dp).clip(CircleShape))
                } else {
                    Icon(Icons.Filled.Android, null, tint = TextSecondary,
                        modifier = Modifier.size(44.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        app.appName, color = TextPrimary,
                        fontWeight = FontWeight.SemiBold, fontSize = 14.sp,
                        maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        app.packageName, color = TextSecondary, fontSize = 11.sp,
                        maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    if (app.isSystemApp) {
                        Text("System", color = TextSecondary, fontSize = 10.sp)
                    }
                    if (isAdvancedMode) {
                        Text("ADV", color = NeonPurple, fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // ── Storage breakdown ──────────────────────────────
            when {
                storageStats != null -> {
                    StorageBreakdownBar(storageStats)
                }
                isLoadingStats -> {
                    ShimmerRow(width = 200.dp, height = 12.dp)
                    Spacer(Modifier.height(4.dp))
                    ShimmerRow(width = 280.dp, height = 8.dp)
                }
                app.sizeBytes > 0 -> {
                    Text(
                        "≈ ${Formatters.formatBytes(app.sizeBytes)} estimated",
                        color = NeonCyan, fontSize = 12.sp
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // ── Action buttons ─────────────────────────────────
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(onClick = onOpen, modifier = Modifier.weight(1f)) {
                    Text("Open", color = NeonGreen, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }
                TextButton(onClick = onSettings, modifier = Modifier.weight(1f)) {
                    Text("Settings", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }
                if (onUninstall != null) {
                    TextButton(onClick = onUninstall, modifier = Modifier.weight(1f)) {
                        Text("Uninstall", color = NeonRed, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }
                }
                if (isAdvancedMode) {
                    TextButton(onClick = onForceStop, modifier = Modifier.weight(1f)) {
                        Text("Force Stop", color = NeonOrange, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

// ── Storage breakdown bar ─────────────────────────────────────

@Composable
private fun StorageBreakdownBar(stats: AppStorageStats) {
    val total = stats.totalBytes.coerceAtLeast(1L).toFloat()

    Column {
        // Segmented bar
        Row(
            modifier = Modifier.fillMaxWidth().height(7.dp).clip(RoundedCornerShape(4.dp))
        ) {
            if (stats.appBytes > 0) {
                Box(Modifier.weight(stats.appBytes / total).height(7.dp).background(NeonPurple))
            }
            if (stats.dataBytes > 0) {
                Box(Modifier.weight(stats.dataBytes / total).height(7.dp).background(NeonCyan))
            }
            if (stats.cacheBytes > 0) {
                Box(Modifier.weight(stats.cacheBytes / total).height(7.dp).background(NeonGreen.copy(alpha = 0.6f)))
            }
            // Fill rest with divider color
            val usedFrac = ((stats.appBytes + stats.dataBytes + stats.cacheBytes) / total).coerceIn(0f, 1f)
            if (usedFrac < 0.99f) {
                Box(Modifier.weight((1f - usedFrac).coerceAtLeast(0.01f)).height(7.dp).background(Divider))
            }
        }
        Spacer(Modifier.height(5.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StorageChip("APK",   Formatters.formatBytes(stats.appBytes),   NeonPurple)
            StorageChip("Data",  Formatters.formatBytes(stats.dataBytes),  NeonCyan)
            StorageChip("Cache", Formatters.formatBytes(stats.cacheBytes), NeonGreen)
        }
    }
}

@Composable
private fun StorageChip(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(7.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(3.dp))
        Text("$label $value", color = TextSecondary, fontSize = 10.sp)
    }
}
