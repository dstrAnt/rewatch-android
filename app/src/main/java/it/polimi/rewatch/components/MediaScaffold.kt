package it.polimi.rewatch.components

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.abs

@Composable
fun MediaScaffold(
    title: String?,
    onNavigateBack: () -> Unit,
    scrollState: ScrollableState,
    modifier: Modifier = Modifier,
    content: @Composable (PaddingValues, (String?) -> Modifier) -> Unit,
) {
    var fullScreenImageUrl by remember { mutableStateOf<String?>(null) }
    var isOverlayVisible by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            AppearingTopBar(scrollState, title, onNavigateBack)
        },
        modifier = modifier,
    ) { padding ->
        content.invoke(
            padding,
        ) {
            Modifier.clickable {
                fullScreenImageUrl = it
                isOverlayVisible = true
            }
        }
    }
    val animationSpec = tween<Float>(durationMillis = 300)
    AnimatedVisibility(
        visible = isOverlayVisible,
        enter = fadeIn(animationSpec = animationSpec),
        exit = fadeOut(animationSpec = animationSpec),
    ) {
        // Use lastSeenImageUrl instead of fullScreenImageUrl here!
        FullScreenImageOverlay(
            imageUrl = fullScreenImageUrl.orEmpty(),
            onDismiss = { isOverlayVisible = false },
        )
    }
}

@Composable
fun FullScreenImageOverlay(imageUrl: String, onDismiss: () -> Unit) {
    var offsetY by remember { mutableFloatStateOf(0f) }
    var backProgress by remember { mutableFloatStateOf(0f) }
    val animatedOffsetY by animateFloatAsState(targetValue = offsetY, label = "offsetY")
    val scale = 1f - (backProgress * 0.15f)
    val animatedScale by animateFloatAsState(targetValue = scale, label = "scale")

    PredictiveBackHandler { progressFlow ->
        try {
            progressFlow.collect { backEvent ->
                backProgress = backEvent.progress
            }
            onDismiss()
        } catch (e: CancellationException) {
            backProgress = 0f
        }
    }

    Box(
        // 1. Center the image inside the Box now that it doesn't fillMaxSize
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss, // This handles clicks on the gray background
            ),
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = "Full Screen",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                // 2. Removed .fillMaxSize() so the gesture bounds strictly match the scaled image
                .fillMaxWidth()
                .graphicsLayer {
                    translationY = animatedOffsetY
                    scaleX = animatedScale
                    scaleY = animatedScale
                }
                .pointerInput(Unit) {
                    // 3. Swallow taps on the image so they don't trigger the Box's dismiss click
                    detectTapGestures(onTap = { /* Do nothing */ })
                }
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onDragEnd = {
                            if (abs(offsetY) > 300f) onDismiss() else offsetY = 0f
                        },
                        onDragCancel = { offsetY = 0f },
                        onVerticalDrag = { change, dragAmount ->
                            change.consume()
                            offsetY += dragAmount
                        },
                    )
                },
        )

        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(16.dp),
        ) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearingTopBar(
    scrollState: ScrollableState,
    title: String?,
    onNavigateBack: () -> Unit,
) {
    val toolbarAlpha by remember {
        derivedStateOf {
            // 1. Extract both index and offset in a single pass
            val (index, offset) = when (scrollState) {
                is LazyListState -> scrollState.firstVisibleItemIndex to scrollState.firstVisibleItemScrollOffset
                is LazyGridState -> scrollState.firstVisibleItemIndex to scrollState.firstVisibleItemScrollOffset
                else -> return@derivedStateOf 1f // Fallback: fully visible if state is unsupported
            }
            // 2. Early exit
            if (index > 0) return@derivedStateOf 1f
            // 3. Simple linear interpolation (lerp) logic
            ((offset - 450f) / 250f).coerceIn(0f, 1f)
        }
    }
    TopAppBar(
        title = {
            if (title != null) {
                Text(
                    title,
                    modifier = Modifier.alpha(toolbarAlpha),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = Color.White,
                )
            }
        },
        // Apply background color with alpha
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Black.copy(alpha = toolbarAlpha),
            scrolledContainerColor = Color.Black.copy(alpha = toolbarAlpha),
        ),
        // Crucial: Add status bar insets so the buttons don't hit the clock/battery
//        windowInsets = WindowInsets.statusBars
    )
}
