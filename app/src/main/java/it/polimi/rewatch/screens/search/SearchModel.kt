package it.polimi.rewatch.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.polimi.rewatch.common.TmdbRepository
import it.polimi.rewatch.common.UiState
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.withContext

enum class SearchType(val title: String) {
    ALL("All"), MOVIES("Movies"), TV("Tv Shows"), CAST("People"),
}

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: TmdbRepository,
) : ViewModel() {
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()
    private val _mediaType = MutableStateFlow(SearchType.ALL)
    val mediaType: StateFlow<SearchType> = _mediaType.asStateFlow()

    /**
     * Reacts to query and type changes with a debounce to avoid excessive API calls.
     */
    val searchResults =
        combine(_query, _mediaType) { query, type -> query to type }
            .distinctUntilChanged()
            .transformLatest { (currentQuery, currentMediaType) ->
                if (currentQuery.isBlank()) {
                    emit(UiState.Success(emptyList()))
                } else {
                    // 1. INSTANT LOADING: Emits immediately before the delay
                    emit(UiState.Loading)
                    // 2. MANUAL DEBOUNCE: Suspend for 200ms.
                    // If the user types again before 200ms, 'transformLatest' cancels
                    // this block and starts over, instantly emitting Loading again.
                    delay(200L)
                    // 3. EXECUTE SEARCH: Only runs if the 200ms delay finishes uninterrupted.
                    try {
                        withContext(Dispatchers.IO) {
                            val results = repository.searchMedia(currentQuery, currentMediaType)
                            emit(UiState.Success(results))
                        }
                    } catch (e: Exception) {
                        emit(UiState.Error(e.message ?: "Search failed"))
                    }
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = UiState.Success(emptyList()), // Start with Success(emptyList()) based on your UX
            )

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
    }

    fun onTabChanged(newType: SearchType) {
        _mediaType.value = newType
    }

    fun reset() {
        _query.value = ""
    }
}