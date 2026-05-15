package com.lagless.booster.ui.screens.folders

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lagless.booster.data.model.FolderInfo
import com.lagless.booster.data.repository.StorageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FolderScannerUiState(
    val folders: List<FolderInfo> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class FolderScannerViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = StorageRepository()

    private val _uiState = MutableStateFlow(FolderScannerUiState())
    val uiState: StateFlow<FolderScannerUiState> = _uiState.asStateFlow()

    init { scan() }

    fun scan() {
        viewModelScope.launch {
            _uiState.value = FolderScannerUiState(isLoading = true)
            try {
                val folders = repo.getCommonFolders()
                _uiState.value = FolderScannerUiState(
                    folders   = folders.sortedByDescending { it.sizeBytes },
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = FolderScannerUiState(
                    isLoading = false,
                    error     = e.message ?: "Scan failed"
                )
            }
        }
    }
}
