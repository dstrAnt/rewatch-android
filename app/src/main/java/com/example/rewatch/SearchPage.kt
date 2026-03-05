package com.example.rewatch

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import app.moviebase.tmdb.Tmdb3
import app.moviebase.tmdb.model.TmdbMovie
import coil3.compose.AsyncImage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn

@OptIn(FlowPreview::class)
class SearchViewModel : ViewModel() {

    // Initialize TMDB client here (or inject via Hilt/Koin in a real app)
    private val tmdb = Tmdb3 {
        tmdbApiKey = BuildConfig.TMDB_API_KEY
    }

    // Backing property for the search text
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    // Automatically reacts to query changes, debounces, and fetches movies
    @OptIn(ExperimentalCoroutinesApi::class)
    val movies: StateFlow<List<TmdbMovie>> =
        _query.debounce(300) // Wait 300ms after the user stops typing
            .mapLatest { currentQuery ->
                if (currentQuery.isBlank()) {
                    emptyList()
                } else {
                    try {
                        tmdb.search.findMovies(query = currentQuery, page = 1).results
                    } catch (e: Exception) {
                        e.printStackTrace()
                        emptyList() // Return empty list on network error
                    }
                }
            }.stateIn(
                scope = viewModelScope, started = SharingStarted.Lazily, // 🟢 CHANGE THIS LINE
                initialValue = emptyList()
            )

    // Called from the UI when the user types
    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchPage(
    viewModel: SearchViewModel = viewModel(),
    onNavigateBack: () -> Unit, // Pass your NavController pop action here
) {
    val query by viewModel.query.collectAsState()
    val movies by viewModel.movies.collectAsState()

    val focusRequester = remember { FocusRequester() }

    // Intercept the back button BEFORE the SearchBar catches it,
    // and forcefully navigate back out of the page.


    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(Modifier.fillMaxSize()) {
        SearchBar(
            inputField = {
                SearchBarDefaults.InputField(
                    query = query,
                    onQueryChange = viewModel::onQueryChange,
                    onSearch = { /* Handled automatically */ },
                    expanded = true,
                    onExpandedChange = { /* Locked */ },
                    placeholder = { Text("Search movies...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    modifier = Modifier.focusRequester(focusRequester)
                )
            },
            expanded = true,
            onExpandedChange = { /* Locked */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            BackHandler {
                onNavigateBack()
            }
            MovieResultsList(
                movies
            )
        }
    }
}

@Composable
fun MovieResultsList(
    movies: List<TmdbMovie>
) {
    val context = LocalContext.current
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(movies) { movie ->
            ListItem(headlineContent = { movie.title?.let { Text(it) } }, leadingContent = {

                PosterImage({ onMovieClick(movie.id, context) }) {
                    AsyncImage(
                        model = "https://image.tmdb.org/t/p/w200${movie.posterPath}",
                        contentDescription = "${movie.title} poster",
                        modifier = Modifier.size(60.dp, 90.dp),
                        contentScale = ContentScale.Crop
                    )
                }
            }, modifier = Modifier.clickable {
                // 2. Trigger callback with the movie ID
                onMovieClick(movie.id, context)
            })
        }
    }
}