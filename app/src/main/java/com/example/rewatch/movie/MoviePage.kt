package com.example.rewatch.movie

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.moviebase.tmdb.image.TmdbImage
import app.moviebase.tmdb.image.TmdbImageType
import app.moviebase.tmdb.image.TmdbImageUrlBuilder.build
import app.moviebase.tmdb.model.TmdbAnyPerson
import app.moviebase.tmdb.model.TmdbCast
import app.moviebase.tmdb.model.TmdbCompany
import app.moviebase.tmdb.model.TmdbCrew
import app.moviebase.tmdb.model.TmdbMovie
import app.moviebase.tmdb.model.TmdbMovieDetail
import app.moviebase.tmdb.model.TmdbProvider
import app.moviebase.tmdb.model.TmdbReview
import coil3.compose.AsyncImage
import com.example.rewatch.ExpandBtn
import com.example.rewatch.PosterImage
import com.example.rewatch.R
import com.example.rewatch.StarRating
import com.example.rewatch.formatDate
import com.example.rewatch.getShortNotation
import com.example.rewatch.onMovieClick
import com.example.rewatch.relativeMove

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MoviePage(
    movieId: Int,
    viewModel: MovieViewModel = MovieViewModel(movieId),
    padding: PaddingValues,
    onNavigateBack: () -> Unit,
) {
    // Osserviamo lo stato del ViewModel
    when (val state = viewModel.uiState) {
        is MovieUiState.Loading -> {
            MovieSkeleton()
        }

        is MovieUiState.Success -> {
            // Chiamiamo la funzione che disegna effettivamente la UI passando i dati
            MovieContent(
                data = state.movie,
                padding = padding,
                onNavigateBack = onNavigateBack
            )
        }

        is MovieUiState.Error -> {
            Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Error: ${state.msg}")
            }
        }
    }
}


@Composable
@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class
)

