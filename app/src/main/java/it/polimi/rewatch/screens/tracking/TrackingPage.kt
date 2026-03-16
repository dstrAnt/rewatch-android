package it.polimi.rewatch.screens.tracking

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import it.polimi.rewatch.R
import it.polimi.rewatch.common.CustomLazyGrid
import it.polimi.rewatch.common.CustomTabRow
import it.polimi.rewatch.components.AnimatedUiState
import it.polimi.rewatch.components.PosterItem
import it.polimi.rewatch.components.StarRating
import it.polimi.rewatch.model.Media
import it.polimi.rewatch.model.MediaType
import it.polimi.rewatch.screens.home.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackingPage(
    viewModel: HomeViewModel = hiltViewModel(),
    type: MediaType,
    onNavigateBack: () -> Unit,
) {
    val title = if (type == MediaType.MOVIE) "Movies" else "Tv Shows"
    val icon = if (type == MediaType.MOVIE) R.drawable.movie else R.drawable.tv
    val tabs = WatchTabItem.entries
    var selectedTab by remember { mutableStateOf(tabs.first()) }

    Scaffold(
        topBar = {
            TopTabBar(
                icon,
                title,
                tabs,
                selectedTab,
                { selectedTab = it },
            )
        },
    ) { paddingValues ->
        AnimatedUiState(
            state = viewModel.uiState,
            animationDurationMillis = 100,
            modifier = Modifier.padding(top = paddingValues.calculateTopPadding()),
            loadingContent = ::TrackingSkeleton,
        ) { data ->
            CustomLazyGrid {
                val items = when (selectedTab) {
                    WatchTabItem.WATCHLIST -> data.trending.map(::Media)
                    WatchTabItem.WATCHED -> data.popular.map(::Media)
                }

                items(key = { "${it.id}" }, items = items) {
                    Column(
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        PosterItem(it, Modifier.animateItem(fadeInSpec = null, fadeOutSpec = null))
                        StarRating(rating = 9.0, starSize = 10.dp)
                    }
                }
            }
        }
    }
}

@Composable
private fun TopTabBar(
    icon: Int,
    title: String,
    tabs: List<WatchTabItem>,
    selectedTab: WatchTabItem,
    onClick: (WatchTabItem) -> Unit,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars), // Keeps it below the system status bar
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(55.dp), // Standard M3 SearchBar height
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
            )
        }
        CustomTabRow(
            availableTabs = tabs,
            onClick = onClick,
            title = { it.title },
            tabState = selectedTab,
        )
    }
}

enum class WatchTabItem(val title: String) {
    WATCHLIST("Watchlist"), WATCHED("Watched")
}