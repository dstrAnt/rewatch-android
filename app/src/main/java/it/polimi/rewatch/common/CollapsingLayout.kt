package it.polimi.rewatch.common

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * [CollapsingLayout] allows to create a top bar that hides as the user scroll down
 */
@Composable
fun CollapsingLayout(
    collapsingTopHeight: Dp,
    pinnedBottomHeight: Dp,
    modifier: Modifier = Modifier,
    listState: LazyGridState = rememberLazyGridState(),
    // 1. THE UPGRADE: Changed from 'Color' to 'Brush' to support gradients
    statusBarBrush: Brush = Brush.verticalGradient(
        colors = listOf(Color.Black, Color.Transparent),
    ),
    collapsingTopBar: @Composable () -> Unit,
    pinnedBottomBar: @Composable () -> Unit = {},
    content: @Composable (listState: LazyGridState, spacerHeight: Dp) -> Unit,
) {
    val collapsingHeightPx = with(LocalDensity.current) { collapsingTopHeight.toPx() }
    val spacerHeight = collapsingTopHeight + pinnedBottomHeight - 7.dp
    // Back to your perfectly synchronized original logic!
    val topBarOffsetHeightPx by remember {
        derivedStateOf {
            if (listState.firstVisibleItemIndex == 0) {
                -(listState.firstVisibleItemScrollOffset.toFloat()
                    .coerceAtMost(collapsingHeightPx))
            } else {
                -collapsingHeightPx
            }
        }
    }
    // THE FIXED SNAPPING LOGIC
    LaunchedEffect(listState) {
        // snapshotFlow safely observes the state without cancelling the coroutine
        snapshotFlow { listState.isScrollInProgress }.collect { isScrolling ->
            // Trigger only when the user lifts their finger and the first item is visible
            if (!isScrolling && listState.firstVisibleItemIndex == 0) {
                val currentScroll = listState.firstVisibleItemScrollOffset
                val maxOffset = collapsingHeightPx.roundToInt()
                // If the bar is stuck in a partial state
                if (currentScroll > 0 && currentScroll < maxOffset) {
                    if (currentScroll > maxOffset / 2) {
                        // Snap fully collapsed (scroll forward)
                        listState.animateScrollBy((maxOffset - currentScroll).toFloat())
                    } else {
                        // Snap fully expanded (scroll backward to 0)
                        listState.animateScrollBy(-currentScroll.toFloat())
                    }
                }
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // LAYER 1: YOUR CUSTOM SCROLLABLE CONTENT (Bottom Layer)
        content(listState, spacerHeight)
        // LAYER 2: THE TOP BAR CONTAINER (Middle Layer)
        // This entire block offsets up and down when scrolling
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(x = 0, y = topBarOffsetHeightPx.roundToInt()) }
                .background(headerColor)
                .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()),
        ) {
            // THE "REAL" SPACER:
            // Transparent layout spacer. It pushes the top bar down so it
            // doesn't overlap the status bar when fully expanded.
            // The actual top bar UI
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(collapsingTopHeight)
                    .graphicsLayer {
                        // SETTING: 0.7f means the fade finishes when the bar is 70% compressed.
                        // Lower the number (e.g., 0.5f) to make it fade out even faster.
                        val fadeThreshold = 0.8f
                        // 1. Calculate standard progress (0.0 to 1.0)
                        val rawCollapseFraction = if (collapsingHeightPx > 0f) {
                            (-topBarOffsetHeightPx) / collapsingHeightPx
                        } else {
                            0f
                        }
                        // 2. Accelerate the fraction so it hits 1.0 at our threshold
                        val acceleratedFraction = rawCollapseFraction / fadeThreshold
                        // 3. Apply to alpha
                        alpha = (1f - acceleratedFraction).coerceIn(0f, 1f)
                    },
            ) {
                collapsingTopBar()
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(pinnedBottomHeight),
            ) {
                pinnedBottomBar()
            }
        }
//        // LAYER 3: THE GRADIENT OVERLAY (Top Layer)
//        // Because it's the last item in the Box, it naturally draws OVER the content
//        // and the scrolling top bar. No zIndex hack required.
//        Spacer(
//            modifier = Modifier
//                .align(Alignment.TopCenter)
//                .fillMaxWidth()
//                .windowInsetsTopHeight(WindowInsets.statusBars.add(WindowInsets(top = 5.dp)))
//                .background(
//                    brush = Brush.verticalGradient(
//                        0.8f to headerColor,
//                        1f to headerColor.copy(alpha = 0f),
//                    ),
//                ),
//        )
    }
}