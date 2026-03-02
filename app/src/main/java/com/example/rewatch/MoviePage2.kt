package com.example.rewatch

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.moviebase.tmdb.image.TmdbImage
import app.moviebase.tmdb.image.TmdbImageType
import app.moviebase.tmdb.image.TmdbImageUrlBuilder
import app.moviebase.tmdb.model.TmdbCast
import app.moviebase.tmdb.model.TmdbReview
import coil3.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
fun MoviePage2(data: MovieData) {

    val movie = data.movie

    var state by remember { mutableIntStateOf(0) }
    val titles = listOf("Cast", "Crew", "Details", "Watch")

    val pad = 16.dp

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(pad)
    ) {
        item {

            AsyncImage(
                model = movie.backdropImage?.let {
                    TmdbImageUrlBuilder.build(
                        it, width = 400, height = 200
                    )
                },
                contentDescription = "Translated description of what the image contains",
                modifier = Modifier.fillParentMaxWidth(),
                contentScale = ContentScale.Crop
            )

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        pad
                    )

            ) {
                Column {
                    Text(movie.title!!, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(pad))
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        PosterImage {
                            Text(
                                movie.releaseDates?.results
                                    ?.firstOrNull { it.iso3166 == "US" }
                                    ?.releaseDates
                                    ?.firstOrNull { it.certification != "" }?.certification ?: "?",
                                modifier = Modifier.padding(horizontal = 5.dp),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(movie.releaseDate.toString().formatDate2("MMM yyyy"))
                        Text("•")
                        Text(movie.runtime.toString() + " mins")

                    }
                    Text("Directed by " + movie.credits?.crew?.find { it.job == "Director" }?.name!!)
                }
                PosterImage {
                    AsyncImage(
                        model = movie.posterImage?.let {
                            TmdbImageUrlBuilder.build(
                                it, width = 300, height = 50
                            )
                        }, movie.title!!, Modifier.height(150.dp)
                    )
                }
            }
            Column(
                modifier = Modifier.padding(horizontal = pad),
                verticalArrangement = Arrangement.spacedBy(pad)
            ) {
                Text(text = movie.tagline!!)
                Text(movie.overview!!)
                Text(movie.voteAverage.toString())

            }
        }

        item {

            PrimaryTabRow(selectedTabIndex = state) {
                titles.forEachIndexed { index, title ->
                    Tab(
                        selected = state == index,
                        onClick = { state = index },
                        text = { Text(text = title) })
                }
            }
        }

        when (state) {
            // cast
            0 -> items(10) {
                val actor = movie.credits?.cast[it]!!

                CastItem(actor)
            }

            // crew
            1 -> item {
                Text(
                    text = "Content for ${titles[state]}", modifier = Modifier.padding(pad)
                )
            }

            // details
            2 -> item {
                Column(verticalArrangement = Arrangement.spacedBy(pad)) {
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
                                PosterImage { Text(genre.name, modifier = Modifier.padding(5.dp)) }
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
                        Text("Release: " + movie.releaseDate.toString().formatDate2("d MMMM yyyy"))
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
                            for (company in movie.productionCompanies!!) item {
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

            // watch
            3 -> item {
                Column() {
                    movie.releaseDates?.results
                        ?.firstOrNull { it.iso3166 == "US" }
                        ?.releaseDates
                        ?.firstOrNull { it.certification != "" }
                }
            }
        }
        item {
            HorizontalDivider()
            Text("Reviews", modifier = Modifier.padding(pad))
        }

        items(5) {
            val review = movie.reviews?.results[it]!!
            ReviewItem(review)
        }

        item {
            HorizontalDivider()

            Text("Similar", modifier = Modifier.padding(pad))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.height(150.dp)

            ) {
                item { Spacer(Modifier.width(pad)) }
                for (movie in data.similar) item {
                    PosterImage {
                        if (movie.posterImage != null) AsyncImage(
                            model = movie.posterImage?.let {
                                TmdbImageUrlBuilder.build(
                                    it, width = 400, height = 200
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
                                movie.title ?: "no title",
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

            }
            Spacer(Modifier.height(pad))
        }

    }
}

@Composable
private fun CastItem(actor: TmdbCast) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(15.dp), modifier = Modifier.padding(15.dp)
    ) {
        AsyncImage(
            model = actor.profileImage?.let {
                TmdbImageUrlBuilder.build(
                    it, width = 300, height = 50
                )
            }, actor.name ?: "actor", Modifier.height(80.dp)
        )
        Column {

            Text(actor.name ?: "actor")
            Text(actor.character ?: "character")
        }
    }
}

@Composable
private fun PosterImage(content: @Composable (() -> Unit)) {

    val shape = RoundedCornerShape(5.dp)

    Box(
        modifier = Modifier
            .fillMaxHeight()
            .clip(shape) // Set the corner radius
            .border(
                BorderStroke(1.dp, Color.Gray), shape
            ) // Add border with rounded corners
    ) {
        content()
    }
}

@Composable
private fun ReviewItem(review: TmdbReview) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.padding(16.dp)
    ) {
        AsyncImage(
            model = review.authorDetails?.avatarPath?.let { imagePath ->
                TmdbImageUrlBuilder.build(
                    imagePath, "w154"
                )
            },
            "", Modifier.width(80.dp),
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(5.dp),

            ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()
            ) {
                Text(review.author)
                review.authorDetails?.rating?.toString()?.let { Text(it) }
            }
            Text(review.content, maxLines = 10)
            HorizontalDivider()
        }
    }
}

fun String.formatDate2(format: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        val date = inputFormat.parse(this)

        val outputFormat = SimpleDateFormat(format, Locale.US)

        if (date != null) outputFormat.format(date) else this
    } catch (e: Exception) {
        this
    }
}