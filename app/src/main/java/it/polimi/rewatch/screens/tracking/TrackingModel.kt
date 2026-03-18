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
}

data class WatchedMedia(
    override val id: Int,
    override val type: MediaType,
    override val mediaData: Media = Media(type),
    val rating: Float?,
    val date: LocalDate,
) : BaseMedia

data class WatchlistMedia(
    override val id: Int,
    override val type: MediaType,
    override val mediaData: Media = Media(type),
) : BaseMedia

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
fun fakeWatched(): List<WatchedMedia> {
    return buildList {
        add(
            WatchedMedia(
                id = 1399,
                type = MediaType.TV_SHOW,
                rating = 8.5f,
                date = LocalDate.of(2026, 3, 23),
            ),
        )
    }
}