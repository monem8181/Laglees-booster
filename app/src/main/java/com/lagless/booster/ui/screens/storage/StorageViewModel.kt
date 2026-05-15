package com.lagless.booster.ui.screens.storage

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lagless.booster.data.model.FolderInfo
import com.lagless.booster.data.model.StorageInfo
import com.lagless.booster.data.repository.StorageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class StorageUiState(
    val storageInfo: StorageInfo? = null,
    val folders: List<FolderInfo> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class StorageViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = StorageRepository()
    private val _uiState = MutableStateFlow(StorageUiState())
    val uiState: StateFlow<StorageUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = StorageUiState(isLoading = true)
            try {
                val storage = repo.getStorageInfo()
                val folders = repo.getCommonFolders()
                _uiState.value = StorageUiState(
                    storageInfo = storage,
                    folders     = folders.sortedByDescending { it.sizeBytes },
                    isLoading   = false
                )
            } catch (e: Exception) {
                _uiState.value = StorageUiState(
                    isLoading = false,
                    error     = e.message ?: "Failed to load storage info"
                )
            }
        }
    }
}
