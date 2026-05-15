package com.lagless.booster.ui.screens.about

import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BoltSharp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lagless.booster.ui.components.InfoBanner
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
fun AboutScreen(onBack: () -> Unit) {
    Scaffold(
        topBar         = { LagLessTopBar("About LagLess", onBack = onBack) },
        containerColor = BackgroundPrimary
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App icon + name hero
            Box(
                modifier         = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(NeonGreen.copy(0.3f), BackgroundCard)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.BoltSharp,
                    contentDescription = null,
                    tint     = NeonGreen,
                    modifier = Modifier.size(50.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                "LagLess",
                color      = TextPrimary,
                fontWeight = FontWeight.ExtraBold,
                fontSize   = 32.sp,
                letterSpacing = 2.sp
            )
            Text(
                "Version 1.0",
                color    = TextSecondary,
                fontSize = 13.sp
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Gamer-grade storage optimizer for Android",
                color     = NeonCyan,
                fontSize  = 14.sp,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(24.dp))

            // Description
            NeonCard(accentColor = NeonGreen) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.SportsEsports, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("What is LagLess?", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "LagLess is a lightweight, honest storage analyzer and device optimizer built for Android gamers. " +
                        "It helps you understand your storage usage, find large apps, detect unused apps, " +
                        "scan for junk files, and launch your favorite games — all without fake boost claims or misleading stats.",
                        color    = TextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // What it can do
            SectionHeader("What LagLess Can Do", accentColor = NeonGreen)
            Spacer(Modifier.height(10.dp))

            val canDo = listOf(
                "Show real storage usage using Android system stats",
                "List installed apps with size information",
                "Detect unused apps using Usage Stats (with permission)",
                "Scan accessible folders in common locations",
                "Find and delete safe junk files (APKs, large downloads, temp files)",
                "Quick-launch favorite games and shortcuts to system settings",
                "Guide you through safe, Android-allowed optimization actions"
            )
            canDo.forEach { item ->
                Row(
                    modifier          = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(Icons.Filled.Check, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(16.dp).padding(top = 2.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(item, color = TextSecondary, fontSize = 13.sp, lineHeight = 19.sp)
                }
            }

            Spacer(Modifier.height(16.dp))

            // Android limitations
            SectionHeader("Android Limitations (Honest Disclosure)", accentColor = NeonOrange)
            Spacer(Modifier.height(10.dp))

            InfoBanner(
                text  = "Android's security model is intentional — it protects you from malicious apps. LagLess respects these boundaries.",
                color = NeonOrange
            )
            Spacer(Modifier.height(10.dp))

            val cantDo = listOf(
                "Clear other apps' cache (requires root or CLEAR_APP_CACHE system permission)",
                "Kill background apps (Android 4.0+ restricts this without root)",
                "Access all folders — Android 11+ enforces scoped storage",
                "Boost RAM by freeing native memory (not possible from user-space apps)",
                "Require root access (LagLess is 100% root-free)"
            )
            cantDo.forEach { item ->
                Row(
                    modifier          = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(Icons.Filled.Close, contentDescription = null, tint = NeonRed, modifier = Modifier.size(16.dp).padding(top = 2.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(item, color = TextSecondary, fontSize = 13.sp, lineHeight = 19.sp)
                }
            }

            Spacer(Modifier.height(16.dp))

            // No root
            NeonCard(accentColor = NeonPurple) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Security, contentDescription = null, tint = NeonPurple, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("100% Root-Free", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(
                            "LagLess never requests root access. Everything works within Android's standard security boundaries.",
                            color    = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                "Built for gamers who deserve honest tools.",
                color     = NeonCyan,
                fontSize  = 13.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}
