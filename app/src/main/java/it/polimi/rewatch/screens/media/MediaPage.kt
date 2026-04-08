package it.polimi.rewatch.screens.media

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccessTimeFilled
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.moviebase.tmdb.image.TmdbImage
import app.moviebase.tmdb.image.TmdbImageType
import app.moviebase.tmdb.image.TmdbImageUrlBuilder.build
import app.moviebase.tmdb.model.TmdbAnyPerson
import app.moviebase.tmdb.model.TmdbCast
import app.moviebase.tmdb.model.TmdbCompany
import app.moviebase.tmdb.model.TmdbCrew
import app.moviebase.tmdb.model.TmdbProvider
import app.moviebase.tmdb.model.TmdbProviders
import app.moviebase.tmdb.model.TmdbReview
import it.polimi.rewatch.R
import it.polimi.rewatch.common.BorderBox
import it.polimi.rewatch.common.CustomTabRow
import it.polimi.rewatch.common.ExpandableText
import it.polimi.rewatch.common.NavigationActions
import it.polimi.rewatch.common.OverlapIcon
import it.polimi.rewatch.common.TabItem
import it.polimi.rewatch.common.expandableSection
import it.polimi.rewatch.common.formatDate
import it.polimi.rewatch.common.getShortNotation
import it.polimi.rewatch.common.layoutEvade
import it.polimi.rewatch.common.relativeMove
import it.polimi.rewatch.components.AnimatedUiState
import it.polimi.rewatch.components.PosterItem
import it.polimi.rewatch.components.RewatchAsyncImage
import it.polimi.rewatch.components.StarRating
import it.polimi.rewatch.components.layout.MediaScaffold
import it.polimi.rewatch.model.EpisodeId
import it.polimi.rewatch.model.MediaDetail
import it.polimi.rewatch.model.MediaId
import it.polimi.rewatch.model.MediaSpecificDetails
import it.polimi.rewatch.model.MediaType
import it.polimi.rewatch.model.PosterInfo
import it.polimi.rewatch.model.SeasonId
import it.polimi.rewatch.model.WatchedMedia
import it.polimi.rewatch.model.WatchlistMedia
import kotlinx.datetime.LocalDate
import java.util.Locale

