package it.polimi.rewatch.screens.media

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import it.polimi.rewatch.common.PersistRepository
import it.polimi.rewatch.common.TmdbRepository
import it.polimi.rewatch.common.UiFetchViewModel
import it.polimi.rewatch.model.MediaDetail
import it.polimi.rewatch.model.MediaId
import it.polimi.rewatch.model.PosterInfo
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class MovieData(
    var movie: MediaDetail, var similar: List<PosterInfo>,
)

@HiltViewModel(assistedFactory = MediaViewModel.Factory::class)
class MediaViewModel @AssistedInject constructor(
    @Assisted private val mediaId: MediaId,
    private val repository: TmdbRepository,
    persistRepo: PersistRepository,
) : UiFetchViewModel<MovieData>() { // <-- Changed to your unified MediaDetail class
    @AssistedFactory
    interface Factory {
        fun create(mediaId: MediaId): MediaViewModel
    }

    @RequiresApi(Build.VERSION_CODES.O)
    val watchedListItem = persistRepo.watchedFlow.map { result -> result.getOrNull()?.find { it.id == mediaId } }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily, // Keeps it alive briefly during config changes
        initialValue = null, // The starting value while it searches
    )

    @RequiresApi(Build.VERSION_CODES.O)
    val watchlistItem = persistRepo.watchlistFlow.map { result -> result.getOrNull()?.find { it.id == mediaId } }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily, // Keeps it alive briefly during config changes
        initialValue = null, // The starting value while it searches
    )

    init {
        executeFetch()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun fetchData() = mediaId.getData(repository)
}