fun MovieContent(
    onNavigateBack: () -> Unit,
    padding: PaddingValues,
    data: MovieData
) {

    val movie = data.movie

    var tabState by remember { mutableStateOf(MovieTabs.CAST) }

    val scrollState = rememberLazyListState()
    val pad = 16.dp

    val availableTabs = MovieTabs.entries.filter { tab ->
        !when (tab) {
            MovieTabs.CAST -> movie?.credits?.cast.isNullOrEmpty()
            MovieTabs.CREW -> movie?.credits?.crew.isNullOrEmpty()
            MovieTabs.DETAILS -> movie?.credits?.cast.isNullOrEmpty()
            MovieTabs.WATCH -> movie?.watchProviders?.results?.get("US")
                ?.run { buy.isEmpty() && flatrate.isEmpty() } ?: true

            //            CAST("Cast"), CREW("Crew"), DETAILS("Details"), WATCH        }
        }
    }

    var expandedSectionIds by remember { mutableStateOf(emptySet<String>()) }
    val handleToggle: (String) -> Unit = { id ->
        expandedSectionIds = if (expandedSectionIds.contains(id)) {
            expandedSectionIds - id
        } else {
            expandedSectionIds + id
        }
    }


    Box {
        LazyColumn(
            state = scrollState,
//            modifier = Modifier.animateContentSize()
//            modifier = Modifier.padding(bottom = padding.calculateBottomPadding())
//            verticalArrangement = Arrangement.spacedBy(pad),
        ) {
            item {
                if (movie != null) {

                    movie?.backdropImage?.let { BackdropImage(it) } ?: Spacer(
                        Modifier.height(
                            padding.calculateTopPadding() + 60.dp
                        )
                    )

                } else {
                    Spacer(Modifier.height(200.dp))
                }

                TitleAndInfoSection(pad, movie)

                Column(
                    modifier = Modifier.padding(bottom = pad),
                    verticalArrangement = Arrangement.spacedBy(
                        10.dp
                    )
                ) {
                    Spacer(modifier = Modifier.height(5.dp))
                    if (!movie?.tagline.isNullOrEmpty()) {
                        Text(
                            text = movie?.tagline.orEmpty(),
                            modifier = Modifier.padding(horizontal = pad)
                        )
                    }
                    if (movie?.overview != null && movie?.overview != "") {
                        ExpandableText(
                            movie?.overview.orEmpty(),
                            modifier = Modifier.padding(horizontal = pad)
                        )
                    }
                }
                HorizontalDivider()
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF022441))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.tmdb_logo),
                            contentDescription = "TMDB logo",
                            modifier = Modifier.width(100.dp)
                        )
                        movie?.voteCount?.let {
                            if (it > 0) {
                                Text(
                                    "%.1f".format(
                                        java.util.Locale.US,
                                        movie?.voteAverage
                                    ) + "/10",
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    movie?.voteCount?.toDouble()?.getShortNotation() + " votes"
                                )
                            } else {
                                Text(
                                    "No votes yet"
                                )
                            }
                        }
                    }
                }
            }

            item {
                HorizontalDivider()

                TabRow(
                    tabState,
                    {
                        tabState = it
                        expandedSectionIds = emptySet()
                    },
                    availableTabs
                )
                Spacer(Modifier.height(7.dp))
            }

            when (tabState) {
                // cast
                MovieTabs.CAST -> expandableSection(
                    title = "cast",
                    itemsList = movie?.credits?.cast.orEmpty(),
                    expandedSectionIds = expandedSectionIds,
                    onToggleSection = handleToggle,
                    initialVisibleCount = 7
                ) { PersonItem(it) }


                // crew
                MovieTabs.CREW -> expandableSection(
                    title = "crew",
                    itemsList = movie?.credits?.crew.orEmpty()
                        .sortedByDescending { it.job == "Director" },
                    expandedSectionIds = expandedSectionIds,
                    onToggleSection = handleToggle,
                    initialVisibleCount = 7
                ) { PersonItem(it) }


                // details
                MovieTabs.DETAILS -> item {
                    DetailsTab(pad, movie)
                }

                // watch
                MovieTabs.WATCH -> item {
                    WatchTab(pad, movie)
                }
            }



            if (!movie?.reviews?.results.isNullOrEmpty()) {
                item {
                    HorizontalDivider()
                    Text(
                        "Reviews", modifier = Modifier.padding(pad)
                    )

                }
                expandableSection(
                    title = "reviews",
                    itemsList = movie?.reviews?.results.orEmpty(),
                    expandedSectionIds = expandedSectionIds,
                    onToggleSection = handleToggle,
                    initialVisibleCount = 3
                ) { ReviewItem(it) }

            }

            if (!data?.similar.isNullOrEmpty()) {

                item {
                    HorizontalDivider()

                    Text(
                        "Similar", modifier = Modifier.padding(pad)
                    )

                    SimilarSection(
                        data.similar.take(7),
                        pad
                    )

                    Spacer(Modifier.height(pad))
                }
            }


        }

        AppearingTopBar(
            scrollState,
            movie?.title,
            onNavigateBack
        )
    }
}

@Composable
private fun TabRow(
    state: MovieTabs,
    onStateChange: (MovieTabs) -> Unit,
    availableTabs: List<MovieTabs>
) {
    PrimaryTabRow(
        selectedTabIndex = MovieTabs.entries.indexOf(
            state
        )
    ) {
        availableTabs.forEach {
            Tab(
                selected = it == state,
                onClick = { onStateChange(it) },
                text = {
                    Text(
                        text = it.title,
                        style = MaterialTheme.typography.labelLarge // Explicitly setting it here
//                                    style = MaterialTheme.typography.labelMedium,  // Use the Large Title style here
                    )
                })
        }
    }
}


