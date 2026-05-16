package com.lagless.booster.ui.screens.cleaner

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lagless.booster.data.model.AppCacheInfo
import com.lagless.booster.data.model.CleanupResult
import com.lagless.booster.data.model.JunkCategory
import com.lagless.booster.data.model.JunkFileItem
import com.lagless.booster.data.model.ShizukuStatus
import com.lagless.booster.ui.components.AnimatedStatValue
import com.lagless.booster.ui.components.EmptyState
import com.lagless.booster.ui.components.InfoBanner
import com.lagless.booster.ui.components.LagLessTopBar
import com.lagless.booster.ui.components.NeonButton
import com.lagless.booster.ui.components.NeonCard
import com.lagless.booster.ui.components.NeonProgressBar
import com.lagless.booster.ui.components.PulsingDot
import com.lagless.booster.ui.components.SectionHeader
import com.lagless.booster.ui.components.SonarRings
import com.lagless.booster.ui.components.neonGlow
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
import com.lagless.booster.utils.formatters.Formatters

@Composable
fun CleanerScreen(onBack: () -> Unit, viewModel: CleanerViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar         = { LagLessTopBar("Smart Cleaner", onBack = onBack) },
        containerColor = BackgroundPrimary
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (val s = state) {
                is CleanerState.Idle     -> IdleContent(viewModel)
                is CleanerState.Scanning -> ScanningContent(s)
                is CleanerState.Results  -> ResultsContent(s, viewModel)
                is CleanerState.Cleaning -> CleaningContent(s)
                is CleanerState.Done     -> DoneContent(s.result) { viewModel.reset() }
                is CleanerState.Error    -> EmptyState(
                    icon     = Icons.Filled.CleaningServices,
                    title    = "Scan Error",
                    subtitle = s.message,
                    accentColor = NeonRed
                ) { NeonButton("Retry", onClick = { viewModel.scan() }, color = NeonOrange) }
            }
        }
    }
}

// ── Idle ──────────────────────────────────────────────────────

@Composable
private fun IdleContent(viewModel: CleanerViewModel) {
    Column(
        modifier            = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.weight(1f))

        Box(contentAlignment = Alignment.Center) {
            SonarRings(NeonCyan, 180.dp)
            Box(
                Modifier.size(72.dp).clip(CircleShape)
                    .background(NeonCyan.copy(alpha = 0.12f))
                    .border(1.5.dp, NeonCyan.copy(alpha = 0.5f), CircleShape),
                Alignment.Center
            ) {
                Icon(Icons.Filled.CleaningServices, null, tint = NeonCyan, modifier = Modifier.size(36.dp))
            }
        }

        Spacer(Modifier.height(32.dp))
        Text("Smart Cleaner", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(
            "Scans junk files and detects app cache. Shizuku enables deeper cache trimming.",
            color = TextSecondary, fontSize = 14.sp, textAlign = TextAlign.Center, lineHeight = 20.sp
        )
        Spacer(Modifier.height(32.dp))
        NeonButton("Scan Now", onClick = { viewModel.scan() }, color = NeonCyan)

        Spacer(Modifier.weight(1f))

        InfoBanner(
            "Only safe, accessible files are listed. App data is never touched. " +
            "Cache trimming is only available with optional Shizuku access.",
            color = NeonCyan
        )
        Spacer(Modifier.height(16.dp))
    }
}

// ── Scanning ──────────────────────────────────────────────────

