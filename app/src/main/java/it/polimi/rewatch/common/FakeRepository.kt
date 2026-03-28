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

@RequiresApi(Build.VERSION_CODES.O)
val fakeWatchlist = listOf(
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

@RequiresApi(Build.VERSION_CODES.O)
val fakeWatch = listOf(
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
        id = MovieId(83541),
        rating = 8.5f,
        date = LocalDate.of(2026, 2, 23),
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

fun generateVerificationCode(): String {
    val code = (100000..999999).random().toString()
    println(code)
    return code
}

fun String.isValidSixDigitCode(): Boolean {
    val codeRegex = "^[0-9]{6}$".toRegex()
    return this.matches(codeRegex)
}

@Singleton
@RequiresApi(Build.VERSION_CODES.O)
class FakeRepository
@Inject constructor() {
    private val _watchlist = MutableStateFlow(fakeWatchlist)
    val watchlist = _watchlist.asStateFlow()
    private val _watchedList = MutableStateFlow(fakeWatch)
    val watchedList = _watchedList.asStateFlow()

    private val _code = MutableStateFlow("")
    fun generateCode() {
        _code.update { generateVerificationCode() }
    }

    private val _email = MutableStateFlow("")
    val email = _email.asStateFlow()
    fun login(email: String) {
        _email.update { email }
    }

    fun logout() {
        _email.update { "" }
    }

    fun isLogged() = _email.value != ""

    fun isValidCode(code: String) = code.isValidSixDigitCode() && code == _code.value

    @RequiresApi(Build.VERSION_CODES.O)
    fun addToWatchlist(id: MediaId) {
        _watchlist.update { currentList ->
            currentList + WatchlistMedia(id)
        }
    }

    fun removeFromWatchlist(id: MediaId) {
        _watchlist.update { currentList ->
            currentList.filter { it.id != id }
        }
    }

    fun addToWatchedList(id: MediaId, rating: Float, date: LocalDate) {
        _watchedList.update { currentList ->
            currentList + WatchedMedia(id, rating, date)
        }
    }

    fun removeFromWatchedList(id: MediaId) {
        _watchedList.update { currentList ->
            currentList.filter { it.id != id }
        }
    }

    fun setRating(id: MediaId, rating: Float) {
        // if media is not present, add it to watched
        if (_watchedList.value.none { it.id == id }) {
            addToWatchedList(id, rating, LocalDate.now())
        } else { // if already present, update the existing entry
            _watchedList.update { currentList ->
                currentList.map {
                    if (it.id != id) it
                    else it.copy(rating = rating)
                }
            }
        }
        // if media present in watchlist, remove it
        removeFromWatchlist(id)
    }

    fun setWatchDate(id: MediaId, newDate: LocalDate) {
        _watchedList.update { currentList ->
            currentList.map {
                if (it.id != id) it
                else it.copy(date = newDate)
            }
        }
    }

    fun toggleWatchlist(id: MediaId) {
        // if element already present, remove it
        if (_watchlist.value.any { it.id == id }) removeFromWatchlist(id)

        // if element not present, add it
        else addToWatchlist(id)
    }

    fun toggleWatchedList(id: MediaId) {
        // if element already present, remove it only if not rated
        if (_watchedList.value.any { it.id == id && it.rating == 0.0f }) removeFromWatchedList(id)
        else if (_watchedList.value.any { it.id == id }) return

        // if element not present, add it and remove it from other list
        else {
            addToWatchedList(id, 0.0f, LocalDate.now())
            removeFromWatchlist(id)
        }
    }

}
