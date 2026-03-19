package it.polimi.rewatch.screens.tracking

import android.os.Build
import androidx.annotation.RequiresApi
import dagger.hilt.android.lifecycle.HiltViewModel
import it.polimi.rewatch.common.BaseViewModel
import it.polimi.rewatch.common.TmdbRepository
import it.polimi.rewatch.model.MovieId
import it.polimi.rewatch.model.TvId
import it.polimi.rewatch.model.WatchedMedia
import it.polimi.rewatch.model.WatchlistMedia
import jakarta.inject.Inject
import java.time.LocalDate

data class TrackingData(
    val watchlist: List<WatchlistMedia>,
    val watched: List<WatchedMedia>,
)

@HiltViewModel
class TrackingViewModel @Inject constructor(
    private val repository: TmdbRepository,
) : BaseViewModel<TrackingData>() {

    init {
        executeFetch()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun fetchData() = repository.getTrackingData()
}

// fake list builders
fun fakeWatchlist(): List<WatchlistMedia> = listOf(
    WatchlistMedia(
        id = MovieId(550),
    ),
    WatchlistMedia(
        id = MovieId(83533),
    ),
    WatchlistMedia(
        id = MovieId(1317288),
    ),
)

@RequiresApi(Build.VERSION_CODES.O)
fun fakeWatched(): List<WatchedMedia> = listOf(
    WatchedMedia(
        id = TvId(1390),
        rating = 8.5f,
        date = LocalDate.of(2026, 3, 23),
    ),
    WatchedMedia(
        id = TvId(1399),
        rating = 8.5f,
        date = LocalDate.of(2023, 3, 23),
    ),
    WatchedMedia(
        id = TvId(1393),
        rating = 8.5f,
        date = LocalDate.of(2024, 3, 23),
    ),
    WatchedMedia(
        id = TvId(1398),
        rating = 8.5f,
        date = LocalDate.of(2026, 4, 23),
    ),
    WatchedMedia(
        id = TvId(1394),
        rating = 8.5f,
        date = LocalDate.of(2026, 3, 23),
    ),
    WatchedMedia(
        id = TvId(1319),
        rating = 8.5f,
        date = LocalDate.of(2026, 3, 23),
    ),
    WatchedMedia(
        id = TvId(1395),
        rating = 8.5f,
        date = LocalDate.of(2026, 3, 23),
    ),
    WatchedMedia(
        id = TvId(1349),
        rating = 8.5f,
        date = LocalDate.of(2026, 3, 23),
    ),
    WatchedMedia(
        id = TvId(1329),
        rating = 8.5f,
        date = LocalDate.of(2026, 3, 23),
    ),
)

