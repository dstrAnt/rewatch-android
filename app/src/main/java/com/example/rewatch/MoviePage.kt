package com.example.rewatch

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.layout.ContentScale
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


@Composable
@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class
)
fun MoviePage(data: MovieData, onNavigateBack: () -> Unit, padding: PaddingValues) {
    val movie = data.movie

    var state by remember { mutableStateOf(MovieTabs.CAST) }

    val scrollState = rememberLazyListState()
    val pad = 16.dp

    val availableTabs = MovieTabs.entries.filter { tab ->
        when (tab) {
            MovieTabs.CAST -> movie.credits?.cast != null
            MovieTabs.CREW -> movie.credits?.crew != null
            MovieTabs.DETAILS -> movie.credits?.cast != null
            MovieTabs.WATCH -> !movie.watchProviders?.results.isNullOrEmpty()

            //            CAST("Cast"), CREW("Crew"), DETAILS("Details"), WATCH        }
        }
    }

    var isCastExpanded by remember { mutableStateOf(false) }

    Box {
        LazyColumn(
            state = scrollState,
//            verticalArrangement = Arrangement.spacedBy(pad),
        ) {
            item {
                movie.backdropImage?.let { BackdropImage(it) }
                    ?: Spacer(Modifier.padding(top = padding.calculateTopPadding()))

                TitleAndInfoSection(pad, movie)
                Column(
                    modifier = Modifier.padding(
                        bottom = pad
                    ), verticalArrangement = Arrangement.spacedBy(
                        10.dp
                    )
                ) {
                    Text(
                        text = movie.tagline.orEmpty(),
                        modifier = Modifier.padding(horizontal = pad)
                    )
                    ExpandableText(
                        movie.overview.orEmpty(), modifier = Modifier.padding(horizontal = pad)
                    )
                }
                HorizontalDivider()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = pad),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    StarRating(movie.voteAverage.toDouble())
                    Text("out of " + movie.voteCount.toString() + " ratings")
                }
            }

            item {
                HorizontalDivider()

                PrimaryTabRow(
                    selectedTabIndex = MovieTabs.entries.indexOf(
                        state
                    )
                ) {
                    availableTabs.forEach {
                        Tab(selected = it == state, onClick = { state = it }, text = {
                            Text(
                                text = it.title,
                                style = MaterialTheme.typography.labelLarge // Explicitly setting it here
//                                    style = MaterialTheme.typography.labelMedium,  // Use the Large Title style here
                            )
                        })
                    }
                }
                Spacer(Modifier.height(7.dp))
            }

            when (state) {
                // cast
                MovieTabs.CAST -> {
// 1. Define the expansion state at the top of your Composable

                    val cast = movie.credits?.cast.orEmpty()

// 2. Determine how many items to show
                    val displayCount = if (isCastExpanded) cast.size else cast.size.coerceAtMost(7)

// 3. Render in the LazyList
                    items(displayCount) { index ->
                        val actor = cast.getOrNull(index)
                        PersonItem(actor)
                    }

// 4. Show the button only if not expanded AND there are more than 7 actors
                    if (cast.size > 7) {
                        item {
                            ExpandBtn(
                                if (!isCastExpanded) movie.credits?.cast?.size.toString() + " Cast" else "Hide"
                            ) {
                                isCastExpanded = !isCastExpanded
                            }
                        }
                    }
                }

                // crew
                MovieTabs.CREW -> {
                    val crew = movie.credits?.crew.orEmpty()

                    // director first
                    val sortedCrew = crew.sortedByDescending { it.job == "Director" }
                    items(
                        minOf(movie.credits?.crew?.size ?: 7, 7)
                    ) {
                        PersonItem(sortedCrew[it])
                    }
                    item {
                        ExpandBtn("All Crew") {}
                    }
                }

                // details
                MovieTabs.DETAILS -> item {
                    DetailsTab(pad, movie)
                }

                // watch
                MovieTabs.WATCH -> item {
                    WatchTab(pad, movie)
                }
            }




            if (!movie.reviews?.results.isNullOrEmpty()) {
                item {
                    HorizontalDivider()
                    Text(
                        "Reviews", modifier = Modifier.padding(pad)
                    )

                }
                items(
                    minOf(movie.reviews?.results?.size!!, 3)
                ) {
                    val review = movie.reviews?.results[it]
                    ReviewItem(review)
                }

                item {
                    ExpandBtn("All reviews") {}
                }
            }

            if (data.similar.isNotEmpty()) {

                item {
                    HorizontalDivider()

                    Text(
                        "Similar", modifier = Modifier.padding(pad)
                    )

                    SimilarSection(data.similar, pad)

                    Spacer(Modifier.height(pad))
                }
            }


        }

        AppearingTopBar(
            scrollState, movie.title, onNavigateBack
        )
    }
}

