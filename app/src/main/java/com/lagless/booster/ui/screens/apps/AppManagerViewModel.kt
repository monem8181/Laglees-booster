package com.lagless.booster.ui.screens.apps

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lagless.booster.data.model.AppSortOrder
import com.lagless.booster.data.model.AppStorageStats
import com.lagless.booster.data.model.InstalledAppInfo
import com.lagless.booster.data.model.ShizukuStatus
import com.lagless.booster.data.repository.AppRepository
import com.lagless.booster.data.shizuku.ShizukuManager
import com.lagless.booster.data.storagestats.StorageStatsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AppManagerUiState(
    val allApps              : List<InstalledAppInfo>       = emptyList(),
    val filteredApps         : List<InstalledAppInfo>       = emptyList(),
    val storageStats         : Map<String, AppStorageStats> = emptyMap(),
    val query                : String                       = "",
    val showSystemApps       : Boolean                      = false,
    val sortOrder            : AppSortOrder                 = AppSortOrder.LARGEST_APP,
    val shizukuStatus        : ShizukuStatus                = ShizukuStatus.NOT_INSTALLED,
    val isLoading            : Boolean                      = false,
    val isLoadingStorage     : Boolean                      = false,
    val storageStatsAvailable: Boolean                      = false,
    val forceStopTarget      : InstalledAppInfo?            = null,
    val forceStopResult      : String?                      = null,
    val error                : String?                      = null
)

class AppManagerViewModel(application: Application) : AndroidViewModel(application) {

    private val repo      = AppRepository(application)
    private val statsRepo = StorageStatsRepository(application)

    private val _uiState = MutableStateFlow(AppManagerUiState())
    val uiState: StateFlow<AppManagerUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val shizuku = ShizukuManager.computeStatus(getApplication())
            ShizukuManager.refreshStatus(getApplication())
            try {
                val apps    = repo.getInstalledApps()
                val current = _uiState.value
                _uiState.value = current.copy(
                    allApps       = apps,
                    filteredApps  = applyFilter(apps, current.query, current.showSystemApps, current.sortOrder, current.storageStats),
                    shizukuStatus = shizuku,
                    isLoading     = false
                )
                loadStorageStats(apps.map { it.packageName })
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error     = e.message ?: "Failed to load apps"
                )
            }
        }
    }

    fun refreshShizukuStatus() {
        ShizukuManager.refreshStatus(getApplication())
        _uiState.value = _uiState.value.copy(
            shizukuStatus = ShizukuManager.computeStatus(getApplication())
        )
    }

    fun requestShizukuPermission() {
        ShizukuManager.requestPermission()
        refreshShizukuStatus()
    }

    private fun loadStorageStats(packages: List<String>) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingStorage = true)
            val stats = statsRepo.getStorageStatsForAll(packages)
            val current = _uiState.value
            _uiState.value = current.copy(
                storageStats          = stats,
                filteredApps          = applyFilter(current.allApps, current.query, current.showSystemApps, current.sortOrder, stats),
                isLoadingStorage      = false,
                storageStatsAvailable = stats.isNotEmpty()
            )
        }
    }

    fun onQueryChange(q: String) {
        val c = _uiState.value
        _uiState.value = c.copy(query = q, filteredApps = applyFilter(c.allApps, q, c.showSystemApps, c.sortOrder, c.storageStats))
    }

    fun toggleSystemApps() {
        val c    = _uiState.value
        val show = !c.showSystemApps
        _uiState.value = c.copy(
            showSystemApps = show,
            filteredApps   = applyFilter(c.allApps, c.query, show, c.sortOrder, c.storageStats)
        )
    }

    fun setSortOrder(order: AppSortOrder) {
        val c = _uiState.value
        _uiState.value = c.copy(
            sortOrder    = order,
            filteredApps = applyFilter(c.allApps, c.query, c.showSystemApps, order, c.storageStats)
        )
    }

    // ── Force Stop (Advanced Mode only) ───────────────────────

    fun promptForceStop(app: InstalledAppInfo) {
        _uiState.value = _uiState.value.copy(forceStopTarget = app)
    }

    fun cancelForceStop() {
        _uiState.value = _uiState.value.copy(forceStopTarget = null)
    }

    fun confirmForceStop() {
        val target = _uiState.value.forceStopTarget ?: return
        _uiState.value = _uiState.value.copy(forceStopTarget = null)
        viewModelScope.launch {
            val result = ShizukuManager.forceStopApp(target.packageName)
            _uiState.value = _uiState.value.copy(
                forceStopResult = if (result.isSuccess)
                    "${target.appName} was force-stopped."
                else
                    "Force stop failed: ${result.exceptionOrNull()?.message}"
            )
        }
    }

    fun clearForceStopResult() {
        _uiState.value = _uiState.value.copy(forceStopResult = null)
    }

    // ── Filter + sort ─────────────────────────────────────────

    private fun applyFilter(
        apps      : List<InstalledAppInfo>,
        query     : String,
        showSystem: Boolean,
        sortOrder : AppSortOrder,
        stats     : Map<String, AppStorageStats>
    ): List<InstalledAppInfo> {
        val filtered = apps
            .filter { if (showSystem) true else !it.isSystemApp }
            .filter { query.isBlank() || it.appName.contains(query, ignoreCase = true) }
        return when (sortOrder) {
            AppSortOrder.LARGEST_APP        -> filtered.sortedByDescending { stats[it.packageName]?.appBytes ?: it.sizeBytes }
            AppSortOrder.LARGEST_DATA       -> filtered.sortedByDescending { stats[it.packageName]?.dataBytes ?: 0L }
            AppSortOrder.MOST_CACHE         -> filtered.sortedByDescending { stats[it.packageName]?.cacheBytes ?: 0L }
            AppSortOrder.RECENTLY_INSTALLED -> filtered.sortedByDescending { it.installedAt }
            AppSortOrder.NAME_AZ            -> filtered.sortedBy { it.appName.lowercase() }
        }
    }
}
