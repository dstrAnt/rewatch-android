package it.polimi.rewatch.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.polimi.rewatch.common.TmdbRepository
import jakarta.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

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
            .debounce { (query, _) -> if (query.isEmpty()) 0L else 200L }
            .distinctUntilChanged().flatMapLatest { (currentQuery, currentMediaType) ->
                if (currentQuery.isBlank()) {
                    flowOf(emptyList())
                } else {
                    flow {
                        emit(repository.searchMedia(currentQuery, currentMediaType))
                    }
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList(),
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