fun <T> LazyListScope.expandableSection(
    title: String,
    itemsList: List<T>,
    expandedSectionIds: Set<String>,
    onToggleSection: (String) -> Unit,
    initialVisibleCount: Int = 3,
    itemContent: @Composable LazyItemScope.(T?) -> Unit
) {
// 1. Evaluate the state internall
    val isExpanded = expandedSectionIds.contains(title)

    val visibleItems = if (isExpanded) itemsList else itemsList.take(initialVisibleCount)

    items(
        items = visibleItems,
        itemContent = itemContent
    )

    // 2. The Header / Toggle Button
    item(key = "header_$title") {
        // 3. Pass the sectionId back up when clicked

        val remaining = itemsList.size - visibleItems.size

        if (isExpanded || remaining > 0) ExpandBtn(
            if (isExpanded) "Hide $title" else "Show $remaining more",
            onClick = { onToggleSection(title) })

    }
}

@Composable
fun ExpandableText(
    text: String,
    modifier: Modifier = Modifier,
    maxLines: Int = 3
) {
    var isExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .animateContentSize() // 1. Internal: Handle animation first
            .clickable { isExpanded = !isExpanded } // 2. Internal: Handle click logic
            .then(modifier) // 3. Argument: Apply user modifiers (padding, background, etc.) last    )

    ) {
        Text(
            text = text,
            // When expanded, we set maxLines to Int.MAX_VALUE
            maxLines = if (isExpanded) Int.MAX_VALUE else maxLines, overflow = TextOverflow.Ellipsis
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppearingTopBar(
    scrollState: LazyListState,
    title: String?,
    onNavigateBack: () -> Unit
) {
    val toolbarAlpha by remember {
        derivedStateOf {
            val scrollOffset = scrollState.firstVisibleItemScrollOffset.toFloat()
            val firstIndex = scrollState.firstVisibleItemIndex
//            println(scrollOffset)
            if (firstIndex > 0f) 1f else ((scrollOffset - 450f) / 250f).coerceIn(
                0f, 1f
            )
        }
    }
    TopAppBar(
        title = {
            if (title != null) {
                Text(
                    title, modifier = Modifier.alpha(toolbarAlpha)
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = Color.White
                )
            }
        },
        // Apply background color with alpha
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Black.copy(alpha = toolbarAlpha),
            scrolledContainerColor = Color.Black.copy(alpha = toolbarAlpha)
        ),
        // Crucial: Add status bar insets so the buttons don't hit the clock/battery
//        windowInsets = WindowInsets.statusBars
    )
}

@Composable
private fun BackdropImage(image: TmdbImage) {
    Box {
        AsyncImage(
            model = build(
                image,
                width = 600,
                height = 300
            ),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    brush = Brush.verticalGradient(
                        0.6f to Color.Transparent, // Stay transparent until 50% height
                        1f to MaterialTheme.colorScheme.surface        // Fully black at the bottom
                    )
                )
        )
    }
}


@Composable
private fun TitleAndInfoSection(
    pad: Dp,
    movie: TmdbMovieDetail?
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = pad
            )
            .relativeMove(movie?.backdropImage?.let { (-20).dp } ?: 0.dp) // Pulls the item UP by 20dp
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Spacer(Modifier.height(10.dp))

            Text(
                movie?.title.orEmpty(),
                style = MaterialTheme.typography.titleLarge,  // Use the Large Title style here
                fontWeight = FontWeight.Bold
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(
                    5.dp
                )
            ) {
                movie?.releaseDates?.results?.firstOrNull { it.iso3166 == "US" }?.releaseDates?.firstOrNull { it.certification != "" }?.certification?.let {
                    PosterImage {
                        Text(
                            it, modifier = Modifier.padding(
                                horizontal = 5.dp
                            ), fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(
                    movie?.releaseDate?.toString()?.formatDate("MMM yyyy") ?: ""
                )
                if (movie?.runtime != null && movie?.runtime != 0) {

                    Text("•")
                    Text("${movie?.runtime} mins")
                }
            }
//            Spacer(Modifier.height(3.dp))
            Text("Directed by " + movie?.credits?.crew?.find { it.job == "Director" }?.name.orEmpty())
        }
        PosterImage {
            AsyncImage(
                model = movie?.posterImage?.let {
                    build(
                        it, width = 300, height = 50
                    )
                },
                movie?.title.orEmpty(),
                Modifier.height(150.dp)
            )
        }
    }
}


