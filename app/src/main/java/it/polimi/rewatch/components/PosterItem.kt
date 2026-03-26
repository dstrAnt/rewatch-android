package it.polimi.rewatch.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.moviebase.tmdb.image.TmdbImageType
import app.moviebase.tmdb.image.TmdbImageUrlBuilder
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.SubcomposeAsyncImageScope
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.crossfade
import it.polimi.rewatch.model.MediaType
import it.polimi.rewatch.model.PosterMedia
import it.polimi.rewatch.model.TrackingMedia

@Composable
fun PosterItem3(
    id: Int,
    posterPath: String?,
    title: String?,
    mediaType: MediaType, // Added parameter
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(5.dp)
    LocalContext.current // Brought back for onMovieClick
    Box(
        modifier = modifier
            .aspectRatio(2f / 3f)
            .clip(shape)
            .border(1.dp, Color.Gray, shape)
            .clickable {
                // Click logic is now handled internally again
                //onMediaClick(id.toString(), mediaType, context)
            },
        contentAlignment = Alignment.Center,
    ) {
        if (posterPath != null) {
            RewatchAsyncImage(
                model = TmdbImageUrlBuilder.build(posterPath, TmdbImageType.PROFILE, width = 70, height = 70),
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

@Composable
fun PosterItem2(
    id: Int,
    posterPath: String?,
    title: String?,
    mediaType: MediaType, // Added parameter
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(5.dp)
    LocalContext.current // Brought back for onMovieClick
    val fallbackContent: @Composable SubcomposeAsyncImageScope.(AsyncImagePainter.State) -> Unit =
        {
            Box(contentAlignment = Alignment.Center) {
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
    val model = TmdbImageUrlBuilder.build(
        posterPath.orEmpty(), TmdbImageType.PROFILE, width = 70, height = 70,
    )
    Box(
        modifier = modifier
            .aspectRatio(2f / 3f)
            .clip(shape)
            .border(1.dp, Color.Gray, shape)
            .clickable {
                // Click logic is now handled internally again
                //onMediaClick(id.toString(), mediaType, context)
            },
        contentAlignment = Alignment.Center,
    ) {
        // 2. Pass it directly into the built-in slots
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(model)
                .crossfade(true)
                .build(),
            contentDescription = title,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
fun PosterItem(
    media: PosterMedia,
    onNavigateToMedia: () -> Unit,
    modifier: Modifier = Modifier,
    onLong: (() -> Unit)? = null,
) = PosterItem(
    posterPath = media.posterPath, title = media.title, modifier = modifier,
    onNavigateToMedia, onLong = onLong,
)

@Composable
fun PosterItem(
    media: TrackingMedia,
    onNavigateToMedia: () -> Unit,
    modifier: Modifier = Modifier,
) = PosterItem(
    posterPath = media.mediaData.posterPath, title = media.mediaData.title, modifier = modifier,
    onNavigateToMedia,
)

@Composable
fun PosterItem(
    posterPath: String?,
    title: String?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onLong: (() -> Unit)? = null,
) {
    val shape = RoundedCornerShape(5.dp)
    val fallbackContent: @Composable SubcomposeAsyncImageScope.(AsyncImagePainter.State) -> Unit = {
        Box(contentAlignment = Alignment.Center) {
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
    val model = TmdbImageUrlBuilder.build(posterPath.orEmpty(), TmdbImageType.PROFILE, width = 70, height = 70)
    Box(
        modifier = modifier
            .aspectRatio(2f / 3f)
            .clip(shape)
            .border(1.dp, Color.Gray, shape)
            .combinedClickable(onClick = onClick, onLongClick = onLong),
        contentAlignment = Alignment.Center,
    ) {
        // 2. Pass it directly into the built-in slots
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(model)
                .crossfade(true)
                .build(),
            contentDescription = title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            loading = fallbackContent,
            error = fallbackContent,
        )
    }
}

@Composable
fun PosterItemCustom(
    id: Int,
    posterPath: String?,
    title: String?,
    mediaType: MediaType, // Added parameter
    modifier: Modifier = Modifier,
) {
    // 1. Remember the painter to observe its state as standard Compose State
    val model = TmdbImageUrlBuilder.build(posterPath.orEmpty(), TmdbImageType.PROFILE, width = 70, height = 70)
    val painter = rememberAsyncImagePainter(model = model)
    val state by painter.state.collectAsState()
    val shape = RoundedCornerShape(5.dp)
    LocalContext.current // Brought back for onMovieClick
    // 2. Wrap in a standard Box (avoids SubcomposeLayout entirely)
    Box(
        modifier = modifier
            .aspectRatio(2f / 3f)
            .clip(shape)
            .border(1.dp, Color.Gray, shape)
            .clickable {
                // Click logic is now handled internally again
                //onMediaClick(id.toString(), mediaType, context)
            },
        contentAlignment = Alignment.Center,
    ) {
        Crossfade(
            targetState = state,
            animationSpec = tween(durationMillis = 500), // Adjust speed here (500ms is default)
            label = "image_crossfade", // Good practice to label animations for debugging
        ) { t ->
            // 3. The actual image. Apply the modifier directly here so
            // sizing behaves exactly like a standard Image component.
            // 4. Overlay your custom Composables based on the state read
            when (state) {
                is AsyncImagePainter.State.Success -> {
                    // Optional: What to show if the image completely fails
                    Text(text = "Failed to load")

                    Image(
                        painter = painter,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
//            contentScale = ContentScale.Crop,
//            alignment = alignment,
                    )
                }

                else -> {
                    Text(
                        text = title.orEmpty(),
                        modifier = Modifier.padding(5.dp),
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelMedium,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                // Success or Empty: Do nothing, the Image is already drawn underneath
            }
        }
    }
}
