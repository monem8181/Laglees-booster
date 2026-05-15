package com.lagless.booster.ui.screens.unused

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lagless.booster.data.model.AppUsageInfo
import com.lagless.booster.ui.components.EmptyState
import com.lagless.booster.ui.components.InfoBanner
import com.lagless.booster.ui.components.LagLessTopBar
import com.lagless.booster.ui.components.LoadingOverlay
import com.lagless.booster.ui.components.NeonButton
import com.lagless.booster.ui.components.NeonCard
import com.lagless.booster.ui.components.SectionHeader
import com.lagless.booster.ui.theme.BackgroundPrimary
import com.lagless.booster.ui.theme.NeonCyan
import com.lagless.booster.ui.theme.NeonGreen
import com.lagless.booster.ui.theme.NeonOrange
import com.lagless.booster.ui.theme.NeonPurple
import com.lagless.booster.ui.theme.TextPrimary
import com.lagless.booster.ui.theme.TextSecondary
import com.lagless.booster.utils.formatters.Formatters

@Composable
fun UnusedAppsScreen(onBack: () -> Unit, viewModel: UnusedAppsViewModel = viewModel()) {
    val state   = viewModel.uiState.collectAsState().value
    val context = LocalContext.current

    LaunchedEffect(Unit) { viewModel.checkPermissionAndLoad() }

    Scaffold(
        topBar         = { LagLessTopBar("Unused Apps", onBack = onBack) },
        containerColor = BackgroundPrimary
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (!state.hasPermission) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    EmptyState(
                        icon        = Icons.Filled.Lock,
                        title       = "Usage Access Required",
                        subtitle    = "LagLess needs Usage Access permission to detect apps you haven't used recently. This permission lets us read app usage statistics — no personal data is collected.",
                        accentColor = NeonPurple
                    ) {
                        NeonButton(
                            text    = "Open Usage Access Settings",
                            color   = NeonPurple,
                            onClick = {
                                context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                            }
                        )
                        Spacer(Modifier.height(8.dp))
                        NeonButton(
                            text    = "I've Granted It — Refresh",
                            color   = NeonGreen,
                            onClick = { viewModel.checkPermissionAndLoad() }
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    InfoBanner(
                        text  = "Usage Access permission is a special Android permission. Tap 'Open Usage Access Settings', find LagLess, and toggle it on.",
                        color = NeonCyan
                    )
                }
            } else {
                when {
                    state.isLoading -> LoadingOverlay("Checking app usage…")
                    state.error != null -> EmptyState(
                        icon     = Icons.Filled.Apps,
                        title    = "Error",
                        subtitle = state.error ?: "Unknown error",
                        accentColor = NeonOrange
                    )
                    state.apps.isEmpty() -> EmptyState(
                        icon     = Icons.Filled.Apps,
                        title    = "No Unused Apps",
                        subtitle = "All your apps have been used in the last 30 days. Nothing to clean up!",
                        accentColor = NeonGreen
                    )
                    else -> {
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                            SectionHeader("Apps unused for 30+ days")
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "${state.apps.size} apps found",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                        LazyColumn(
                            modifier       = Modifier.fillMaxSize(),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(state.apps, key = { it.packageName }) { app ->
                                UnusedAppRow(app = app, context = context)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UnusedAppRow(app: AppUsageInfo, context: android.content.Context) {
    NeonCard(accentColor = NeonPurple) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    app.appName,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Text(
                    "Last used: ${Formatters.formatRelativeTime(app.lastUsed)}",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
            NeonButton(
                text     = "Settings",
                onClick  = {
                    context.startActivity(
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            .setData(android.net.Uri.parse("package:${app.packageName}"))
                    )
                },
                modifier = Modifier
                    .padding(start = 8.dp)
                    .fillMaxWidth(0.35f),
                color    = NeonPurple
            )
        }
    }
}
