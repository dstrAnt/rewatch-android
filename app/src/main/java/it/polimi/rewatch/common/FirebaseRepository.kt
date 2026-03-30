package it.polimi.rewatch.common

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import it.polimi.rewatch.model.MediaId
import it.polimi.rewatch.model.MediaType
import it.polimi.rewatch.model.MovieId
import it.polimi.rewatch.model.TvId
import it.polimi.rewatch.model.WatchlistMedia
import jakarta.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime

// 1. The Data Class (Cleaned up, no UI state here)
data class UserProfile(
    val name: String = "",
    val role: String = "",
)

// 2. The Interface
interface UserRepository {
    fun getCurrentUserId(): String?
    suspend fun getUserProfile(uid: String): Result<UserProfile>
    suspend fun addToWatchlist(movie: WatchlistMedia): Result<Unit>
    suspend fun removeFromWatchlist(movieId: String): Result<Unit>
    suspend fun getWatchlist(): Result<List<WatchlistMedia>>
    suspend fun sendSignInLink(email: String): Result<Unit>
    suspend fun verifySignInLink(email: String, link: String): Result<Unit>
    fun getWatchlistFlow(): Flow<Result<List<WatchlistMedia>>>
    fun isLogged(): Boolean
}

// 3. The Firebase Implementation
class FirebaseUserRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
) : UserRepository {
    override fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    override fun isLogged(): Boolean {
        println(getCurrentUserId())
        return getCurrentUserId() != null
    }

    override suspend fun sendSignInLink(email: String): Result<Unit> {
        return try {
            // This tells Firebase how to format the link it emails to the user.
            val actionCodeSettings = ActionCodeSettings.newBuilder()
                // The URL you want to redirect to. This must be whitelisted in your Firebase Console.
                // Usually, you just use your Firebase project's default hosting URL.
                .setUrl("https://app-rewatch.firebaseapp.com/finishSignUp")
                .setHandleCodeInApp(true) // Required to open the app directly
                .setAndroidPackageName(
                    "it.polimi.rewatch", // Replace with your actual package name
                    true, // install app if not available
                    "12", // minimum version
                )
                .build()

            auth.sendSignInLinkToEmail(email, actionCodeSettings).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun verifySignInLink(email: String, link: String): Result<Unit> {
        return try {
            // Double check that the link is actually an auth link
            if (auth.isSignInWithEmailLink(link)) {
                // Sign in using the email and the link
                auth.signInWithEmailLink(email, link).await()
                Result.success(Unit)
            } else {
                Result.failure(Exception("Invalid authentication link."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserProfile(uid: String): Result<UserProfile> {
        return try {
            // .await() suspends the coroutine until Firestore returns the data
            val document = db.collection("users").document(uid).get().await()

            if (document.exists()) {
                // Map the document to our data class
                val profile = UserProfile(
                    name = document.getString("name") ?: "Unknown",
                    role = document.getString("role") ?: "User",
                )
                Result.success(profile)
            } else {
                Result.failure(Exception("User profile not found in database."))
            }
        } catch (e: Exception) {
            // Catch network errors, permission denied, etc.
            Result.failure(e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun addToWatchlist(movie: WatchlistMedia): Result<Unit> {
        return try {
            val uid = auth.currentUser?.uid ?: throw Exception("User not logged in")
            // Save the movie to the user's specific watchlist subcollection
            db.collection("users")
                .document(uid)
                .collection("watchlist")
                .document(movie.id.toString()) // Use the movie's ID as the document ID to prevent duplicates
                .set(movie)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun removeFromWatchlist(movieId: String): Result<Unit> {
        return try {
            val uid = auth.currentUser?.uid ?: throw Exception("User not logged in")

            db.collection("users")
                .document(uid)
                .collection("watchlist")
                .document(movieId)
                .delete()
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getWatchlistFlow(): Flow<Result<List<WatchlistMedia>>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(Result.failure(Exception("User not logged in")))
            close() // Close the flow if there's no user
            return@callbackFlow
        }
        // 1. Attach the real-time listener to Firestore
        val listenerRegistration = db.collection("users")
            .document(uid)
            .collection("watchlist")
            // .orderBy("addedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                // 2. Handle errors
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                // 3. Send new data to the Flow whenever the database changes
                if (snapshot != null) {
                    // ---> THIS IS WHERE THE MAGIC HAPPENS <---
                    // We parse the raw snapshot into a pristine List<WatchlistMedia>
                    val parsedMovies = parseWatchlistSnapshot(snapshot)
                    // Send the beautifully parsed list to the ViewModel!
                    trySend(Result.success(parsedMovies))
                }
            }
        // 4. CRITICAL: Clean up the listener when the ViewModel stops collecting
        // to prevent memory leaks!
        awaitClose {
            listenerRegistration.remove()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun parseWatchlistSnapshot(snapshot: QuerySnapshot): List<WatchlistMedia> {
        // mapNotNull will safely skip any corrupted documents instead of crashing the app
        return snapshot.documents.mapNotNull { document ->
            try {
                // 1. Pull the raw primitive data from Firestore
                document.id
                val parentId = document.getLong("mediaParentId")?.toInt() ?: 0
                val typeString = document.getString("mediaType") ?: "MOVIE"
                // Handle the date safely (fallback to current time if missing)
                // val date: LocalDateTime = document.getDate("date") ?: LocalDateTime()
                // 2. Rebuild your complex Sealed Interface
                val mediaType = MediaType.valueOf(typeString)
                val reconstructedItemId: MediaId =
                    if (mediaType == MediaType.MOVIE) MovieId(parentId) else TvId(parentId)
                // Add other types if your interface has them!
                // 3. Return the fully built object
                WatchlistMedia(
                    id = reconstructedItemId,
                    date = LocalDateTime.now(),
                )
            } catch (e: Exception) {
                // If one specific movie is broken, log it and return null to skip it
                Log.e("FirebaseRepo", "Failed to parse document ${document.id}", e)
                null
            }
        }
    }

    override suspend fun getWatchlist(): Result<List<WatchlistMedia>> {
        return try {
            val uid = auth.currentUser?.uid ?: throw Exception("User not logged in")
            // Fetch the subcollection, optionally ordered by when they were added
            val snapshot = db.collection("users")
                .document(uid)
                .collection("watchlist")
                .orderBy("addedAt", Query.Direction.DESCENDING)
                .get()
                .await()
            // Convert the Firestore documents directly into a list of Movie objects
            val movies = snapshot.toObjects(WatchlistMedia::class.java)
            Result.success(movies)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}