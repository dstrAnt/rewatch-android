package it.polimi.rewatch.screens.tracking

import android.os.Build
import androidx.annotation.RequiresApi
import dagger.hilt.android.lifecycle.HiltViewModel
import it.polimi.rewatch.common.BaseViewModel
import it.polimi.rewatch.common.TmdbRepository
import it.polimi.rewatch.model.Media
import it.polimi.rewatch.model.MediaType
import jakarta.inject.Inject
import java.time.LocalDate

interface BaseMedia {
    val id: Int
    val type: MediaType
    val mediaData: Media
    val rating: Float? get() = null
    val title: String? get() = null
    val popularity: Float? get() = null // Only Watchlist has this
    val date: LocalDate? get() = null         // Only Watched has this
}

data class WatchedMedia(
    override val id: Int,
    override val type: MediaType,
    override val mediaData: Media = Media(type),
    override val rating: Float?,
    override val date: LocalDate,
) : BaseMedia {
    override val title get() = mediaData.title
}
data class WatchlistMedia(
    override val id: Int,
    override val type: MediaType,
    override val mediaData: Media = Media(type),
) : BaseMedia {
    override val rating: Float get() = mediaData.rating
    override val title get() = mediaData.title
    override val popularity get() = mediaData.popularity
}

data class TrackingData(
    val watchlist: List<WatchlistMedia>,
    val watched: List<WatchedMedia>,
)

@HiltViewModel
class TrackingViewModel @Inject constructor(
    private val repository: TmdbRepository,
) : BaseViewModel<TrackingData>() {

    init {
        executeFetch()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun fetchData() = repository.getTrackingData()
}

// fake list builders
fun fakeWatchlist(): List<WatchlistMedia> {
    return buildList {
        add(
            WatchlistMedia(
                id = 550,
                type = MediaType.MOVIE,
            ),
        )
        add(
            WatchlistMedia(
                id = 83533,
                type = MediaType.MOVIE,
            ),
        )
        add(
            WatchlistMedia(
                id = 1317288,
                type = MediaType.MOVIE,
            ),
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun fakeWatched(): List<WatchedMedia> = listOf(
    WatchedMedia(
        id = 1390,
        type = MediaType.TV_SHOW,
        rating = 8.5f,
        date = LocalDate.of(2026, 3, 23),
    ),
    WatchedMedia(
        id = 1399,
        type = MediaType.TV_SHOW,
        rating = 8.5f,
        date = LocalDate.of(2023, 3, 23),
    ),
    WatchedMedia(
        id = 1393,
        type = MediaType.TV_SHOW,
        rating = 8.5f,
        date = LocalDate.of(2024, 3, 23),
    ),
    WatchedMedia(
        id = 1398,
        type = MediaType.TV_SHOW,
        rating = 8.5f,
        date = LocalDate.of(2026, 4, 23),
    ),
    WatchedMedia(
        id = 1394,
        type = MediaType.TV_SHOW,
        rating = 8.5f,
        date = LocalDate.of(2026, 3, 23),
    ),
    WatchedMedia(
        id = 1319,
        type = MediaType.TV_SHOW,
        rating = 8.5f,
        date = LocalDate.of(2026, 3, 23),
    ),
    WatchedMedia(
        id = 1395,
        type = MediaType.TV_SHOW,
        rating = 8.5f,
        date = LocalDate.of(2026, 3, 23),
    ),
    WatchedMedia(
        id = 1349,
        type = MediaType.TV_SHOW,
        rating = 8.5f,
        date = LocalDate.of(2026, 3, 23),
    ),
    WatchedMedia(
        id = 1329,
        type = MediaType.TV_SHOW,
        rating = 8.5f,
        date = LocalDate.of(2026, 3, 23),
    ),
)

