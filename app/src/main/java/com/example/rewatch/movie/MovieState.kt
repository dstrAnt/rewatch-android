package com.example.rewatch.movie

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.moviebase.tmdb.Tmdb3
import app.moviebase.tmdb.model.AppendResponse
import app.moviebase.tmdb.model.TmdbMovie
import app.moviebase.tmdb.model.TmdbMovieDetail
import com.example.rewatch.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


data class MovieData(
    var movie: TmdbMovieDetail, var similar: List<TmdbMovie>
)

sealed class MovieUiState {
    object Loading : MovieUiState()
    data class Success(val movie: MovieData) : MovieUiState()

    data class Error(val msg: String) : MovieUiState()
}

class MovieViewModel(private val movieId: Int) : ViewModel() {
    private val tmdb = Tmdb3(BuildConfig.TMDB_API_KEY)

    var uiState by mutableStateOf<MovieUiState>(MovieUiState.Loading)
        private set

    init {
        fetchMovies()
    }

    private fun fetchMovies() = viewModelScope.launch {
        uiState = try {
            val data = getMovieDataFromApi()
            MovieUiState.Success(data)
        } catch (e: Exception) {
            MovieUiState.Error(e.localizedMessage ?: "Network Error")
        }
    }

    private suspend fun getMovieDataFromApi(): MovieData = withContext(Dispatchers.IO) {
        val details = tmdb.movies.getDetails(
            movieId = movieId,
            appendResponses = listOf(
                AppendResponse.CREDITS,
                AppendResponse.REVIEWS,
                AppendResponse.RELEASES_DATES,
                AppendResponse.WATCH_PROVIDERS,
            )
        )
        val similar = tmdb.movies.getSimilar(movieId = movieId).results

        MovieData(
            details,
            similar
        )
    }
}

