package it.polimi.rewatch.screens.home

import dagger.hilt.android.lifecycle.HiltViewModel
import it.polimi.rewatch.common.TmdbRepository
import it.polimi.rewatch.common.UiFetchViewModel
import it.polimi.rewatch.model.PosterInfo
import jakarta.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

// Dati che compongono la nostra Homepage
data class MediaData(
    val trending: List<PosterInfo>,
    val popular: List<PosterInfo>,
    val recent: List<PosterInfo>,
)

data class HomeData(
    val movieData: MediaData,
    val showData: MediaData,
)


@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: TmdbRepository,
) : UiFetchViewModel<HomeData>() {

    init {
        executeFetch()
    }

    override suspend fun fetchData(): HomeData = coroutineScope {
        val moviesDeferred = async { repository.getHomeMovieData() }
        val showsDeferred = async { repository.getHomeShowData() }

        HomeData(
            movieData = moviesDeferred.await(),
            showData = showsDeferred.await(),
        )
    }
}