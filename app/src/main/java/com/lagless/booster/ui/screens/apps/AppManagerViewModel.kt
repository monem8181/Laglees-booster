package com.lagless.booster.ui.screens.apps

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lagless.booster.data.model.InstalledAppInfo
import com.lagless.booster.data.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AppManagerUiState(
    val allApps: List<InstalledAppInfo> = emptyList(),
    val filteredApps: List<InstalledAppInfo> = emptyList(),
    val query: String = "",
    val showSystemApps: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

class AppManagerViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = AppRepository(application)

    private val _uiState = MutableStateFlow(AppManagerUiState())
    val uiState: StateFlow<AppManagerUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val apps = repo.getInstalledApps()
                val current = _uiState.value
                _uiState.value = current.copy(
                    allApps      = apps,
                    filteredApps = applyFilter(apps, current.query, current.showSystemApps),
                    isLoading    = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error     = e.message ?: "Failed to load apps"
                )
            }
        }
    }

    fun onQueryChange(query: String) {
        val current = _uiState.value
        _uiState.value = current.copy(
            query        = query,
            filteredApps = applyFilter(current.allApps, query, current.showSystemApps)
        )
    }

    fun toggleSystemApps() {
        val current = _uiState.value
        val show    = !current.showSystemApps
        _uiState.value = current.copy(
            showSystemApps = show,
            filteredApps   = applyFilter(current.allApps, current.query, show)
        )
    }

    private fun applyFilter(
        apps: List<InstalledAppInfo>,
        query: String,
        showSystem: Boolean
    ): List<InstalledAppInfo> {
        return apps
            .filter { if (showSystem) true else !it.isSystemApp }
            .filter { query.isBlank() || it.appName.contains(query, ignoreCase = true) }
            .sortedByDescending { it.sizeBytes }
    }
}
