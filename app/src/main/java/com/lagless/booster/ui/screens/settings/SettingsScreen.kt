package com.lagless.booster.ui.screens.settings

import android.content.Intent
import android.provider.Settings
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lagless.booster.ui.components.LagLessTopBar
import com.lagless.booster.ui.components.NeonCard
import com.lagless.booster.ui.components.SectionHeader
import com.lagless.booster.ui.theme.BackgroundCard
import com.lagless.booster.ui.theme.BackgroundPrimary
import com.lagless.booster.ui.theme.NeonCyan
import com.lagless.booster.ui.theme.NeonGreen
import com.lagless.booster.ui.theme.NeonOrange
import com.lagless.booster.ui.theme.NeonPurple
import com.lagless.booster.ui.theme.NeonRed
import com.lagless.booster.ui.theme.TextPrimary
import com.lagless.booster.ui.theme.TextSecondary

@Composable
fun SettingsScreen(
    onBack     : () -> Unit,
    onAbout    : () -> Unit,
    viewModel  : SettingsViewModel = viewModel()
) {
    val context = LocalContext.current
    var showClearConfirm by remember { mutableStateOf(false) }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Clear Favorite Game?", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text  = { Text("This will remove your saved favorite game shortcut.", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearFavoriteGame()
                    showClearConfirm = false
                }) { Text("Clear", color = NeonRed, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) { Text("Cancel", color = NeonGreen) }
            },
            containerColor = BackgroundCard
        )
    }

    Scaffold(
        topBar         = { LagLessTopBar("Settings", onBack = onBack) },
        containerColor = BackgroundPrimary
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // ── Permissions ───────────────────────────────────────
            SectionHeader("Permissions", accentColor = NeonCyan)
            Spacer(Modifier.height(10.dp))

            SettingsItem(
                icon        = Icons.Filled.Lock,
                title       = "Usage Access",
                subtitle    = "Required for Unused Apps screen",
                color       = NeonCyan,
                actionLabel = "Open Settings",
                onClick     = {
                    context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }
            )
            Spacer(Modifier.height(8.dp))
            SettingsItem(
                icon        = Icons.Filled.Storage,
                title       = "Storage Permission",
                subtitle    = "Required for folder scanning and junk cleaner",
                color       = NeonGreen,
                actionLabel = "App Settings",
                onClick     = {
                    context.startActivity(
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            .setData(android.net.Uri.parse("package:${context.packageName}"))
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                }
            )
            Spacer(Modifier.height(8.dp))
            SettingsItem(
                icon        = Icons.Filled.Apps,
                title       = "All Apps Permission",
                subtitle    = "Used for App Manager listing",
                color       = NeonPurple,
                actionLabel = "App Settings",
                onClick     = {
                    context.startActivity(
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            .setData(android.net.Uri.parse("package:${context.packageName}"))
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                }
            )

            Spacer(Modifier.height(24.dp))

            // ── Data ─────────────────────────────────────────────
            SectionHeader("Data", accentColor = NeonOrange)
            Spacer(Modifier.height(10.dp))

            SettingsItem(
                icon        = Icons.Filled.Delete,
                title       = "Clear Favorite Game",
                subtitle    = "Remove saved game shortcut from Gamer Mode",
                color       = NeonOrange,
                actionLabel = "Clear",
                onClick     = { showClearConfirm = true }
            )

            Spacer(Modifier.height(24.dp))

            // ── About ─────────────────────────────────────────────
            SectionHeader("About", accentColor = NeonGreen)
            Spacer(Modifier.height(10.dp))

            SettingsItem(
                icon        = Icons.Filled.Info,
                title       = "About LagLess",
                subtitle    = "Version, limitations, and credits",
                color       = NeonGreen,
                actionLabel = "View",
                onClick     = onAbout
            )

            Spacer(Modifier.height(24.dp))

            // ── Privacy note ──────────────────────────────────────
            NeonCard(accentColor = NeonCyan) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Filled.AdminPanelSettings, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text("Privacy", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "LagLess does not collect, transmit, or store any personal data. " +
                            "All analysis is performed locally on your device. No internet permission is requested.",
                            color    = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsItem(
    icon       : ImageVector,
    title      : String,
    subtitle   : String,
    color      : androidx.compose.ui.graphics.Color,
    actionLabel: String,
    onClick    : () -> Unit
) {
    NeonCard(accentColor = color, onClick = onClick) {
        Row(
            modifier          = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(subtitle, color = TextSecondary, fontSize = 12.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(actionLabel, color = color, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.width(4.dp))
                Icon(Icons.Filled.OpenInNew, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
            }
        }
    }
}
