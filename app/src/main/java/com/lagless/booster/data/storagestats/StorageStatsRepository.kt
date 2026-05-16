package com.lagless.booster.data.storagestats

import android.app.usage.StorageStatsManager
import android.content.Context
import android.os.Build
import android.os.Process
import android.os.storage.StorageManager
import com.lagless.booster.data.model.AppStorageStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StorageStatsRepository(private val context: Context) {

    /**
     * Query detailed storage stats for a single package.
     *
     * Requires [android.permission.PACKAGE_USAGE_STATS] to be granted by the user
     * (Settings → Apps → Special App Access → Usage Access).
     *
     * Returns null if:
     * - Below Android O
     * - PACKAGE_USAGE_STATS not granted (SecurityException)
     * - Package not found on device
     */
    suspend fun getStorageStats(packageName: String): AppStorageStats? = withContext(Dispatchers.IO) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return@withContext null
        try {
            val mgr = context.getSystemService(Context.STORAGE_STATS_SERVICE)
                as? StorageStatsManager ?: return@withContext null
            val stats = mgr.queryStatsForPackage(
                StorageManager.UUID_DEFAULT,
                packageName,
                Process.myUserHandle()
            )
            AppStorageStats(
                packageName = packageName,
                appBytes    = stats.appBytes,
                dataBytes   = stats.dataBytes,
                cacheBytes  = stats.cacheBytes
            )
        } catch (_: SecurityException) {
            null  // Usage Access not granted — caller should surface hint to user
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Bulk-query storage stats for a list of packages.
     * Returns a map of packageName → AppStorageStats for every package that succeeded.
     * Packages that fail (not found, no permission) are simply omitted from the result.
     *
     * Returns an empty map if:
     * - Below Android O
     * - PACKAGE_USAGE_STATS not granted (SecurityException on first call)
     */
    suspend fun getStorageStatsForAll(
        packages: List<String>
    ): Map<String, AppStorageStats> = withContext(Dispatchers.IO) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return@withContext emptyMap()

        val mgr = context.getSystemService(Context.STORAGE_STATS_SERVICE)
            as? StorageStatsManager ?: return@withContext emptyMap()
        val uuid       = StorageManager.UUID_DEFAULT
        val userHandle = Process.myUserHandle()

        val result = LinkedHashMap<String, AppStorageStats>(packages.size)

        for (pkg in packages) {
            try {
                val stats = mgr.queryStatsForPackage(uuid, pkg, userHandle)
                result[pkg] = AppStorageStats(
                    packageName = pkg,
                    appBytes    = stats.appBytes,
                    dataBytes   = stats.dataBytes,
                    cacheBytes  = stats.cacheBytes
                )
            } catch (_: SecurityException) {
                // Permission not granted — abort early, no point trying rest
                return@withContext emptyMap()
            } catch (_: Exception) {
                // This specific package failed (not found, etc.) — skip it
            }
        }
        result
    }
}
