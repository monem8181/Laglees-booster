package com.lagless.booster.ui.screens.apps

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lagless.booster.data.model.InstalledAppInfo
import com.lagless.booster.ui.components.EmptyState
import com.lagless.booster.ui.components.LagLessTopBar
import com.lagless.booster.ui.components.LoadingOverlay
import com.lagless.booster.ui.components.NeonCard
import com.lagless.booster.ui.theme.BackgroundPrimary
import com.lagless.booster.ui.theme.Divider
import com.lagless.booster.ui.theme.NeonCyan
import com.lagless.booster.ui.theme.NeonGreen
import com.lagless.booster.ui.theme.NeonOrange
import com.lagless.booster.ui.theme.NeonRed
import com.lagless.booster.ui.theme.TextPrimary
import com.lagless.booster.ui.theme.TextSecondary
import com.lagless.booster.utils.appinfo.AppInfoUtils
import com.lagless.booster.utils.formatters.Formatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppManagerScreen(onBack: () -> Unit, viewModel: AppManagerViewModel = viewModel()) {
    val state   = viewModel.uiState.collectAsState().value
    val context = LocalContext.current

    Scaffold(
        topBar = {
            LagLessTopBar(
                title  = "App Manager",
                onBack = onBack,
                actions = {
                    IconButton(onClick = { viewModel.toggleSystemApps() }) {
                        Icon(
                            Icons.Filled.FilterList,
                            contentDescription = "Toggle System Apps",
                            tint = if (state.showSystemApps) NeonGreen else TextSecondary
                        )
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
            // Search bar
            OutlinedTextField(
                value         = state.query,
                onValueChange = { viewModel.onQueryChange(it) },
                modifier      = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder   = { Text("Search apps…", color = TextSecondary) },
                leadingIcon   = { Icon(Icons.Filled.Search, contentDescription = null, tint = TextSecondary) },
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

            // System apps filter hint
            if (state.showSystemApps) {
                Text(
                    "Showing all apps (including system)",
                    color    = NeonCyan,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            when {
                state.isLoading -> LoadingOverlay("Loading installed apps…")
                state.error != null -> EmptyState(
                    icon     = Icons.Filled.Apps,
                    title    = "Failed to load apps",
                    subtitle = state.error ?: "",
                    accentColor = NeonOrange
                )
                state.filteredApps.isEmpty() -> EmptyState(
                    icon     = Icons.Filled.Apps,
                    title    = "No Apps Found",
                    subtitle = "Try adjusting your search or filter.",
                    accentColor = NeonGreen
                )
                else -> {
                    Text(
                        "${state.filteredApps.size} apps",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                    LazyColumn(
                        modifier            = Modifier.fillMaxSize(),
                        contentPadding      = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(state.filteredApps, key = { it.packageName }) { app ->
                            AppItemRow(
                                app     = app,
                                onOpen  = {
                                    val intent = context.packageManager.getLaunchIntentForPackage(app.packageName)
                                    if (intent != null) context.startActivity(intent)
                                },
                                onSettings = {
                                    context.startActivity(
                                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                            .setData(Uri.parse("package:${app.packageName}"))
                                    )
                                },
                                onUninstall = if (!app.isSystemApp) ({
                                    context.startActivity(
                                        Intent(Intent.ACTION_DELETE)
                                            .setData(Uri.parse("package:${app.packageName}"))
                                    )
                                }) else null
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppItemRow(
    app: InstalledAppInfo,
    onOpen: () -> Unit,
    onSettings: () -> Unit,
    onUninstall: (() -> Unit)?
) {
    val context = LocalContext.current
    val icon: ImageBitmap? = remember(app.packageName) {
        AppInfoUtils.getAppIcon(context, app.packageName)
    }

    NeonCard(accentColor = if (app.isSystemApp) Divider else NeonCyan) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) {
                    Image(
                        bitmap            = icon,
                        contentDescription = app.appName,
                        modifier          = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Icon(
                        Icons.Filled.Android,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(44.dp)
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        app.appName,
                        color      = TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize   = 14.sp,
                        maxLines   = 1,
                        overflow   = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Text(
                        app.packageName,
                        color    = TextSecondary,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    if (app.sizeBytes > 0) {
                        Text(
                            Formatters.formatBytes(app.sizeBytes),
                            color    = NeonCyan,
                            fontSize = 12.sp
                        )
                    }
                }

                if (app.isSystemApp) {
                    Text(
                        "System",
                        color    = TextSecondary,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                TextButton(
                    onClick = onOpen,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Open", color = NeonGreen, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
                TextButton(
                    onClick = onSettings,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Settings", color = NeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
                if (onUninstall != null) {
                    TextButton(
                        onClick = onUninstall,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Uninstall", color = NeonRed, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}
