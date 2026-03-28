package it.polimi.rewatch.screens.media

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccessTimeFilled
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
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
import it.polimi.rewatch.common.ExpandBtn
import it.polimi.rewatch.common.ExpandableText
import it.polimi.rewatch.common.NavigationActions
import it.polimi.rewatch.common.OverlapIcon
import it.polimi.rewatch.common.TabItem
import it.polimi.rewatch.common.formatDate
import it.polimi.rewatch.common.getShortNotation
import it.polimi.rewatch.common.layoutEvade
import it.polimi.rewatch.common.relativeMove
import it.polimi.rewatch.components.AnimatedUiState
import it.polimi.rewatch.components.MediaScaffold
import it.polimi.rewatch.components.PosterItem
import it.polimi.rewatch.components.RewatchAsyncImage
import it.polimi.rewatch.components.StarRating
import it.polimi.rewatch.model.EpisodeId
import it.polimi.rewatch.model.MediaDetail
import it.polimi.rewatch.model.MediaId
import it.polimi.rewatch.model.MediaSpecificDetails
import it.polimi.rewatch.model.MediaType
import it.polimi.rewatch.model.PosterMedia
import it.polimi.rewatch.model.SeasonId
import it.polimi.rewatch.model.WatchedMedia
import it.polimi.rewatch.model.WatchlistMedia
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.LocalDate
import java.util.Locale
import kotlin.math.max

fun isDetailsAvailable(details: MediaDetail): Boolean {
    if (details.specificDetails !is MediaSpecificDetails.Movie) return false
    val movie: MediaSpecificDetails.Movie = details.specificDetails
    return (movie.genres.isNotEmpty() ||
            movie.budget > 0 ||
            movie.releaseDate != null ||
            !movie.productionCompanies.isNullOrEmpty())
}

