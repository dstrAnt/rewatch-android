package com.example.rewatch.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.moviebase.tmdb.Tmdb3
import app.moviebase.tmdb.model.TmdbDiscover
import app.moviebase.tmdb.model.TmdbDiscoverMovieSortBy
import app.moviebase.tmdb.model.TmdbDiscoverTimeRange
import app.moviebase.tmdb.model.TmdbMovie
import app.moviebase.tmdb.model.TmdbTimeWindow
import com.example.rewatch.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

// Dati che compongono la nostra Homepage
data class HomeData(
    val trending: List<TmdbMovie>,
    val popular: List<TmdbMovie>,
    val recent: List<TmdbMovie>,
)

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(val data: HomeData) : HomeUiState()
    data class Error(val msg: String) : HomeUiState()
}

@RequiresApi(Build.VERSION_CODES.O)
class HomeViewModel : ViewModel() {
    private val tmdb = Tmdb3(BuildConfig.TMDB_API_KEY)

    var uiState by mutableStateOf<HomeUiState>(HomeUiState.Loading)
        private set

    init {
        fetchHomeContent()
    }

    fun fetchHomeContent() = viewModelScope.launch {
        uiState = HomeUiState.Loading
        uiState = try {
            val data = getHomeDataFromApi()
            HomeUiState.Success(data)
        } catch (e: Exception) {
            HomeUiState.Error(e.localizedMessage ?: "Errore di connessione")
        }
    }

    private suspend fun getHomeDataFromApi(): HomeData = withContext(Dispatchers.IO) {
        // Calcoliamo una data di 3 mesi fa per il filtro "recenti"
        val threeMonthsAgo = LocalDate.now().minusMonths(3).toString()

        // Eseguiamo le chiamate in parallelo per velocizzare il caricamento
        val trendingDeferred = async {
            tmdb.trending.getTrendingMovies(TmdbTimeWindow.DAY, 1).results
        }
        val topDeferred = async {
            tmdb.movies.getTopRated().results
        }
        val recentDeferred = async {

            // Creiamo l'oggetto di configurazione richiesto dalla funzione
            val discoverParams = TmdbDiscover.Movie(
                sortBy = TmdbDiscoverMovieSortBy.POPULARITY,
                releaseDate = TmdbDiscoverTimeRange.Custom(threeMonthsAgo),
                voteCountGte = 50,
            )

            // Chiamiamo la funzione che hai postato sopra
            tmdb.discover.discoverMovie(
                page = 1,
                discover = discoverParams,
            ).results
        }

        HomeData(
            trending = trendingDeferred.await(),
            popular = topDeferred.await(),
            recent = recentDeferred.await(),
        )
    }
}