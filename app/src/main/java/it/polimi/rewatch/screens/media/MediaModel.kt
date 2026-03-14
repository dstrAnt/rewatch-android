package it.polimi.rewatch.screens.media

import android.os.Build
import androidx.annotation.RequiresApi
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import it.polimi.rewatch.common.BaseViewModel
import it.polimi.rewatch.common.TmdbRepository

data class MovieData(
    var movie: MediaDetail, var similar: List<Media>,
)

enum class MediaType {
    MOVIE, TV_SHOW, EPISODE
}

@HiltViewModel(assistedFactory = MediaViewModel.Factory::class)
class MediaViewModel @AssistedInject constructor(
    @Assisted("mediaId") private val mediaId: String,
    @Assisted("mediaType") private val mediaType: MediaType,
    private val repository: TmdbRepository,
) : BaseViewModel<MovieData>() { // <-- Changed to your unified MediaDetail class
    @AssistedFactory
    interface Factory {
        // Factory now requires both ID and Type
        fun create(
            @Assisted("mediaId") mediaId: String,
            @Assisted("mediaType") mediaType: MediaType,
        ): MediaViewModel
    }

    init {
        executeFetch()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun fetchData() =
        // Switch the repository call based on the media type
        when (mediaType) {
            MediaType.MOVIE -> repository.movieData(mediaId.toInt())
            MediaType.TV_SHOW -> repository.tvData(mediaId.toInt())
            MediaType.EPISODE -> repository.episodeData(mediaId)
        }
}