@Composable
fun MediaPage(
    mediaId: MediaId,
    outerPadding: PaddingValues,
    actions: NavigationActions,
    modifier: Modifier = Modifier,
    viewModel: MediaViewModel = hiltViewModel<MediaViewModel, MediaViewModel.Factory>(
        key = mediaId.toString(),
        creationCallback = { it.create(mediaId) },
    ),
) {
    // Hoist the state! Collect it at the screen-level composable.
    val watchlistItem by viewModel.watchlistItem.collectAsStateWithLifecycle()
    val watchedListItem by viewModel.watchedListItem.collectAsStateWithLifecycle()

    AnimatedUiState(
        state = viewModel.uiState,
        loadingContent = ::MovieSkeleton,
        modifier = modifier,
    ) { data ->
        MediaContent(
            data = data,
            watchlistItem = watchlistItem,
            watchedListItem = watchedListItem,
            padding = outerPadding,
            mediaId = mediaId,
            actions = actions,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MediaContent(
    data: MovieData,
    watchlistItem: WatchlistMedia?,
    watchedListItem: WatchedMedia?,
    padding: PaddingValues,
    mediaId: MediaId,
    actions: NavigationActions,
    modifier: Modifier = Modifier,
) {
    val movie = data.movie
    val scrollState = rememberLazyListState()
    val spacing = 16.dp
    var selectedTab by remember { mutableStateOf<String?>(null) }
    var expandedSectionIds by rememberSaveable { mutableStateOf(emptySet<String>()) }
    val handleToggle: (String) -> Unit = { id ->
        expandedSectionIds = if (id in expandedSectionIds) expandedSectionIds - id else expandedSectionIds + id
    }
    val tabs = remember(expandedSectionIds) {
        buildTabs(movie, expandedSectionIds, handleToggle, mediaId, spacing, actions)
    }
    val windowInfo = LocalWindowInfo.current
// containerSize is already in exact pixels!
    val screenSize = remember(windowInfo.containerSize) {
        maxOf(windowInfo.containerSize.width, windowInfo.containerSize.height)
    }



    MediaScaffold(
        scrollState = scrollState,
        title = movie.title,
        onNavigateBack = actions.onNavigateBack,
        modifier = modifier,
    ) {
        LazyColumn(
            state = scrollState,
            verticalArrangement = Arrangement.spacedBy(3.dp),
            contentPadding = PaddingValues(
                start = padding.calculateStartPadding(LayoutDirection.Ltr),
                end = padding.calculateEndPadding(LayoutDirection.Ltr),
                bottom = padding.calculateBottomPadding(),
            ),
        ) {
            mediaHeaderItems(
                movie = movie,
                spacing = spacing,
                padding = padding,
                watchlistItem = watchlistItem,
                watchedListItem = watchedListItem,
                mediaId = mediaId,
                actions = actions,
                screenSize = screenSize,
            )

            tabbedSection(
                allTabs = tabs,
                activeTab = selectedTab,
                onTabSelected = { newTab ->
                    selectedTab = newTab
                    expandedSectionIds = emptySet()
                },
            )

            optionalTitledSection(
                isVisible = movie.reviews.isNotEmpty(),
                title = "Reviews",
                pad = spacing,
            ) {
                expandableSection(
                    title = "reviews",
                    itemsList = movie.reviews,
                    expandedSectionIds = expandedSectionIds,
                    onToggleSection = handleToggle,
                    key = { it.id },
                    showDividers = true,
                    initialVisibleCount = 3,
                    divider = {
                        HorizontalDivider(
                            Modifier.padding(start = 75.dp),
                        )
                    },
                ) { review ->
                    ReviewItem(review)
                }
            }

            optionalTitledSection(
                isVisible = data.similar.isNotEmpty(),
                title = "Similar",
                pad = spacing,
            ) {
                item(key = "similar_section") {
                    Column(Modifier.animateItem()) {
                        SimilarSection(data.similar) { similarItem ->
                            PosterItem(similarItem, actions)
                        }
                        Spacer(Modifier.height(spacing))
                    }
                }
            }
        }
    }
}

// 4. Extracted a Header component to reduce the size of the LazyColumn block
private fun LazyListScope.mediaHeaderItems(
    movie: MediaDetail,
    spacing: Dp,
    padding: PaddingValues,
    watchlistItem: WatchlistMedia?,
    watchedListItem: WatchedMedia?,
    mediaId: MediaId,
    actions: NavigationActions,
    screenSize: Int,
) {
    item(key = "header_backdrop") {
        BackdropImage(
            image = movie.backdropImage,
            padding = padding,
            modifier = Modifier
                .animateItem()
                .clickable {
                    movie.backdropImage?.let {
                        actions.onFullscreenImage(build(it, width = screenSize, height = screenSize))
                    }
                },
        )
    }

    item(key = "header_title") {
        TitleAndInfoSection(
            pad = spacing,
            backdropImage = movie.backdropImage,
            posterImage = movie.posterImage,
            director = movie.director,
            directorId = movie.crew.find { it.job == "Director" }?.id,
            title = movie.title,
            runtime = movie.runtime,
            releaseDate = movie.releaseDate,
            certification = movie.certification,
            actions = actions,
            modifier = Modifier
                .animateItem()
                .clickable {
                    movie.posterImage?.let {
                        actions.onFullscreenImage(build(it, width = screenSize, height = screenSize))
                    }
                },
        )
    }

    item(key = "header_tagline") {
        Box(modifier = Modifier.animateItem()) {
            TaglineAndOverview(
                pad = spacing,
                overview = movie.overview,
                tagline = movie.tagline,
                // Ensure TaglineAndOverview no longer has .animateContentSize() inside it!
            )
        }
    }

    item(key = "header_ratings") {
        Box(modifier = Modifier.animateItem()) {
            TmdbRatings(
                voteCount = movie.voteCount,
                voteAverage = movie.voteAverage,
            )
        }
    }

    if (mediaId.type == MediaType.MOVIE || mediaId.type == MediaType.TV_SHOW) {
        item(key = "header_tracking") {
            Box(modifier = Modifier.animateItem()) {
                TrackingInfo(
                    watchlistItem = watchlistItem,
                    watchedListItem = watchedListItem,
                    onClick = { actions.onBottomSheet(PosterInfo(movie, mediaId)) },
                )
            }
        }
    }
}

@Composable
private fun TrackingInfo(
    watchlistItem: WatchlistMedia?,
    watchedListItem: WatchedMedia?,
    onClick: () -> Unit,
) {
    HorizontalDivider()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 12.dp)
            .padding(bottom = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier
                .widthIn(max = 400.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                .clickable(onClick = onClick)
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (watchedListItem != null) {
                if (watchlistItem != null) OverlapIcon(Icons.Filled.Visibility, Icons.Filled.AccessTimeFilled)
                else Icon(
                    Icons.Filled.Visibility,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = if (watchedListItem.rating == 0.0f) "You watched this" else "Your rating",
                    style = MaterialTheme.typography.titleSmall,
                )
                if (watchedListItem.rating > 0.0f) {
                    StarRating(
                        watchedListItem.rating / 2,
                        modifier = Modifier.weight(1f),
                    )
                } else Spacer(
                    modifier = Modifier.weight(1f),
                )
            } else if (watchlistItem != null) {
                Icon(
                    Icons.Filled.AccessTimeFilled,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    "In your watchlist",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
            } else {
                Icon(
                    painterResource(R.drawable.popcorn),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    "Add to your lists",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
            }

            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            )
        }
    }
}

private fun buildTabs(
    movie: MediaDetail,
    expandedSectionIds: Set<String>,
    handleToggle: (String) -> Unit,
    mediaId: MediaId,
    pad: Dp,
    actions: NavigationActions,
): List<TabItem> =
    buildList {
        add(
            TabItem.expandable(
                title = "Cast",
                itemsList = movie.cast,
                expandedSectionIds = expandedSectionIds,
                onToggleSection = handleToggle,
                key = { it.id },
                initialVisibleCount = 7,
                itemContent = { PersonItem(it) { actions.onNavigateToPerson(it.id) } },
            ),
        )
        add(
            TabItem.expandable(
                title = "Crew",
                itemsList = movie.crew.sortedByDescending { it.job == "Director" },
                expandedSectionIds = expandedSectionIds,
                onToggleSection = handleToggle,
                key = { it.id },
                initialVisibleCount = 7,
                itemContent = { PersonItem(it) { actions.onNavigateToPerson(it.id) } },
            ),
        )
        // ... TV SHOW: Seasons tab ...
        if (mediaId.type == MediaType.TV_SHOW) add(
            TabItem.expandable(
                title = "Seasons",
                itemsList = (movie.specificDetails as? MediaSpecificDetails.Show)?.seasons.orEmpty(),
                expandedSectionIds = expandedSectionIds,
                onToggleSection = handleToggle,
                key = { it.id },
                initialVisibleCount = 7,
                showDividers = true,
                itemContent = { season ->
                    EpisodeItem(
                        number = season.seasonNumber,
                        name = season.name,
                        date = season.airDate,
                        count = season.episodeCount?.let { "$it episodes" }.orEmpty(),
                    ) { SeasonId(mediaId.parentId, season.seasonNumber).onClick(actions) }
                },
            ),
        )
        // ... SEASON: Episodes tab ...
        if (mediaId.type == MediaType.SEASON) add(
            TabItem.expandable(
                title = "Episodes",
                itemsList = (movie.specificDetails as? MediaSpecificDetails.Season)?.episodes.orEmpty(),
                expandedSectionIds = expandedSectionIds,
                onToggleSection = handleToggle,
                key = { it.id },
                initialVisibleCount = 7,
                showDividers = true,
                itemContent = { episode ->
                    EpisodeItem(
                        number = episode.episodeNumber,
                        name = episode.name,
                        date = episode.airDate,
                        count = episode.runtime?.let { "$it min" }.orEmpty(),
                    ) { EpisodeId(mediaId.parentId, episode.seasonNumber, episode.episodeNumber).onClick(actions) }
                },
            ),
        )
        // ... Details and Watch tabs remain the same, they just ignore the lambda parameters
        add(
            TabItem(
                title = "Details",
                isAvailable = { movie.isDetailsAvailable },
                content = {
                    item(key = "details_tab") {
                        DetailsTab(pad, movie, modifier = Modifier.animateItem())
                    }
                },
            ),
        )
        // ... Watch tab ...
        add(
            TabItem(
                title = "Watch",
                isAvailable = {
                    movie.watchProviders["US"]?.run {
                        buy.isNotEmpty() || flatrate.isNotEmpty()
                    } ?: false
                },
                content = {
                    item(key = "watch_tab") {
                        WatchTab(pad, movie.watchProviders["US"], modifier = Modifier.animateItem())
                    }
                },
            ),
        )
    }


fun LazyListScope.optionalTitledSection(
    isVisible: Boolean,
    title: String,
    pad: Dp,
    content: LazyListScope.() -> Unit,
) {
    if (isVisible) {
        item(key = "title_$title") {
            Column(Modifier.animateItem()) {
                HorizontalDivider()
                Text(
                    text = title,
                    modifier = Modifier.padding(pad),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        // Execute the specific content for this section
        content()
    }
}

fun LazyListScope.tabbedSection(
    allTabs: List<TabItem>,
    activeTab: String?,
    onTabSelected: (String) -> Unit,
) {
    // 1. Perform the available tabs calculation
    val availableTabs = allTabs.filter { it.isAvailable() }

    if (availableTabs.isNotEmpty()) {
        // 2. Resolve the active tab (fallback to the first available if none selected/found)
        val resolvedActiveTab = availableTabs.find { it.title == activeTab } ?: availableTabs.first()

        item(key = "tabs_row") {
            Column(Modifier.animateItem()) {
                HorizontalDivider()

                CustomTabRow(
                    availableTabs = availableTabs.map { it.title },
                    onClick = onTabSelected,
                    title = { it },
                    tabState = resolvedActiveTab.title,
                )
                Spacer(Modifier.height(7.dp))
            }
        }
        // 3. Render the content seamlessly
        // Pass the state into the content execution
        resolvedActiveTab.content(this)
    }
}

@Composable
fun TaglineAndOverview(
    pad: Dp,
    overview: String?,
    tagline: String?,
    modifier: Modifier = Modifier, // It's good practice to expose a modifier
) {
    if (tagline.isNullOrBlank() && overview.isNullOrBlank()) return

    Column(
        modifier = modifier
            .animateContentSize() // <-- ADD THIS HERE
            .padding(horizontal = pad),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Spacer(modifier = Modifier.height(5.dp))
        if (!tagline.isNullOrEmpty()) {
            Text(
                text = tagline,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
        if (!overview.isNullOrBlank()) {
            ExpandableText(
                overview,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
fun TmdbRatings(
    voteCount: Int?,
    voteAverage: Float?,
) {
    val vote = voteAverage?.toDouble() ?: 0.0
    val count = voteCount ?: 0

    if (vote == 0.0 && count == 0) return

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF022441))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Image(
                painter = painterResource(id = R.drawable.tmdb_logo),
                contentDescription = "TMDB logo",
                modifier = Modifier.width(70.dp),
            )

            VerticalDivider(
                modifier = Modifier.height(16.dp),
                color = Color.White.copy(alpha = 0.3f),
            )

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "%.1f".format(Locale.US, vote),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
                Text(
                    text = "/10",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.6f),
                )
            }

            if (count > 0) {
                VerticalDivider(
                    modifier = Modifier.height(16.dp),
                    color = Color.White.copy(alpha = 0.3f),
                )
                Text(
                    text = "${count.toDouble().getShortNotation()} votes",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.8f),
                )
            }
        }
    }
}

@Composable
fun BackdropImage(
    image: TmdbImage?,
    padding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    if (image == null) {
        Spacer(Modifier.height(padding.calculateTopPadding() + 60.dp))
        return
    }

    Box(
        modifier = Modifier
            .layoutEvade(
                padding.calculateStartPadding(LayoutDirection.Ltr) + padding.calculateEndPadding(LayoutDirection.Ltr),
            ),
    ) {
        RewatchAsyncImage(
            model = build(image, width = 1000, height = 500),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier
                .fillMaxWidth()
                .height(230.dp),
        )
        val statusBarHeight = padding.calculateTopPadding() / 230.dp
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    brush = Brush.verticalGradient(
                        0f to MaterialTheme.colorScheme.surface.copy(0.25f),
                        statusBarHeight to Color.Transparent,
                        0.7f to Color.Transparent,
                        1f to MaterialTheme.colorScheme.surface,
                    ),
                ),
        )
    }
}

@Composable
fun TitleAndInfoSection(
    pad: Dp,
    backdropImage: TmdbImage?,
    posterImage: TmdbImage?,
    director: String?,
    title: String?,
    runtime: String,
    releaseDate: LocalDate?,
    certification: String?,
    modifier: Modifier = Modifier,
    actions: NavigationActions,
    directorId: Int?,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = pad)
            .relativeMove(if (backdropImage != null) (-24.dp) else 0.dp),
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Spacer(Modifier.height(if (backdropImage != null) 32.dp else 12.dp))

            Text(
                text = title.orEmpty(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = MaterialTheme.typography.headlineSmall.lineHeight * 0.9f,
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 4.dp),
            ) {
                certification?.let {
                    BorderBox {
                        Text(
                            text = it,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
                val infoText = buildList {
                    releaseDate?.let { add(it.toString().formatDate("MMM yyyy")) }
                    val intRuntime = runtime.toIntOrNull()
                    if (intRuntime != null && intRuntime > 0) add("$runtime min")
                }.joinToString("  •  ")

                if (infoText.isNotEmpty()) {
                    Text(
                        text = infoText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (directorId != null && !director.isNullOrEmpty()) {
                Text(
                    text = "Directed by $director",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { actions.onNavigateToPerson(directorId) },
                )
            }
        }

        posterImage?.let {
            BorderBox {
                RewatchAsyncImage(
                    model = build(it, width = 200, height = 300),
                    contentScale = ContentScale.Crop,
                    contentDescription = title,
                    modifier = modifier
                        .width(100.dp)
                        .height(150.dp),
                )
            }
        }
    }
}

@Composable
fun WatchTab(
    pad: Dp,
    providers: TmdbProviders?,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(pad),
        modifier = modifier.padding(vertical = 15.dp),
    ) {
        if (providers != null) {
            if (providers.flatrate.isNotEmpty()) {
                WatchProviders(
                    "Streaming", providers.flatrate,
                )
            }

            if (providers.buy.isNotEmpty()) {
                WatchProviders(
                    "Buy", providers.buy,
                )
            }
        } else {
            Text(
                "No providers available", style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
fun DetailsTab(
    pad: Dp,
    details: MediaDetail,
    modifier: Modifier = Modifier,
) {
    if (details.specificDetails !is MediaSpecificDetails.Movie) return
    val movie: MediaSpecificDetails.Movie = details.specificDetails
    val genres = movie.genres
    val budget = movie.budget.toDouble()
    val releaseDate = movie.releaseDate
    val productionCountries = movie.productionCountries
    val productionCompanies = movie.productionCompanies

    Column(
        verticalArrangement = Arrangement.spacedBy(pad),
        modifier = modifier.padding(vertical = 15.dp),
    ) {
        if (genres.isNotEmpty()) {
            DetailRow("Genres:") {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(genres) { genre ->
                        BorderBox {
                            Text(genre.name, modifier = Modifier.padding(5.dp))
                        }
                    }
                    item { Spacer(Modifier.width(10.dp)) }
                }
            }
        }
        if (budget > 0) {
            DetailRow("Budget: $ ${budget.getShortNotation()}")
        }

        if (!productionCountries.isNullOrEmpty()) {
            val countries = productionCountries.joinToString(", ") { it.name }
            DetailRow("Countries: $countries")
        }

        releaseDate?.let { date ->
            DetailRow("Release: ${date.toString().formatDate("d MMMM yyyy")}")
        }
        if (!productionCompanies.isNullOrEmpty()) {
            DetailRow("Production:") {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(productionCompanies) { company ->
                        CompanyItem(company)
                    }
                    item { Spacer(Modifier.width(5.dp)) }
                }
            }
        }
    }
}

@Composable
fun DetailRow(
    label: String,
    content: @Composable () -> Unit = {},
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Spacer(Modifier.width(10.dp))
        Text(label)
        content()
    }
}

@Composable
fun CompanyItem(company: TmdbCompany) {
    val companyName = company.name ?: "Unknown Company"

    BorderBox {
        Row(
            horizontalArrangement = Arrangement.spacedBy(15.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(5.dp),
        ) {
            company.logoPath?.let { logoPath ->
                RewatchAsyncImage(
                    model = build(logoPath, TmdbImageType.PROFILE, width = 400, height = 200),
                    contentDescription = companyName,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .height(20.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color.White)
                        .padding(2.dp),
                )
            }
            Text(text = companyName)
        }
    }
}

@Composable
fun <T> SimilarSection(
    similar: List<T>,
    modifier: Modifier = Modifier,
    content: @Composable (LazyItemScope.(T) -> Unit),
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.height(160.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
    ) {
        items(items = similar, itemContent = content)
    }
}

@Composable
fun PersonItem(
    person: TmdbAnyPerson?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) = ListItem(
    leadingContent = {
        RewatchAsyncImage(
            model = person?.profileImage?.let { build(it, width = 70, height = 70) },
            contentDescription = person?.name.orEmpty(),
            fallbackResId = R.drawable.ticket1,
            alignment = BiasAlignment(horizontalBias = 0f, verticalBias = -0.7f),
            modifier = Modifier
                .size(width = 55.dp, height = 60.dp)
                .clip(RoundedCornerShape(7.dp)),
        )
    },
    headlineContent = {
        Text(
            text = person?.name.orEmpty(),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
        )
    },
    supportingContent = {
        val text = ((person as? TmdbCast)?.character ?: (person as? TmdbCrew)?.job).orEmpty()
        if (text.isNotBlank())
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
            )
    },
    trailingContent = {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "Dettagli attore",
            tint = Color.Gray,
        )
    },
    modifier = modifier
        .fillMaxWidth()
        .clickable(onClick = onClick),
)

@Composable
fun ReviewItem(review: TmdbReview?) {
    var isExpanded by remember { mutableStateOf(false) }

    ListItem(
        leadingContent = {
            RewatchAsyncImage(
                model = review?.authorDetails?.avatarPath?.let { image ->
                    build(image, TmdbImageType.PROFILE, width = 70, height = 70)
                },
                contentDescription = "Avatar for ${review?.author}",
                fallbackIcon = Icons.Default.Person,
                modifier = Modifier
                    .size(45.dp)
                    .clip(CircleShape)
                    .border(1.dp, Color.Gray, CircleShape),
            )
        },
        headlineContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = review?.author.orEmpty(),
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                )
                review?.authorDetails?.rating?.let { rating ->
                    StarRating(rating / 2)
                }
            }
        },
        supportingContent = {
            Text(
                text = review?.content.orEmpty(),
                maxLines = if (isExpanded) Int.MAX_VALUE else 5,
                overflow = TextOverflow.Ellipsis,
            )
        },
        modifier = Modifier
            .animateContentSize()
            .clickable { isExpanded = !isExpanded },
    )
}

@Composable
fun WatchProviders(
    title: String,
    providers: List<TmdbProvider>,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(start = 15.dp),
            fontWeight = FontWeight.Bold,
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Leading spacer for scrollable padding
            item { Spacer(Modifier.width(5.dp)) }

            items(providers) { provider ->
                val providerName = provider.providerName ?: "Unknown Provider"

                BorderBox {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(15.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(5.dp),
                    ) {
                        RewatchAsyncImage(
                            model = provider.logoPath?.let {
                                build(it, TmdbImageType.PROFILE, width = 70, height = 70)
                            },
                            contentDescription = providerName,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .height(20.dp)
                                .clip(RoundedCornerShape(5.dp)),
                        )

                        Text(text = providerName)
                    }
                }
            }
            item { Spacer(Modifier.width(5.dp)) }
        }
    }
}

@Composable
fun EpisodeItem(
    number: Int?,
    name: String?,
    date: LocalDate?,
    count: String,
    modifier: Modifier = Modifier,
    onNavigateToMedia: () -> Unit,
) = ListItem(
    leadingContent = {
        Text(
            text = number.toString(),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.width(30.dp),
            textAlign = TextAlign.Center,
        )
    },
    headlineContent = {
        Text(
            text = name ?: "Untitled",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    },
    supportingContent = {
        val dateFormatted = date?.toString()?.formatDate("d MMM YYYY") ?: ""
        val separator = if (dateFormatted.isNotBlank() && count.isNotBlank()) " • " else ""
        val text = "$dateFormatted$separator$count"
        if (text.isNotBlank())
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
    },
    modifier = modifier.clickable(onClick = onNavigateToMedia),
)