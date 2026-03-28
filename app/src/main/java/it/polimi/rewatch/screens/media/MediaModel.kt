package it.polimi.rewatch.screens.media

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.polimi.rewatch.common.FakeRepository
import it.polimi.rewatch.common.TmdbRepository
import it.polimi.rewatch.model.MediaDetail
import it.polimi.rewatch.model.MediaId
import it.polimi.rewatch.model.PosterMedia
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class MovieData(
    var movie: MediaDetail, var similar: List<PosterMedia>,
)

@HiltViewModel
class MediaViewModel @Inject constructor(
    private val repository: TmdbRepository,
    fakeRepo: FakeRepository,
) : ViewModel() { // <-- Changed to your unified MediaDetail class
    private val _data = MutableStateFlow<MovieData?>(null)
    val data = _data.asStateFlow()

    // Keep track of the current ID so we don't accidentally reload
    // if the screen just rotates or recomposes
    private var currentMediaId: MediaId? = null
    fun loadMedia(id: MediaId) {
        // 1. If we are already looking at this movie, do nothing
        if (currentMediaId == id) return
        currentMediaId = id
        // 2. INSTANTLY wipe the old data.
        // This pushes 'null' to the UI, triggering your MovieSkeleton!
        _data.value = null
        // 3. Fetch the new data
        viewModelScope.launch {
            // Assume getDetails fetches your specific movie
            _data.value = withContext(Dispatchers.IO) { id.getData(repository) }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    val watchedListItem = fakeRepo.watchedList
        .map { list -> list.find { it.id == currentMediaId } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null,
        )

    @RequiresApi(Build.VERSION_CODES.O)
    val watchlistItem = fakeRepo.watchlist
        .map { list -> list.find { it.id == currentMediaId } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null,
        )
}