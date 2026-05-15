package com.lagless.booster.data.model

import java.io.File

// ── Storage ───────────────────────────────────────────────────

data class StorageInfo(
    val totalBytes: Long,
    val usedBytes: Long,
    val freeBytes: Long
) {
    val usedPercent: Float get() =
        if (totalBytes > 0) usedBytes.toFloat() / totalBytes.toFloat() else 0f
}

// ── Apps ──────────────────────────────────────────────────────

data class InstalledAppInfo(
    val packageName: String,
    val appName: String,
    val sizeBytes: Long,
    val isSystemApp: Boolean,
    val installedAt: Long,
    val lastUsed: Long = 0L
)

data class AppUsageInfo(
    val packageName: String,
    val appName: String,
    val lastUsed: Long,
    val totalTimeInForeground: Long
)

// ── Junk / Cleaner ────────────────────────────────────────────

enum class JunkCategory(val label: String) {
    APK("Old APK Files"),
    TEMP("Temp Files"),
    LARGE_DOWNLOAD("Large Downloads"),
    EMPTY_FOLDER("Empty Folders"),
    ACCESSIBLE_CACHE("Cache-like Files")
}

data class JunkFileItem(
    val file: File,
    val displayName: String,
    val sizeBytes: Long,
    val category: JunkCategory,
    val isSelected: Boolean = true
)

// ── Folders ───────────────────────────────────────────────────

data class FolderInfo(
    val name: String,
    val path: String,
    val sizeBytes: Long,
    val fileCount: Int,
    val isAccessible: Boolean = true
)

// ── Gamer Mode ────────────────────────────────────────────────

data class GameShortcut(
    val packageName: String,
    val appName: String
)

// ── Optimization ─────────────────────────────────────────────

data class OptimizationTip(
    val title: String,
    val description: String,
    val actionLabel: String? = null,
    val intentAction: String? = null
)

// ── Generic UI state ──────────────────────────────────────────

sealed class UiState<out T> {
    data object Idle : UiState<Nothing>()
    data object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}
