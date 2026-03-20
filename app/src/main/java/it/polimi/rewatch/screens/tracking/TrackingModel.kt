package it.polimi.rewatch.screens.tracking

import android.os.Build
import androidx.annotation.RequiresApi
import dagger.hilt.android.lifecycle.HiltViewModel
import it.polimi.rewatch.common.BaseViewModel
import it.polimi.rewatch.common.FakeRepository
import it.polimi.rewatch.common.TmdbRepository
import it.polimi.rewatch.model.WatchedMedia
import it.polimi.rewatch.model.WatchlistMedia
import jakarta.inject.Inject

data class TrackingData(
    val watchlist: List<WatchlistMedia>,
    val watched: List<WatchedMedia>,
)

@HiltViewModel
class TrackingViewModel @Inject constructor(
    private val repository: TmdbRepository,
    private val fakeRepo: FakeRepository,
) : BaseViewModel<TrackingData>() {

    init {
        executeFetch()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun fetchData() = repository.getTrackingData(fakeRepo)
}

@HiltViewModel
class BottomSheetViewModel @Inject constructor(
    private val repository: TmdbRepository,
    private val fakeRepo: FakeRepository,
) : BaseViewModel<TrackingData>() {

    init {
        executeFetch()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun fetchData() = repository.getTrackingData(fakeRepo)
}
