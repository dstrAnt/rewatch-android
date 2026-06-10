package it.polimi.rewatch.common


import app.moviebase.tmdb.model.TmdbShowDetail
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.EmailAuthCredential
import com.google.firebase.auth.EmailAuthProvider
import it.polimi.rewatch.model.MediaId
import it.polimi.rewatch.model.MovieId
import it.polimi.rewatch.model.PosterInfo
import it.polimi.rewatch.model.TvId
import it.polimi.rewatch.model.WatchedMedia
import it.polimi.rewatch.model.WatchlistMedia
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.time.LocalDate
import java.time.LocalDateTime


interface PersistRepository {
    suspend fun sendSignInLink(email: String): Result<Unit>
    suspend fun verifySignInLink(email: String, link: String): Result<LinkResult>

    //val email: SharedFlow<String?>
    val watchlistFlow: SharedFlow<Result<List<WatchlistMedia>>>
    val watchedFlow: SharedFlow<Result<List<WatchedMedia>>>

    var allowEnrichment: Boolean

    fun addToWatchlist(id: MediaId)
    fun removeFromWatchlist(id: MediaId)
    fun toggleWatchlist(id: MediaId)

    fun setRating(id: MediaId, rating: Float)
    fun setWatchDate(id: MediaId, newDate: LocalDateTime)
    fun addToWatchedList(id: MediaId, rating: Float, date: LocalDateTime)
    fun removeFromWatchedList(id: MediaId)
    fun toggleWatchedList(id: MediaId)
    fun logout()
    val authState: SharedFlow<AuthState>
    suspend fun signInAnonymously(): Result<Unit>
    suspend fun resolveCollision(anonymousUid: String, credential: AuthCredential, merge: Boolean): Result<Unit>
}

/**
 * [FakeRepository] mimics Firebase's functionalities in a local context
 */
@Singleton
class FakeRepository @Inject constructor() : PersistRepository {
    private val _watchlistFlow = MutableSharedFlow<Result<List<WatchlistMedia>>>(replay = 1)
    override var watchlistFlow = _watchlistFlow.asSharedFlow()
    private val _watchedFlow = MutableSharedFlow<Result<List<WatchedMedia>>>(replay = 1)
    override var watchedFlow = _watchedFlow.asSharedFlow()
    private var _email = ""

    private val defaultTestEmail = "a@b.com"

    private val defaultTestEmail2 = "c@b.com"

    private val defaultMergeTestEmail = "merge.me@b.com"

    private val defaultMergeTestEmail2 = "cannotMerge.me@b.com"

    var loadStateTracking = false

    var emptyWatchList = false
    var emptyWatchedList = false

    override var allowEnrichment = true

    private val _authState = MutableSharedFlow<AuthState>(replay = 1)
    override val authState = _authState.asSharedFlow()

    companion object {
        fun empty(): FakeRepository {
            return FakeRepository().apply {
                _watchlistFlow.tryEmit(Result.success(emptyList()))
                _watchedFlow.tryEmit(Result.success(emptyList()))
            }
        }
    }

    init {
        if(!emptyWatchList) {
            _watchlistFlow.tryEmit(Result.success(fakeWatchlist))
        }
        else{
            _watchlistFlow.tryEmit(Result.success(emptyList()))
        }
        if (!emptyWatchedList) {
            _watchedFlow.tryEmit(Result.success(fakeWatch))
        }
        else{
            _watchedFlow.tryEmit(Result.success(emptyList()))
        }
    }

    override suspend fun sendSignInLink(email: String): Result<Unit> {
        if(loadStateTracking)
            delay(5000)
        return if (email.equals(defaultTestEmail)) Result.failure(exception = Throwable()) else Result.success(Unit)
    }