@Composable
private fun WatchTab(
    pad: Dp,
    movie: TmdbMovieDetail?
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(pad),
        modifier = Modifier.padding(vertical = 15.dp)
    ) {
        val usProviders = movie?.watchProviders?.results["US"]

        if (usProviders != null) {

            if (usProviders.flatrate.isNotEmpty()) {
                WatchProviders(
                    "Streaming", usProviders.flatrate
                )
            }

            if (usProviders.buy.isNotEmpty()) {
                WatchProviders(
                    "Buy", usProviders.buy
                )
            }
        } else {
            Text(
                "No providers available",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun DetailsTab(
    pad: Dp,
    movie: TmdbMovieDetail?
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(pad),
        modifier = Modifier.padding(vertical = 15.dp)
    ) {
        if (!movie?.genres.isNullOrEmpty()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Spacer(Modifier.width(10.dp))
                Text("Genres:")
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(
                        10.dp
                    )
                ) {
                    for (genre in movie.genres) item {
                        PosterImage {
                            Text(
                                genre.name,
                                modifier = Modifier.padding(5.dp)
                            )
                        }
                    }
                    item { Spacer(Modifier.width(10.dp)) }
                }
            }
        }

        val budget = movie?.budget?.toDouble()
        if (budget != null) {
            if (budget > 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Spacer(Modifier.width(10.dp))
                    Text("Budget: $ " + budget.getShortNotation())
                }
            }
        }
        if (!movie?.productionCountries.isNullOrEmpty()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Spacer(Modifier.width(10.dp))
                Text(
                    "Countries: " + movie?.productionCountries?.joinToString(
                        separator = ", "
                    ) { it.name })
            }
        }
        if (movie?.releaseDate != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Spacer(Modifier.width(10.dp))
                Text(
                    "Release: " + movie?.releaseDate?.toString()?.formatDate("d MMMM yyyy")
                )
            }
        }
        if (!movie?.productionCompanies.isNullOrEmpty()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Spacer(Modifier.width(10.dp))
                Text("Production:")
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(
                        10.dp
                    )
                ) {
                    for (company in movie?.productionCompanies.orEmpty()) item {
                        CompanyItem(company)
                    }
                    item { Spacer(Modifier.width(10.dp)) }
                }
            }
        }
    }
}

