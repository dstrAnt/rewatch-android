package it.polimi.rewatch.screens.tracking

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import it.polimi.rewatch.common.layoutEvade
import it.polimi.rewatch.components.AnimatedUiState
import it.polimi.rewatch.components.PosterItem
import it.polimi.rewatch.components.StarRating
import it.polimi.rewatch.model.Media
import it.polimi.rewatch.model.MediaType
import it.polimi.rewatch.screens.home.HomeData
import it.polimi.rewatch.screens.home.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackingPage(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Tracking",
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    scrolledContainerColor = Color.Unspecified,
                    navigationIconContentColor = Color.Unspecified,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.Unspecified,
                ),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = Color.White,
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        AnimatedUiState(
            state = viewModel.uiState,
            animationDurationMillis = 100,
            modifier = Modifier.padding(top = paddingValues.calculateTopPadding()),
            loadingContent = ::TrackingSkeleton,
        ) { data ->
            TrackingContent(data)
        }
    }
}

data class WatchTabItem(
    val title: String,
    val list: List<Media>,
)

@Composable
fun TrackingContent(data: HomeData) {
    val watchlist = data.trending.map(::Media)
    val watched = data.popular.map(::Media)
    val tabs = buildList {
        add(WatchTabItem(title = "Watchlist", list = watchlist))
        add(WatchTabItem(title = "Watched", list = watched))
    }

    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }

    val selectedTab = tabs[selectedTabIndex]

    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp),
        modifier = Modifier
            .fillMaxSize()
            .animateContentSize()
            .padding(horizontal = 7.dp),
    ) {
        item(
            key = "Tabs",
            span = { GridItemSpan(maxLineSpan) },
        ) {
            Column(
                Modifier.layoutEvade(),
            ) {
                PrimaryTabRow(
                    selectedTabIndex = selectedTabIndex,
                ) {
                    tabs.forEachIndexed { index, tab ->
                        Tab(
                            selected = index == selectedTabIndex,
                            onClick = { selectedTabIndex = index },
                            text = {
                                Text(tab.title)
                            },
                        )
                    }
                }
            }
        }

        items(
            key = { "${it.id}" },
            items = selectedTab.list,
        ) {
            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                PosterItem(
                    it.id.toString(), it.posterPath, it.title, MediaType.MOVIE,
                    Modifier.animateItem(fadeInSpec = null, fadeOutSpec = null),
                )
                StarRating(rating = 9.0, starSize = 10.dp)
            }
        }
    }
}