package it.polimi.rewatch.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import it.polimi.rewatch.R
import it.polimi.rewatch.common.CustomLazyGrid
import it.polimi.rewatch.common.CustomTabRow
import it.polimi.rewatch.common.Logo
import it.polimi.rewatch.common.NavigationActions
import it.polimi.rewatch.common.headerColor
import it.polimi.rewatch.components.AnimatedUiState
import it.polimi.rewatch.components.PosterItem
import it.polimi.rewatch.model.PosterInfo

enum class HomeTabItem(val title: String) {
    MOVIES("Movies"), SHOWS("TV Shows")
}

@Composable
private fun HomeTopBar(
    tabs: List<HomeTabItem>,
    selectedTab: HomeTabItem,
    onClick: (HomeTabItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(Color.Black)
            .then(modifier)
            .windowInsetsPadding(WindowInsets.statusBars), // Keeps it below the system status bar
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Logo()
            Spacer(Modifier.width(10.dp))
            Text(
                text = "ReWatch",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            )
        }
        CustomTabRow(
            availableTabs = tabs,
            onClick = onClick,
            title = { it.title },
            tabState = selectedTab,
            containerColor = headerColor,
            divider = {},
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePage(
    viewModel: HomeViewModel = hiltViewModel(),
    actions: NavigationActions,
    outerPadding: PaddingValues,
) {
    val tabs = HomeTabItem.entries
    var selectedTab by rememberSaveable { mutableStateOf(tabs.first()) }

    Scaffold(
        topBar = {
            HomeTopBar(
                tabs, selectedTab, { selectedTab = it },
                modifier = Modifier.padding(
                    start = outerPadding.calculateStartPadding(LayoutDirection.Ltr),
                    end = outerPadding.calculateEndPadding(LayoutDirection.Ltr),
                ),
            )
        },
    ) { paddingValues ->
        AnimatedUiState(
            state = viewModel.uiState,
            animationDurationMillis = 100,
            //            contentPadding = PaddingValues(
//                start = padding.calculateStartPadding(LayoutDirection.Ltr),
//                end = padding.calculateEndPadding(LayoutDirection.Ltr),
//            ),
            modifier = Modifier.padding(
                top = paddingValues.calculateTopPadding(),
                start = outerPadding.calculateStartPadding(LayoutDirection.Ltr),
                end = outerPadding.calculateEndPadding(LayoutDirection.Ltr),
                bottom = outerPadding.calculateBottomPadding(),
            ),
            loadingContent = ::HomeSkeleton,
        ) { data ->
            HomeContent(
                data,
                actions,
                selectedTab,
            )
        }
    }
}

@Composable
fun HomeContent(
    data: HomeData,
    actions: NavigationActions,
    selectedTab: HomeTabItem,
) {
    var expandedSectionIds by rememberSaveable { mutableStateOf(emptySet<String>()) }
    val handleToggle: (String) -> Unit = { id ->
        expandedSectionIds = if (id in expandedSectionIds) expandedSectionIds - id else expandedSectionIds + id
    }
    val data = when (selectedTab) {
        HomeTabItem.MOVIES -> data.movieData
        HomeTabItem.SHOWS -> data.showData
    }

    CustomLazyGrid {
        listOf(
            Triple("Trending", data.trending, R.drawable.trending),
            Triple("Top Rated", data.popular, R.drawable.stars),
            Triple("Newest", data.recent, R.drawable.spark),
        )
            .forEach { (title, data, icon) ->
                movieSection(
                    title, data, icon,
                    cellsPerRow = it,
                    expandedSectionIds = expandedSectionIds,
                    onToggleSection = handleToggle,
                    actions,
                )
            }
        //BottomOverlayStretchExample()
    }
}


fun LazyGridScope.movieSection(
    title: String,
    movies: List<PosterInfo>,
    icon: Int,
    cellsPerRow: Int,
    expandedSectionIds: Set<String>,
    onToggleSection: (String) -> Unit,
    actions: NavigationActions,
) {
    val isExpanded = expandedSectionIds.contains(title)
    val displayedMovies = if (isExpanded) movies else movies.take(cellsPerRow * 3)

    item(key = "header_$title", span = { GridItemSpan(maxLineSpan) }) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(painterResource(icon), null, Modifier.size(24.dp), Color.White)
            Text(
                text = title,
                color = Color.White,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .animateItem(fadeInSpec = null, fadeOutSpec = null),
            )
        }
    }
    // --- Adaptive Grid ---
    items(
        items = displayedMovies,
        key = { "${title}_${it.id}" },
    ) { // CRUCIAL: Compose needs this to track which items are new
        PosterItem(
            it,
            actions,
            Modifier.animateItem(fadeInSpec = null, fadeOutSpec = null),
        )
    }
    // --- Dynamic Button ---
    item(key = "button_$title", span = { GridItemSpan(maxLineSpan) }) {
        if (movies.size > cellsPerRow * 3) {
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