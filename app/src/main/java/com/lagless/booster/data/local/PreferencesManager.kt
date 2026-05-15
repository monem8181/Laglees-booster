package com.lagless.booster.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "lagless_prefs")

class PreferencesManager(private val context: Context) {

    companion object {
        private val KEY_FAVORITE_GAME_PACKAGE = stringPreferencesKey("favorite_game_package")
        private val KEY_FAVORITE_GAME_NAME    = stringPreferencesKey("favorite_game_name")
    }

    val favoriteGamePackage: Flow<String> = context.dataStore.data
        .map { prefs -> prefs[KEY_FAVORITE_GAME_PACKAGE] ?: "" }

    val favoriteGameName: Flow<String> = context.dataStore.data
        .map { prefs -> prefs[KEY_FAVORITE_GAME_NAME] ?: "" }

    suspend fun saveFavoriteGame(packageName: String, appName: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_FAVORITE_GAME_PACKAGE] = packageName
            prefs[KEY_FAVORITE_GAME_NAME]    = appName
        }
    }

    suspend fun clearFavoriteGame() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_FAVORITE_GAME_PACKAGE)
            prefs.remove(KEY_FAVORITE_GAME_NAME)
        }
    }
}
