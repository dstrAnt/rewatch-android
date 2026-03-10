package it.polimi.rewatch.screens.home

import android.os.Build
import androidx.annotation.RequiresApi
import app.moviebase.tmdb.model.TmdbMovie
import dagger.hilt.android.lifecycle.HiltViewModel
import it.polimi.rewatch.common.BaseViewModel
import it.polimi.rewatch.common.TmdbRepository
import jakarta.inject.Inject

// Dati che compongono la nostra Homepage
data class HomeData(
    val trending: List<TmdbMovie>,
    val popular: List<TmdbMovie>,
    val recent: List<TmdbMovie>,
)

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel // Tells Hilt to manage this ViewModel
class HomeViewModel @Inject constructor(
    private val repository: TmdbRepository,
) : BaseViewModel<HomeData>() {

    init {
        executeFetch()
    }

    // We implement the abstract method to provide the actual HomeData
    override suspend fun fetchData() = repository.getHomeData()
}