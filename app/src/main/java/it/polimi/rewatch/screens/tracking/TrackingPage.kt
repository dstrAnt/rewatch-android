package it.polimi.rewatch.screens.tracking

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import it.polimi.rewatch.R
import it.polimi.rewatch.common.CustomLazyGrid
import it.polimi.rewatch.common.CustomTabRow
import it.polimi.rewatch.common.NavigationActions
import it.polimi.rewatch.components.AnimatedUiState
import it.polimi.rewatch.components.PosterItem
import it.polimi.rewatch.components.StarRating
import it.polimi.rewatch.model.MediaType
import it.polimi.rewatch.model.TrackingMedia
import java.time.LocalDate

enum class SortOption(val label: String) {
    POPULARITY("Popularity"),
    RATING("Rating"),
    ALPHABETICAL("A-Z"),
    DATE("Date")
}

enum class WatchTabItem(val title: String) {
    WATCHLIST("Watchlist"), WATCHED("Watched")
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackingPage(
    viewModel: TrackingViewModel = hiltViewModel(),
    type: MediaType,
    actions: NavigationActions,
    outerPadding: PaddingValues,
) {
    val title = if (type == MediaType.MOVIE) "Movies" else "Tv Shows"
    val icon = if (type == MediaType.MOVIE) R.drawable.movie else R.drawable.tv
    val tabs = WatchTabItem.entries
    var selectedTab by remember { mutableStateOf(tabs.first()) }
    val tabSortOrders = remember {
        mutableStateMapOf(
            WatchTabItem.WATCHLIST to SortOption.POPULARITY, // Default for Watchlist
            WatchTabItem.WATCHED to SortOption.DATE,          // Default for Watched
        )
    }
    val availableSortOptions = mapOf(
        WatchTabItem.WATCHLIST to listOf(SortOption.DATE, SortOption.POPULARITY, SortOption.RATING, SortOption.ALPHABETICAL),
        WatchTabItem.WATCHED to listOf(SortOption.DATE, SortOption.RATING, SortOption.ALPHABETICAL),
    )
    val activeOrder = tabSortOrders.getValue(selectedTab)
    val watchlist by viewModel.watchlist.collectAsState()
    val watched by viewModel.watchedList.collectAsState()

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
            modifier = Modifier.padding(top = paddingValues.calculateTopPadding(), bottom = outerPadding.calculateBottomPadding()),
            loadingContent = ::TrackingSkeleton,
        ) { data ->
            CustomLazyGrid {
                // 1. CHIPS DI ORDINAMENTO (Span completo)
                item(span = { GridItemSpan(maxLineSpan) }) {
                    SortFilterChips(
                        options = availableSortOptions.getValue(selectedTab),
                        selectedSort = activeOrder,
                        onSortSelected = { newSort ->
                            // Update the map for the currently active tab
                            tabSortOrders[selectedTab] = newSort
                        },
                    )
                }

                // 1. Route to the correct list and order
                val items = when (selectedTab) {
                    WatchTabItem.WATCHLIST -> watchlist.filter { it.id.type == type }
                    WatchTabItem.WATCHED -> watched.filter { it.id.type == type }
                }
// 2. Sort and group the unified data structure
                val groupedItems = items.run {
                    when (activeOrder) {
                        SortOption.POPULARITY -> sortedByDescending { it.popularity }
                            .groupBy { getPopularityHeader(it.popularity) }

                        SortOption.RATING -> sortedByDescending { it.rating }
                            .groupBy { getRatingHeader(it.rating) }

                        SortOption.ALPHABETICAL -> sortedBy { it.title }
                            .groupBy { getAlphabeticalHeader(it.title) }
                        SortOption.DATE -> sortedBy { it.date }
                            .groupBy { getDateHeader(it.date) }
                    }
                }
                // 3. RENDERIZZAZIONE DELLA GRIGLIA A SEZIONI
                groupedItems.forEach { (header, items) ->
                    // Titolo della sezione (span completo)
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Text(
                            text = header,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 16.dp, bottom = 4.dp, start = 4.dp),
                        )
                    }
                    // Elementi della sezione
                    items(key = { "${it.id}" }, items = items) { media ->
                        PosterStarsItem(media, actions)
                    }
                }
            }
        }
    }
}

@Composable
private fun LazyGridItemScope.PosterStarsItem(
    media: TrackingMedia,
    actions: NavigationActions,
) {
    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(3.dp),
        modifier = Modifier.animateItem(),
    ) {
        PosterItem(media.mediaData, actions)
        val rating = media.rating ?: 0.0f

        StarRating(rating = rating / 2, starSize = 10.dp)
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
                .height(55.dp) // Standard M3 SearchBar height
                .padding(start = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortFilterChips(
    options: List<SortOption>,
    selectedSort: SortOption,
    onSortSelected: (SortOption) -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        options.forEach { sort ->
            CompositionLocalProvider(
                LocalMinimumInteractiveComponentSize provides 0.dp,
            ) {
                FilterChip(
                    modifier = Modifier.height(28.dp),
                    selected = selectedSort == sort,
                    onClick = { onSortSelected(sort) },
                    label = {
                        Text(
                            text = sort.label,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    },
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun getDateHeader(date: LocalDate?): String {
    if (date == null) return "No Date"
    val today = LocalDate.now()
    return when {
        date.year == today.year && date.month == today.month -> "This Month"
        date.year == today.year -> date.month.name.lowercase().replaceFirstChar { it.uppercase() }
        else -> "${date.year}"
    }
}

fun getRatingHeader(rating: Float?): String {
    val r = rating ?: 0f
    return when {
        r == 0f -> "Unrated"
        r >= 8.5f -> "Masterpiece"
        r >= 7.0f -> "Great"
        r >= 5.0f -> "Average"
        else -> "Below Average"
    }
}

fun getPopularityHeader(popularity: Float?): String {
    val pop = popularity ?: 0f
    return when {
        pop >= 1000f -> "Trending Worldwide"
        pop >= 300f -> "Very Popular"
        pop >= 50f -> "Well Known"
        else -> "Niche"
    }
}

fun getAlphabeticalHeader(title: String?): String {
    if (title.isNullOrBlank()) return "#"
    val firstChar = title.first().uppercaseChar()
    return if (firstChar.isLetter()) firstChar.toString() else "#"
}