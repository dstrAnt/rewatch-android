package it.polimi.rewatch.screens.tracking

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.polimi.rewatch.common.BaseViewModel
import it.polimi.rewatch.common.FakeRepository
import it.polimi.rewatch.common.TmdbRepository
import it.polimi.rewatch.model.WatchedMedia
import it.polimi.rewatch.model.WatchlistMedia
import jakarta.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
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
    private val fakeRepo: FakeRepository,
) : BaseViewModel<TrackingData>() {
    val watchlist = fakeRepo.watchlist
        .map { list -> list.map { tmdbRepo.enrichWatched(it) } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = emptyList(),
        )
    val watchedList = fakeRepo.watchedlist
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

