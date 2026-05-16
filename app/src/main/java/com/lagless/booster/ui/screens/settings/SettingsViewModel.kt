package com.lagless.booster.ui.screens.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lagless.booster.data.local.PreferencesManager
import com.lagless.booster.data.model.ShizukuStatus
import com.lagless.booster.data.shizuku.ShizukuManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val shizukuStatus: ShizukuStatus = ShizukuStatus.NOT_INSTALLED
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = PreferencesManager(application)

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init { refreshShizuku() }

    fun refreshShizuku() {
        _uiState.value = _uiState.value.copy(
            shizukuStatus = ShizukuManager.computeStatus(getApplication())
        )
    }

    fun clearFavoriteGame() {
        viewModelScope.launch { prefs.clearFavoriteGame() }
    }
}
