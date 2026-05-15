package com.lagless.booster.ui.screens.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lagless.booster.data.model.InstalledAppInfo
import com.lagless.booster.data.model.StorageInfo
import com.lagless.booster.data.repository.AppRepository
import com.lagless.booster.data.repository.StorageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val storageInfo: StorageInfo? = null,
    val junkEstimateBytes: Long = 0L,
    val unusedAppsCount: Int = 0,
    val biggestApp: InstalledAppInfo? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val storageRepo = StorageRepository()
    private val appRepo     = AppRepository(application)

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadDashboard()
    }

    fun loadDashboard() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val storage = storageRepo.getStorageInfo()
                val apps    = appRepo.getUserInstalledApps()
                val biggest = apps.maxByOrNull { it.sizeBytes }
                // Rough junk estimate based on free/used ratio
                val junkEstimate = (storage.usedBytes * 0.05f).toLong()
                _uiState.value = HomeUiState(
                    storageInfo       = storage,
                    junkEstimateBytes = junkEstimate,
                    unusedAppsCount   = 0, // Requires usage permission — shown as 0 by default
                    biggestApp        = biggest,
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
