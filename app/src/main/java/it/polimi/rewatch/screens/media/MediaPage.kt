package it.polimi.rewatch.screens.media

import android.annotation.SuppressLint
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
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
import it.polimi.rewatch.common.ExpandBtn
import it.polimi.rewatch.common.PosterItem
import it.polimi.rewatch.common.TabItem
import it.polimi.rewatch.common.formatDate
import it.polimi.rewatch.common.getShortNotation
import it.polimi.rewatch.common.onPersonClick
import it.polimi.rewatch.common.relativeMove
import it.polimi.rewatch.components.AnimatedUiState
import it.polimi.rewatch.components.RewatchAsyncImage
import it.polimi.rewatch.components.StarRating
import kotlinx.datetime.LocalDate
import java.util.Locale

fun isDetailsAvailable(details: MediaDetail): Boolean {
    if (details.specificDetails !is MediaSpecificDetails.Movie) return false
    val movie: MediaSpecificDetails.Movie = details.specificDetails
    return (movie.genres.isNotEmpty() ||
            movie.budget > 0 ||
            movie.releaseDate != null ||
            !movie.productionCompanies.isNullOrEmpty())
}

// Define this outside your Composable (e.g., at the bottom of the file or in a separate configuration file)
fun buildMediaTabs(movie: MediaDetail, pad: Dp): List<TabItem> {
    return listOf(
        TabItem(
            title = "Cast",
            isAvailable = { movie.cast.isNotEmpty() },
            content = { expandedIds, onToggle ->
                expandableSection(
                    title = "cast",
                    itemsList = movie.cast,
                    expandedSectionIds = expandedIds,
                    onToggleSection = onToggle,
                    initialVisibleCount = 7,
                    itemContent = { PersonItem(it) },
                )
            },
        ),
        TabItem(
            title = "Crew",
            isAvailable = { movie.crew.isNotEmpty() },
            content = { expandedIds, onToggle ->
                expandableSection(
                    title = "crew",
                    itemsList = movie.crew.sortedByDescending { it.job == "Director" },
                    expandedSectionIds = expandedIds,
                    onToggleSection = onToggle,
                    initialVisibleCount = 7,
                    itemContent = { PersonItem(it) },
                )
            },
        ),
        // ... Details and Watch tabs remain the same, they just ignore the lambda parameters
        TabItem(
            title = "Details",
            isAvailable = { isDetailsAvailable(movie) },
            content = { _, _ ->
                item {
                    DetailsTab(pad, movie)
                }
            },
        ),
        // ... Watch tab ...
        TabItem(
            title = "Watch",
            isAvailable = {
                movie.watchProviders["US"]?.run {
                    buy.isNotEmpty() || flatrate.isNotEmpty()
                } ?: false
            },
            content = { _, _ ->
                item { WatchTab(pad, movie.watchProviders["US"]) }
            },
        ),
    )
}

