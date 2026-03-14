package it.polimi.rewatch.common

import android.content.Context
import android.content.Intent
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.moviebase.tmdb.image.TmdbImageType
import app.moviebase.tmdb.image.TmdbImageUrlBuilder
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import it.polimi.rewatch.screens.media.Media
import it.polimi.rewatch.screens.media.MediaActivity
import it.polimi.rewatch.screens.media.MediaType
import it.polimi.rewatch.screens.person.PersonActivity
import java.text.SimpleDateFormat
import java.util.Locale

fun String.formatDate(format: String): String {
    return try {
        val inputFormat =
            SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val date = inputFormat.parse(this)
        val outputFormat =
            SimpleDateFormat(format, Locale.US)

        if (date != null) outputFormat.format(date) else this
    } catch (_: Exception) {
        this
    }
}

fun Modifier.relativeMove(y: Dp) =
    this.layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        layout(
            placeable.width,
            placeable.height + y.roundToPx(),
        ) {
            // Placing it at the negative Y offset
            placeable.placeRelative(0, y.roundToPx())
        }
    }

@Composable
fun ExpandBtn(
    string: String,
    isExpanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(15.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(string, style = MaterialTheme.typography.labelLarge, modifier = modifier)
            if (isExpanded) Icon(Icons.Default.KeyboardArrowUp, contentDescription = null)
            else Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
        }
    }
}

@Composable
fun BorderBox(
    onClick: () -> Unit = {},
    content: @Composable (() -> Unit),
) {
    val shape = RoundedCornerShape(5.dp)

    Box(
        modifier = Modifier
            .clip(shape) // Set the corner radius
            .border(
                BorderStroke(
                    1.dp,
                    Color.Gray,
                ),
                shape,
            ) // Add border with rounded corners
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onClick,
            ),
    ) {
        content()
    }
}

fun onMovieClick(
    media: Media,
    context: Context,
) {
    onMovieClick(media.id.toString(), media.type, context)
}

fun movieFromIntent(intent: Intent): Pair<String, MediaType> {
    val mediaId = intent.getStringExtra("EXTRA_MEDIA_ID") ?: "-1"

    val typeString = intent.getStringExtra("EXTRA_MEDIA_TYPE")
    val mediaType = try {
        typeString?.let { enumValueOf<MediaType>(it) } ?: MediaType.MOVIE
    } catch (_: IllegalArgumentException) {
        MediaType.MOVIE
    }

    return Pair(mediaId, mediaType)
}

fun onMovieClick(
    mediaId: String,
    mediaType: MediaType,
    context: Context,
) {
    // Create an Intent to start MovieActivity
    val intent = Intent(context, MediaActivity::class.java).apply {
        // Bundle the movieId into the Intent extras
        putExtra("EXTRA_MEDIA_ID", mediaId)
        putExtra("EXTRA_MEDIA_TYPE", mediaType.name) // Passes "MOVIE"
    }
    // Launch the activity! (This triggers the native OS transition)
    context.startActivity(intent)
}

fun onPersonClick(
    personId: Int,
    context: Context,
) {
    val intent = Intent(
        context, PersonActivity::class.java,
    ).apply {
        putExtra(
            "EXTRA_PERSON_ID", personId,
        )
    }

    context.startActivity(intent)
}

fun Double.getShortNotation(): String {
    return when {
        this >= 1_000_000_000 -> String.format(Locale.US, "%.1f billion", this / 1_000_000_000)
        this >= 1_000_000 -> String.format(Locale.US, "%.1f million", this / 1_000_000)
        this >= 1_000 -> String.format(Locale.US, "%.1fk", this / 1_000)
        this > 0 -> this.toInt().toString()
        else -> " - " // TMDB spesso restituisce 0 se il budget è sconosciuto
    }.replace(".0", "") // Toglie il decimale se è zero (es: 50.0 -> 50)
}

@Composable
fun SkeletonPart(
    modifier: Modifier = Modifier,
    width: Dp? = null,
    height: Dp? = null,
    shape: Shape = RoundedCornerShape(4.dp),
) {
    Box(
        modifier = modifier
            .then(if (width != null) Modifier.width(width) else Modifier)
            .then(if (height != null) Modifier.height(height) else Modifier)
            .clip(shape)
            .pulseEffect(), // Updated here
    )
}