@Composable
private fun CompanyItem(company: TmdbCompany) {
    PosterImage {
        Row(
            horizontalArrangement = Arrangement.spacedBy(
                15.dp
            ),
            modifier = Modifier.padding(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = company.logoPath?.let {
                    build(
                        it,
                        TmdbImageType.PROFILE,
                        width = 400,
                        height = 200
                    )
                },
                company.name ?: "company",
                Modifier
                    .height(20.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(Color.White)
                    .padding(2.dp)
            )
            Column {
                Text(
                    company.name ?: "company"
                )
            }
        }
    }
}


@Composable
private fun SimilarSection(
    similar: List<TmdbMovie>,
    pad: Dp
) {
    val context = LocalContext.current
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.height(150.dp)
    ) {
//                item { Spacer(Modifier.width(5.dp)) }
        item {
            Spacer(Modifier.width(5.dp))
        }
        for (movie in similar) item {
            PosterImage(onClick = { onMovieClick(movie.id, context) }) {
                if (movie.posterImage != null) AsyncImage(
                    model = movie.posterImage?.let {
                        build(
                            it, width = 100, height = 200
                        )
                    },
                    contentDescription = "Translated description of what the image contains",
                    modifier = Modifier.width(100.dp)
//                            contentScale = ContentScale.Crop
                ) else Box(
                    modifier = Modifier
                        .width(100.dp)
                        .height(150.dp)
                        .fillMaxHeight()
                        .padding(pad),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        movie?.title ?: "no title",
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        item {
            Spacer(Modifier.width(5.dp))
        }
    }
}

@Composable
private fun PersonItem(person: TmdbAnyPerson?) {

    val role = (person as? TmdbCast)?.character ?: (person as? TmdbCrew)?.job

    Row(
        verticalAlignment = Alignment.CenterVertically, // Allinea verticalmente gli elementi al centro
        horizontalArrangement = Arrangement.spacedBy(15.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = { /* Action */ })
            .padding(
                vertical = 7.dp,
                horizontal = 15.dp
            )
    ) {

        AsyncImage(
            model = person?.profileImage?.let {
                build(
                    it, width = 70, height = 70
                )
            },
            contentDescription = person?.name.orEmpty(),
//            placeholder = painterResource(R.drawable.ticket1),
            error = painterResource(R.drawable.ticket1),
            fallback = painterResource(R.drawable.ticket1),
            contentScale = ContentScale.Crop,
            alignment = BiasAlignment(
                horizontalBias = 0f, verticalBias = -0.7f
            ),
            modifier = Modifier
                .size(
                    55.dp,
                    60.dp
                )
                .clip(RoundedCornerShape(7.dp))
        )

        Column(modifier = Modifier.weight(1f)) { // Il weight(1f) spinge tutto il resto a destra
            Text(
                text = person?.name.orEmpty(),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = role.orEmpty(),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray // Un tocco di stile per differenziare
            )
        }

        // Lo Chevron alla fine
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "Dettagli attore",
            tint = Color.Gray // Colore discreto
        )
    }
}

@Composable
private fun ReviewItem(review: TmdbReview?) {
    var isExpanded by remember { mutableStateOf(false) }
    val maxLines = 5

    Row(
        horizontalArrangement = Arrangement.spacedBy(15.dp),
        modifier = Modifier
            .animateContentSize() // 1. Internal: Handle animation first
            .clickable {
                isExpanded = !isExpanded
            } // 2. Internal: Handle click logic
            .padding(
                top = 7.dp,
                start = 15.dp,
                end = 0.dp
            )) {


        AsyncImage(
            model = review?.authorDetails?.avatarPath?.let { image ->
                build(
                    image, TmdbImageType.PROFILE, width = 70, height = 70

                )
            },
            "",
            placeholder = rememberVectorPainter(Icons.Default.Person),
            error = rememberVectorPainter(Icons.Default.Person),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(45.dp)
                .clip(CircleShape) // Set the corner radius
                .border(
                    1.dp,
                    Color.Gray,
                    CircleShape
                )
                .background(Color(0xFF6E6E6E))

        )
        Column(
            verticalArrangement = Arrangement.spacedBy(7.dp),

            ) {
            Column(
                modifier = Modifier.padding(end = 15.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp),

                ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically, // Allinea verticalmente i due elementi
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = review?.author.orEmpty(),
                        modifier = Modifier.weight(1f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(Modifier.width(8.dp)) // Un po' di respiro tra nome e stelle

                    review?.authorDetails?.rating?.toDouble()?.let {
                        StarRating(it)
                    }
                }

                Box(
                    modifier = Modifier

                ) {
                    Text(
                        text = review?.content.orEmpty(),
                        // When expanded, we set maxLines to Int.MAX_VALUE
                        maxLines = if (isExpanded) Int.MAX_VALUE else maxLines,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            HorizontalDivider()
        }
    }
}

@Composable
fun WatchProviders(
    title: String,
    providers: List<TmdbProvider>
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.padding(start = 15.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            for (provider in providers) item {
                PosterImage {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(
                            15.dp
                        ),
                        modifier = Modifier.padding(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = provider.logoPath?.let {
                                build(
                                    it,
                                    TmdbImageType.PROFILE,
                                    width = 70,
                                    height = 70

                                )
                            },
                            provider.providerName ?: "company",
                            Modifier
                                .height(20.dp)
                                .clip(RoundedCornerShape(5.dp))
                        )
                        Column {
                            Text(
                                provider.providerName ?: "company"
                            )
                        }
                    }
                }
            }
            item { Spacer(Modifier.width(15.dp)) }
        }
    }
}


enum class MovieTabs(
    val title: String,
) {
    CAST("Cast"), CREW("Crew"), DETAILS("Details"), WATCH("Watch")

}

