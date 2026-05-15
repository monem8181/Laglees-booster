package com.lagless.booster.data.repository

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import com.lagless.booster.data.model.AppUsageInfo
import com.lagless.booster.data.model.InstalledAppInfo
import com.lagless.booster.utils.appinfo.AppInfoUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class AppRepository(private val context: Context) {

    suspend fun getInstalledApps(): List<InstalledAppInfo> = withContext(Dispatchers.IO) {
        AppInfoUtils.getInstalledApps(context)
    }

    suspend fun getUserInstalledApps(): List<InstalledAppInfo> = withContext(Dispatchers.IO) {
        AppInfoUtils.getInstalledApps(context).filter { !it.isSystemApp }
    }

    suspend fun getUnusedApps(thresholdDays: Int = 30): List<AppUsageInfo> =
        withContext(Dispatchers.IO) {
            val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE)
                as? UsageStatsManager ?: return@withContext emptyList()

            val endTime   = System.currentTimeMillis()
            val startTime = endTime - TimeUnit.DAYS.toMillis(90)

            val statsMap = try {
                usageStatsManager.queryAndAggregateUsageStats(startTime, endTime)
            } catch (e: Exception) {
                return@withContext emptyList()
            }

            val pm = context.packageManager
            val threshold = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(thresholdDays.toLong())

            val result = mutableListOf<AppUsageInfo>()
            for ((packageName, stats) in statsMap) {
                if (stats.lastTimeUsed in 1..threshold) {
                    val appName = try {
                        val appInfo = pm.getApplicationInfo(packageName, 0)
                        pm.getApplicationLabel(appInfo).toString()
                    } catch (e: Exception) {
                        continue
                    }
                    result.add(
                        AppUsageInfo(
                            packageName          = packageName,
                            appName              = appName,
                            lastUsed             = stats.lastTimeUsed,
                            totalTimeInForeground = stats.totalTimeInForeground
                        )
                    )
                }
            }
            result.sortedBy { it.lastUsed }
        }

    suspend fun getRecentlyUsedGames(): List<InstalledAppInfo> = withContext(Dispatchers.IO) {
        val pm    = context.packageManager
        val flags = PackageManager.GET_META_DATA
        val allPkgs = try { pm.getInstalledPackages(flags) } catch (e: Exception) { emptyList() }

        allPkgs.mapNotNull { pkg ->
            val appInfo = pkg.applicationInfo ?: return@mapNotNull null
            val isGame = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                appInfo.category == ApplicationInfo.CATEGORY_GAME
            } else {
                @Suppress("DEPRECATION")
                (appInfo.flags and ApplicationInfo.FLAG_IS_GAME) != 0
            }
            if (isGame) {
                InstalledAppInfo(
                    packageName = pkg.packageName,
                    appName     = pm.getApplicationLabel(appInfo).toString(),
                    sizeBytes   = 0L,
                    isSystemApp = false,
                    installedAt = pkg.firstInstallTime
                )
            } else null
        }
    }
}
