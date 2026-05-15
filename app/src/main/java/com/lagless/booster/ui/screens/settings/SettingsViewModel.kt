package com.lagless.booster.ui.screens.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lagless.booster.data.local.PreferencesManager
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = PreferencesManager(application)

    fun clearFavoriteGame() {
        viewModelScope.launch { prefs.clearFavoriteGame() }
    }
}