fun Modifier.pulseEffect(): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "pulse")
    // Animate alpha from 0.4 to 0.8 and back
    val alpha by transition.animateFloat(
        initialValue = 0.2f, targetValue = 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse, // This creates the "breathing" pulse
        ),
        label = "pulseAlpha",
    )
    // Use a solid color with the animated alpha
    background(Color.LightGray.copy(alpha = alpha))
}

data class TabItem(
    val title: String,
    val isAvailable: () -> Boolean,
    // We now pass the volatile state directly into the lambda when it executes
    val content: LazyListScope.() -> Unit,
)

//@Composable
//fun PosterItem3(
//    id: Int,
//    posterPath: String?,
//    title: String?,
//    mediaType: MediaType, // Added parameter
//    modifier: Modifier = Modifier,
//) {
//    val shape = RoundedCornerShape(5.dp)
//    val context = LocalContext.current // Brought back for onMovieClick
//
//    Box(
//        modifier = modifier
//            .aspectRatio(2f / 3f)
//            .clip(shape)
//            .border(1.dp, Color.Gray, shape)
//            .clickable {
//                // Click logic is now handled internally again
//                onMovieClick(id, mediaType, context)
//            },
//        contentAlignment = Alignment.Center,
//    ) {
//        if (posterPath != null) {
//            RewatchAsyncImage(
//                model = TmdbImageUrlBuilder.build(posterPath, TmdbImageType.PROFILE, width = 70, height = 70),
//                contentDescription = title,
//                modifier = Modifier.fillMaxSize(),
//                contentScale = ContentScale.Crop,
//            )
//        } else {
//            Text(
//                text = title.orEmpty(),
//                modifier = Modifier.padding(5.dp),
//                color = Color.Gray,
//                textAlign = TextAlign.Center,
//                style = MaterialTheme.typography.labelMedium,
//                overflow = TextOverflow.Ellipsis,
//            )
//        }
//    }
//}

//@Composable
//fun PosterItem2(
//    id: Int,
//    posterPath: String?,
//    title: String?,
//    mediaType: MediaType, // Added parameter
//    modifier: Modifier = Modifier,
//) {
//    val shape = RoundedCornerShape(5.dp)
//    val context = LocalContext.current // Brought back for onMovieClick
//    val fallbackContent: @Composable SubcomposeAsyncImageScope.(AsyncImagePainter.State) -> Unit =
//        {
//            Box(contentAlignment = Alignment.Center) {
//                Text(
//                    text = title.orEmpty(),
//                    modifier = Modifier.padding(5.dp),
//                    color = Color.Gray,
//                    textAlign = TextAlign.Center,
//                    style = MaterialTheme.typography.labelMedium,
//                    overflow = TextOverflow.Ellipsis,
//                )
//            }
//        }
//    val model = TmdbImageUrlBuilder.build(
//        posterPath.orEmpty(), TmdbImageType.PROFILE, width = 70, height = 70,
//    )
//    Box(
//        modifier = modifier
//            .aspectRatio(2f / 3f)
//            .clip(shape)
//            .border(1.dp, Color.Gray, shape)
//            .clickable {
//                // Click logic is now handled internally again
//                onMovieClick(id, mediaType, context)
//            },
//        contentAlignment = Alignment.Center,
//    ) {
//        // 2. Pass it directly into the built-in slots
//        AsyncImage(
//            model = ImageRequest.Builder(LocalContext.current)
//                .data(model)
//                .crossfade(true)
//                .build(),
//            contentDescription = title,
//            modifier = Modifier.fillMaxSize(),
//        )
//    }
//}

@Composable
fun PosterItem(
    id: String,
    posterPath: String?,
    title: String?,
    mediaType: MediaType, // Added parameter
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(5.dp)
    val context = LocalContext.current // Brought back for onMovieClick
    val model = TmdbImageUrlBuilder.build(posterPath.orEmpty(), TmdbImageType.PROFILE, width = 70, height = 70)


    Box(
        modifier = modifier
            .aspectRatio(2f / 3f)
            .clip(shape)
            .border(1.dp, Color.Gray, shape)
            .clickable {
                // Click logic is now handled internally again
                onMovieClick(id, mediaType, context)
            },
        contentAlignment = Alignment.Center,
    ) {
        if (posterPath != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(model)
                    .crossfade(true)
                    .build(),
                contentDescription = title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Text(
                text = title.orEmpty(),
                modifier = Modifier.padding(5.dp),
                color = Color.Gray,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelMedium,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

fun getEpisodeId(
    show: Int,
    season: Int,
    episode: Int,
): String {
    return show.toString() + "_" + season.toString() + "_" + episode.toString()
}