package it.polimi.rewatch.screens.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import app.moviebase.tmdb.model.TmdbMovie
import it.polimi.rewatch.components.AnimatedUiState
import it.polimi.rewatch.components.PosterItem
import it.polimi.rewatch.model.MediaType

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
            animationDurationMillis = 100,
            modifier = Modifier.padding(top = paddingValues.calculateTopPadding()),
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
    var expandedSectionIds by rememberSaveable { mutableStateOf(emptySet<String>()) }
    val handleToggle: (String) -> Unit = { id ->
        expandedSectionIds = if (id in expandedSectionIds) expandedSectionIds - id else expandedSectionIds + id
    }

    LazyVerticalGrid(
        // This creates exactly 4 columns, allocating 1/4 of the width to each
        columns = GridCells.Fixed(4),
        // Adds 5.dp spacing between the columns
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp),
        // Optional: Adds padding around the entire grid so the edges aren't flush against the screen
        contentPadding = PaddingValues(5.dp),
        modifier = Modifier
            .fillMaxSize()
            .animateContentSize()
            .padding(horizontal = 7.dp),
        // <--- Put it here!
    ) {
        movieSection(
            "Trending", data.trending,
            expandedSectionIds = expandedSectionIds,
            onToggleSection = handleToggle,
        )
        movieSection(
            "Top Rated", data.popular,
            expandedSectionIds = expandedSectionIds,
            onToggleSection = handleToggle,
        )
        movieSection(
            "Recent", data.recent,
            expandedSectionIds = expandedSectionIds,
            onToggleSection = handleToggle,
        )
    }
}

fun LazyGridScope.movieSection(
    title: String,
    movies: List<TmdbMovie>,
    expandedSectionIds: Set<String>,
    onToggleSection: (String) -> Unit,
) {
    val isExpanded = expandedSectionIds.contains(title)
    val displayedMovies = if (isExpanded) movies else movies.take(12)

    item(key = "header_$title", span = { GridItemSpan(4) }) {
        Text(
            text = title,
            color = Color.White,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier
                .padding(bottom = 15.dp)
                .animateItem(fadeInSpec = null, fadeOutSpec = null),
        )
    }
    // --- Griglia Adattiva ---
    items(
        items = displayedMovies,
        key = { "${title}_${it.id}" },
    ) { // CRUCIAL: Compose needs this to track which items are new) {
        PosterItem(
            it.id.toString(), it.posterPath, it.title, MediaType.MOVIE,
            Modifier.animateItem(fadeInSpec = null, fadeOutSpec = null),
        )
    }
    // --- Pulsante Dinamico ---
    item(key = "button_$title", span = { GridItemSpan(4) }) {
        if (movies.size > 8) {
            HomeButton(
                string = if (isExpanded) "Show Less" else "More",
                isExpanded = isExpanded,
                onClick = { onToggleSection(title) },
                Modifier.animateItem(fadeInSpec = null, fadeOutSpec = null),
            )
        }
    }
}

@Composable
fun HomeButton(
    string: String,
    isExpanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth(),
    ) {
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .clickable(onClick = onClick)
                .padding(horizontal = 24.dp, vertical = 8.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = string,
                    style = MaterialTheme.typography.labelLarge,
                )

                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp
                    else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                )
            }
        }
    }
}