@Composable
fun MediaPage(
    movieId: Int,
    mediaType: MediaType,
    viewModel: MediaViewModel = hiltViewModel<MediaViewModel, MediaViewModel.Factory>(
        creationCallback = { it.create(movieId, mediaType) },
    ),
    padding: PaddingValues,
    onNavigateBack: () -> Unit,
) {
    AnimatedUiState(
        state = viewModel.uiState,
        loadingContent = ::MovieSkeleton,
    ) { data ->
        MediaContent(
            data = data,
            mediaType = mediaType,
            padding = padding,
            onNavigateBack = onNavigateBack,
        )
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class,
)
fun MediaContent(onNavigateBack: () -> Unit, padding: PaddingValues, data: MovieData, mediaType: MediaType) {
    val movie: MediaDetail = data.movie
    val scrollState = rememberLazyListState()
    val pad = 16.dp
    var selectedTabTitle by rememberSaveable { mutableStateOf<String?>(null) }
    var expandedSectionIds by rememberSaveable { mutableStateOf(emptySet<String>()) }
    val handleToggle: (String) -> Unit = { id ->
        expandedSectionIds = if (id in expandedSectionIds) expandedSectionIds - id else expandedSectionIds + id
    }
    val tabs = remember(movie) {
        buildMediaTabs(movie, pad)
    }

    Scaffold(
        topBar = {
            AppearingTopBar(
                scrollState, movie.title, onNavigateBack,
            )
        },
    ) {
        LazyColumn(
            state = scrollState,
            verticalArrangement = Arrangement.spacedBy(
                3.dp,
            ),
//            contentPadding = PaddingValues(
//                start = padding.calculateStartPadding(LayoutDirection.Ltr),
//                end = padding.calculateEndPadding(LayoutDirection.Ltr),
//            ),
        ) {
            item {
                BackdropImage(movie.backdropImage, padding)

                TitleAndInfoSection(
                    pad,
                    backdropImage = movie.backdropImage,
                    posterImage = movie.posterImage,
                    director = movie.director,
                    title = movie.title,
                    runtime = movie.runtime,
                    releaseDate = movie.releaseDate,
                    certification = movie.certification,
                )

                TaglineAndOverview(
                    pad, overview = movie.overview, tagline = movie.tagline,
                )

                HorizontalDivider()
                TmdbRatings(
                    voteCount = movie.voteCount,
                    voteAverage = movie.voteAverage,
                )
            }
//            println(movie.crew)
            tabbedSection(
                allTabs = tabs,
                selectedTabTitle = selectedTabTitle,
                expandedIds = expandedSectionIds, // Pass the volatile state here
                onToggle = handleToggle, // Pass the action here
                onTabSelected = { title ->
                    selectedTabTitle = title
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
                    SimilarSection(data.similar.take(7), pad)
                    Spacer(Modifier.height(pad))
                }
            }

            item {
                Spacer(Modifier.height(padding.calculateBottomPadding()))
            }
        }
    }
}

fun LazyListScope.optionalTitledSection(isVisible: Boolean, title: String, pad: Dp, content: LazyListScope.() -> Unit) {
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
    selectedTabTitle: String?,
    expandedIds: Set<String>, // Add this
    onToggle: (String) -> Unit, // Add this
    onTabSelected: (String) -> Unit,
) {
    // 1. Perform the available tabs calculation
    val availableTabs = allTabs.filter { it.isAvailable() }

    if (availableTabs.isNotEmpty()) {
        // 2. Resolve the active tab (fallback to the first available if none selected/found)
        val activeTab = availableTabs.find { it.title == selectedTabTitle } ?: availableTabs.first()

        item {
            HorizontalDivider()

            PrimaryTabRow(
                selectedTabIndex = availableTabs.indexOf(activeTab).coerceAtLeast(0),
            ) {
                availableTabs.forEach { tab ->
                    Tab(
                        selected = tab.title == activeTab.title,
                        onClick = { onTabSelected(tab.title) },
                        text = {
                            Text(
                                text = tab.title,
                                style = MaterialTheme.typography.labelLarge,
                            )
                        },
                    )
                }
            }
            Spacer(Modifier.height(7.dp))
        }
        // 3. Render the content seamlessly
// Pass the state into the content execution
        activeTab.content(this, expandedIds, onToggle)
    }
}

@Composable
fun TaglineAndOverview(pad: Dp, overview: String?, tagline: String?) {
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
fun TmdbRatings(voteCount: Int?, voteAverage: Float?) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
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
            voteCount?.let {
                if (it > 0) {
                    Text(
                        "%.1f".format(
                            Locale.US, voteAverage,
                        ) + "/10",
                        fontWeight = FontWeight.Bold,
                    )
                    Text(voteCount.toDouble().getShortNotation() + " votes")
                } else {
                    Text("No votes yet")
                }
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
    itemContent: @Composable LazyItemScope.(T?) -> Unit,
) {
// 1. Evaluate the state internall
    val isExpanded = expandedSectionIds.contains(title)
    val visibleItems = if (isExpanded) itemsList else itemsList.take(initialVisibleCount)

    items(
        items = visibleItems, itemContent = itemContent,
    )

    println(title + itemsList.size)
    // 2. The Header / Toggle Button
    item(key = "header_$title") {
        // 3. Pass the sectionId back up when clicked
        val remaining = itemsList.size - visibleItems.size

        if (isExpanded || remaining > 0) ExpandBtn(
            if (isExpanded) "Hide $title" else "Show $remaining more",
            onClick = { onToggleSection(title) },
            isExpanded = isExpanded,
        )
    }
}

@Composable
fun ExpandableText(text: String, modifier: Modifier = Modifier, maxLines: Int = 3) {
    var isExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .animateContentSize() // 1. Internal: Handle animation first
            .clickable { isExpanded = !isExpanded } // 2. Internal: Handle click logic
            .then(modifier), // 3. Argument: Apply user modifiers (padding, background, etc.) last    )
    ) {
        Text(
            text = text,
            // When expanded, we set maxLines to Int.MAX_VALUE
            maxLines = if (isExpanded) Int.MAX_VALUE else maxLines,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearingTopBar(scrollState: LazyListState, title: String?, onNavigateBack: () -> Unit) {
    val toolbarAlpha by remember {
        derivedStateOf {
            // 1. Direct index check first (Early exit)
            if (scrollState.firstVisibleItemIndex > 0) return@derivedStateOf 1f
            // 2. Use the scroll offset (already an Int, no need for toFloat() until the math)
            val offset = scrollState.firstVisibleItemScrollOffset
            // 3. Simple linear interpolation (lerp) logic
            // Starts appearing at 450px, fully opaque at 700px (450 + 250)
            ((offset - 450f) / 250f).coerceIn(0f, 1f)
        }
    }
    TopAppBar(
        title = {
            if (title != null) {
                Text(
                    title,
                    modifier = Modifier.alpha(toolbarAlpha),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = Color.White,
                )
            }
        },
        // Apply background color with alpha
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Black.copy(alpha = toolbarAlpha),
            scrolledContainerColor = Color.Black.copy(alpha = toolbarAlpha),
        ),
        // Crucial: Add status bar insets so the buttons don't hit the clock/battery
//        windowInsets = WindowInsets.statusBars
    )
}

@Composable
fun BackdropImage(image: TmdbImage?, padding: PaddingValues) {
    image?.let {
        Box {
            RewatchAsyncImage(
                model = build(it, width = 600, height = 300),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp),
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
            if (director != null) {
                Text("Directed by $director")
            }
        }

        BorderBox {
            RewatchAsyncImage(
                model = posterImage?.let { build(it, width = 100, height = 100) },
                contentScale = ContentScale.Fit,
                contentDescription = title.orEmpty(),
                modifier = Modifier.height(150.dp),
            )
        }
    }
}

@Composable
fun WatchTab(pad: Dp, providers: TmdbProviders?) {
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
fun DetailsTab(pad: Dp, details: MediaDetail) {
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
fun DetailRow(label: String, content: @Composable () -> Unit = {}) {
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
                        .clip(RoundedCornerShape(5.dp))
                        .background(Color.White)
                        .padding(2.dp),
                )
            }
            Text(text = companyName)
        }
    }
}

@Composable
fun SimilarSection(similar: List<Media>, pad: Dp) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.height(150.dp),
    ) {
        // Leading spacer
        item { Spacer(Modifier.width(5.dp)) }
        // Movie items
        items(similar) {
            PosterItem(it.id, it.posterPath, it.title, MediaType.MOVIE)

        }
        item { Spacer(Modifier.width(5.dp)) }
    }
}

