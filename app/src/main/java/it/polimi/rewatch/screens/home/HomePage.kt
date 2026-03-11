package it.polimi.rewatch.screens.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import app.moviebase.tmdb.model.TmdbMovie
import it.polimi.rewatch.common.ExpandBtn
import it.polimi.rewatch.common.PosterItem
import it.polimi.rewatch.components.AnimatedUiState
import it.polimi.rewatch.screens.media.MediaType

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePage(
    viewModel: HomeViewModel = hiltViewModel(),
) {
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
        AnimatedUiState(
            state = viewModel.uiState,
            modifier = Modifier.padding(top = paddingValues.calculateTopPadding() + 10.dp),
            loadingContent = ::HomeSkeleton,
        ) { data ->
            HomeContent(data)
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
    val displayedMovies = if (isExpanded) movies else movies.take(12)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            // 1. APPLICA L'ANIMAZIONE QUI
            .animateContentSize(),
    ) {
        // --- Header ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 15.dp),
        ) {
            Text(
                text = title,
                color = Color.White,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier
                    .padding(bottom = 15.dp)
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
                    rowMovies.forEach {
                        Box(modifier = Modifier.weight(1f)) {
                            PosterItem(it.id, it.posterPath, title, MediaType.MOVIE)
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
                string = if (isExpanded) "Show Less" else "More",
                isExpanded = isExpanded,
                onClick = { isExpanded = !isExpanded },
            )
        }
    }
}

