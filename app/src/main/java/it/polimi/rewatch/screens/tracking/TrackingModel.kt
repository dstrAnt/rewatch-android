package it.polimi.rewatch.screens.tracking

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.polimi.rewatch.common.BaseViewModel
import it.polimi.rewatch.common.FakeRepository
import it.polimi.rewatch.common.FirebaseUserRepository
import it.polimi.rewatch.common.TmdbRepository
import it.polimi.rewatch.model.WatchedMedia
import it.polimi.rewatch.model.WatchlistMedia
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TrackingData(
    val watchlist: List<WatchlistMedia>,
    val watched: List<WatchedMedia>,
)

@HiltViewModel
@RequiresApi(Build.VERSION_CODES.O)
class TrackingViewModel @Inject constructor(
    private val tmdbRepo: TmdbRepository,
    private val fakeRepo: FakeRepository,
    val fbRepo: FirebaseUserRepository,
) : BaseViewModel<TrackingData>() {
    private val _watchlist = MutableStateFlow<List<WatchlistMedia>>(emptyList())
    val watchlist: StateFlow<List<WatchlistMedia>> = _watchlist.asStateFlow()

    // Optional: Keep track of loading/errors in a separate state
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    init {
        observeWatchlist()
    }

    private fun observeWatchlist() {
        viewModelScope.launch {
            Log.d("WatchlistVM", "Starting to observe watchlist...")

            fbRepo.getWatchlistFlow().collect { result ->
                result.onSuccess { movies ->
                    Log.d("WatchlistVM", "Success! Received ${movies.size} movies from Firestore.")
                    _watchlist.value = movies.map { tmdbRepo.enrichWatched(it) }
                    _errorMessage.value = null
                }.onFailure { error ->
                    Log.e("WatchlistVM", "Error fetching watchlist", error)
                    _errorMessage.value = error.message
                }
            }
        }
    }

    //   val _watchlist = fakeRepo.watchlist
//        .map { list -> list.map { tmdbRepo.enrichWatched(it) } }
//        .stateIn(
//            scope = viewModelScope,
//            started = SharingStarted.Lazily,
//            initialValue = emptyList(),
//        )
    val watchedList = fakeRepo.watchedList
        .map { list -> list.map { tmdbRepo.enrichWatched(it) } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = emptyList(),
        )

    init {
        executeFetch()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun fetchData() = tmdbRepo.getTrackingData(fakeRepo)
}

