package com.lagless.booster.ui.screens.optimize

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.SettingsApplications
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lagless.booster.ui.components.InfoBanner
import com.lagless.booster.ui.components.LagLessTopBar
import com.lagless.booster.ui.components.NeonButton
import com.lagless.booster.ui.components.NeonCard
import com.lagless.booster.ui.components.SectionHeader
import com.lagless.booster.ui.theme.BackgroundPrimary
import com.lagless.booster.ui.theme.NeonCyan
import com.lagless.booster.ui.theme.NeonGreen
import com.lagless.booster.ui.theme.NeonOrange
import com.lagless.booster.ui.theme.NeonPurple
import com.lagless.booster.ui.theme.NeonYellow
import com.lagless.booster.ui.theme.TextPrimary
import com.lagless.booster.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun OptimizeScreen(
    onBack       : () -> Unit,
    onGoToCleaner: () -> Unit
) {
    val context     = LocalContext.current
    val scope       = rememberCoroutineScope()
    var optimizing  by remember { mutableStateOf(false) }
    var doneMessage by remember { mutableStateOf("") }

    Scaffold(
        topBar         = { LagLessTopBar("RAM Boost / Optimize", onBack = onBack) },
        containerColor = BackgroundPrimary
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Honest Android limitation notice
            InfoBanner(
                text  = "Android's security model prevents third-party apps from directly clearing RAM or killing other apps' processes without root. LagLess performs only safe, OS-allowed optimizations.",
                color = NeonCyan
            )

            Spacer(Modifier.height(20.dp))

            // Optimize Now button
            NeonButton(
                text    = if (optimizing) "Optimizing…" else "Optimize Now",
                enabled = !optimizing,
                color   = NeonGreen,
                onClick = {
                    scope.launch {
                        optimizing = true
                        doneMessage = ""
                        // Safe actions: yield to GC, small delay to simulate
                        System.gc()
                        delay(1500)
                        optimizing  = false
                        doneMessage = "Optimization complete. Background GC triggered. Use the actions below to further optimize your device."
                    }
                }
            )

            if (doneMessage.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                InfoBanner(text = doneMessage, color = NeonGreen)
            }

            Spacer(Modifier.height(24.dp))
            SectionHeader("Safe Optimization Actions", accentColor = NeonGreen)
            Spacer(Modifier.height(10.dp))

            val actions = listOf(
                OptimizeAction(
                    icon        = Icons.Filled.BatteryChargingFull,
                    title       = "Battery Optimization",
                    description = "Manage which apps run in background. Restricting background activity frees RAM and improves performance.",
                    color       = NeonYellow,
                    action      = { context.startActivity(Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
                ),
                OptimizeAction(
                    icon        = Icons.Filled.DeveloperMode,
                    title       = "Running Services",
                    description = "View and stop background services in Developer Options → Running Services.",
                    color       = NeonCyan,
                    action      = { context.startActivity(Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
                ),
                OptimizeAction(
                    icon        = Icons.Filled.SettingsApplications,
                    title       = "App Settings",
                    description = "Review per-app data, battery, and notification settings to identify resource-heavy apps.",
                    color       = NeonPurple,
                    action      = { context.startActivity(Intent(Settings.ACTION_APPLICATION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
                ),
                OptimizeAction(
                    icon        = Icons.Filled.Storage,
                    title       = "Storage Settings",
                    description = "Use Android's built-in Storage settings to view space usage by category.",
                    color       = NeonGreen,
                    action      = { context.startActivity(Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
                ),
                OptimizeAction(
                    icon        = Icons.Filled.CleaningServices,
                    title       = "Junk Cleaner",
                    description = "Scan and remove accessible junk files — old APKs, large downloads, temp files.",
                    color       = NeonOrange,
                    action      = onGoToCleaner
                )
            )

            actions.forEach { item ->
                OptimizeActionCard(item)
                Spacer(Modifier.height(8.dp))
            }

            Spacer(Modifier.height(20.dp))
            SectionHeader("What 'Optimize Now' Does", accentColor = NeonCyan)
            Spacer(Modifier.height(10.dp))

            NeonCard(accentColor = NeonCyan) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(
                        "Requests Java garbage collection (frees unused Java heap memory)",
                        "No root required — no dangerous operations",
                        "Does NOT kill other apps (Android prevents this without root)",
                        "Does NOT clear other apps' cache (requires root or system privilege)"
                    ).forEach { point ->
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                Icons.Filled.Bolt,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(14.dp).padding(top = 2.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(point, color = TextSecondary, fontSize = 13.sp, lineHeight = 18.sp)
                        }
                    }
                }
            }
        }
    }
}

private data class OptimizeAction(
    val icon       : ImageVector,
    val title      : String,
    val description: String,
    val color      : androidx.compose.ui.graphics.Color,
    val action     : () -> Unit
)

@Composable
private fun OptimizeActionCard(item: OptimizeAction) {
    NeonCard(accentColor = item.color, onClick = item.action) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Icon(item.icon, contentDescription = null, tint = item.color, modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.title, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(item.description, color = TextSecondary, fontSize = 12.sp, lineHeight = 17.sp)
            }
            Icon(Icons.Filled.OpenInNew, contentDescription = null, tint = item.color, modifier = Modifier.size(18.dp))
        }
    }
}
