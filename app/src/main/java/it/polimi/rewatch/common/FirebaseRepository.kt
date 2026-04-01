package it.polimi.rewatch.common

import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import it.polimi.rewatch.model.MediaId
import it.polimi.rewatch.model.TrackingMediaDto
import it.polimi.rewatch.model.WatchedMedia
import it.polimi.rewatch.model.WatchlistMedia
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime



// 3. The Firebase Implementation
/**
 * Repository to access Firebase methods and services
 */
class FirebaseRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    @ApplicationScope private val appScope: CoroutineScope, // lives as long as the app
) : PersistRepository {
    override fun isLogged(): Boolean = auth.currentUser != null



    private val actionCodeSettings =
        ActionCodeSettings.newBuilder().setUrl("https://app-rewatch.firebaseapp.com/finishSignUp").setHandleCodeInApp(true)
            .setAndroidPackageName("it.polimi.rewatch", true, "12").build()

    override suspend fun sendSignInLink(email: String): Result<Unit> = runCatching {
        auth.sendSignInLinkToEmail(email, actionCodeSettings).await()
    }

    override suspend fun verifySignInLink(email: String, link: String): Result<Unit> = runCatching {
        require(auth.isSignInWithEmailLink(link)) { "Invalid authentication link." }
        auth.signInWithEmailLink(email, link).await()
    }

    override fun logout() {
        auth.signOut()
    }

    // 1. Create a basic Flow using callbackFlow to bridge the Firebase Listener
    private val authStateFlow: Flow<String?> = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            // Send the current user downstream whenever the state changes
            trySend(firebaseAuth.currentUser?.email)
        }
        // Register the listener
        auth.addAuthStateListener(authStateListener)
        // Unregister the listener automatically when the flow collection is cancelled
        awaitClose {
            auth.removeAuthStateListener(authStateListener)
        }
    }

    // 2. Convert it to a StateFlow (which implements SharedFlow)
    override val email: SharedFlow<String?> = authStateFlow.stateIn(
        scope = appScope,
        // Keeps the upstream listener active for 5 seconds after the last subscriber leaves
        // This prevents tearing down and rebuilding the listener during configuration changes (like screen rotations)
        started = SharingStarted.WhileSubscribed(5000),
        // The initial value is synchronously grabbed from the current Firebase state
        initialValue = auth.currentUser?.email,
    )

    // --- Firestore helpers ---
    private fun collectionRef(subcollection: String): CollectionReference {
        val uid = auth.currentUser?.uid ?: throw Exception("User not logged in")
        return db.collection("users").document(uid).collection(subcollection)
    }

    private fun setDoc(sub: String, dto: TrackingMediaDto) {
        appScope.launch { runCatching { collectionRef(sub).document(dto.documentId).set(dto).await() } }
    }

    private fun deleteDoc(sub: String, id: MediaId) {
        appScope.launch {
            runCatching {
                collectionRef(sub).document(id.getUid()).delete().await()
            }
        }
    }

    private fun updateDoc(sub: String, id: MediaId, fields: Map<String, Any>) {
        appScope.launch {
            runCatching {
                collectionRef(sub).document(id.getUid()).update(fields).await()
            }
        }
    }

    // --- Observe ---
    private fun <T> observeMedia(subcollection: String, mapper: (TrackingMediaDto) -> T): Flow<Result<List<T>>> = callbackFlow {
        val ref = try {
            collectionRef(subcollection)
        } catch (e: Exception) {
            trySend(Result.failure(e)); close(); return@callbackFlow
        }
        val registration = ref.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Result.failure(error)); return@addSnapshotListener
            }
            val models = snapshot?.toObjects(TrackingMediaDto::class.java)?.map(mapper)
            trySend(Result.success(models.orEmpty()))
        }
        awaitClose { registration.remove() }
    }

    override val watchlistFlow = observeMedia("watchlist") { it.toWatchlistMedia() }.shareIn(
        appScope, SharingStarted.WhileSubscribed(5000), replay = 1,
    )
    override val watchedFlow = observeMedia("watched") { it.toWatchedMedia() }.shareIn(
        appScope, SharingStarted.WhileSubscribed(5000), replay = 1,
    )

    // --- State helpers ---
    private val currentWatchlist get() = watchlistFlow.replayCache.firstOrNull()?.getOrNull().orEmpty()
    private val currentWatched get() = watchedFlow.replayCache.firstOrNull()?.getOrNull().orEmpty()

    // --- Watchlist ---
    override fun addToWatchlist(id: MediaId) = setDoc("watchlist", TrackingMediaDto(WatchlistMedia(id)))

    override fun removeFromWatchlist(id: MediaId) = deleteDoc("watchlist", id)

    override fun toggleWatchlist(id: MediaId) = if (currentWatchlist.any { it.id == id }) removeFromWatchlist(id) else addToWatchlist(id)

    // --- Watched ---
    override fun addToWatchedList(id: MediaId, rating: Float, date: LocalDateTime) =
        setDoc("watched", TrackingMediaDto(WatchedMedia(id, rating = rating, date = date)))

    override fun removeFromWatchedList(id: MediaId) = deleteDoc("watched", id)

    override fun toggleWatchedList(id: MediaId) {
        val existing = currentWatched.find { it.id == id }
        if (existing != null) {
            if (existing.rating == 0.0f) removeFromWatchedList(id)
        } else {
            addToWatchedList(id, 0.0f, LocalDateTime.now())
            removeFromWatchlist(id)
        }
    }

    override fun setRating(id: MediaId, rating: Float) {
        val existing = currentWatched.find { it.id == id }
        if (existing != null) {
            setDoc("watched", TrackingMediaDto(existing.copy(rating = rating)))
        } else {
            addToWatchedList(id, rating, LocalDateTime.now())
        }
        removeFromWatchlist(id)
    }


    override fun setWatchDate(id: MediaId, newDate: LocalDateTime) {
        val existing = currentWatched.find { it.id == id } ?: return
        setDoc("watched", TrackingMediaDto(existing.copy(date = newDate)))
    }
}