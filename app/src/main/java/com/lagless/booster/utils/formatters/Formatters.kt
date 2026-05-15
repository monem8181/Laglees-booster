package com.lagless.booster.utils.formatters

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Formatters {

    fun formatBytes(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        var value = bytes.toDouble()
        var unitIndex = 0
        while (value >= 1024 && unitIndex < units.size - 1) {
            value /= 1024.0
            unitIndex++
        }
        return if (unitIndex == 0) "${value.toLong()} ${units[unitIndex]}"
        else "%.1f %s".format(value, units[unitIndex])
    }

    fun formatDate(timestamp: Long): String {
        if (timestamp <= 0L) return "Never"
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatRelativeTime(timestamp: Long): String {
        if (timestamp <= 0L) return "Never used"
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        val minutes = diff / 60_000
        val hours = diff / 3_600_000
        val days = diff / 86_400_000
        val weeks = days / 7
        val months = days / 30
        return when {
            minutes < 1    -> "Just now"
            minutes < 60   -> "${minutes}m ago"
            hours < 24     -> "${hours}h ago"
            days < 7       -> "${days}d ago"
            weeks < 5      -> "${weeks}w ago"
            else           -> "${months}mo ago"
        }
    }

    fun formatPercent(value: Float): String = "%.0f%%".format(value * 100f)
}
