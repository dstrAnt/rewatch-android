package it.polimi.rewatch.common

import android.util.Log
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import it.polimi.rewatch.model.MediaId
import it.polimi.rewatch.model.TrackingMediaDto
import it.polimi.rewatch.model.WatchedMedia
import it.polimi.rewatch.model.WatchlistMedia
import jakarta.inject.Inject
import java.time.LocalDateTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Repository to access Firebase methods and services
 */
class FirebaseRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    @ApplicationScope private val appScope: CoroutineScope,
) : PersistRepository {
    override var allowEnrichment = true
    // --- Auth state ---
    // Emit uid instead of email — anonymous users have a uid but no email
    override val authState: SharedFlow<AuthState> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            trySend(
                when {
                    user == null -> AuthState.SignedOut
                    user.isAnonymous -> AuthState.Anonymous(user.uid)
                    else -> AuthState.Authenticated(user.uid, user.email)
                },
            )
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }.shareIn(appScope, SharingStarted.WhileSubscribed(5000), replay = 1)

    // --- Anonymous auth ---
    override suspend fun signInAnonymously(): Result<Unit> = runCatching {
        auth.signInAnonymously().await()
    }

    // --- Email magic link ---
    private val actionCodeSettings =
        ActionCodeSettings.newBuilder()
            .setUrl("https://app-rewatch.firebaseapp.com/finishSignUp")
            .setHandleCodeInApp(true)
            .setAndroidPackageName("it.polimi.rewatch", true, "12")
            .build()

    override suspend fun sendSignInLink(email: String): Result<Unit> = runCatching {
        auth.sendSignInLinkToEmail(email, actionCodeSettings).await()
    }

    /**
     * Tries to link the magic-link credential to the current anonymous account.
     * If the email already exists → returns LinkResult.Collision so the UI can
     * ask the user whether to merge or discard local data.
     * If no anonymous user exists, falls back to normal sign-in.
     */
    override suspend fun verifySignInLink(email: String, link: String): Result<LinkResult> = runCatching {
        require(auth.isSignInWithEmailLink(link)) { "Invalid authentication link." }
        val credential = EmailAuthProvider.getCredentialWithLink(email, link)
        val currentUser = auth.currentUser

        if (currentUser == null || !currentUser.isAnonymous) {
            auth.signInWithEmailLink(email, link).await()
            return@runCatching LinkResult.SignedIn
        }

        try {
            currentUser.linkWithCredential(credential).await()
            LinkResult.Linked
        } catch (e: FirebaseAuthUserCollisionException) {
            val hasData = hasAnyData(currentUser.uid)
            if (hasData) {
                LinkResult.Collision(
                    anonymousUid = currentUser.uid,
                    credential = credential,
                )
            } else {
                // No local data — just switch accounts, no merge needed
                currentUser.delete().await()
                auth.signInWithCredential(credential).await()
                LinkResult.SignedIn
            }
        }
    }

    private suspend fun hasAnyData(uid: String): Boolean {
        for (sub in listOf("watchlist", "watched")) {
            val snapshot = collectionRef(sub, uid).limit(1).get().await()
            if (!snapshot.isEmpty) return true
        }
        return false
    }

    /**
     * Called after the user chooses merge or discard on a collision.
     */
    override suspend fun resolveCollision(anonymousUid: String, credential: AuthCredential, merge: Boolean): Result<Unit> = runCatching {
        val anonUser = auth.currentUser
            ?: throw IllegalStateException("No current user")

        if (merge) {
            // Read anonymous data WHILE STILL the anonymous user
            val data = readAllUserData(anonymousUid)
            // Now switch to the email account
            anonUser.delete().await() // must delete anon first, releases the email link
            auth.signInWithCredential(credential).await()
            val emailUid = auth.currentUser?.uid
                ?: throw IllegalStateException("Sign-in failed")
            // Write into the email account (now we have permission)
            writeUserData(emailUid, data)
        } else {
            deleteUserData(anonymousUid)
            anonUser.delete().await()
            auth.signInWithCredential(credential).await()
        }
    }

    private suspend fun readAllUserData(uid: String): Map<String, List<DocumentSnapshot>> {
        return listOf("watchlist", "watched").associateWith { sub ->
            collectionRef(sub, uid).get().await().documents
        }
    }

    private suspend fun writeUserData(uid: String, data: Map<String, List<DocumentSnapshot>>) {
        val batch = db.batch()
        for ((sub, docs) in data) {
            val targetRef = collectionRef(sub, uid)
            for (doc in docs) {
                val docData = doc.data ?: continue
                batch.set(targetRef.document(doc.id), docData)
            }
        }
        batch.commit().await()
    }

    override fun logout() {
        auth.signOut()
    }

    // Keep synchronous version for reads where user might not exist yet
    private fun currentUid(): String? = auth.currentUser?.uid

    // --- Firestore helpers ---
    private fun requireUid(): String = auth.currentUser?.uid ?: throw IllegalStateException("User not logged in")

    private fun userDocRef(uid: String = requireUid()) = db.collection("users").document(uid)

    private fun collectionRef(sub: String, uid: String): CollectionReference = db.collection("users").document(uid).collection(sub)

    /**
     * Ensures a Firebase user exists (anonymous if needed).
     * Call before any Firestore operation that requires a uid.
     */
    private suspend fun ensureAuthenticated(): String {
        auth.currentUser?.let { return it.uid }
        auth.signInAnonymously().await()
        return auth.currentUser?.uid ?: throw IllegalStateException("Anonymous sign-in failed")
    }

    private fun launchWrite(operation: String, block: suspend () -> Unit) {
        appScope.launch {
            try {
                ensureAuthenticated()
                block()
            } catch (e: Exception) {
                Log.e("FirebaseRepo", "Failed to $operation", e)
            }
        }
    }

    private fun setDoc(sub: String, dto: TrackingMediaDto) = launchWrite("set ${dto.documentId} in $sub") {
        val uid = ensureAuthenticated()
        collectionRef(sub, uid).document(dto.documentId).set(dto).await()
    }

    private fun deleteDoc(sub: String, id: MediaId) = launchWrite("delete ${id.getUid()} from $sub") {
        val uid = ensureAuthenticated()
        collectionRef(sub, uid).document(id.getUid()).delete().await()
    }

    // --- Data migration helpers ---
    private suspend fun copyUserData(fromUid: String, toUid: String) {
        for (sub in listOf("watchlist", "watched")) {
            val docs = collectionRef(sub, fromUid).get().await()
            val targetRef = collectionRef(sub, toUid)
            val batch = db.batch()
            for (doc in docs.documents) {
                val data = doc.data ?: continue
                batch.set(targetRef.document(doc.id), data)
            }
            batch.commit().await()
        }
    }

    private suspend fun deleteUserData(uid: String) {
        val batch = db.batch()
        for (sub in listOf("watchlist", "watched")) {
            val docs = collectionRef(sub, uid).get().await()
            docs.documents.forEach { batch.delete(it.reference) }
        }
        batch.delete(userDocRef(uid))
        batch.commit().await()
    }

    // --- Observe ---
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun <T> observeMedia(sub: String, mapper: (TrackingMediaDto) -> T): Flow<Result<List<T>>> = authState.flatMapLatest { state ->
        when (state) {
            is AuthState.SignedOut -> flowOf(Result.success(emptyList()))

            is AuthState.Anonymous,
            is AuthState.Authenticated,
            -> {
                callbackFlow {
                    val uid = currentUid() ?: run {
                        close()
                        return@callbackFlow
                    }
                    val ref = collectionRef(sub, uid)
                    val registration = ref.addSnapshotListener { snapshot, error ->
                        when {
                            error != null -> trySend(Result.failure(error))

                            else -> {
                                val models = snapshot?.toObjects(TrackingMediaDto::class.java)?.map(mapper)
                                models?.let { trySend(Result.success(it)) }
                            }
                        }
                    }
                    awaitClose { registration.remove() }
                }
            }
        }
    }

    override val watchlistFlow = observeMedia("watchlist") { it.toWatchlistMedia() }
        .shareIn(appScope, SharingStarted.WhileSubscribed(5000), replay = 1)
    override val watchedFlow = observeMedia("watched") { it.toWatchedMedia() }
        .shareIn(appScope, SharingStarted.WhileSubscribed(5000), replay = 1)
    private val currentWatchlist get() = watchlistFlow.replayCache.firstOrNull()?.getOrNull().orEmpty()
    private val currentWatched get() = watchedFlow.replayCache.firstOrNull()?.getOrNull().orEmpty()

    override fun addToWatchlist(id: MediaId) = setDoc("watchlist", TrackingMediaDto(WatchlistMedia(id)))
    override fun removeFromWatchlist(id: MediaId) = deleteDoc("watchlist", id)
    override fun toggleWatchlist(id: MediaId) = if (currentWatchlist.any { it.id == id }) removeFromWatchlist(id) else addToWatchlist(id)

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
        if (existing != null) setDoc("watched", TrackingMediaDto(existing.copy(rating = rating)))
        else addToWatchedList(id, rating, LocalDateTime.now())
        removeFromWatchlist(id)
    }

    override fun setWatchDate(id: MediaId, newDate: LocalDateTime) {
        val existing = currentWatched.find { it.id == id } ?: return
        setDoc("watched", TrackingMediaDto(existing.copy(date = newDate)))
    }
}

// --- Supporting types ---
sealed interface AuthState {
    data object SignedOut : AuthState
    data class Anonymous(val uid: String) : AuthState
    data class Authenticated(val uid: String, val email: String?) : AuthState
}

sealed interface LinkResult {
    /** Anonymous account successfully upgraded to email account (same uid). */
    data object Linked : LinkResult

    /** Email already existed — UI should ask merge or discard. */
    data class Collision(val anonymousUid: String, val credential: AuthCredential) : LinkResult

    /** No anonymous user was present, did a normal sign-in. */
    data object SignedIn : LinkResult
}
