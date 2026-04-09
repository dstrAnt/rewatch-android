package it.polimi.rewatch.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImagePainter
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.SubcomposeAsyncImageScope
import coil3.request.ImageRequest
import coil3.request.crossfade

/**
 * A wrapper around [SubcomposeAsyncImage] that provides a unified fallback system
 * for both loading and error states.
 *
 * This component simplifies image handling by allowing you to define a single visual
 * placeholder (either a resource-based [Image] or a vector-based [Icon]) that
 * remains visible from the moment the request starts until the image successfully
 * loads, or if the request fails.
 *
 * ### Features:
 * * **Consistency:** Uses the same fallbackContent for both `loading` and `error`
 * slots to prevent flickering during state transitions.
 * * **Flexible Fallbacks:** Prioritizes [fallbackResId] (full-sized image) over
 * [fallbackIcon] (centered tinted icon).
 * * **Coil Integration:** Built-in support for [ImageRequest] with `crossfade(true)`
 * enabled by default for smooth visual transitions.
 *
 * @param model The data source for the image (URL, URI, File, etc.).
 * @param contentDescription Text used by accessibility services to describe the image.
 * @param modifier [Modifier] to be applied to the image layout.
 * @param contentScale Strategy for scaling the image content (defaults to [ContentScale.Crop]).
 * @param alignment Alignment parameter for the image within its bounds.
 * @param fallbackResId Optional drawable resource ID to show while loading or on error.
 * @param fallbackIcon Optional [ImageVector] to show if no [fallbackResId] is provided.
 * @param fallbackIconTint The color tint applied to the [fallbackIcon].
 *
 * @see SubcomposeAsyncImage
 * @see ImageRequest.Builder.crossfade
 */
@Composable
fun RewatchAsyncImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    alignment: Alignment = Alignment.Center,
    fallbackResId: Int? = null,
    fallbackIcon: ImageVector? = null,
    fallbackIconTint: Color = Color.LightGray,
) {
    // 1. Define the fallback UI once to reuse for both loading and error states
    val fallbackContent: @Composable SubcomposeAsyncImageScope.(AsyncImagePainter.State) -> Unit = {
        if (fallbackResId != null) {
            Image(
                painter = painterResource(id = fallbackResId),
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = contentScale,
                alignment = alignment,
            )
        } else if (fallbackIcon != null) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(
                    imageVector = fallbackIcon,
                    contentDescription = null,
                    tint = fallbackIconTint,
                    modifier = Modifier.padding(10.dp),
                )
            }
        }
    }
    // 2. Pass it directly into the built-in slots
    SubcomposeAsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(model)
            .crossfade(true)
            .build(),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale,
        alignment = alignment,
        loading = fallbackContent,
        error = fallbackContent,
    )
}

