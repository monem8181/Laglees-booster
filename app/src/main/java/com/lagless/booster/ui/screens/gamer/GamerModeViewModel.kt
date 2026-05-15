package com.lagless.booster.ui.screens.gamer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lagless.booster.data.local.PreferencesManager
import com.lagless.booster.data.model.InstalledAppInfo
import com.lagless.booster.data.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class GamerModeUiState(
    val favoriteGamePackage: String = "",
    val favoriteGameName: String    = "",
    val recentGames: List<InstalledAppInfo> = emptyList(),
    val allUserApps: List<InstalledAppInfo> = emptyList(),
    val isLoading: Boolean = false
)

class GamerModeViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs   = PreferencesManager(application)
    private val appRepo = AppRepository(application)

    private val _uiState = MutableStateFlow(GamerModeUiState())
    val uiState: StateFlow<GamerModeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val games    = try { appRepo.getRecentlyUsedGames() } catch (e: Exception) { emptyList() }
            val userApps = try { appRepo.getUserInstalledApps() } catch (e: Exception) { emptyList() }
            combine(prefs.favoriteGamePackage, prefs.favoriteGameName) { pkg, name -> pkg to name }
                .collect { (pkg, name) ->
                    _uiState.value = GamerModeUiState(
                        favoriteGamePackage = pkg,
                        favoriteGameName    = name,
                        recentGames         = games,
                        allUserApps         = userApps,
                        isLoading           = false
                    )
                }
        }
    }

    fun setFavoriteGame(packageName: String, appName: String) {
        viewModelScope.launch {
            prefs.saveFavoriteGame(packageName, appName)
        }
    }

    fun clearFavoriteGame() {
        viewModelScope.launch { prefs.clearFavoriteGame() }
    }
}