@Composable
fun ExpandableText(
    text: String, modifier: Modifier = Modifier, maxLines: Int = 3
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
    scrollState: LazyListState, title: String?, onNavigateBack: () -> Unit
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
            model = image?.let {
                build(
                    it, width = 400, height = 200
                )
            },
            contentDescription = "Translated description of what the image contains",
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
    pad: Dp, movie: TmdbMovieDetail
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = pad
            )
            .relativeMove(movie.backdropImage?.let { (-20).dp }
                ?: 0.dp) // Pulls the item UP by 20dp
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Spacer(Modifier.height(10.dp))

            Text(
                movie.title.orEmpty(),
                style = MaterialTheme.typography.titleLarge,  // Use the Large Title style here
                fontWeight = FontWeight.Bold
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(
                    5.dp
                )
            ) {
                movie.releaseDates?.results?.firstOrNull { it.iso3166 == "US" }?.releaseDates?.firstOrNull { it.certification != "" }?.certification?.let {
                    PosterImage {
                        Text(
                            it, modifier = Modifier.padding(
                                horizontal = 5.dp
                            ), fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(
                    movie.releaseDate.toString().formatDate("MMM yyyy")
                )
                Text("•")
                Text(movie.runtime.toString() + " mins")

            }
//            Spacer(Modifier.height(3.dp))
            Text("Directed by " + movie.credits?.crew?.find { it.job == "Director" }?.name.orEmpty())
        }
        PosterImage {
            AsyncImage(
                model = movie.posterImage?.let {
                    build(
                        it, width = 300, height = 50
                    )
                }, movie.title.orEmpty(), Modifier.height(150.dp)
            )
        }
    }
}


@Composable
private fun WatchTab(
    pad: Dp, movie: TmdbMovieDetail
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(pad),
        modifier = Modifier.padding(vertical = 15.dp)
    ) {
        val usProviders = movie.watchProviders?.results["US"]

        if (usProviders != null) {

            if (usProviders.flatrate.isNotEmpty()) {
                WatchProviders(
                    "Streaming", usProviders.flatrate
                )
            }

//                            if (!usProviders.rent.isNullOrEmpty()) {
//                                ProviderCategorySection("Rent", usProviders.rent)
//                            }

            if (usProviders.buy.isNotEmpty()) {
                WatchProviders(
                    "Buy", usProviders.buy
                )
            }
        } else {
            Text(
                "No providers available", style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun DetailsTab(
    pad: Dp, movie: TmdbMovieDetail
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(pad),
        modifier = Modifier.padding(vertical = 15.dp)
    ) {
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
                            genre.name, modifier = Modifier.padding(5.dp)
                        )
                    }
                }
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Spacer(Modifier.width(10.dp))
            Text("Budget: $" + movie.budget.toDouble() / 1000000 + " million")
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Spacer(Modifier.width(10.dp))
            Text(
                "Countries: " + movie.productionCountries?.joinToString(
                    separator = ", "
                ) { it.name })
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Spacer(Modifier.width(10.dp))
            Text(
                "Release: " + movie.releaseDate?.toString()?.formatDate("d MMMM yyyy")
            )
        }
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
                for (company in movie.productionCompanies.orEmpty()) item {
                    CompanyItem(company)
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
            ), modifier = Modifier.padding(5.dp), verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = company.logoPath?.let {

                    TmdbImage.build(
                        it, TmdbImageType.PROFILE
                    )?.let { image ->

                        build(
                            image, width = 400, height = 200

                        )
                    }
                },
                company.name ?: "company",
                Modifier
                    .height(20.dp)
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
private fun SimilarSection(similar: List<TmdbMovie>, pad: Dp) {
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
            PosterImage {
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
                        .fillMaxHeight()
                        .padding(pad),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        movie.title ?: "no title", color = Color.Gray, textAlign = TextAlign.Center
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
            .padding(vertical = 7.dp, horizontal = 15.dp)
    ) {
        AsyncImage(
            model = person?.profileImage?.let {
                build(
                    it, width = 70, height = 70
                )
            },
            contentDescription = person?.name.orEmpty(),
            contentScale = ContentScale.Crop,
            alignment = BiasAlignment(
                horizontalBias = 0f, verticalBias = -0.7f
            ),
            modifier = Modifier
                .size(55.dp, 60.dp)
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
            .clickable { isExpanded = !isExpanded } // 2. Internal: Handle click logic
            .padding(top = 7.dp, start = 15.dp, end = 0.dp)) {


        AsyncImage(
            model = review?.authorDetails?.avatarPath?.let {
                TmdbImage.build(it, TmdbImageType.PROFILE)?.let { image ->
                    build(
                        image, width = 70, height = 70

                    )
                }
            },
            "",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(45.dp)
                .clip(CircleShape) // Set the corner radius
                .border(1.dp, Color.Gray, CircleShape)

        )
        Column(
            verticalArrangement = Arrangement.spacedBy(5.dp),

            ) {
            Column(
                modifier = Modifier.padding(end = 15.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(review?.author.orEmpty())
                    review?.authorDetails?.rating?.toString()?.let { Text(it) }
                }
//                ExpandableText(
//                    review?.content.orEmpty(), maxLines = 5
//                )

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
    title: String, providers: List<TmdbProvider>
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(start = 5.dp)
    ) {
        Text(
            text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold
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

                                TmdbImage.build(
                                    it, TmdbImageType.PROFILE
                                )?.let { image ->

                                    build(
                                        image, width = 70, height = 70

                                    )
                                }
                            },
                            provider.providerName ?: "company",
                            Modifier
                                .height(20.dp)
                                .background(Color.White)
                                .padding(2.dp)
                        )
                        Column {
                            Text(
                                provider.providerName ?: "company"
                            )
                        }
                    }
                }
            }
        }
    }
}


enum class MovieTabs(
    val title: String,
) {
    CAST("Cast"), CREW("Crew"), DETAILS("Details"), WATCH("Watch")

}