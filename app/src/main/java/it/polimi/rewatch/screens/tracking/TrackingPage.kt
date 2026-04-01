package it.polimi.rewatch.screens.tracking

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.polimi.rewatch.R
import it.polimi.rewatch.common.CustomLazyGrid
import it.polimi.rewatch.common.CustomTabRow
import it.polimi.rewatch.common.NavigationActions
import it.polimi.rewatch.components.PosterItem
import it.polimi.rewatch.components.StarRating
import it.polimi.rewatch.model.MediaType
import it.polimi.rewatch.model.TrackingMedia
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date

enum class SortOption(val label: String) {
    POPULARITY("Popularity"),
    RATING("Rating"),
    ALPHABETICAL("A-Z"),
    DATE("Date")
}

enum class WatchTabItem(val title: String) {
    WATCHLIST("Watchlist"), WATCHED("Watched")
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackingPage(
    viewModel: TrackingViewModel = hiltViewModel(),
    type: MediaType,
    actions: NavigationActions,
    outerPadding: PaddingValues,
    backToHome: () -> Unit,
) {
    val title = if (type == MediaType.MOVIE) "Movies" else "Tv Shows"
    val icon = if (type == MediaType.MOVIE) R.drawable.movie else R.drawable.tv
    val tabs = WatchTabItem.entries
    var isAscending by rememberSaveable { mutableStateOf(false) }
    var selectedTab by rememberSaveable { mutableStateOf(tabs.first()) }
    val tabSortOrders = remember {
        mutableStateMapOf(
            WatchTabItem.WATCHLIST to SortOption.DATE, // Default for Watchlist
            WatchTabItem.WATCHED to SortOption.DATE,   // Default for Watched
        )
    }
    val availableSortOptions = mapOf(
        WatchTabItem.WATCHLIST to listOf(SortOption.DATE, SortOption.POPULARITY, SortOption.RATING, SortOption.ALPHABETICAL),
        WatchTabItem.WATCHED to listOf(SortOption.DATE, SortOption.RATING, SortOption.ALPHABETICAL),
    )
    val activeOrder = tabSortOrders.getValue(selectedTab)
    val watchlist by viewModel.watchlist.collectAsStateWithLifecycle()
    val watched by viewModel.watchedList.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopTabBar(
                icon, title, tabs, selectedTab,
                { selectedTab = it },
                modifier = Modifier.padding(
                    start = outerPadding.calculateStartPadding(LayoutDirection.Ltr),
                    end = outerPadding.calculateEndPadding(LayoutDirection.Ltr),
                ),
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier.padding(
                top = paddingValues.calculateTopPadding(),
                bottom = outerPadding.calculateBottomPadding(),
                start = outerPadding.calculateStartPadding(LayoutDirection.Ltr),
                end = outerPadding.calculateEndPadding(LayoutDirection.Ltr),
            ),
        ) {
            CustomLazyGrid {
                // 1. CHIPS DI ORDINAMENTO (Span completo)
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        SortFilterChips(
                            options = availableSortOptions.getValue(selectedTab),
                            selectedSort = activeOrder,
                            onSortSelected = { newSort ->
                                // Update the map for the currently active tab
                                if (tabSortOrders[selectedTab] == newSort) isAscending = !isAscending
                                else {
                                    tabSortOrders[selectedTab] = newSort
                                    isAscending = false
                                }
                            },
                            isAscending = isAscending,
                            label = SortOption::label,
                        )
                    }
                }
                // 1. Route to the correct list and order
                val items = when (selectedTab) {
                    WatchTabItem.WATCHLIST -> watchlist.filter { it.id.type == type }
                    WatchTabItem.WATCHED -> watched.filter { it.id.type == type }
                }
// 2. Sort and group the unified data structure
                val groupedItems = items.run {
                    when (activeOrder) {
                        SortOption.POPULARITY ->
                            sortedByDirection(isAscending) { it.popularity }
                                .groupBy { getPopularityHeader(it.popularity) }

                        SortOption.RATING ->
                            sortedByDirection(isAscending) { it.rating }
                                .groupBy { getRatingHeader(it.rating) }

                        SortOption.ALPHABETICAL ->
                            sortedByDirection(!isAscending) { it.title }
                                .groupBy { getAlphabeticalHeader(it.title) }

                        SortOption.DATE ->
                            sortedByDirection(isAscending) { it.date }
                                .groupBy { getDateHeader(it.date) }
                    }
                }
                if (groupedItems.isEmpty()) item(span = { GridItemSpan(maxLineSpan) }) {
                    Column(
                        Modifier
                            .padding(top = 50.dp)
                            .padding(horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        val tab = if (type == MediaType.MOVIE) "Movies" else "Shows"
                        val icon = when {
                            selectedTab == WatchTabItem.WATCHLIST && tab == "Movies" -> R.drawable.ticket
                            selectedTab == WatchTabItem.WATCHED && tab == "Movies" -> R.drawable.movie_library
                            selectedTab == WatchTabItem.WATCHLIST && tab == "Shows" -> R.drawable.tv_play
                            selectedTab == WatchTabItem.WATCHED && tab == "Shows" -> R.drawable.list_stars
                            else -> R.drawable.ticket
                        }
                        val `in` = if (selectedTab == WatchTabItem.WATCHLIST) " in " else " "
                        val tip = if (selectedTab == WatchTabItem.WATCHLIST) "want to watch later" else "already watched"
                        Icon(
                            painter = painterResource(icon),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(70.dp),
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "No $tab$`in`${selectedTab.title}",
                            style = MaterialTheme.typography.titleLarge.copy(textAlign = TextAlign.Center),
                        )
                        Text(
                            "Save the ${tab.lowercase()} you $tip and you'll see them here",
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = backToHome,
                        ) {
                            Text("Explore")
                        }
                    }
                } else {
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
                            if (selectedTab == WatchTabItem.WATCHED) PosterStarsItem(media, actions)
                            else PosterItem(media.mediaData, actions, modifier = Modifier.animateItem())
                        }
                    }
                }
            }
        }
    }
}

fun <T, R : Comparable<R>> Iterable<T>.sortedByDirection(
    ascending: Boolean,
    selector: (T) -> R?,
): List<T> {
    return if (ascending) sortedBy(selector) else sortedByDescending(selector)
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
        val rating = media.rating

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
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
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
fun <T> SortFilterChips(
    options: List<T>,
    selectedSort: T,
    onSortSelected: (T) -> Unit,
    isAscending: Boolean,
    label: (T) -> String,
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
                            text = label(sort),
                            style = MaterialTheme.typography.labelMedium,
                        )
                    },
                    trailingIcon = {
                        if (selectedSort == sort)
                            Icon(
                                imageVector = if (isAscending) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                contentDescription = "sort_order",
                                modifier = Modifier.size(18.dp),
                            )
                    },
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                )
            }
        }
    }
}


fun getDateHeader(date: LocalDateTime?): String {
    if (date == null) return "No Date"
    val today = LocalDateTime.now()
    return when {
        date.year == today.year && date.month == today.month -> "This Month"
        date.year == today.year -> date.month.name.lowercase().replaceFirstChar { it.uppercase() }
        else -> "${date.year}"
    }
}

fun Date.toLocalDateTime(): LocalDateTime {
    return this.toInstant()
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()
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