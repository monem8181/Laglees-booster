package com.lagless.booster.data.model

import java.io.File

// ── Storage ───────────────────────────────────────────────────

data class StorageInfo(
    val totalBytes: Long,
    val usedBytes : Long,
    val freeBytes : Long
) {
    val usedPercent: Float get() =
        if (totalBytes > 0) usedBytes.toFloat() / totalBytes.toFloat() else 0f
}

// ── Apps ──────────────────────────────────────────────────────

data class InstalledAppInfo(
    val packageName: String,
    val appName    : String,
    val sizeBytes  : Long,
    val isSystemApp: Boolean,
    val installedAt: Long,
    val lastUsed   : Long = 0L
)

data class AppUsageInfo(
    val packageName          : String,
    val appName              : String,
    val lastUsed             : Long,
    val totalTimeInForeground: Long
)

// ── Advanced: Per-app storage breakdown ──────────────────────

data class AppStorageStats(
    val packageName: String,
    val appBytes   : Long,
    val dataBytes  : Long,
    val cacheBytes : Long
) {
    val totalBytes: Long get() = appBytes + dataBytes + cacheBytes
}

// ── Advanced: Per-app cache info (for Cleaner) ───────────────

data class AppCacheInfo(
    val packageName: String,
    val appName    : String,
    val cacheBytes : Long,
    val totalBytes : Long
) {
    val cacheFraction: Float get() =
        if (totalBytes > 0) cacheBytes.toFloat() / totalBytes.toFloat() else 0f
}

// ── Advanced: Cleanup result ──────────────────────────────────

data class CleanupResult(
    val filesDeleted    : Int,
    val filesFailed     : Int,
    val fileBytesFreed  : Long,
    val cacheAppsScanned: Int,
    val cacheBytesFreed : Long,
    val wasAdvancedMode : Boolean,
    val durationMs      : Long
) {
    val totalBytesFreed: Long get() = fileBytesFreed + cacheBytesFreed
}

// ── Advanced: Sort order for App Manager ─────────────────────

enum class AppSortOrder(val label: String) {
    LARGEST_APP        ("App Size"),
    LARGEST_DATA       ("Data Size"),
    MOST_CACHE         ("Cache"),
    RECENTLY_INSTALLED ("Recently Installed"),
    NAME_AZ            ("Name A–Z")
}

// ── Advanced: Shizuku connection states ─────────────────────

enum class ShizukuStatus {
    NOT_INSTALLED,
    NOT_RUNNING,
    PERMISSION_DENIED,
    CONNECTED
}

// ── Junk / Cleaner ────────────────────────────────────────────

enum class JunkCategory(val label: String) {
    APK             ("Old APK Files"),
    TEMP            ("Temp Files"),
    LARGE_DOWNLOAD  ("Large Downloads"),
    EMPTY_FOLDER    ("Empty Folders"),
    ACCESSIBLE_CACHE("Cache-like Files")
}

data class JunkFileItem(
    val file       : File,
    val displayName: String,
    val sizeBytes  : Long,
    val category   : JunkCategory,
    val isSelected : Boolean = true
)

// ── Folders ───────────────────────────────────────────────────

data class FolderInfo(
    val name        : String,
    val path        : String,
    val sizeBytes   : Long,
    val fileCount   : Int,
    val isAccessible: Boolean = true
)

// ── Gamer Mode ────────────────────────────────────────────────

data class GameShortcut(
    val packageName: String,
    val appName    : String
)

// ── Optimization ─────────────────────────────────────────────

data class OptimizationTip(
    val title       : String,
    val description : String,
    val actionLabel : String? = null,
    val intentAction: String? = null
)

// ── Generic UI state ──────────────────────────────────────────

sealed class UiState<out T> {
    data object Idle    : UiState<Nothing>()
    data object Loading : UiState<Nothing>()
    data class  Success<T>(val data: T) : UiState<T>()
    data class  Error(val message: String) : UiState<Nothing>()
}
