package com.lagless.booster.data.repository

import android.os.Environment
import com.lagless.booster.data.model.JunkCategory
import com.lagless.booster.data.model.JunkFileItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class CleanerRepository {

    suspend fun scanJunkFiles(): List<JunkFileItem> = withContext(Dispatchers.IO) {
        val results = mutableListOf<JunkFileItem>()
        val externalStorage = Environment.getExternalStorageDirectory()

        // 1 — Old APKs in Downloads
        scanApkFiles(externalStorage, results)

        // 2 — Large files in Downloads (> 50 MB)
        scanLargeDownloads(externalStorage, results)

        // 3 — Empty folders
        scanEmptyFolders(externalStorage, results)

        // 4 — Temp / .tmp files
        scanTempFiles(externalStorage, results)

        results.sortedByDescending { it.sizeBytes }
    }

    private fun scanApkFiles(root: File, out: MutableList<JunkFileItem>) {
        val downloadDir = File(root, "Download")
        if (!downloadDir.exists() || !downloadDir.canRead()) return
        downloadDir.listFiles()?.forEach { file ->
            if (file.isFile && file.extension.equals("apk", ignoreCase = true)) {
                out.add(
                    JunkFileItem(
                        file        = file,
                        displayName = file.name,
                        sizeBytes   = file.length(),
                        category    = JunkCategory.APK
                    )
                )
            }
        }
    }

    private fun scanLargeDownloads(root: File, out: MutableList<JunkFileItem>) {
        val downloadDir = File(root, "Download")
        if (!downloadDir.exists() || !downloadDir.canRead()) return
        val threshold = 50L * 1024 * 1024 // 50 MB
        downloadDir.listFiles()?.forEach { file ->
            if (file.isFile && file.length() > threshold &&
                !file.extension.equals("apk", ignoreCase = true)
            ) {
                out.add(
                    JunkFileItem(
                        file        = file,
                        displayName = file.name,
                        sizeBytes   = file.length(),
                        category    = JunkCategory.LARGE_DOWNLOAD
                    )
                )
            }
        }
    }

    private fun scanEmptyFolders(root: File, out: MutableList<JunkFileItem>) {
        val dirsToCheck = listOf(
            File(root, "Download"),
            File(root, "Pictures"),
            File(root, "Movies"),
            File(root, "Documents")
        )
        dirsToCheck.forEach { dir ->
            if (!dir.exists() || !dir.canRead()) return@forEach
            dir.walkTopDown()
                .onEnter { it.canRead() }
                .filter { it.isDirectory && it != dir }
                .filter { folder -> folder.listFiles()?.isEmpty() == true }
                .take(20)
                .forEach { emptyFolder ->
                    out.add(
                        JunkFileItem(
                            file        = emptyFolder,
                            displayName = emptyFolder.name + "/",
                            sizeBytes   = 0L,
                            category    = JunkCategory.EMPTY_FOLDER
                        )
                    )
                }
        }
    }

    private fun scanTempFiles(root: File, out: MutableList<JunkFileItem>) {
        val dirsToCheck = listOf(
            File(root, "Download"),
            File(root, "Documents")
        )
        val tempExtensions = setOf("tmp", "temp", "bak", "log", "crdownload", "part")
        dirsToCheck.forEach { dir ->
            if (!dir.exists() || !dir.canRead()) return@forEach
            dir.walkTopDown()
                .onEnter { it.canRead() }
                .filter { it.isFile && it.extension.lowercase() in tempExtensions }
                .take(30)
                .forEach { file ->
                    out.add(
                        JunkFileItem(
                            file        = file,
                            displayName = file.name,
                            sizeBytes   = file.length(),
                            category    = JunkCategory.TEMP
                        )
                    )
                }
        }
    }

    suspend fun deleteFiles(items: List<JunkFileItem>): Pair<Int, Int> =
        withContext(Dispatchers.IO) {
            var deleted = 0
            var failed  = 0
            items.forEach { item ->
                try {
                    val success = if (item.file.isDirectory) {
                        item.file.deleteRecursively()
                    } else {
                        item.file.delete()
                    }
                    if (success) deleted++ else failed++
                } catch (e: SecurityException) {
                    failed++
                }
            }
            deleted to failed
        }
}