    override suspend fun verifySignInLink(email: String, link: String): Result<LinkResult> {
        if(loadStateTracking)
            delay(5000)
        _email = email
        _authState.tryEmit(AuthState.Authenticated("", email))
        if (email.equals(defaultTestEmail2))
            return Result.failure(exception = Throwable())
        else {
            val choice = when (email) {
                defaultMergeTestEmail -> 0
                defaultMergeTestEmail2 -> 1
                else -> 1111
            }
            if (email.equals(defaultMergeTestEmail) || email.equals(defaultMergeTestEmail2))
//                return Result.success(LinkResult.Collision(choice.toString(), EmailAuthProvider.getCredentialWithLink(email, link)))
                  return Result.success(LinkResult.Collision(choice.toString(), EmailAuthProvider.getCredential("hello", "world")))
            else return Result.success(LinkResult.SignedIn)
        }
    }

    override fun logout() {
        _authState.tryEmit(AuthState.SignedOut)
        _email = ""
    }

    override suspend fun signInAnonymously(): Result<Unit> {
        _authState.tryEmit(AuthState.Anonymous(""))
        return Result.success(Unit)
    }

    override suspend fun resolveCollision(
        anonymousUid: String,
        credential: AuthCredential,
        merge: Boolean,
    ): Result<Unit> {
        if(loadStateTracking)
            delay(5000)
        return if(anonymousUid.equals("0")) Result.success(Unit) else Result.failure(Throwable())
    }

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

fun shortLocalDateTime(year: Int, month: Int, day: Int) : LocalDateTime{
    return LocalDateTime.of(year, month, day, 0, 0)
}

val fakeWatchlist = listOf(
    WatchlistMedia(
        id = MovieId(550),
    ),
    WatchlistMedia(
        id = TvId(1390), mediaData = PosterInfo(TmdbShowDetail(popularity = 5000f))
    ),
    WatchlistMedia(
        id = TvId(1393),
        mediaData = PosterInfo(TmdbShowDetail(popularity = 50f, name = "Scavengers", voteAverage = 5.5f)),
        date = shortLocalDateTime(2026, 2, 23)
    ),
    WatchlistMedia(
        id = MovieId(1393),
        mediaData = PosterInfo(TmdbShowDetail(popularity = 50f, name = "Scavengers - The Movie", voteAverage = 7.0f)),
        date = shortLocalDateTime(2026, 2, 23)
    ),
    WatchlistMedia(
        id = TvId(1395), mediaData = PosterInfo(TmdbShowDetail(popularity = 600f, voteAverage = 7.2f, name = "Feelings",))
    ),
    WatchlistMedia(
        id = MovieId(83533), mediaData = PosterInfo(TmdbShowDetail(popularity = 350f, voteAverage = 2.5f, name = "Alice in the Wonders"))
    ),
    WatchlistMedia(
        id = MovieId(1317288), mediaData = PosterInfo(TmdbShowDetail(popularity = 2000f, voteAverage = 8.5f, name = "Betamax - The Return"))
    ),
    WatchlistMedia(
        id = MovieId(1317287), mediaData = PosterInfo(TmdbShowDetail(popularity = 15f, voteAverage = 5.0f, name = "1984"))
    ),
    WatchlistMedia(
        id = MovieId(1317289), mediaData = PosterInfo(TmdbShowDetail(popularity = 30f, voteAverage = 4.0f)),
        date = shortLocalDateTime(2026, 5, 18)
    ),
    WatchlistMedia(
        id = MovieId(551), mediaData = PosterInfo(TmdbShowDetail(popularity = 500f, voteAverage = 3.5f, name = "Tito Puente"))
    ),
    WatchlistMedia(
        id = MovieId(83532), mediaData = PosterInfo(TmdbShowDetail(popularity = 80f, voteAverage = 1.5f,)),
        date = shortLocalDateTime(2025, 12, 2)
    ),
    WatchlistMedia(
        id = TvId(1394), mediaData = PosterInfo(TmdbShowDetail(popularity = 8f, name = "Chickens vs Space")),
        date = shortLocalDateTime(2025, 11, 3)
    ),
    WatchlistMedia(
        id = MovieId(99396), mediaData = PosterInfo(TmdbShowDetail(popularity = 8000f))
    ),
    WatchlistMedia(
        id = MovieId(99394), mediaData = PosterInfo(TmdbShowDetail(popularity = 800f, voteAverage = 0f))
    ),
    WatchlistMedia(
        id = TvId(93405), mediaData = PosterInfo(TmdbShowDetail(popularity = 1100f, voteAverage = 8.7f, name = "Nelson Mandela")),
        date = LocalDateTime.of(LocalDate.now().year, 5, 14, 0, 0),
    ),
    WatchlistMedia(
        id = TvId(99394), mediaData = PosterInfo(TmdbShowDetail(popularity = 800f,  voteAverage = 2.2f, name = "Tennis Origins"))
    )
)
val fakeWatch = listOf(
    WatchedMedia(
        id = TvId(1390),
        rating = 8.5f,
        date = LocalDateTime.of(2026, 3, 23, 0, 0),
        mediaData = PosterInfo(TmdbShowDetail( name = "William Tiafoe"))
    ),
    WatchedMedia(
        id = TvId(1399),
        rating = 7.5f,
        date = LocalDateTime.of(2023, 3, 22, 0, 0),
    ),
    WatchedMedia(
        id = TvId(1393),
        rating = 1.5f,
        date = LocalDateTime.of(2024, 3, 25, 0, 0),
        mediaData = PosterInfo(TmdbShowDetail(name = "Super Portrait of A Man, a Legend"))
    ),
    WatchedMedia(
        id = MovieId(10392),
        rating = 0f,
        date = LocalDateTime.of(2024, 3, 25, 0, 0),
        mediaData = PosterInfo(TmdbShowDetail(name = "The Real(ity) Hero"))
    ),
    WatchedMedia(
        id = MovieId(549),
        rating = 2.0f,
        date = LocalDateTime.of(LocalDate.now().year, 4, 23, 0, 0),
        mediaData = PosterInfo(TmdbShowDetail(name = "A Magic World, A Hope"))
    ),
    WatchedMedia(
        id = MovieId(83541),
        rating = 8.5f,
        date = LocalDateTime.of(2026, 2, 23, 0, 0),
        mediaData = PosterInfo(TmdbShowDetail(name = "Be that one"))
    ),
    WatchedMedia(
        id = MovieId(83540),
        rating = 5.0f,
        date = LocalDateTime.of(2026, 4, 23, 0, 0),
        mediaData = PosterInfo(TmdbShowDetail(name = "Sarah, Where Are U?"))
    ),
    WatchedMedia(
        id = TvId(1394),
        rating = 0f,
        date = LocalDateTime.of(2026, 5, 12, 0, 0),
    ),
    WatchedMedia(
        id = TvId(1319),
        rating = 1.0f,
        date = LocalDateTime.of(2026, 6, 2, 0, 0),
        mediaData = PosterInfo(TmdbShowDetail(name = "Felix Escalibur"))
    ),
    WatchedMedia(
        id = TvId(1395),
        rating = 9.0f,
        date = LocalDateTime.of(LocalDate.now().year, 3, 13, 0, 0),
        mediaData = PosterInfo(TmdbShowDetail(name = "Casper The Ghost"))
    ),
    WatchedMedia(
        id = TvId(1349),
        rating = 7.5f,
        date = LocalDateTime.of(2026, 3, 10, 0, 0),
        mediaData = PosterInfo(TmdbShowDetail(name = "Naruto - The Great Return"))
    ),
    WatchedMedia(
        id = MovieId(8349),
        rating = 7.5f,
        date = LocalDateTime.of(2026, 3, 10, 0, 0),
    ),
    WatchedMedia(
        id = TvId(1329),
        rating = 7.2f,
        date = LocalDateTime.of(2026, 6, 7, 0, 0),
        mediaData = PosterInfo(TmdbShowDetail(voteAverage = 2.0f, name = "1000 Lives... And Beyond"))
    ),
    WatchedMedia(
        id = TvId(1339),
        rating = 6.3f,
        date = LocalDateTime.of(LocalDate.now().year, LocalDate.now().month, 17, 0, 0),
    ),
    WatchedMedia(
        id = MovieId(9340),
        rating = 6.3f,
        date = LocalDateTime.of(LocalDate.now().year, LocalDate.now().month, 17, 0, 0),
    ),
)