data class TrackingItems(
    val watchlistItem: StateFlow<WatchlistMedia?>,
    val watchedListItem: StateFlow<WatchedMedia?>,
)

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MediaPage(
    mediaId: MediaId,
    viewModel: MediaViewModel = hiltViewModel<MediaViewModel, MediaViewModel.Factory>(
        key = mediaId.toString(),
        creationCallback = { it.create(mediaId) },
    ),
    outerPadding: PaddingValues,
    actions: NavigationActions,
) {
    TrackingItems(viewModel.watchlistItem, viewModel.watchedListItem)
    AnimatedUiState(
        state = viewModel.uiState,
        loadingContent = ::MovieSkeleton,
    ) { data ->
        MediaContent(
            data = data,
            padding = outerPadding,
            mediaId = mediaId,
            actions = actions,
            vm = viewModel,
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class,
)
fun MediaContent(
    actions: NavigationActions,
    padding: PaddingValues,
    data: MovieData,
    mediaId: MediaId,
    vm: MediaViewModel,
    // trackingItems: TrackingItems,
) {
    val movie: MediaDetail = data.movie
    val scrollState = rememberLazyListState()
    val pad = 16.dp
    val watchlistItem by vm.watchlistItem.collectAsStateWithLifecycle()
    val watchedListItem by vm.watchedListItem.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf<String?>(null) }
    var expandedSectionIds by rememberSaveable { mutableStateOf(emptySet<String>()) }
    val handleToggle: (String) -> Unit = { id ->
        expandedSectionIds = if (id in expandedSectionIds) expandedSectionIds - id else expandedSectionIds + id
    }
    val tabs: List<TabItem> = remember(
        expandedSectionIds,
        buildTabs(movie, expandedSectionIds, handleToggle, mediaId, pad, actions),
    )
    MediaScaffold(
        scrollState = scrollState,
        title = movie.title,
        onNavigateBack = actions.onNavigateBack,
    ) { _ ->
        LazyColumn(
            state = scrollState,
            verticalArrangement = Arrangement.spacedBy(3.dp),
            contentPadding = PaddingValues(
                start = padding.calculateStartPadding(LayoutDirection.Ltr),
                end = padding.calculateEndPadding(LayoutDirection.Ltr),
            ),
        ) {
            item {
                BackdropImage(
                    movie.backdropImage, padding,
                    modifier = actions.onFullscreenImage(
                        movie.backdropImage?.let { build(it, width = 500, height = 500) },
                    ),
                )

                TitleAndInfoSection(
                    pad,
                    backdropImage = movie.backdropImage,
                    posterImage = movie.posterImage,
                    director = movie.director,
                    directorId = movie.crew.find { it.job == "Director" }?.id,
                    title = movie.title,
                    runtime = movie.runtime,
                    releaseDate = movie.releaseDate,
                    certification = movie.certification,
                    modifier = actions.onFullscreenImage(
                        movie.posterImage?.let { build(it, width = 500, height = 500) },
                    ),
                    actions = actions,
                )

                TaglineAndOverview(
                    pad, overview = movie.overview, tagline = movie.tagline,
                )

                TmdbRatings(
                    voteCount = movie.voteCount,
                    voteAverage = movie.voteAverage,
                )

                TrackingInfo(watchlistItem, watchedListItem) { actions.onBottomSheet(PosterMedia(movie, mediaId)) }
            }
//            println(movie.crew)
            tabbedSection(
                allTabs = tabs,
                activeTab = selectedTab,
                // Pass the volatile state here
                onTabSelected = {
                    selectedTab = it
                    expandedSectionIds = emptySet() // Clear expansions on tab switch
                },
            )
            // 1. Reviews Section
            optionalTitledSection(
                isVisible = movie.reviews.isNotEmpty(),
                title = "Reviews",
                pad = pad,
            ) {
                expandableSection(
                    title = "reviews",
                    itemsList = movie.reviews,
                    expandedSectionIds = expandedSectionIds,
                    onToggleSection = handleToggle,
                    showDividers = true,
                    divider = {
                        HorizontalDivider(
                            Modifier
                                .padding(start = 75.dp)
                                .animateItem(),
                        )
                    },
                    initialVisibleCount = 3,
                ) {
                    ReviewItem(it)
                }
            }
// 2. Similar Movies Section
            optionalTitledSection(
                isVisible = data.similar.isNotEmpty(),
                title = "Similar",
                pad = pad,
            ) {
                item {
                    SimilarSection(data.similar) {
                        PosterItem(it, actions)
                    }
                    Spacer(Modifier.height(pad))
                }
            }

            item {
                Spacer(Modifier.height(padding.calculateBottomPadding()))
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
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(vertical = 10.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(DividerDefaults.color)
            .clickable(onClick = onClick),
    ) {
        Spacer(Modifier.height(10.dp))
        if (watchedListItem != null) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(horizontal = 16.dp),
            ) {
                if (watchlistItem != null) OverlapIcon(Icons.Filled.Visibility, Icons.Filled.AccessTimeFilled)
                else Icon(Icons.Filled.Visibility, contentDescription = null, tint = Color.White)
                if (watchedListItem.rating == 0.0f) Text("You watched this content")
                else {
                    Text("You rated this content")
                    StarRating(watchedListItem.rating / 2)
                }
            }
            Spacer(Modifier.height(10.dp))

        } else if (watchlistItem != null) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp),
            ) {
                Icon(Icons.Filled.AccessTimeFilled, contentDescription = null, tint = Color.White)
                Text("This content is in your watchlist")
            }
            Spacer(Modifier.height(10.dp))

        } else {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp),
            ) {
                Icon(painterResource(R.drawable.popcorn), contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                Text("Add this content to your lists")
            }
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun buildTabs(
    movie: MediaDetail,
    expandedSectionIds: Set<String>,
    handleToggle: (String) -> Unit,
    mediaId: MediaId,
    pad: Dp,
    actions: NavigationActions,
): () -> List<TabItem> = {
    buildList {
        add(
            TabItem.expandable(
                title = "Cast",
                itemsList = movie.cast,
                expandedSectionIds = expandedSectionIds,
                onToggleSection = handleToggle,
                initialVisibleCount = 7,
                itemContent = { PersonItem(it) { actions.onNavigateToProfile(it.id) } },
            ),
        )
        add(
            TabItem.expandable(
                title = "Crew",
                itemsList = movie.crew.sortedByDescending { it.job == "Director" },
                expandedSectionIds = expandedSectionIds,
                onToggleSection = handleToggle,
                initialVisibleCount = 7,
                itemContent = { PersonItem(it) { actions.onNavigateToProfile(it.id) } },
            ),
        )
        // ... TV SHOW: Seasons tab ...
        if (mediaId.type == MediaType.TV_SHOW) add(
            TabItem.expandable(
                title = "Seasons",
                itemsList = (movie.specificDetails as? MediaSpecificDetails.Show)?.seasons.orEmpty(),
                expandedSectionIds = expandedSectionIds,
                onToggleSection = handleToggle,
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
                isAvailable = { isDetailsAvailable(movie) },
                content = {
                    item {
                        DetailsTab(pad, movie)
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
                    item {
                        WatchTab(pad, movie.watchProviders["US"])
                    }
                },
            ),
        )
    }
}

fun LazyListScope.optionalTitledSection(
    isVisible: Boolean,
    title: String,
    pad: Dp,
    content: LazyListScope.() -> Unit,
) {
    if (isVisible) {
        item {
            HorizontalDivider()
            Text(
                text = title,
                modifier = Modifier.padding(pad),
                // Add your standard typography style here if needed, e.g.:
                // style = MaterialTheme.typography.titleMedium
            )
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
        //val activeTab = availableTabs.find { it == activeTab } ?: availableTabs.first()
        val activeTab = availableTabs.find { it.title == activeTab } ?: availableTabs.first()

        item {
            HorizontalDivider()

            CustomTabRow(
                availableTabs = availableTabs.map { it.title },
                onClick = onTabSelected,
                title = { it },
                tabState = activeTab.title,
            )
            Spacer(Modifier.height(7.dp))
        }
        // 3. Render the content seamlessly
// Pass the state into the content execution
        activeTab.content(this)
    }
}

@Composable
fun TaglineAndOverview(
    pad: Dp,
    overview: String?,
    tagline: String?,
) {
    Column(
        modifier = Modifier.padding(bottom = pad),
        verticalArrangement = Arrangement.spacedBy(
            10.dp,
        ),
    ) {
        Spacer(modifier = Modifier.height(5.dp))
        if (!tagline.isNullOrEmpty()) {
            Text(
                text = tagline, modifier = Modifier.padding(horizontal = pad),
            )
        }
        if (overview != null && overview != "") {
            ExpandableText(
                overview, modifier = Modifier.padding(horizontal = pad),
            )
        }
    }
}

@Composable
fun TmdbRatings(
    voteCount: Int?,
    voteAverage: Float?,
) {
    val vote = (voteAverage as? Number)?.toDouble() ?: 0.0
    val count = voteCount ?: 0
    Column(
        Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF022441))
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Image(
                painter = painterResource(id = R.drawable.tmdb_logo),
                contentDescription = "TMDB logo",
                modifier = Modifier.width(100.dp),
            )
            when {
                (vote > 0 && count > 0) -> {
                    Text(
                        "%.1f".format(Locale.US, vote) + "/10",
                        fontWeight = FontWeight.Bold,
                    )
                    Text("${count.toDouble().getShortNotation()} votes")
                }

                vote > 0 -> Text("%.1f".format(Locale.US, vote) + "/10", fontWeight = FontWeight.Bold)
                else -> Text("No votes yet")
            }
        }
    }
}

