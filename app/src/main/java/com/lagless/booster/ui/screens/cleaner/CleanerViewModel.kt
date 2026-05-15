package com.lagless.booster.ui.screens.cleaner

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lagless.booster.data.model.JunkFileItem
import com.lagless.booster.data.repository.CleanerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class CleanerState {
    data object Idle : CleanerState()
    data object Scanning : CleanerState()
    data class Results(val items: List<JunkFileItem>) : CleanerState()
    data object Deleting : CleanerState()
    data class Done(val deleted: Int, val failed: Int) : CleanerState()
    data class Error(val message: String) : CleanerState()
}

class CleanerViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = CleanerRepository()

    private val _state = MutableStateFlow<CleanerState>(CleanerState.Idle)
    val state: StateFlow<CleanerState> = _state.asStateFlow()

    fun scan() {
        viewModelScope.launch {
            _state.value = CleanerState.Scanning
            try {
                val items = repo.scanJunkFiles()
                _state.value = CleanerState.Results(items)
            } catch (e: Exception) {
                _state.value = CleanerState.Error(e.message ?: "Scan failed")
            }
        }
    }

    fun toggleItem(item: JunkFileItem) {
        val current = _state.value as? CleanerState.Results ?: return
        val updated = current.items.map {
            if (it.file.absolutePath == item.file.absolutePath) it.copy(isSelected = !it.isSelected) else it
        }
        _state.value = CleanerState.Results(updated)
    }

    fun selectAll(selected: Boolean) {
        val current = _state.value as? CleanerState.Results ?: return
        _state.value = CleanerState.Results(current.items.map { it.copy(isSelected = selected) })
    }

    fun deleteSelected() {
        val current = _state.value as? CleanerState.Results ?: return
        val toDelete = current.items.filter { it.isSelected }
        viewModelScope.launch {
            _state.value = CleanerState.Deleting
            val (deleted, failed) = repo.deleteFiles(toDelete)
            _state.value = CleanerState.Done(deleted, failed)
        }
    }

    fun reset() { _state.value = CleanerState.Idle }
}
