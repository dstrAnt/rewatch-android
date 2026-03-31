package it.polimi.rewatch.common

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing> // Use 'object Loading' if your Kotlin version is older than 1.9
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
            val data = withContext(Dispatchers.IO) { fetchData() }
            UiState.Success(data)
        } catch (e: Exception) {
            UiState.Error(e.localizedMessage ?: "Errore di connessione")
        }
    }

    /**
     * Child ViewModels must implement this to define HOW they get their specific data.
     */
    abstract suspend fun fetchData(): T
}