fun <T> LazyListScope.expandableSection(
    title: String,
    itemsList: List<T>,
    expandedSectionIds: Set<String>,
    onToggleSection: (String) -> Unit,
    initialVisibleCount: Int = 3,
    showDividers: Boolean = false,
    divider: @Composable LazyItemScope.() -> Unit = { HorizontalDivider() },
    itemContent: @Composable LazyItemScope.(T) -> Unit,
) {
// 1. Evaluate the state internall
    val isExpanded = expandedSectionIds.contains(title)
    val visibleItems = if (isExpanded) itemsList else itemsList.take(initialVisibleCount)

    itemsIndexed(
        items = visibleItems,
        itemContent = { idx, item ->
            itemContent.invoke(this, item)
            if (showDividers && idx != itemsList.lastIndex) divider()
        },
    )
    //println(title + itemsList.size)
    // 2. The Header / Toggle Button
    item(key = "header_$title") {
        // 3. Pass the sectionId back up when clicked
        val remaining = itemsList.size - visibleItems.size

        if (isExpanded || remaining > 0)
            ExpandBtn(
                if (isExpanded) "Hide $title" else "Show $remaining more",
                onClick = { onToggleSection(title) },
                isExpanded = isExpanded,
                modifier = Modifier.animateItem(),
            )
    }
}

