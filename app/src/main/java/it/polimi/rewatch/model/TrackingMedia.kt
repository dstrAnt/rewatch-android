package it.polimi.rewatch.model

import java.time.LocalDate

interface TrackingMedia {
    val id: MediaId
    val mediaData: PosterMedia
    val rating: Float get() = 0.0f
    val title: String? get() = null
    val popularity: Float? get() = null // Only Watchlist has this
    val date: LocalDate? get() = null         // Only Watched has this
}

data class WatchedMedia(
    override val id: MediaId,
    override var rating: Float,
    override var date: LocalDate,
    override val mediaData: PosterMedia = PosterMedia(id),
) : TrackingMedia {
    override val title get() = mediaData.title
}

data class WatchlistMedia(
    override val id: MediaId,
    override val mediaData: PosterMedia = PosterMedia(id),
) : TrackingMedia {
    override val rating: Float get() = mediaData.rating
    override val title get() = mediaData.title
    override val popularity get() = mediaData.popularity
}
