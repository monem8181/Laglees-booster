package com.lagless.booster.ui.screens.home

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.os.BatteryManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lagless.booster.data.model.InstalledAppInfo
import com.lagless.booster.data.model.StorageInfo
import com.lagless.booster.data.repository.AppRepository
import com.lagless.booster.data.repository.StorageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class HomeUiState(
    val storageInfo      : StorageInfo? = null,
    val ramTotalBytes    : Long = 0L,
    val ramAvailBytes    : Long = 0L,
    val batteryLevel     : Int = -1,
    val isCharging       : Boolean = false,
    val junkEstimateBytes: Long = 0L,
    val unusedAppsCount  : Int = 0,
    val biggestApp       : InstalledAppInfo? = null,
    val totalUserApps    : Int = 0,
    val isLoading        : Boolean = true,
    val error            : String? = null
) {
    val ramUsedBytes    : Long  get() = (ramTotalBytes - ramAvailBytes).coerceAtLeast(0L)
    val ramUsedPercent  : Float get() = if (ramTotalBytes > 0) ramUsedBytes.toFloat() / ramTotalBytes else 0f
}

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val storageRepo = StorageRepository()
    private val appRepo     = AppRepository(application)

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init { loadDashboard() }

    fun loadDashboard() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val storage = storageRepo.getStorageInfo()
                val apps    = appRepo.getUserInstalledApps()
                val biggest = apps.maxByOrNull { it.sizeBytes }

                // Real RAM stats via ActivityManager
                val (ramTotal, ramAvail) = withContext(Dispatchers.IO) {
                    val am      = getApplication<Application>().getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
                    val memInfo = ActivityManager.MemoryInfo()
                    am?.getMemoryInfo(memInfo)
                    (memInfo.totalMem) to (memInfo.availMem)
                }

                // Real battery info via BatteryManager
                val (battLevel, charging) = withContext(Dispatchers.Default) {
                    val bm       = getApplication<Application>().getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
                    val level    = bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: -1
                    val isCharge = bm?.isCharging ?: false
                    level to isCharge
                }

                _uiState.value = HomeUiState(
                    storageInfo       = storage,
                    ramTotalBytes     = ramTotal,
                    ramAvailBytes     = ramAvail,
                    batteryLevel      = battLevel,
                    isCharging        = charging,
                    junkEstimateBytes = (storage.usedBytes * 0.04f).toLong(),
                    unusedAppsCount   = 0,
                    biggestApp        = biggest,
                    totalUserApps     = apps.size,
                    isLoading         = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error     = e.message ?: "Failed to load dashboard"
                )
            }
        }
    }
}
