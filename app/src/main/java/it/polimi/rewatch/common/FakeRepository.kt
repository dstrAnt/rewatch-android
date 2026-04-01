package it.polimi.rewatch.common

import it.polimi.rewatch.model.MediaId
import it.polimi.rewatch.model.MovieId
import it.polimi.rewatch.model.TvId
import it.polimi.rewatch.model.WatchedMedia
import it.polimi.rewatch.model.WatchlistMedia
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDateTime


interface PersistRepository {
    suspend fun sendSignInLink(email: String): Result<Unit>
    suspend fun verifySignInLink(email: String, link: String): Result<Unit>
    fun isLogged(): Boolean

    val email: SharedFlow<String?>

    val watchlistFlow: SharedFlow<Result<List<WatchlistMedia>>>
    val watchedFlow: SharedFlow<Result<List<WatchedMedia>>>

    fun addToWatchlist(id: MediaId)
    fun removeFromWatchlist(id: MediaId)
    fun toggleWatchlist(id: MediaId)

    fun setRating(id: MediaId, rating: Float)
    fun setWatchDate(id: MediaId, newDate: LocalDateTime)
    fun addToWatchedList(id: MediaId, rating: Float, date: LocalDateTime)
    fun removeFromWatchedList(id: MediaId)
    fun toggleWatchedList(id: MediaId)
    fun logout()
}

@Singleton
class FakeRepository @Inject constructor() : PersistRepository {
    private val _watchlistFlow = MutableSharedFlow<Result<List<WatchlistMedia>>>(replay = 1)

    // 2. Expose it as a read-only SharedFlow
    override val watchlistFlow = _watchlistFlow.asSharedFlow()
    private val _watchedFlow = MutableSharedFlow<Result<List<WatchedMedia>>>(replay = 1)
    override val watchedFlow = _watchedFlow.asSharedFlow()

    init {
        _watchlistFlow.tryEmit(Result.success(fakeWatchlist))
        _watchedFlow.tryEmit(Result.success(fakeWatch))
    }

    private val _code = MutableStateFlow("")
    fun generateCode() {
        _code.update { generateVerificationCode() }
    }

    private val _email = MutableStateFlow("")
    override val email = _email.asSharedFlow()
    override suspend fun sendSignInLink(email: String): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun verifySignInLink(email: String, link: String): Result<Unit> {
        return Result.success(Unit)
    }

    override fun logout() {
        _email.update { "" }
    }

    override fun isLogged() = _email.value != ""

    // The base helper we created earlier
    private inline fun <T> MutableSharedFlow<Result<List<T>>>.updateList(crossinline transform: (List<T>) -> List<T>) {
        val currentList = replayCache.firstOrNull()?.getOrNull() ?: emptyList()
        tryEmit(Result.success(transform(currentList)))
    }

    // NEW: Extension property to easily read the current list state
    private val <T> MutableSharedFlow<Result<List<T>>>.currentList: List<T>
        get() = replayCache.firstOrNull()?.getOrNull() ?: emptyList()

    // NEW: Generic Add
    private fun <T> MutableSharedFlow<Result<List<T>>>.addItem(item: T) {
        updateList { currentList -> currentList + item }
    }

    // NEW: Generic Remove (using a condition)
    private inline fun <T> MutableSharedFlow<Result<List<T>>>.removeItem(crossinline condition: (T) -> Boolean) {
        updateList { currentList -> currentList.filterNot(condition) }
    }

    // The Factorized Helper:
// This handles all the messy Result unwrapping and SharedFlow emitting for you.
    override fun addToWatchlist(id: MediaId) =
        _watchlistFlow.addItem(WatchlistMedia(id))


    override fun removeFromWatchlist(id: MediaId) =
        _watchlistFlow.removeItem { it.id == id }



    override fun addToWatchedList(id: MediaId, rating: Float, date: LocalDateTime) =
        _watchedFlow.addItem(WatchedMedia(id, rating = rating, date = date))


    override fun removeFromWatchedList(id: MediaId) =
        _watchedFlow.removeItem { it.id == id }

    override fun setRating(id: MediaId, rating: Float) {
        _watchedFlow.updateList { currentList ->
            val existing = currentList.find { it.id == id }

            if (existing == null) {
                currentList + WatchedMedia(id, rating = rating, date = LocalDateTime.now())
            } else {
                currentList.map {
                    if (it.id == id) it.copy(rating = rating) else it
                }
            }
        }

        removeFromWatchlist(id)
    }

    override fun toggleWatchedList(id: MediaId) {
        // Grab the current state using our new property
        val existingItem = _watchedFlow.currentList.find { it.id == id }

        if (existingItem != null) {
            // If element is present, remove it ONLY if not rated. Otherwise, do nothing.
            if (existingItem.rating == 0.0f) {
                removeFromWatchedList(id)
            }
        } else {
            // If element not present, add it and remove from watchlist
            addToWatchedList(id, 0.0f, LocalDateTime.now())
            removeFromWatchlist(id)
        }
    }

    override fun setWatchDate(id: MediaId, newDate: LocalDateTime) {
        // Use the updateList helper we made previously for map operations
        _watchedFlow.updateList { currentList ->
            currentList.map {
                if (it.id == id) it.copy(date = newDate) else it
            }
        }
    }

    override fun toggleWatchlist(id: MediaId) {
        // Check state using the new property and route accordingly
        if (_watchlistFlow.currentList.any { it.id == id }) {
            removeFromWatchlist(id)
        } else {
            addToWatchlist(id)
        }
    }
}

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


val fakeWatch = listOf(
    WatchedMedia(
        id = TvId(1390),
        rating = 8.5f,
        date = LocalDateTime.of(2026, 3, 23, 0, 0),
    ),
    WatchedMedia(
        id = TvId(1399),
        rating = 8.5f,
        date = LocalDateTime.of(2023, 3, 23, 0, 0),
    ),
    WatchedMedia(
        id = TvId(1393),
        rating = 8.5f,
        date = LocalDateTime.of(2024, 3, 23, 0, 0),
    ),
    WatchedMedia(
        id = MovieId(549),
        rating = 8.5f,
        date = LocalDateTime.of(2026, 4, 23, 0, 0),
    ),
    WatchedMedia(
        id = MovieId(83541),
        rating = 8.5f,
        date = LocalDateTime.of(2026, 2, 23, 0, 0),
    ),
    WatchedMedia(
        id = MovieId(83540),
        rating = 8.5f,
        date = LocalDateTime.of(2026, 4, 23, 0, 0),
    ),
    WatchedMedia(
        id = TvId(1394),
        rating = 8.5f,
        date = LocalDateTime.of(2026, 3, 23, 0, 0),
    ),
    WatchedMedia(
        id = TvId(1319),
        rating = 8.5f,
        date = LocalDateTime.of(2026, 3, 23, 0, 0),
    ),
    WatchedMedia(
        id = TvId(1395),
        rating = 8.5f,
        date = LocalDateTime.of(2026, 3, 23, 0, 0),
    ),
    WatchedMedia(
        id = TvId(1349),
        rating = 8.5f,
        date = LocalDateTime.of(2026, 3, 23, 0, 0),
    ),
    WatchedMedia(
        id = TvId(1329),
        rating = 8.5f,
        date = LocalDateTime.of(2026, 3, 23, 0, 0),
    ),
)

fun generateVerificationCode(): String {
    val code = "111111"
//    val code = (100000..999999).random().toString()
//    println(code)
    return code
}

fun String.isValidSixDigitCode(): Boolean {
    val codeRegex = "^[0-9]{6}$".toRegex()
    return this.matches(codeRegex)
}
