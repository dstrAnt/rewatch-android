package it.polimi.rewatch.components.layout

import androidx.compose.foundation.background
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import it.polimi.rewatch.common.headerColor
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

/**
 * [CollapsingLayout] allows to create a top bar that hides as the user scrolls down
 */
@Composable
fun CollapsingLayout(
    collapsingTopHeight: Dp,
    pinnedBottomHeight: Dp,
    modifier: Modifier = Modifier,
    listState: LazyGridState = rememberLazyGridState(),
    collapsingTopBar: @Composable () -> Unit,
    pinnedBottomBar: @Composable () -> Unit = {},
    content: @Composable (listState: LazyGridState, spacerHeight: Dp) -> Unit,
) {
    val collapsingHeightPx = with(LocalDensity.current) { collapsingTopHeight.toPx() }
    val spacerHeight = collapsingTopHeight + pinnedBottomHeight - 7.dp
    // A scope to safely launch interruptible animations
    val coroutineScope = rememberCoroutineScope()

    val topBarOffsetHeightPx by remember {
        derivedStateOf {
            if (listState.firstVisibleItemIndex == 0) {
                -(
                    listState.firstVisibleItemScrollOffset.toFloat()
                        .coerceAtMost(collapsingHeightPx)
                    )
            } else {
                -collapsingHeightPx
            }
        }
    }

    // Snap to a fully collapsed or expanded state
    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }.collect { isScrolling ->
            if (!isScrolling && listState.firstVisibleItemIndex == 0) {
                val currentScroll = listState.firstVisibleItemScrollOffset
                val maxOffset = collapsingHeightPx.roundToInt()

                // If the bar is stuck in a partial state
                if (currentScroll > 0 && currentScroll < maxOffset) {
                    // Isolate the animation in its own job so user interruptions
                    // (CancellationExceptions) don't kill the LaunchedEffect listener
                    coroutineScope.launch {
                        if (currentScroll > maxOffset / 2) {
                            // Snap fully collapsed
                            listState.animateScrollToItem(0, maxOffset)
                        } else {
                            // Snap fully expanded
                            listState.animateScrollToItem(0, 0)
                        }
                    }
                }
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Bottom layer: scrollable content
        content(listState, spacerHeight)
        // Middle layer: top bar container
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(x = 0, y = topBarOffsetHeightPx.roundToInt()) }
                .background(headerColor)
                .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()),
        ) {
            // The actual top bar UI
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(collapsingTopHeight)
                    .testTag("collapsingTopBar")
                    .graphicsLayer {
                        val fadeThreshold = 0.9f
                        val rawCollapseFraction = if (collapsingHeightPx > 0f) {
                            (-topBarOffsetHeightPx) / collapsingHeightPx
                        } else {
                            0f
                        }
                        val acceleratedFraction = rawCollapseFraction / fadeThreshold
                        alpha = (1f - acceleratedFraction).coerceIn(0f, 1f)
                    },
            ) {
                collapsingTopBar()
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(pinnedBottomHeight)
                    .testTag("pinnedBottomBar"),
            ) {
                pinnedBottomBar()
            }
        }
    }
}
