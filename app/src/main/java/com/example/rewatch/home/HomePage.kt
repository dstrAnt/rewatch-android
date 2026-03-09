package com.example.rewatch.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.moviebase.tmdb.model.TmdbMovie
import coil3.compose.AsyncImage
import com.example.rewatch.ExpandBtn
import com.example.rewatch.PosterImage
import com.example.rewatch.onMovieClick

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomePage(
    viewModel: HomeViewModel = HomeViewModel(),
) {
    val uiState = viewModel.uiState

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Rewatch",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    scrolledContainerColor = Color.Unspecified,
                    navigationIconContentColor = Color.Unspecified,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.Unspecified,
                ),
            )
        },
    ) { paddingValues ->

        // Transizione fluida tra caricamento e contenuti
        AnimatedContent(
            targetState = uiState,
            transitionSpec = { fadeIn(animationSpec = tween(700)) togetherWith fadeOut() },
            modifier = Modifier.padding(top = paddingValues.calculateTopPadding()),
            label = "homeTransition",
        ) { state ->
            when (state) {
                is HomeUiState.Loading -> {
//                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//                        CircularWavyProgressIndicator(modifier = Modifier.size(48.dp))
//                    }
                    HomeSkeleton()
                }

                is HomeUiState.Success -> HomeContent(state.data)
                is HomeUiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Error: ${state.msg}")
                    }
                }
            }
        }
    }
}

@Composable
fun HomeContent(
    data: HomeData,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
//        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        item { MovieSection("Trending", data.trending) }
        item { MovieSection("Top Rated", data.popular) }
        item { MovieSection("Recent", data.recent) }
    }
}

@Composable
fun MovieSection(
    title: String,
    movies: List<TmdbMovie>,
) {
    var isExpanded by remember { mutableStateOf(false) }
    val displayedMovies = if (isExpanded) movies else movies.take(8)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp)
            // 1. APPLICA L'ANIMAZIONE QUI
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy, // Un leggero effetto molla
                    stiffness = Spring.StiffnessLow, // Movimento fluido e non troppo veloce
                ),
            ),
    ) {
        // --- Header ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        ) {
            Text(
                text = title,
                color = Color.White,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .weight(1f),
            )
        }

        // --- Griglia Adattiva ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            displayedMovies.chunked(4).forEach { rowMovies ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    rowMovies.forEach { movie ->
                        Box(modifier = Modifier.weight(1f)) {
                            MovieItem(movie)
                        }
                    }
                    if (rowMovies.size < 4) {
                        repeat(4 - rowMovies.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // --- Pulsante Dinamico ---
        if (movies.size > 8) {
            ExpandBtn(
                string = if (isExpanded) "Show Less" else "Expand",
                isExpanded = isExpanded,
                onClick = { isExpanded = !isExpanded },
            )
        }
    }
}

@Composable
fun MovieItem(
    movie: TmdbMovie,
) {
    val context = LocalContext.current

    PosterImage(
        onClick = { onMovieClick(movie.id, context) },
    ) {
        AsyncImage(
            model = "https://image.tmdb.org/t/p/w185${movie.posterPath}",
            contentDescription = movie.title,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f), // Rapporto classico poster (es: 100w x 150h)
            contentScale = ContentScale.Crop,
        )
    }
}