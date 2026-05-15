package com.lagless.booster.utils.storage

import android.os.Environment
import android.os.StatFs
import com.lagless.booster.data.model.FolderInfo
import com.lagless.booster.data.model.StorageInfo
import java.io.File

object StorageUtils {

    fun getStorageInfo(): StorageInfo {
        val path = Environment.getDataDirectory()
        val stat = StatFs(path.absolutePath)
        val blockSize = stat.blockSizeLong
        val totalBlocks = stat.blockCountLong
        val availableBlocks = stat.availableBlocksLong
        val total = blockSize * totalBlocks
        val free = blockSize * availableBlocks
        val used = total - free
        return StorageInfo(
            totalBytes = total,
            usedBytes  = used,
            freeBytes  = free
        )
    }

    fun getCommonFolders(): List<FolderInfo> {
        val externalStorage = Environment.getExternalStorageDirectory()
        val commonPaths = listOf(
            "Download"    to "Downloads",
            "DCIM"        to "Camera / DCIM",
            "Pictures"    to "Pictures",
            "Movies"      to "Movies",
            "Music"       to "Music",
            "Documents"   to "Documents",
            "WhatsApp"    to "WhatsApp",
            "Telegram"    to "Telegram",
            "Android/data" to "App Data (Android/data)"
        )
        return commonPaths.mapNotNull { (subPath, displayName) ->
            val folder = File(externalStorage, subPath)
            if (folder.exists()) {
                val (size, count) = try {
                    getFolderSizeAndCount(folder)
                } catch (e: SecurityException) {
                    0L to 0
                }
                FolderInfo(
                    name         = displayName,
                    path         = folder.absolutePath,
                    sizeBytes    = size,
                    fileCount    = count,
                    isAccessible = folder.canRead()
                )
            } else {
                FolderInfo(
                    name         = displayName,
                    path         = File(externalStorage, subPath).absolutePath,
                    sizeBytes    = 0L,
                    fileCount    = 0,
                    isAccessible = false
                )
            }
        }
    }

    fun getFolderSizeAndCount(folder: File): Pair<Long, Int> {
        var size = 0L
        var count = 0
        folder.walkTopDown()
            .onEnter { it.canRead() }
            .forEach { file ->
                if (file.isFile) {
                    size += file.length()
                    count++
                }
            }
        return size to count
    }

    fun getDownloadDirectory(): File =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
}
