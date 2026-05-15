package com.lagless.booster.ui.screens.cleaner

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
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lagless.booster.data.model.JunkCategory
import com.lagless.booster.data.model.JunkFileItem
import com.lagless.booster.ui.components.EmptyState
import com.lagless.booster.ui.components.InfoBanner
import com.lagless.booster.ui.components.LagLessTopBar
import com.lagless.booster.ui.components.LoadingOverlay
import com.lagless.booster.ui.components.NeonButton
import com.lagless.booster.ui.components.NeonCard
import com.lagless.booster.ui.components.SectionHeader
import com.lagless.booster.ui.theme.BackgroundPrimary
import com.lagless.booster.ui.theme.NeonGreen
import com.lagless.booster.ui.theme.NeonOrange
import com.lagless.booster.ui.theme.NeonRed
import com.lagless.booster.ui.theme.TextPrimary
import com.lagless.booster.ui.theme.TextSecondary
import com.lagless.booster.utils.formatters.Formatters

@Composable
fun CleanerScreen(onBack: () -> Unit, viewModel: CleanerViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()
    var showConfirmDialog by remember { mutableStateOf(false) }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Confirm Delete", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text  = {
                Text(
                    "Selected files will be permanently deleted. This cannot be undone.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showConfirmDialog = false
                    viewModel.deleteSelected()
                }) {
                    Text("Delete", color = NeonRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancel", color = NeonGreen)
                }
            },
            containerColor = com.lagless.booster.ui.theme.BackgroundCard
        )
    }

    Scaffold(
        topBar          = { LagLessTopBar(title = "Junk Cleaner", onBack = onBack) },
        containerColor  = BackgroundPrimary
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            when (val s = state) {
                is CleanerState.Idle -> {
                    EmptyState(
                        icon     = Icons.Filled.CleaningServices,
                        title    = "Ready to Scan",
                        subtitle = "Tap Scan to find safe junk files — old APKs, large downloads, temp files, and empty folders.",
                        accentColor = NeonGreen
                    ) {
                        NeonButton(text = "Scan for Junk", onClick = { viewModel.scan() })
                    }
                    Spacer(Modifier.height(16.dp))
                    InfoBanner("Only safe, accessible files are listed. No system or app data is touched without your confirmation.")
                }

                is CleanerState.Scanning -> LoadingOverlay("Scanning for junk files…")

                is CleanerState.Results -> {
                    val items = s.items
                    if (items.isEmpty()) {
                        EmptyState(
                            icon     = Icons.Filled.CleaningServices,
                            title    = "All Clean!",
                            subtitle = "No accessible junk files were found. Your device looks good.",
                            accentColor = NeonGreen
                        ) {
                            NeonButton("Scan Again", onClick = { viewModel.scan() })
                        }
                    } else {
                        val selected    = items.filter { it.isSelected }
                        val totalSize   = selected.sumOf { it.sizeBytes }
                        val allSelected = selected.size == items.size

                        NeonCard(accentColor = NeonGreen) {
                            Column {
                                Text(
                                    "${items.size} junk items found",
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    "${selected.size} selected • ${Formatters.formatBytes(totalSize)} to free",
                                    color = NeonGreen,
                                    fontSize = 13.sp
                                )
                                Spacer(Modifier.height(12.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    NeonButton(
                                        text = if (allSelected) "Deselect All" else "Select All",
                                        onClick = { viewModel.selectAll(!allSelected) },
                                        modifier = Modifier.weight(1f),
                                        color = NeonGreen
                                    )
                                    NeonButton(
                                        text    = "Delete Selected",
                                        onClick = { showConfirmDialog = true },
                                        modifier = Modifier.weight(1f),
                                        color   = NeonRed,
                                        enabled = selected.isNotEmpty()
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // Group by category
                        val grouped = items.groupBy { it.category }
                        JunkCategory.values().forEach { category ->
                            val catItems = grouped[category] ?: return@forEach
                            SectionHeader(
                                title = category.label,
                                accentColor = categoryColor(category)
                            )
                            Spacer(Modifier.height(8.dp))
                            catItems.forEach { item ->
                                JunkItemRow(
                                    item     = item,
                                    onToggle = { viewModel.toggleItem(item) }
                                )
                                Spacer(Modifier.height(6.dp))
                            }
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }

                is CleanerState.Deleting -> LoadingOverlay("Deleting files…")

                is CleanerState.Done -> {
                    EmptyState(
                        icon     = Icons.Filled.Delete,
                        title    = "Cleaning Complete",
                        subtitle = "${s.deleted} files deleted${if (s.failed > 0) ", ${s.failed} could not be deleted (possibly in use or protected)" else ""}.",
                        accentColor = NeonGreen
                    ) {
                        NeonButton("Scan Again", onClick = { viewModel.reset() })
                    }
                }

                is CleanerState.Error -> EmptyState(
                    icon     = Icons.Filled.CleaningServices,
                    title    = "Scan Error",
                    subtitle = s.message,
                    accentColor = NeonRed
                ) {
                    NeonButton("Retry", onClick = { viewModel.scan() }, color = NeonOrange)
                }
            }
        }
    }
}

@Composable
private fun JunkItemRow(item: JunkFileItem, onToggle: () -> Unit) {
    NeonCard(
        accentColor = categoryColor(item.category),
        onClick     = onToggle
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked         = item.isSelected,
                onCheckedChange = { onToggle() },
                colors          = CheckboxDefaults.colors(
                    checkedColor   = categoryColor(item.category),
                    uncheckedColor = TextSecondary
                )
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.displayName,
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Text(item.category.label, color = TextSecondary, fontSize = 11.sp)
            }
            Text(
                Formatters.formatBytes(item.sizeBytes),
                color = categoryColor(item.category),
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun categoryColor(category: JunkCategory) = when (category) {
    JunkCategory.APK              -> NeonOrange
    JunkCategory.LARGE_DOWNLOAD   -> NeonRed
    JunkCategory.TEMP             -> com.lagless.booster.ui.theme.NeonYellow
    JunkCategory.EMPTY_FOLDER     -> TextSecondary
    JunkCategory.ACCESSIBLE_CACHE -> NeonGreen
}
