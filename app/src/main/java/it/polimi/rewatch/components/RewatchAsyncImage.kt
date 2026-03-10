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

