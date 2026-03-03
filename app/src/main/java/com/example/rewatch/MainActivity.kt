package com.example.rewatch

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


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

@OptIn(ExperimentalMaterial3Api::class)
@PreviewScreenSizes
@Composable
fun RewatchApp() {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }

    Scaffold(
        bottomBar = {
            // Usiamo direttamente NavigationBar per avere il controllo totale
            NavigationBar(
                modifier = Modifier.height(60.dp), // Ecco i tuoi 60dp
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp // Opzionale: rimuove l'ombra se vuoi un look più flat
            ) {
                AppDestinations.entries.forEach { destination ->
                    val isSelected = destination == currentDestination

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentDestination = destination },
                        label = null, // Rimuoviamo il testo
                        alwaysShowLabel = false,
                        icon = {
                            val iconModifier =
                                if (destination.imgVec == null) Modifier.size(20.dp) else Modifier
                            if (destination.imgVec != null) {
                                Icon(
                                    imageVector = destination.imgVec,
                                    contentDescription = destination.label
                                )
                            } else {
                                Icon(
                                    painter = painterResource(id = destination.intIcon!!),
                                    contentDescription = destination.label,
                                    modifier = iconModifier
                                )
                            }
                        })
                }
            }
        }) {

        Surface(
            modifier = Modifier
                .fillMaxSize()
        ) {
            when (currentDestination) {
                AppDestinations.MOVIE -> MoviePageState()
                AppDestinations.SEARCH -> SearchPage()
                else -> {}
            }
        }
    }
}

data class MovieData(
    var movie: TmdbMovieDetail, var similar: List<TmdbMovie>
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
                val movieId =
//                    1317288
                    83533
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
                uiState = MovieUiState.Error(e.localizedMessage ?: "Network Error")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun MoviePageState(vm: MovieViewModel = MovieViewModel()) {
    when (val state = vm.uiState) {
        is MovieUiState.Success -> MoviePage(state.movie)
        else -> Box(
            modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
        ) {
            // This is the official M3 "Expressive" component
            CircularWavyProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primary,
                // You can also adjust the amplitude of the waves if needed
            )
        }

    }
}


enum class AppDestinations(
    val label: String,
    val imgVec: ImageVector? = null,
    val intIcon: Int? = null,
) {
    HOME("Home", Icons.Default.Home), SEARCH("Search", Icons.Default.Search), MOVIE(
        "Movies", intIcon = R.drawable.movie_svgrepo_com
    ),
    TV("Tv Shows", intIcon = R.drawable.tv_svgrepo_com), PROFILE(
        "Profile", Icons.Default.AccountBox
    ),
}

