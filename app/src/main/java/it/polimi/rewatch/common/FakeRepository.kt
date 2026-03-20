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
import java.time.LocalDate

@Singleton
class FakeRepository
@Inject constructor() {
    private val watchlist: MutableList<WatchlistMedia> = mutableListOf(
        WatchlistMedia(
            id = MovieId(550),
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

    @RequiresApi(Build.VERSION_CODES.O)
    private val watched: MutableList<WatchedMedia> = mutableListOf(

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

    fun getWatchlist() = watchlist
    fun addToWatchlist(id: MediaId) {
        watchlist.add(WatchlistMedia(id))
    }

    fun removeFromWatchlist(id: MediaId) {
        watchlist.removeAll { it.id == id }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getWatched() = watched

    @RequiresApi(Build.VERSION_CODES.O)
    fun addToWatched(id: MediaId, rating: Float = 0.0f, date: LocalDate) {
        watched.add(WatchedMedia(id, rating, date))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun changeWatchedRating(id: MediaId, rating: Float) {
        watched.find { it.id == id }?.rating = rating
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun changeWatchedDate(id: MediaId, date: LocalDate) {
        watched.find { it.id == id }?.date = date
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun removeFromWatched(id: MediaId) {
        watched.removeAll { it.id == id }
    }
}