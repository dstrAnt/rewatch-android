package com.example.rewatch.movie

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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

    private fun fetchMovies() {
        viewModelScope.launch {
            try {

                uiState = withContext(Dispatchers.IO) {

                    MovieUiState.Success(
                        MovieData(
                            tmdb.movies.getDetails(
                                movieId = movieId,
//                            language = "IT",
                                appendResponses = listOf(
                                    AppendResponse.CREDITS,
                                    AppendResponse.REVIEWS,
                                    AppendResponse.RELEASES_DATES,
                                    AppendResponse.WATCH_PROVIDERS,
                                )
                            ), tmdb.movies.getSimilar(
                                movieId = movieId,
                            ).results
                        )
                    )
                }

            } catch (e: Exception) {
                uiState = MovieUiState.Error(
                    e.localizedMessage ?: "Network Error"
                )
            }
        }
    }
}

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class
)
@Composable
fun MoviePageState(
    movieId: Int,
    vm: MovieViewModel = MovieViewModel(movieId),
    onNavigateBack: () -> Unit,
    padding: PaddingValues
) {
    when (val state = vm.uiState) {
        is MovieUiState.Loading -> MoviePage(
            MovieData(
                TmdbMovieDetail(), listOf()
            ), onNavigateBack, padding
        )

        is MovieUiState.Success -> MoviePage(
            state.movie, onNavigateBack, padding
        )


        else -> Box(
            modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
        ) {
            // This is the official M3 "Expressive" component
            CircularWavyProgressIndicator(
                modifier = Modifier.size(48.dp),
//                color = MaterialTheme.colorScheme.primary,
                // You can also adjust the amplitude of the waves if needed
            )
        }

    }
}
