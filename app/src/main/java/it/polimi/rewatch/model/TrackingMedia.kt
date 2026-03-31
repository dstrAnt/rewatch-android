package it.polimi.rewatch.model

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@RequiresApi(Build.VERSION_CODES.O)
interface TrackingMedia {
    val id: MediaId
    val mediaData: PosterInfo
    val date: LocalDateTime? // Both implementations use this, so we can formalize it here

    // Factorized common logic
    val title: String? get() = mediaData.title
    val rating: Float get() = 0f
    val popularity: Float? get() = null // Only Watchlist overrides this
}

@RequiresApi(Build.VERSION_CODES.O)
data class WatchedMedia(
    override val id: MediaId,
    override val mediaData: PosterInfo = PosterInfo(id),
    override val date: LocalDateTime = LocalDateTime.now(),
    override val rating: Float = 0f,
) : TrackingMedia // Body removed completely

@RequiresApi(Build.VERSION_CODES.O)
data class WatchlistMedia(
    override val id: MediaId,
    override val mediaData: PosterInfo = PosterInfo(id),
    override val date: LocalDateTime = LocalDateTime.now(),
) : TrackingMedia {
    override val rating: Float get() = mediaData.rating
    override val popularity: Float get() = mediaData.popularity
}

// We rename it to represent both types of media!
data class TrackingMediaDto(
    val documentId: String = "",
    val parentId: Int = 0,
    val mediaType: String = "",
    val rating: Float = 0f,
    // Changed to Long? to support your nullable LocalDateTime? in the interface
    val date: Long? = null,
) {
    // 1. Unified Constructor: Takes ANY TrackingMedia (Watchlist or Watched)
    constructor(media: TrackingMedia) : this(
        documentId = media.id.getUid(),
        parentId = media.id.parentId,
        mediaType = media.id.type.name,
        rating = media.rating,
        // Safely handle the nullable date
        date = media.date?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli(),
    )

    // --- HELPER FUNCTIONS ---
    // Private helper to reconstruct the Sealed Interface for both mappers
    private fun reconstructId(): MediaId {
        val type = try {
            MediaType.valueOf(mediaType)
        } catch (e: Exception) {
            MediaType.MOVIE
        }
        return if (type == MediaType.MOVIE) MovieId(parentId) else TvId(parentId)
    }

    // Private helper to reconstruct the Date
    @RequiresApi(Build.VERSION_CODES.O)
    private fun reconstructDate(): LocalDateTime {
        return if (this.date != null) {
            Instant.ofEpochMilli(this.date).atZone(ZoneId.systemDefault()).toLocalDateTime()
        } else {
            LocalDateTime.now() // Fallback if database is missing the date
        }
    }

    // --- MAPPERS ---
    // 2a. Convert back to WatchedMedia (Use this in your getWatchedFlow)
    @RequiresApi(Build.VERSION_CODES.O)
    fun toWatchedMedia(): WatchedMedia {
        return WatchedMedia(
            id = reconstructId(),
            rating = this.rating,
            date = reconstructDate(),
        )
    }

    // 2b. Convert back to WatchlistMedia (Use this in your getWatchlistFlow)
    @RequiresApi(Build.VERSION_CODES.O)
    fun toWatchlistMedia(): WatchlistMedia {
        return WatchlistMedia(
            id = reconstructId(),
            // We ignore 'this.rating' here because WatchlistMedia pulls rating from PosterInfo!
            date = reconstructDate(),
        )
    }
}