package com.lagless.booster.data.repository

import android.app.usage.StorageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import android.os.storage.StorageManager
import com.lagless.booster.data.model.AppCacheInfo
import com.lagless.booster.data.shizuku.ShizukuManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class CacheRepository(private val context: Context) {

    /**
     * Scans all installed apps for cache size using StorageStatsManager.
     *
     * Requires PACKAGE_USAGE_STATS to be user-granted via
     * Settings → Apps → Special App Access → Usage Access.
     *
     * Returns an empty list if:
     * - Android version < 8.0
     * - Usage Access permission not granted (SecurityException caught silently)
     * - PackageManager unavailable
     */
    suspend fun scanAppCaches(): List<AppCacheInfo> = withContext(Dispatchers.IO) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return@withContext emptyList()

        val pm  = context.packageManager
        val mgr = context.getSystemService(Context.STORAGE_STATS_SERVICE) as? StorageStatsManager
            ?: return@withContext emptyList()

        val uuid       = StorageManager.UUID_DEFAULT
        val userHandle = Process.myUserHandle()

        val packages = try {
            pm.getInstalledPackages(0)
        } catch (_: Exception) { return@withContext emptyList() }

        val result = mutableListOf<AppCacheInfo>()

        for (pkg in packages) {
            try {
                val stats = mgr.queryStatsForPackage(uuid, pkg.packageName, userHandle)
                if (stats.cacheBytes <= 0L) continue

                val appInfo = try {
                    pm.getApplicationInfo(pkg.packageName, 0)
                } catch (_: PackageManager.NameNotFoundException) { continue }

                result.add(
                    AppCacheInfo(
                        packageName = pkg.packageName,
                        appName     = pm.getApplicationLabel(appInfo).toString(),
                        cacheBytes  = stats.cacheBytes,
                        totalBytes  = stats.appBytes + stats.dataBytes + stats.cacheBytes
                    )
                )
            } catch (_: SecurityException) {
                // Usage Access not granted — abort early, no point scanning the rest
                return@withContext emptyList()
            } catch (_: Exception) {
                // Package not found or I/O error — skip this app silently
            }
        }

        result.sortedByDescending { it.cacheBytes }
    }

    /**
     * Returns the total cache bytes summed across all installed apps.
     * Returns 0 if Usage Access is not granted or API < O.
     */
    suspend fun getTotalCacheBytes(): Long = withContext(Dispatchers.IO) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return@withContext 0L

        val mgr = context.getSystemService(Context.STORAGE_STATS_SERVICE) as? StorageStatsManager
            ?: return@withContext 0L
        val uuid       = StorageManager.UUID_DEFAULT
        val userHandle = Process.myUserHandle()

        var total = 0L
        try {
            context.packageManager.getInstalledPackages(0).forEach { pkg ->
                try {
                    total += mgr.queryStatsForPackage(uuid, pkg.packageName, userHandle).cacheBytes
                } catch (_: Exception) { /* skip this package */ }
            }
        } catch (_: SecurityException) { return@withContext 0L }
        catch (_: Exception) {}
        total
    }

    /**
     * Trims all app caches via Shizuku using the ADB shell command:
     *   pm trim-caches <desired-free-bytes>
     *
     * Passing a large value (10 GB) triggers maximum cache trimming.
     * Measures bytes freed by comparing total cache before/after (requires Usage Access).
     * If Usage Access is unavailable, freed bytes reported as 0 (trim still runs).
     *
     * Fails with a [Result.failure] if Shizuku is not connected.
     */
    suspend fun trimAllCachesViaShizuku(): Result<Long> = withContext(Dispatchers.IO) {
        try {
            val beforeBytes = getTotalCacheBytes()

            val cmd = ShizukuManager.runShellCommand("pm", "trim-caches", "9999999999")
            if (cmd.isFailure) {
                return@withContext Result.failure(
                    cmd.exceptionOrNull() ?: Exception("pm trim-caches command failed")
                )
            }

            // Give the OS time to actually release cache blocks before re-measuring
            delay(1200)

            val afterBytes  = getTotalCacheBytes()
            val freed       = (beforeBytes - afterBytes).coerceAtLeast(0L)
            Result.success(freed)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Quick check: returns true if the user has granted Usage Access
     * (PACKAGE_USAGE_STATS) to this app.
     */
    fun hasUsageAccess(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return false
        return try {
            val mgr = context.getSystemService(Context.STORAGE_STATS_SERVICE)
                as? StorageStatsManager ?: return false
            mgr.queryStatsForPackage(
                StorageManager.UUID_DEFAULT,
                context.packageName,
                Process.myUserHandle()
            )
            true
        } catch (_: SecurityException) { false }
        catch (_: Exception) { false }
    }
}
