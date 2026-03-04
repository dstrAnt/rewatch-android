package com.example.rewatch

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import app.moviebase.tmdb.image.TmdbImage
import app.moviebase.tmdb.image.TmdbImageType
import app.moviebase.tmdb.image.TmdbImageUrlBuilder
import app.moviebase.tmdb.model.TmdbCast
import app.moviebase.tmdb.model.TmdbCrew
import app.moviebase.tmdb.model.TmdbMovieDetail
import app.moviebase.tmdb.model.TmdbProvider
import app.moviebase.tmdb.model.TmdbReview
import coil3.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.Locale


@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
fun MoviePage(data: MovieData, onNavigateBack: () -> Unit) {
    val movie = data.movie

    var state by remember { mutableStateOf(MovieTab.CAST) }
    listOf("Cast", "Crew", "Details", "Watch")

    val scrollState = rememberLazyListState()
    val pad = 16.dp

    Box {
        LazyColumn(
            state = scrollState,
//            verticalArrangement = Arrangement.spacedBy(pad),
        ) {
            item {

                PosterImage(movie.backdropImage)

                TitleAndInfoSection(pad, movie)

                Column(
                    modifier = Modifier.padding(start = pad, end = pad, bottom = pad),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(text = movie.tagline.orEmpty())
                    Text(movie.overview.orEmpty(), maxLines = 3, overflow = TextOverflow.Ellipsis)
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

                PrimaryTabRow(selectedTabIndex = MovieTab.entries.indexOf(state)) {
                    MovieTab.entries.forEach {
                        Tab(selected = it == state, onClick = { state = it }, text = {
                            Text(
                                text = it.title,
                                style = MaterialTheme.typography.labelLarge // Explicitly setting it here
//                                    style = MaterialTheme.typography.labelMedium,  // Use the Large Title style here
                            )
                        })
                    }
                }
            }

            when (state) {
                // cast
                MovieTab.CAST -> {
                    items(movie.credits?.cast?.size?.coerceAtMost(7) ?: 7) {
                        val actor = movie.credits?.cast[it]

                        CastItem(actor)
                    }
                    item {
                        ExpandBtn("All Cast") {}
                    }
                }

                // crew
                MovieTab.CREW -> {
                    val crew = movie.credits?.crew ?: emptyList()

                    // director first
                    val sortedCrew = crew.sortedByDescending { it.job == "Director" }
                    items(movie.credits?.crew?.size?.coerceAtMost(7) ?: 7) {

                        CrewItem(sortedCrew[it])

                    }
                    item {
                        ExpandBtn("All Crew") {}
                    }
                }

                // details
                MovieTab.DETAILS -> item {
                    MovieDetailsTab(pad, movie)
                }

                // watch
                MovieTab.WATCH -> item {
                    MovieWatchTab(pad, movie)
                }
            }



            item {
                HorizontalDivider()
                Text("Reviews", modifier = Modifier.padding(pad))

            }

            items(movie.reviews?.results?.size?.coerceAtMost(3) ?: 3) {
                val review = movie.reviews?.results[it]
                ReviewItem(review)
            }

            item {
                ExpandBtn("All reviews") {}
            }

            item {
                HorizontalDivider()

                Text("Similar", modifier = Modifier.padding(pad))

                SimilarSection(data, pad)

                Spacer(Modifier.height(pad))
            }


        }

        MovieTopBar(scrollState, movie.title, onNavigateBack)
    }
}

@Composable
private fun SimilarSection(data: MovieData, pad: Dp) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.height(150.dp)
    ) {
//                item { Spacer(Modifier.width(5.dp)) }
        item {
            Spacer(Modifier.width(5.dp))
        }
        for (movie in data.similar) item {
            PosterImage {
                if (movie.posterImage != null) AsyncImage(
                    model = movie.posterImage?.let {
                        TmdbImageUrlBuilder.build(
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
private fun MovieWatchTab(
    pad: Dp, movie: TmdbMovieDetail
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(pad),
        modifier = Modifier.padding(vertical = 15.dp)
    ) {
        val usProviders = movie.watchProviders?.results["US"]

        if (usProviders != null) {

            if (usProviders.flatrate.isNotEmpty()) {
                ProviderCategorySection("Streaming", usProviders.flatrate)
            }

//                            if (!usProviders.rent.isNullOrEmpty()) {
//                                ProviderCategorySection("Rent", usProviders.rent)
//                            }

            if (usProviders.buy.isNotEmpty()) {
                ProviderCategorySection("Buy", usProviders.buy)
            }
        } else {
            Text("No providers available", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun MovieDetailsTab(
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
                horizontalArrangement = Arrangement.spacedBy(10.dp)
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
            Text("Countries: " + movie.productionCountries?.joinToString(separator = ", ") { it.name })
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
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                for (company in movie.productionCompanies.orEmpty()) item {
                    PosterImage {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(15.dp),
                            modifier = Modifier.padding(5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = company.logoPath?.let {

                                    TmdbImage.build(
                                        it, TmdbImageType.PROFILE
                                    )?.let { image ->

                                        TmdbImageUrlBuilder.build(
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
                                Text(company.name ?: "company")
                            }
                        }
                    }
                }
            }
        }
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
            .relativeMove((-20).dp) // Pulls the item UP by 20dp
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
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                PosterImage {
                    Text(
                        movie.releaseDates?.results?.firstOrNull { it.iso3166 == "US" }?.releaseDates?.firstOrNull { it.certification != "" }?.certification
                            ?: "?",
                        modifier = Modifier.padding(horizontal = 5.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(movie.releaseDate.toString().formatDate("MMM yyyy"))
                Text("•")
                Text(movie.runtime.toString() + " mins")

            }
//            Spacer(Modifier.height(3.dp))
            Text("Directed by " + movie.credits?.crew?.find { it.job == "Director" }?.name.orEmpty())
        }
        PosterImage {
            AsyncImage(
                model = movie.posterImage?.let {
                    TmdbImageUrlBuilder.build(
                        it, width = 300, height = 50
                    )
                }, movie.title.orEmpty(), Modifier.height(150.dp)
            )
        }
    }
}

@Composable
private fun PosterImage(image: TmdbImage?) {
    Box {
        AsyncImage(
            model = image?.let {
                TmdbImageUrlBuilder.build(
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
private fun ExpandBtn(string: String, function: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = function)
            .padding(15.dp)
    ) {

        Text(string)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MovieTopBar(scrollState: LazyListState, title: String?, onNavigateBack: () -> Unit) {
    val toolbarAlpha by remember {
        derivedStateOf {
            val scrollOffset = scrollState.firstVisibleItemScrollOffset.toFloat()
            val firstIndex = scrollState.firstVisibleItemIndex
//            println(scrollOffset)
            if (firstIndex > 0f) 1f else ((scrollOffset - 450f) / 250f).coerceIn(0f, 1f)
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
private fun PosterImage(content: @Composable (() -> Unit)) {

    val shape = RoundedCornerShape(5.dp)

    Box(
        modifier = Modifier
            .clip(shape) // Set the corner radius
            .border(
                BorderStroke(1.dp, Color.Gray), shape
            ) // Add border with rounded corners
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = { /* Action */ })
    ) {
        content()
    }
}


@Composable
private fun CastItem(actor: TmdbCast?) {
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
            model = actor?.profileImage?.let {
                TmdbImageUrlBuilder.build(it, width = 70, height = 70)
            },
            contentDescription = actor?.name.orEmpty(),
            contentScale = ContentScale.Crop,
            alignment = BiasAlignment(horizontalBias = 0f, verticalBias = -0.7f),
            modifier = Modifier
                .size(55.dp, 60.dp)
                .clip(RoundedCornerShape(7.dp))
        )

        Column(modifier = Modifier.weight(1f)) { // Il weight(1f) spinge tutto il resto a destra
            Text(
                text = actor?.name.orEmpty(),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = actor?.character.orEmpty(),
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
    Row(
        horizontalArrangement = Arrangement.spacedBy(15.dp),
        modifier = Modifier
            .clickable(onClick = {})
            .padding(top = 7.dp, start = 15.dp, end = 0.dp)
    ) {
        AsyncImage(
            model = review?.authorDetails?.avatarPath?.let {
                TmdbImage.build(it, TmdbImageType.PROFILE)?.let { image ->
                    TmdbImageUrlBuilder.build(
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
                Text(review?.content.orEmpty(), maxLines = 5, overflow = TextOverflow.Ellipsis)
            }
            HorizontalDivider()
        }
    }
}

fun String.formatDate(format: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        val date = inputFormat.parse(this)

        val outputFormat = SimpleDateFormat(format, Locale.US)

        if (date != null) outputFormat.format(date) else this
    } catch (_: Exception) {
        this
    }
}

@Composable
fun ProviderCategorySection(title: String, providers: List<TmdbProvider>) {
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
                        horizontalArrangement = Arrangement.spacedBy(15.dp),
                        modifier = Modifier.padding(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = provider.logoPath?.let {

                                TmdbImage.build(
                                    it, TmdbImageType.PROFILE
                                )?.let { image ->

                                    TmdbImageUrlBuilder.build(
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
                            Text(provider.providerName ?: "company")
                        }
                    }
                }
            }
        }
    }
}

fun Modifier.relativeMove(y: Dp) = this.layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)
    layout(placeable.width, placeable.height + y.roundToPx()) {
        // Placing it at the negative Y offset
        placeable.placeRelative(0, y.roundToPx())
    }
}

@Composable
fun StarRating(rating: Double, maxRating: Int = 5) {
    // Convertiamo il voto da base 10 a base 5
    val starRating = (rating / 2).coerceIn(0.0, 5.0)

    val fullStars = starRating.toInt()
    val partialStarFill = (starRating - fullStars).toFloat()
    val emptyStars = maxRating - fullStars - (if (partialStarFill > 0) 1 else 0)

    Row(verticalAlignment = Alignment.CenterVertically) {
        // 1. Stelle Piene
        repeat(fullStars) {
            Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFFFFC107))
        }

        // 2. Stella Parziale (il "pezzettino")
        if (partialStarFill > 0) {
            Box {
                // Sfondo della stella (vuota)
                Icon(Icons.Filled.Star, contentDescription = null, tint = Color.LightGray)
                // Parte riempita (ritagliata)
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = Color(0xFFFFC107),
                    modifier = Modifier.clip(FractionalClip(partialStarFill))
                )
            }
        }

        // 3. Stelle Vuote
        repeat(emptyStars) {
            Icon(Icons.Filled.Star, contentDescription = null, tint = Color.LightGray)
        }

        Text(
            text = " (${String.format("%.2f", rating)})",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}

class FractionalClip(val fraction: Float) : Shape {
    override fun createOutline(
        size: Size, layoutDirection: LayoutDirection, density: Density
    ): Outline {
        return Outline.Rectangle(Rect(0f, 0f, size.width * fraction, size.height))
    }
}

enum class MovieTab(
    val title: String,
) {
    CAST("Cast"), CREW("Crew"), DETAILS("Details"), WATCH("Watch")
}

@Composable
private fun CrewItem(person: TmdbCrew) {
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
            model = person.profileImage?.let {
                TmdbImageUrlBuilder.build(it, width = 70, height = 70)
            },
            contentDescription = person.name.orEmpty(),
            contentScale = ContentScale.Crop,
            alignment = BiasAlignment(horizontalBias = 0f, verticalBias = -0.7f),
            modifier = Modifier
                .size(55.dp, 60.dp)
                .clip(RoundedCornerShape(7.dp))
        )

        Column(modifier = Modifier.weight(1f)) { // Il weight(1f) spinge tutto il resto a destra
            Text(
                text = person.name.orEmpty(),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = person.job.orEmpty(),
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
