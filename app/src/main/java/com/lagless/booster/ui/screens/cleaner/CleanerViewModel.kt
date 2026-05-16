package com.lagless.booster.ui.screens.cleaner

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lagless.booster.data.model.AppCacheInfo
import com.lagless.booster.data.model.CleanupResult
import com.lagless.booster.data.model.JunkFileItem
import com.lagless.booster.data.model.ShizukuStatus
import com.lagless.booster.data.repository.CacheRepository
import com.lagless.booster.data.repository.CleanerRepository
import com.lagless.booster.data.shizuku.ShizukuManager
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ── State ─────────────────────────────────────────────────────

sealed class CleanerState {
    data object Idle : CleanerState()

    data class Scanning(
        val phase: String = "Scanning…"
    ) : CleanerState()

    data class Results(
        val junkItems          : List<JunkFileItem>,
        val appCaches          : List<AppCacheInfo>,
        val totalCacheBytes    : Long,
        val shizukuStatus      : ShizukuStatus,
        val cacheStatsAvailable: Boolean
    ) : CleanerState()

    data class Cleaning(
        val progress  : Float  = 0f,
        val phase     : String = "Preparing…",
        val bytesFreed: Long   = 0L
    ) : CleanerState()

    data class Done(val result: CleanupResult) : CleanerState()

    data class Error(val message: String) : CleanerState()
}

// ── ViewModel ─────────────────────────────────────────────────

class CleanerViewModel(application: Application) : AndroidViewModel(application) {

    private val cleanerRepo = CleanerRepository()
    private val cacheRepo   = CacheRepository(application)

    private val _state = MutableStateFlow<CleanerState>(CleanerState.Idle)
    val state: StateFlow<CleanerState> = _state.asStateFlow()

    // ── Scan ──────────────────────────────────────────────────

    fun scan() {
        viewModelScope.launch {
            _state.value = CleanerState.Scanning("Scanning for junk files…")
            try {
                val shizuku = ShizukuManager.computeStatus(getApplication())
                ShizukuManager.refreshStatus(getApplication())

                // Run both scans concurrently
                val junkDeferred  = async { cleanerRepo.scanJunkFiles() }
                val cacheDeferred = async { cacheRepo.scanAppCaches() }

                _state.value = CleanerState.Scanning("Analyzing app caches…")

                val junkFiles = junkDeferred.await()
                val appCaches = cacheDeferred.await()

                _state.value = CleanerState.Results(
                    junkItems           = junkFiles,
                    appCaches           = appCaches,
                    totalCacheBytes     = appCaches.sumOf { it.cacheBytes },
                    shizukuStatus       = shizuku,
                    cacheStatsAvailable = appCaches.isNotEmpty()
                )
            } catch (e: Exception) {
                _state.value = CleanerState.Error(e.message ?: "Scan failed")
            }
        }
    }

    // ── Item selection ────────────────────────────────────────

    fun toggleItem(item: JunkFileItem) {
        val current = _state.value as? CleanerState.Results ?: return
        _state.value = current.copy(
            junkItems = current.junkItems.map {
                if (it.file.absolutePath == item.file.absolutePath) it.copy(isSelected = !it.isSelected) else it
            }
        )
    }

    fun selectAll(selected: Boolean) {
        val current = _state.value as? CleanerState.Results ?: return
        _state.value = current.copy(junkItems = current.junkItems.map { it.copy(isSelected = selected) })
    }

    // ── Clean All (files + app cache trim if Shizuku) ─────────

    fun cleanAll() {
        val current   = _state.value as? CleanerState.Results ?: return
        val startTime = System.currentTimeMillis()

        viewModelScope.launch {
            _state.value = CleanerState.Cleaning(0f, "Preparing…", 0L)

            var filesDeleted    = 0
            var filesFailed     = 0
            var fileBytesFreed  = 0L
            var cacheBytesFreed = 0L

            // ── Phase 1: Delete selected junk files ───────────
            val toDelete = current.junkItems.filter { it.isSelected }
            if (toDelete.isNotEmpty()) {
                _state.value = CleanerState.Cleaning(0.15f, "Removing temp files and junk…", 0L)
                val (deleted, failed) = cleanerRepo.deleteFiles(toDelete)
                filesDeleted  = deleted
                filesFailed   = failed
                fileBytesFreed = estimateBytesFreed(toDelete, deleted, failed)
                _state.value  = CleanerState.Cleaning(0.48f, "Junk files cleared…", fileBytesFreed)
            } else {
                _state.value = CleanerState.Cleaning(0.48f, "No junk files to remove…", 0L)
                delay(300)
            }

            // ── Phase 2: Trim app caches via Shizuku ──────────
            if (current.shizukuStatus == ShizukuStatus.CONNECTED) {
                _state.value = CleanerState.Cleaning(0.62f, "Trimming app caches via Shizuku…", fileBytesFreed)
                val trimResult  = cacheRepo.trimAllCachesViaShizuku()
                cacheBytesFreed = trimResult.getOrDefault(0L)
                _state.value    = CleanerState.Cleaning(0.9f, "Finalizing…", fileBytesFreed + cacheBytesFreed)
            } else {
                _state.value = CleanerState.Cleaning(0.9f, "Finalizing…", fileBytesFreed)
            }

            delay(500) // Let progress bar animate to 0.9 before Done

            _state.value = CleanerState.Done(
                CleanupResult(
                    filesDeleted     = filesDeleted,
                    filesFailed      = filesFailed,
                    fileBytesFreed   = fileBytesFreed,
                    cacheAppsScanned = current.appCaches.size,
                    cacheBytesFreed  = cacheBytesFreed,
                    wasAdvancedMode  = current.shizukuStatus == ShizukuStatus.CONNECTED,
                    durationMs       = System.currentTimeMillis() - startTime
                )
            )
        }
    }

    /** Conservative mode: delete selected junk files only, no cache trimming. */
    fun deleteFilesOnly() {
        val current   = _state.value as? CleanerState.Results ?: return
        val startTime = System.currentTimeMillis()

        viewModelScope.launch {
            _state.value = CleanerState.Cleaning(0.1f, "Removing selected junk files…", 0L)
            val toDelete = current.junkItems.filter { it.isSelected }
            val (deleted, failed) = cleanerRepo.deleteFiles(toDelete)
            val freed = estimateBytesFreed(toDelete, deleted, failed)

            _state.value = CleanerState.Cleaning(0.9f, "Done.", freed)
            delay(400)

            _state.value = CleanerState.Done(
                CleanupResult(
                    filesDeleted     = deleted,
                    filesFailed      = failed,
                    fileBytesFreed   = freed,
                    cacheAppsScanned = 0,
                    cacheBytesFreed  = 0L,
                    wasAdvancedMode  = false,
                    durationMs       = System.currentTimeMillis() - startTime
                )
            )
        }
    }

    fun reset() { _state.value = CleanerState.Idle }

    // ── Helpers ───────────────────────────────────────────────

    private fun estimateBytesFreed(items: List<JunkFileItem>, deleted: Int, failed: Int): Long {
        val total = items.sumOf { it.sizeBytes }
        val count = deleted + failed
        return if (count > 0 && failed > 0) total * deleted / count else total
    }
}