@Composable
private fun ScanningContent(s: CleanerState.Scanning) {
    Column(
        modifier            = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(contentAlignment = Alignment.Center) {
            SonarRings(NeonGreen, 180.dp)
            Box(
                Modifier.size(72.dp).clip(CircleShape)
                    .background(NeonGreen.copy(alpha = 0.1f))
                    .border(1.5.dp, NeonGreen.copy(alpha = 0.4f), CircleShape),
                Alignment.Center
            ) {
                Icon(Icons.Filled.CleaningServices, null, tint = NeonGreen, modifier = Modifier.size(36.dp))
            }
        }
        Spacer(Modifier.height(36.dp))
        Text("Scanning Device…", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(s.phase, color = TextSecondary, fontSize = 13.sp)
    }
}

// ── Results ───────────────────────────────────────────────────

@Composable
private fun ResultsContent(s: CleanerState.Results, viewModel: CleanerViewModel) {
    var showCleanAllConfirm by remember { mutableStateOf(false) }
    var showFilesOnlyConfirm by remember { mutableStateOf(false) }

    val selectedJunk     = s.junkItems.filter { it.isSelected }
    val totalJunkBytes   = selectedJunk.sumOf { it.sizeBytes }
    val allSelected      = selectedJunk.size == s.junkItems.size
    val nothingSelected  = selectedJunk.isEmpty() && !s.cacheStatsAvailable
    val advancedMode     = s.shizukuStatus == ShizukuStatus.CONNECTED

    // Confirmation — Clean All
    if (showCleanAllConfirm) {
        AlertDialog(
            onDismissRequest = { showCleanAllConfirm = false },
            title = { Text("Confirm Full Clean?", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text  = {
                Column {
                    if (selectedJunk.isNotEmpty()) {
                        Text("• ${selectedJunk.size} junk files (${Formatters.formatBytes(totalJunkBytes)}) will be permanently deleted.", color = TextSecondary, fontSize = 13.sp)
                        Spacer(Modifier.height(6.dp))
                    }
                    if (advancedMode) {
                        Text("• App caches will be trimmed via Shizuku (cache only — no data deleted).", color = TextSecondary, fontSize = 13.sp)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCleanAllConfirm = false; viewModel.cleanAll() }) {
                    Text("Clean Now", color = NeonGreen, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCleanAllConfirm = false }) { Text("Cancel", color = TextSecondary) }
            },
            containerColor = BackgroundCard
        )
    }

    // Confirmation — Files Only
    if (showFilesOnlyConfirm) {
        AlertDialog(
            onDismissRequest = { showFilesOnlyConfirm = false },
            title = { Text("Delete Selected Files?", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text  = { Text("${selectedJunk.size} files (${Formatters.formatBytes(totalJunkBytes)}) will be permanently deleted.", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = { showFilesOnlyConfirm = false; viewModel.deleteFilesOnly() }) {
                    Text("Delete", color = NeonRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showFilesOnlyConfirm = false }) { Text("Cancel", color = TextSecondary) }
            },
            containerColor = BackgroundCard
        )
    }

    LazyColumn(
        contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // ── Summary card ──────────────────────────────────────
        item("summary") {
            ScanSummaryCard(
                junkCount        = s.junkItems.size,
                junkBytes        = totalJunkBytes,
                cacheBytes       = s.totalCacheBytes,
                cacheAvailable   = s.cacheStatsAvailable,
                advancedMode     = advancedMode
            )
        }

        // ── Mode banner ───────────────────────────────────────
        item("mode") { ModeBanner(s.shizukuStatus) }

        // ── Usage access hint ─────────────────────────────────
        if (!s.cacheStatsAvailable) {
            item("cacheHint") {
                NeonCard(accentColor = NeonCyan) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Lock, null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Grant Usage Access in Settings to see per-app cache sizes.",
                            color = TextSecondary, fontSize = 12.sp, modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // ── Top cached apps ───────────────────────────────────
        if (s.appCaches.isNotEmpty()) {
            item("cacheHeader") {
                SectionHeader("Largest App Caches (${s.appCaches.size} apps)", NeonPurple)
            }
            items(s.appCaches.take(6), key = { "cache_${it.packageName}" }) { app ->
                AppCacheRow(app)
            }
            if (s.appCaches.size > 6) {
                item("cacheMore") {
                    Text(
                        "+ ${s.appCaches.size - 6} more apps with cache",
                        color = TextSecondary, fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }
        }

        // ── Junk files ────────────────────────────────────────
        if (s.junkItems.isNotEmpty()) {
            item("junkHeader") {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionHeader("Junk Files (${s.junkItems.size})", NeonOrange)
                    TextButton(onClick = { viewModel.selectAll(!allSelected) }) {
                        Text(
                            if (allSelected) "Deselect All" else "Select All",
                            color = NeonOrange, fontSize = 12.sp
                        )
                    }
                }
            }

            val grouped = s.junkItems.groupBy { it.category }
            JunkCategory.entries.forEach { category ->
                val catItems = grouped[category]
                if (!catItems.isNullOrEmpty()) {
                    item("cat_$category") {
                        Text(
                            category.label,
                            color = junkCategoryColor(category),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 6.dp, start = 2.dp)
                        )
                    }
                    items(catItems, key = { it.file.absolutePath }) { item ->
                        JunkItemRow(item, onToggle = { viewModel.toggleItem(item) })
                    }
                }
            }
        }

        // ── Nothing found ─────────────────────────────────────
        if (s.junkItems.isEmpty() && !s.cacheStatsAvailable) {
            item("allClean") {
                EmptyState(
                    icon     = Icons.Filled.CheckCircle,
                    title    = "All Clean!",
                    subtitle = "No accessible junk files detected. Grant Usage Access to also analyze app caches.",
                    accentColor = NeonGreen
                ) { NeonButton("Scan Again", onClick = { viewModel.scan() }) }
            }
        }

        // ── Action buttons ────────────────────────────────────
        if (s.junkItems.isNotEmpty() || advancedMode) {
            item("actions") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Primary: Clean All
                    NeonButton(
                        text    = if (advancedMode) "⚡ Clean All + Trim Caches" else "Clean Selected Files",
                        onClick = { showCleanAllConfirm = true },
                        color   = if (advancedMode) NeonGreen else NeonOrange,
                        enabled = selectedJunk.isNotEmpty() || advancedMode
                    )
                    // Secondary: Files only (only show if Shizuku active AND there are files)
                    if (advancedMode && selectedJunk.isNotEmpty()) {
                        NeonButton(
                            text    = "Delete Files Only (No Cache Trim)",
                            onClick = { showFilesOnlyConfirm = true },
                            color   = NeonCyan
                        )
                    }
                }
            }
        }

        // ── Honesty banner ────────────────────────────────────
        item("info") {
            InfoBanner(
                "Cache trimming only clears cache — never app data or user files. " +
                "Root is never used. Some OEMs may restrict cache trimming even with Shizuku.",
                color = NeonOrange
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

// ── Cleaning animation ────────────────────────────────────────

@Composable
private fun CleaningContent(s: CleanerState.Cleaning) {
    val animProgress by animateFloatAsState(
        targetValue   = s.progress,
        animationSpec = tween(700, easing = FastOutSlowInEasing),
        label         = "cleanProg"
    )

    Column(
        modifier            = Modifier.fillMaxSize().padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Spinning icon with sonar rings
        val transition = rememberInfiniteTransition(label = "spin")
        val glow by transition.animateFloat(
            0.4f, 1f,
            infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
            label = "g"
        )

        Box(contentAlignment = Alignment.Center) {
            SonarRings(NeonGreen, 160.dp)
            Box(
                Modifier.size(64.dp).clip(CircleShape)
                    .background(NeonGreen.copy(alpha = 0.1f * glow))
                    .border((1.5 * glow).dp, NeonGreen.copy(alpha = 0.5f * glow), CircleShape),
                Alignment.Center
            ) {
                Icon(Icons.Filled.CleaningServices, null, tint = NeonGreen, modifier = Modifier.size(32.dp))
            }
        }

        Spacer(Modifier.height(36.dp))

        Text("Cleaning…", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(10.dp))
        Text(s.phase, color = TextSecondary, fontSize = 14.sp)

        if (s.bytesFreed > 0) {
            Spacer(Modifier.height(12.dp))
            Text(
                "${Formatters.formatBytes(s.bytesFreed)} freed",
                color = NeonGreen, fontSize = 20.sp, fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(28.dp))

        NeonProgressBar(animProgress, modifier = Modifier.fillMaxWidth())

        Spacer(Modifier.height(10.dp))
        Text("${(s.progress * 100).toInt()}%", color = NeonGreen, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

// ── Done result screen ────────────────────────────────────────

@Composable
private fun DoneContent(result: CleanupResult, onReset: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    LazyColumn(
        modifier            = Modifier.fillMaxSize(),
        contentPadding      = PaddingValues(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item("check") {
            AnimatedVisibility(visible, enter = fadeIn() + expandVertically(expandFrom = Alignment.Top)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Animated checkmark
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(NeonGreen.copy(alpha = 0.1f))
                            .border(2.dp, NeonGreen.copy(alpha = 0.35f), CircleShape)
                            .then(Modifier.neonGlow(NeonGreen, 60.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.CheckCircle, null, tint = NeonGreen, modifier = Modifier.size(64.dp))
                    }

                    Spacer(Modifier.height(24.dp))
                    Text("Cleaning Complete", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                    Spacer(Modifier.height(4.dp))
                    Text("Your device is cleaner now.", color = TextSecondary, fontSize = 13.sp)
                    Spacer(Modifier.height(24.dp))

                    // Animated bytes counter
                    AnimatedStatValue(
                        target  = result.totalBytesFreed.toFloat(),
                        format  = { Formatters.formatBytes(it.toLong()) },
                        color   = NeonGreen,
                        style   = MaterialTheme.typography.headlineLarge
                    )
                    Text("total freed", color = TextSecondary, fontSize = 13.sp)
                    Spacer(Modifier.height(28.dp))

                    // Stat chips
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        ResultStatChip(
                            label = "Files",
                            value = "${result.filesDeleted} deleted",
                            color = NeonOrange,
                            modifier = Modifier.weight(1f)
                        )
                        if (result.wasAdvancedMode) {
                            ResultStatChip(
                                label = "Cache",
                                value = if (result.cacheBytesFreed > 0)
                                    Formatters.formatBytes(result.cacheBytesFreed)
                                else "Trimmed",
                                color = NeonPurple,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        ResultStatChip(
                            label = "Time",
                            value = "${"%.1f".format(result.durationMs / 1000.0)}s",
                            color = NeonCyan,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Failure note
                    if (result.filesFailed > 0) {
                        Spacer(Modifier.height(12.dp))
                        InfoBanner(
                            "⚠ ${result.filesFailed} file(s) could not be deleted — they may be in use or protected.",
                            color = NeonYellow
                        )
                    }

                    // Cache disclaimer
                    if (result.wasAdvancedMode && result.cacheBytesFreed == 0L) {
                        Spacer(Modifier.height(8.dp))
                        InfoBanner(
                            "App cache was trimmed. Updated sizes will reflect after apps relaunch. " +
                            "Grant Usage Access for precise measurements.",
                            color = NeonCyan
                        )
                    }

                    Spacer(Modifier.height(24.dp))
                    NeonButton("Scan Again", onClick = onReset)
                }
            }
        }
    }
}

// ── Sub-components ────────────────────────────────────────────

@Composable
private fun ScanSummaryCard(
    junkCount     : Int,
    junkBytes     : Long,
    cacheBytes    : Long,
    cacheAvailable: Boolean,
    advancedMode  : Boolean
) {
    NeonCard(accentColor = NeonGreen, glowing = advancedMode) {
        Column {
            Text("Scan Results", color = TextSecondary, fontSize = 11.sp, letterSpacing = 0.8.sp)
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                SummaryStatBlock("Junk Files", "$junkCount items", NeonOrange, Modifier.weight(1f))
                SummaryStatBlock("File Size", Formatters.formatBytes(junkBytes), NeonOrange, Modifier.weight(1f))
                if (cacheAvailable) {
                    SummaryStatBlock("App Cache", Formatters.formatBytes(cacheBytes), NeonPurple, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun SummaryStatBlock(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = color, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
        Text(label, color = TextSecondary, fontSize = 11.sp)
    }
}

@Composable
private fun ModeBanner(status: ShizukuStatus) {
    val connected = status == ShizukuStatus.CONNECTED
    val (color, label, sub) = when (status) {
        ShizukuStatus.CONNECTED         -> Triple(NeonGreen,  "⚡ Advanced Cleaning Active",   "App cache trimming enabled via Shizuku")
        ShizukuStatus.NOT_INSTALLED     -> Triple(NeonPurple, "Basic Cleaning Mode",            "Install Shizuku to unlock app cache trimming")
        ShizukuStatus.NOT_RUNNING       -> Triple(NeonYellow, "Shizuku Not Running",            "Start Shizuku to enable advanced cache trim")
        ShizukuStatus.PERMISSION_DENIED -> Triple(NeonOrange, "Shizuku: Permission Needed",     "Grant permission in the Shizuku app")
    }
    NeonCard(accentColor = color, glowing = connected) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (connected) PulsingDot(NeonGreen, 8.dp) else
                Icon(Icons.Filled.Terminal, null, tint = color, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(label, color = color, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(sub, color = TextSecondary, fontSize = 11.sp, lineHeight = 16.sp)
            }
        }
    }
}

@Composable
private fun AppCacheRow(app: AppCacheInfo) {
    NeonCard(accentColor = NeonPurple) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        app.appName, color = TextPrimary,
                        fontWeight = FontWeight.Medium, fontSize = 13.sp,
                        maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        "Cache: ${Formatters.formatBytes(app.cacheBytes)}",
                        color = NeonPurple, fontSize = 12.sp
                    )
                }
                Text(
                    Formatters.formatBytes(app.totalBytes),
                    color = TextSecondary, fontSize = 11.sp
                )
            }
            Spacer(Modifier.height(8.dp))
            // Cache fraction bar
            Box(
                Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(3.dp)).background(Divider)
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(app.cacheFraction.coerceIn(0f, 1f))
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(NeonPurple)
                )
            }
        }
    }
}

@Composable
private fun JunkItemRow(item: JunkFileItem, onToggle: () -> Unit) {
    NeonCard(accentColor = junkCategoryColor(item.category), onClick = onToggle) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked         = item.isSelected,
                onCheckedChange = { onToggle() },
                colors          = CheckboxDefaults.colors(
                    checkedColor   = junkCategoryColor(item.category),
                    uncheckedColor = TextSecondary
                )
            )
            Column(Modifier.weight(1f)) {
                Text(
                    item.displayName, color = TextPrimary,
                    fontWeight = FontWeight.Medium, fontSize = 13.sp,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Text(item.category.label, color = TextSecondary, fontSize = 11.sp)
            }
            Text(
                Formatters.formatBytes(item.sizeBytes),
                color = junkCategoryColor(item.category),
                fontWeight = FontWeight.SemiBold, fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun ResultStatChip(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    NeonCard(accentColor = color, modifier = modifier) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(value, color = color, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
            Text(label, color = TextSecondary, fontSize = 11.sp)
        }
    }
}

// ── Category color ────────────────────────────────────────────

@Composable
private fun junkCategoryColor(category: JunkCategory): Color = when (category) {
    JunkCategory.APK              -> NeonOrange
    JunkCategory.LARGE_DOWNLOAD   -> NeonRed
    JunkCategory.TEMP             -> NeonYellow
    JunkCategory.EMPTY_FOLDER     -> TextSecondary
    JunkCategory.ACCESSIBLE_CACHE -> NeonGreen
}
