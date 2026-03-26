package it.polimi.rewatch.screens.media

import android.os.Build
import androidx.annotation.RequiresApi
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import it.polimi.rewatch.common.BaseViewModel
import it.polimi.rewatch.common.FakeRepository
import it.polimi.rewatch.common.TmdbRepository
import it.polimi.rewatch.model.MediaDetail
import it.polimi.rewatch.model.MediaId
import it.polimi.rewatch.model.PosterMedia

data class MovieData(
    var movie: MediaDetail, var similar: List<PosterMedia>,
)

@HiltViewModel(assistedFactory = MediaViewModel.Factory::class)
class MediaViewModel @AssistedInject constructor(
    @Assisted("mediaId") private val mediaId: MediaId,
    private val repository: TmdbRepository,
    private val fakeRepository: FakeRepository,
) : BaseViewModel<MovieData>() { // <-- Changed to your unified MediaDetail class
    @AssistedFactory
    interface Factory {
        // Factory now requires both ID and Type
        fun create(
            @Assisted("mediaId") mediaId: MediaId,
        ): MediaViewModel
    }

    @RequiresApi(Build.VERSION_CODES.O)
    val watchlistItem = fakeRepository.watchlist.value.find { it.id == mediaId }

    @RequiresApi(Build.VERSION_CODES.O)
    val watchedListItem = fakeRepository.watchedList.value.find { it.id == mediaId }

    init {
        executeFetch()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun fetchData() = mediaId.getData(repository)
}