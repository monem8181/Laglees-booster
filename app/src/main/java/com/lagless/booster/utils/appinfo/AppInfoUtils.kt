package com.lagless.booster.utils.appinfo

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.lagless.booster.data.model.InstalledAppInfo

object AppInfoUtils {

    fun getInstalledApps(context: Context): List<InstalledAppInfo> {
        val pm = context.packageManager
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            PackageManager.GET_META_DATA
        } else {
            PackageManager.GET_META_DATA
        }

        val packages = try {
            pm.getInstalledPackages(flags)
        } catch (e: Exception) {
            emptyList()
        }

        return packages.mapNotNull { pkg ->
            try {
                val appInfo = pkg.applicationInfo ?: return@mapNotNull null
                val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0

                val sizeBytes: Long = try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val storageStats = context.getSystemService(
                            android.app.usage.StorageStatsManager::class.java
                        )
                        val uuid = appInfo.storageUuid
                        storageStats?.queryStatsForPackage(
                            uuid, pkg.packageName, android.os.Process.myUserHandle()
                        )?.appBytes ?: 0L
                    } else {
                        0L
                    }
                } catch (e: Exception) {
                    0L
                }

                InstalledAppInfo(
                    packageName = pkg.packageName,
                    appName     = pm.getApplicationLabel(appInfo).toString(),
                    sizeBytes   = sizeBytes,
                    isSystemApp = isSystem,
                    installedAt = pkg.firstInstallTime
                )
            } catch (e: Exception) {
                null
            }
        }.sortedByDescending { it.sizeBytes }
    }

    fun drawableToImageBitmap(drawable: Drawable): ImageBitmap {
        val width  = drawable.intrinsicWidth.coerceAtLeast(1)
        val height = drawable.intrinsicHeight.coerceAtLeast(1)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap.asImageBitmap()
    }

    fun getAppIcon(context: Context, packageName: String): ImageBitmap? {
        return try {
            val drawable = context.packageManager.getApplicationIcon(packageName)
            drawableToImageBitmap(drawable)
        } catch (e: Exception) {
            null
        }
    }
}
