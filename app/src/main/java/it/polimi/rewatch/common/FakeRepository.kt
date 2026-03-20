package it.polimi.rewatch.common

import android.os.Build
import androidx.annotation.RequiresApi
import it.polimi.rewatch.model.MediaId
import it.polimi.rewatch.model.MovieId
import it.polimi.rewatch.model.TvId
import it.polimi.rewatch.model.WatchedMedia
import it.polimi.rewatch.model.WatchlistMedia
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate

@Singleton
@RequiresApi(Build.VERSION_CODES.O)
class FakeRepository
@Inject constructor() {
    private val fakeWatchlist = listOf(
        WatchlistMedia(
            id = MovieId(550),
        ),
        WatchlistMedia(
            id = TvId(1399),
        ),
        WatchlistMedia(
            id = TvId(1393),
        ),
        WatchlistMedia(
            id = TvId(1395),
        ),
        WatchlistMedia(
            id = MovieId(83533),
        ),
        WatchlistMedia(
            id = MovieId(1317288),
        ),
        WatchlistMedia(
            id = MovieId(1317287),
        ),
        WatchlistMedia(
            id = MovieId(1317289),
        ),
        WatchlistMedia(
            id = MovieId(551),
        ),
        WatchlistMedia(
            id = MovieId(83532),
        ),
    )
    private val fakeWatch = listOf(
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
            id = MovieId(549),
            rating = 8.5f,
            date = LocalDate.of(2026, 4, 23),
        ),
        WatchedMedia(
            id = MovieId(83538),
            rating = 8.5f,
            date = LocalDate.of(2026, 4, 23),
        ),
        WatchedMedia(
            id = MovieId(83540),
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
    private val _watchlist = MutableStateFlow(fakeWatchlist)
    val watchlist = _watchlist.asStateFlow()
    private val _watchedList = MutableStateFlow(fakeWatch)
    val watchedList = _watchedList.asStateFlow()
    fun addToWatchlist(id: MediaId) {
        _watchlist.update { currentList ->
            currentList + WatchlistMedia(id) // Creates and returns a new list!
        }
    }

    fun removeFromWatchlist(id: MediaId) {
        _watchlist.update { currentList ->
            currentList.filter { it != WatchlistMedia(id) } // Creates and returns a new list!
        }
    }

}