package com.example.rewatch

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import app.moviebase.tmdb.image.TmdbImageUrlBuilder
import app.moviebase.tmdb.model.TmdbCast
import app.moviebase.tmdb.model.TmdbReview
import coil3.compose.AsyncImage

@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
fun MoviePage(data: MovieData) {

    val movie = data.movie

    var state by remember { mutableIntStateOf(0) }
    val titles = listOf("Tab 1", "Tab 2", "Tab 3")

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(15.dp),
    ) {
        item {

            AsyncImage(
                model = TmdbImageUrlBuilder.build(movie.backdropImage!!, width = 400, height = 200),
                contentDescription = "Translated description of what the image contains",
                modifier = Modifier.fillParentMaxWidth(),
                contentScale = ContentScale.Crop
            )

            Row(
                horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()

            ) {
                Column {
                    Text(movie.title!!)
                    Row {
                        Text(movie.releaseDate.toString())
                        Text(movie.runtime.toString())

                    }
                    Text(movie.credits?.crew?.find { it.job == "Director" }?.name!!)
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
        }
        item {
            Text(movie.tagline!!)
        }
        item {
            Text(movie.overview!!)
        }
        item {
            Text(movie.voteAverage.toString())
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
            1 -> item {
                Text(
                    text = "Content for ${titles[state]}", modifier = Modifier.padding(16.dp)
                )
            }

            0 -> items(10) {
                val actor = movie.credits?.cast[it]!!

                CastItem(actor)
            }
        }
        item {
            Text("Reviews")
        }

        items(5) {
            val review = movie.reviews?.results[it]!!
            ReviewItem(review)
        }

        item {
            HorizontalDivider()

            Text("Similar")
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp)


            ) {
//                item { Spacer(Modifier.width(5.dp)) }
                for (movie in data.similar) item {
                    PosterImage {
                        AsyncImage(
                            model = TmdbImageUrlBuilder.build(
                                movie.posterImage!!, width = 400, height = 200
                            ),
                            contentDescription = "Translated description of what the image contains",
                            modifier = Modifier.width(100.dp)
//                            contentScale = ContentScale.Crop
                        )
                    }
                }

            }
        }

    }
}

@Composable
private fun CastItem(actor: TmdbCast) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(5.dp)

    ) {
        AsyncImage(
            model = actor.profileImage?.let {
                TmdbImageUrlBuilder.build(
                    it, width = 300, height = 50
                )
            }, actor.name!!, Modifier.height(80.dp)
        )
        Column {

            Text(actor.name!!)
            Text(actor.character!!)
        }
    }
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
    ) {
        content()
    }
}

@Composable
private fun ReviewItem(review: TmdbReview) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        AsyncImage(
            model = review.authorDetails?.avatarPath?.let { imagePath ->
                TmdbImageUrlBuilder.build(
                    imagePath, "w154"
                )
            }, "", Modifier.width(80.dp)
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

