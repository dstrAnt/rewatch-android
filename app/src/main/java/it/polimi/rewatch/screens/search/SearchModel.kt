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
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: TmdbRepository,
) : ViewModel() {    // Initialize TMDB client here (or inject via Hilt/Koin in a real app)
    // Backing property for the search text
    private val _query = MutableStateFlow("")
    val query = _query.asStateFlow()
    private val _mediaType = MutableStateFlow(SearchType.MOVIES)
    val mediaType = _mediaType.asStateFlow()

    // Public immutable state that the Composable will observe
    // Function to update the state
    fun onTabChanged(newText: SearchType) {
        _mediaType.value = newText
    }

    // Automatically reacts to query changes, debounces, and fetches movies
    @OptIn(ExperimentalCoroutinesApi::class)
    val media = combine(_query, _mediaType, transform = ::Pair)
        .mapLatest { (currentQuery, currentMediaType) ->
        // 2. mapLatest receives the updated Pair.
        // It will cancel the previous search if a new query or type comes in quickly!
        repository.searchMedia(currentQuery, currentMediaType)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = emptyList(),
    )

    // Called from the UI when the user types
    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
    }
}