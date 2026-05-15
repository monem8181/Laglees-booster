package com.lagless.booster.ui.screens.unused

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lagless.booster.data.model.AppUsageInfo
import com.lagless.booster.data.repository.AppRepository
import com.lagless.booster.utils.permissions.PermissionUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class UnusedAppsUiState(
    val hasPermission: Boolean = false,
    val apps: List<AppUsageInfo> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class UnusedAppsViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = AppRepository(application)
    private val ctx  = application

    private val _uiState = MutableStateFlow(UnusedAppsUiState())
    val uiState: StateFlow<UnusedAppsUiState> = _uiState.asStateFlow()

    fun checkPermissionAndLoad() {
        val hasPerm = PermissionUtils.hasUsageStatsPermission(ctx)
        _uiState.value = _uiState.value.copy(hasPermission = hasPerm)
        if (hasPerm) load()
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val apps = repo.getUnusedApps(thresholdDays = 30)
                _uiState.value = _uiState.value.copy(apps = apps, isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error     = e.message ?: "Failed to load usage data"
                )
            }
        }
    }
}
