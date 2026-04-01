package it.polimi.rewatch.screens.home

import dagger.hilt.android.lifecycle.HiltViewModel
import it.polimi.rewatch.common.TmdbRepository
import it.polimi.rewatch.common.UiFetchViewModel
import it.polimi.rewatch.model.PosterInfo
import jakarta.inject.Inject

// Dati che compongono la nostra Homepage
data class HomeData(
    val trending: List<PosterInfo>,
    val popular: List<PosterInfo>,
    val recent: List<PosterInfo>,
)


@HiltViewModel // Tells Hilt to manage this ViewModel
class HomeViewModel @Inject constructor(
    private val repository: TmdbRepository,
) : UiFetchViewModel<HomeData>() {

    init {
        executeFetch()
    }

    // We implement the abstract method to provide the actual HomeData
    override suspend fun fetchData() = repository.getHomeData()
}