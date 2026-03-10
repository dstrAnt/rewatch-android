package it.polimi.rewatch.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.polimi.rewatch.common.TmdbRepository
import it.polimi.rewatch.screens.media.Media
import it.polimi.rewatch.screens.media.MediaType
import jakarta.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: TmdbRepository,
) : ViewModel() {    // Initialize TMDB client here (or inject via Hilt/Koin in a real app)
    // Backing property for the search text
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    // Automatically reacts to query changes, debounces, and fetches movies
    @OptIn(ExperimentalCoroutinesApi::class)
    val media: StateFlow<List<Media>> = _query
//        .debounce(0) // Wait 300ms after the user stops typing
        .mapLatest { currentQuery ->
            // The ViewModel doesn't care how the search works, it just asks the Repo!
            repository.searchMedia(currentQuery, MediaType.MOVIE)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = emptyList(),
        )

    // Called from the UI when the user types
    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
    }
}