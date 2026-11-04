package it.polimi.rewatch.common

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val msg: String) : UiState<Nothing>
}

abstract class UiFetchViewModel<T> : ViewModel() {
    var uiState by mutableStateOf<UiState<T>>(UiState.Loading)
        protected set // Allows the base class to change it, but keeps it read-only for the UI

    /**
     * Reusable fetch method that handles the Loading and Error states automatically.
     */
    protected fun executeFetch() = viewModelScope.launch {
        uiState = UiState.Loading
        uiState = try {
            val data = fetchData()
            UiState.Success(data)
        } catch (e: Exception) {
            UiState.Error(e.localizedMessage ?: "Connection error")
        }
    }

    /**
     * Child ViewModels must implement this to define HOW they get their specific data.
     */
    abstract suspend fun fetchData(): T
}
