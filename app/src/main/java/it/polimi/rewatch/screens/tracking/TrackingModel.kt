package it.polimi.rewatch.screens.tracking

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.polimi.rewatch.common.PersistRepository
import it.polimi.rewatch.common.TmdbRepository
import it.polimi.rewatch.common.UiState
import it.polimi.rewatch.model.WatchedMedia
import it.polimi.rewatch.model.WatchlistMedia
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class TrackingData(
    val watchlist: List<WatchlistMedia>,
    val watched: List<WatchedMedia>,
)

@HiltViewModel
@RequiresApi(Build.VERSION_CODES.O)
class TrackingViewModel @Inject constructor(
    private val tmdbRepo: TmdbRepository,
    private val persistRepo: PersistRepository,
) : ViewModel() {

    val watchlist = persistRepo.watchlistFlow
        .map { result ->
            result.fold(
                onSuccess = { list ->
                    UiState.Success(list.map { tmdbRepo.enrichWatched(it) })
                },
                onFailure = { error ->
                    UiState.Error(error.message ?: "Unknown error")
                },
            )
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            UiState.Loading, // <-- initial value is Loading, not empty list
        )

    // Optional: Keep track of loading/errors in a separate state
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    //   val _watchlist = fakeRepo.watchlist
//        .map { list -> list.map { tmdbRepo.enrichWatched(it) } }
//        .stateIn(
//            scope = viewModelScope,
//            started = SharingStarted.Lazily,
//            initialValue = emptyList(),
//        )
    val watchedList = persistRepo.watchedFlow
        .map { result ->
            result.getOrElse { error ->
                _errorMessage.value = error.message
                emptyList()
            }
        }
        .map { list -> list.map { tmdbRepo.enrichWatched(it) } }
        .stateIn(
            scope = viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList(),
        )
//    @RequiresApi(Build.VERSION_CODES.O)
    //  override suspend fun fetchData() = tmdbRepo.getTrackingData(fakeRepo)
}

