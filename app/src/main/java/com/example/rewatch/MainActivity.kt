package com.example.rewatch

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.moviebase.tmdb.Tmdb3
import app.moviebase.tmdb.model.AppendResponse
import app.moviebase.tmdb.model.TmdbMovie
import app.moviebase.tmdb.model.TmdbMovieDetail
import com.example.rewatch.ui.theme.RewatchTheme
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(
                scrim = Color.TRANSPARENT,
            )
        )
        setContent {
            RewatchTheme {
                RewatchApp()
            }
        }
    }
}

@PreviewScreenSizes
@Composable
fun RewatchApp() {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
                item(
                    icon = {
                        when {
                            it.imgVec != null -> Icon(
                                imageVector = it.imgVec,
                                contentDescription = it.label
                            )

                            else -> Icon(
                                painter = painterResource(id = it.intIcon!!),
                                contentDescription = it.label,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    label = { Text(it.label) },
                    selected = it == currentDestination,
                    onClick = { currentDestination = it }
                )
            }
        }
    ) {
        Surface(modifier = Modifier.fillMaxSize()) {
            MoviePageState()
        }
    }
}

data class MovieData(
    var movie: TmdbMovieDetail,
    var similar: List<TmdbMovie>
)

sealed class MovieUiState {
    object Loading : MovieUiState()
    data class Success(val movie: MovieData) : MovieUiState()
    data class Error(val msg: String) : MovieUiState()
}

class MovieViewModel : ViewModel() {
    private val tmdb = Tmdb3(BuildConfig.TMDB_API_KEY)

    var uiState by mutableStateOf<MovieUiState>(MovieUiState.Loading)
        private set

    init {
        fetchMovies()
    }

    private fun fetchMovies() {
        viewModelScope.launch {
            try {
                val movieId = 1317288//83533
                uiState = MovieUiState.Success(
                    MovieData(
                        tmdb.movies.getDetails(
                            movieId = movieId,
//                            language = "IT",
                            appendResponses = listOf(
                                AppendResponse.CREDITS,
                                AppendResponse.REVIEWS,
                                AppendResponse.RELEASES_DATES
                            )
                        ),
                        tmdb.movies.getSimilar(
                            movieId = movieId,
                        ).results
                    )
                )

            } catch (e: Exception) {
                uiState = MovieUiState.Error(e.localizedMessage ?: "Network Error")
            }
        }
    }
}

@Composable
private fun MoviePageState(vm: MovieViewModel = MovieViewModel()) {
    when (val state = vm.uiState) {
        is MovieUiState.Success -> MoviePage(state.movie)
        else -> {}
    }
}


enum class AppDestinations(
    val label: String,
    val imgVec: ImageVector? = null,
    val intIcon: Int? = null,
) {
    HOME("Home", Icons.Default.Home),
    SEARCH("Search", Icons.Default.Search),
    MOVIE("Movies", intIcon = R.drawable.movie_svgrepo_com),
    TV("Tv Shows", intIcon = R.drawable.tv_svgrepo_com),
    PROFILE("Profile", Icons.Default.AccountBox),
}