@Composable
fun PersonItem(person: TmdbAnyPerson?) {
    val role = (person as? TmdbCast)?.character ?: (person as? TmdbCrew)?.job
    val ctx = LocalContext.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(15.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPersonClick(person?.id!!, ctx) }
            .padding(vertical = 7.dp, horizontal = 15.dp),
    ) {
        RewatchAsyncImage(
            model = person?.profileImage?.let { build(it, width = 70, height = 70) },
            contentDescription = person?.name.orEmpty(),
            fallbackResId = R.drawable.ticket1,
            alignment = BiasAlignment(horizontalBias = 0f, verticalBias = -0.7f),
            modifier = Modifier
                .size(width = 55.dp, height = 60.dp)
                .clip(RoundedCornerShape(7.dp)),
        )

        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = person?.name.orEmpty(),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = role.orEmpty(),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "Dettagli attore",
            tint = Color.Gray,
        )
    }
}

@Composable
fun ReviewItem(review: TmdbReview?) {
    var isExpanded by remember { mutableStateOf(false) }

    Row(
        horizontalArrangement = Arrangement.spacedBy(15.dp),
        modifier = Modifier
            .animateContentSize()
            .clickable { isExpanded = !isExpanded }
            .padding(top = 7.dp, start = 15.dp), // end = 0.dp is default, so it can be omitted
    ) {
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

        Column(
            verticalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Column(
                modifier = Modifier.padding(end = 15.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp),
            ) {
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
                    Spacer(Modifier.width(8.dp))
                    review?.authorDetails?.rating?.toDouble()?.let { rating ->
                        StarRating(rating)
                    }
                }
                Text(
                    text = review?.content.orEmpty(),
                    maxLines = if (isExpanded) Int.MAX_VALUE else 5,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            HorizontalDivider()
        }
    }
}

@Composable
fun WatchProviders(title: String, providers: List<TmdbProvider>) {
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