@Composable
fun BackdropImage(
    image: TmdbImage?,
    padding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    image?.let {
        Box {
            RewatchAsyncImage(
                model = build(it, width = 600, height = 300),
                contentDescription = null,
                modifier = modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .layoutEvade(
                        padding.calculateStartPadding(LayoutDirection.Ltr) + padding.calculateEndPadding(LayoutDirection.Ltr),
                    ),
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        brush = Brush.verticalGradient(
                            0.6f to Color.Transparent, // Stay transparent until 50% height
                            1f to MaterialTheme.colorScheme.surface, // Fully black at the bottom
                        ),
                    ),
            )
        }
    } ?: Spacer(
        Modifier.height(
            padding.calculateTopPadding() + 60.dp,
        ),
    )
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
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = pad,
            )
            .relativeMove(backdropImage?.let { (-15).dp } ?: 0.dp), // Pulls the item UP by 20dp
    ) {
        Column(
            modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Spacer(Modifier.height(10.dp))

            Text(
                title.orEmpty(),
                style = MaterialTheme.typography.titleLarge, // Use the Large Title style here
                fontWeight = FontWeight.Bold,
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(
                    5.dp,
                ),
            ) {
                certification?.let {
                    BorderBox {
                        Text(
                            it,
                            modifier = Modifier.padding(
                                horizontal = 5.dp,
                            ),
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
                if (releaseDate != null) Text(
                    releaseDate.toString().formatDate("MMM yyyy"),
                )
                val intRuntime = runtime.toIntOrNull()
                if (intRuntime != null && intRuntime > 0) {
                    if (releaseDate != null) Text("•")
                    Text("$runtime mins")
                }
            }
//            Spacer(Modifier.height(3.dp))
            if (directorId != null) {
                Text("Directed by $director", Modifier.clickable { actions.onNavigateToProfile(directorId) })
            }
        }

        posterImage?.let {
            BorderBox {
                RewatchAsyncImage(
                    model = build(it, width = 100, height = 100),
                    contentScale = ContentScale.Fit,
                    contentDescription = title.orEmpty(),
                    modifier = modifier.height(150.dp),
                )
            }
        }
    }
}

@Composable
fun WatchTab(
    pad: Dp,
    providers: TmdbProviders?,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(pad),
        modifier = Modifier.padding(vertical = 15.dp),
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
        modifier = Modifier.padding(vertical = 15.dp),
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
    content: @Composable (LazyItemScope.(T) -> Unit),
) {
    BoxWithConstraints {
        // 1. Define your adaptive minimum size
        val minCellSize = 90.dp
        // 2. Calculate the columns (Available Width / Min Cell Size)
        // We use max(1, ...) to ensure it never crashes with 0 columns on tiny screens
        val calculatedColumnCount = max(1, (maxWidth / minCellSize).toInt())
        // Now you have the exact integer to use however you want!
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.height(150.dp),
        ) {
            // Leading spacer
            item { Spacer(Modifier.width(5.dp)) }
            // Movie items
            items(items = similar.take(calculatedColumnCount), itemContent = content)
            item { Spacer(Modifier.width(5.dp)) }
        }
    }
}

@Composable
fun PersonItem(person: TmdbAnyPerson?, function: () -> Unit) = ListItem(
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
        Text(
            text = ((person as? TmdbCast)?.character ?: (person as? TmdbCrew)?.job).orEmpty(),
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
    modifier = LocalContext.current.let {
        Modifier
            .fillMaxWidth()
            .clickable(onClick = function)
    },
)

@Composable
fun LazyItemScope.ReviewItem(review: TmdbReview?) {
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
                    StarRating(rating)
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
            .animateItem()
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
    modifier =
        Modifier.clickable(onClick = onNavigateToMedia),
)