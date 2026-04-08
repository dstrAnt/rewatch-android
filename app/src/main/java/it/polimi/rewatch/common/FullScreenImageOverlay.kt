package it.polimi.rewatch.common

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImagePainter
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.SubcomposeAsyncImageContent
import it.polimi.rewatch.screens.media.MediaPage
import it.polimi.rewatch.screens.person.PersonPage
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.abs

/**
 * [FullScreenImageOverlay] implements the functionality of displaying an image on the
 * whole screen after clicking on it in [MediaPage] or [PersonPage]
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FullScreenImageOverlay(
    imageUrl: String,
    onDismiss: () -> Unit,
) {
    var offsetY by remember { mutableFloatStateOf(0f) }
    val backProgress = remember { mutableFloatStateOf(0f) }
    val animatedOffsetY by animateFloatAsState(targetValue = offsetY, label = "offsetY")
    val animatedScale by animateFloatAsState(targetValue = 1f - (backProgress.floatValue * 0.15f), label = "scale")

    PredictiveBackHandler { progressFlow ->
        try {
            progressFlow.collect { backProgress.floatValue = it.progress }
            onDismiss()
        } catch (_: CancellationException) {
            backProgress.floatValue = 0f
        }
    }
    // Root Box: Handles the background, click-to-dismiss, and stacking the close button
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss,
            ),
    ) {
        SubcomposeAsyncImage(
            model = imageUrl,
            contentDescription = "Full Screen",
            contentScale = ContentScale.Fit,
        ) {
            val scope = this
            val state by painter.state.collectAsState()

            Crossfade(
                targetState = state,
                label = "imageFade",
                animationSpec = tween(durationMillis = 600),
            ) { currentState ->
                if (currentState is AsyncImagePainter.State.Success) {
                    // A single Box replaces the nested boxes by using wrapContentSize to handle centering
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        scope.SubcomposeAsyncImageContent(
                            modifier = Modifier
                                .fillMaxByOrientation()
                                .swipeToDismiss(
                                    offsetY = offsetY,
                                    animatedOffsetY = animatedOffsetY,
                                    animatedScale = animatedScale,
                                    onOffsetChange = { offsetY = it },
                                    onDismiss = onDismiss,
                                ),
                        )
                    }
                } else {
                    LoadingOverlay()
                }
            }
        }

        OverlayCloseButton(onDismiss = onDismiss)
    }
}

// --- Extracted Components & Modifiers Below ---
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun LoadingOverlay() {
    // Dropped the Box here entirely; the modifier handles centering the indicator
    CircularWavyProgressIndicator(
        color = Color.White,
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.Center),
    )
}

@Composable
private fun BoxScope.OverlayCloseButton(onDismiss: () -> Unit) {
    IconButton(
        onClick = onDismiss,
        modifier = Modifier
            .align(Alignment.TopEnd)
            .statusBarsPadding()
            .padding(16.dp),
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Close",
            tint = Color.White,
        )
    }
}


private fun Modifier.swipeToDismiss(
    offsetY: Float,
    animatedScale: Float,
    animatedOffsetY: Float,
    dismissThreshold: Float = 300f,
    onOffsetChange: (Float) -> Unit,
    onDismiss: () -> Unit,
): Modifier = composed {
    // 1. Wrap the changing variables in rememberUpdatedState.
    // This creates a state object that always holds the latest value passed into the modifier.
    val currentOffsetY by rememberUpdatedState(offsetY)
    val currentOnOffsetChange by rememberUpdatedState(onOffsetChange)
    val currentOnDismiss by rememberUpdatedState(onDismiss)

    this
        .graphicsLayer {
            translationY = animatedOffsetY
            scaleX = animatedScale
            scaleY = animatedScale
        }
        .clip(RoundedCornerShape(5.dp))
        .pointerInput(Unit) {
            detectTapGestures(onTap = { /* Do nothing */ })
        }
        .pointerInput(Unit) { // The block still runs only once...
            detectVerticalDragGestures(
                onDragEnd = {
                    // 2. ...but now it reads the freshest state dynamically!
                    if (abs(currentOffsetY) > dismissThreshold) {
                        currentOnDismiss()
                    } else {
                        currentOnOffsetChange(0f)
                    }
                },
                onDragCancel = { currentOnOffsetChange(0f) },
                onVerticalDrag = { change, dragAmount ->
                    change.consume()
                    currentOnOffsetChange(currentOffsetY + dragAmount)
                },
            )
        }
}