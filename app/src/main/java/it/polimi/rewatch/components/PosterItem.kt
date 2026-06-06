package it.polimi.rewatch.components

import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.moviebase.tmdb.image.TmdbImageType
import app.moviebase.tmdb.image.TmdbImageUrlBuilder
import coil3.compose.AsyncImagePainter
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.SubcomposeAsyncImageScope
import coil3.request.ImageRequest
import coil3.request.crossfade
import it.polimi.rewatch.common.NavigationActions
import it.polimi.rewatch.model.MediaId
import it.polimi.rewatch.model.PosterInfo

//@Composable
//fun PosterItem3(
//    id: Int,
//    posterPath: String?,
//    title: String?,
//    mediaType: MediaType, // Added parameter
//    modifier: Modifier = Modifier,
//) {
//    val shape = RoundedCornerShape(5.dp)
//    LocalContext.current // Brought back for onMovieClick
//    Box(
//        modifier = modifier
//            .aspectRatio(2f / 3f)
//            .clip(shape)
//            .border(1.dp, Color.Gray, shape)
//            .clickable {
//                // Click logic is now handled internally again
//                //onMediaClick(id.toString(), mediaType, context)
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
//    LocalContext.current // Brought back for onMovieClick
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
//                //onMediaClick(id.toString(), mediaType, context)
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

//@Composable
//private fun PosterItem(
//    media: PosterInfo,
//    onNavigateToMedia: () -> Unit,
//    modifier: Modifier = Modifier,
//    onLong: (() -> Unit)? = null,
//) = PosterItem(
//    posterPath = media.posterPath,
//    title = media.title,
//    modifier = modifier,
//    onNavigateToMedia,
//    onLong = onLong,
//)

/**
 * A standard UI component for displaying media posters in a grid or list.
 *
 * This component handles the transformation of [PosterInfo] into a visual poster tile,
 * automatically managing image loading, click/long-press interactions, and fallback
 * states if the image fails to load.
 *
 * ### Key Features:
 * * **Aspect Ratio:** Enforces a standard **2:3** poster aspect ratio.
 * * **Visuals:** Includes a [RoundedCornerShape] with a 1dp border and uses [SubcomposeAsyncImage]
 * for seamless loading/error transitions.
 * * **Conditional Interaction:** Only becomes clickable if the provided [media.id][MediaId]
 * is valid. Long-pressing triggers a bottom sheet action via [NavigationActions].
 * * **Fallback:** Displays the [media.title][PosterInfo.title] centered within the frame
 * if the poster path is null or the image fails to download.
 *
 * @param media The data model containing the poster path, title, and ID.
 * @param actions The navigation handler for click (details) and long-click (bottom sheet) events.
 * @param modifier The [Modifier] to be applied to the outer container.
 */
@Composable
fun PosterItem(
    media: PosterInfo,
    actions: NavigationActions,
    modifier: Modifier = Modifier,
) = PosterItem(
    posterPath = media.posterPath,
    title = media.title,
    modifier = modifier,
    onClick = if (media.id is MediaId) {
        { actions.onNavigateToMedia(media.id) }
    } else null,
) { actions.onBottomSheet(media) }

@Composable
private fun PosterItem(
    posterPath: String?,
    title: String?,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
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
            .let { baseModifier ->
                if (onClick != null) {
                    baseModifier.combinedClickable(
                        onClick = onClick, // Fallback required since onClick parameter cannot be null
                        onLongClick = onLong,
                    )
                } else {
                    baseModifier // If both are null, return the modifier without click behavior
                }
            }.testTag("posterItem_$title")
            .testTag("posterItem"),
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

