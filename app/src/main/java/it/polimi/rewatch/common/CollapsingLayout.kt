package it.polimi.rewatch.common

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

// Make sure you have your other standard imports
@Composable
fun CollapsingLayout(
    collapsingTopHeight: Dp,
    pinnedBottomHeight: Dp,
    modifier: Modifier = Modifier,
    listState: LazyGridState = rememberLazyGridState(),
    statusBarColor: Color = Color.Transparent,
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
        // 1. YOUR CUSTOM SCROLLABLE CONTENT
        content(listState, spacerHeight)
        // 2. THE TOP BAR CONTAINER
        Column(modifier = Modifier.fillMaxWidth()) {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(statusBarColor)
                    .windowInsetsTopHeight(WindowInsets.statusBars),
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clipToBounds()
                    .offset { IntOffset(x = 0, y = topBarOffsetHeightPx.roundToInt()) },
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(collapsingTopHeight)
                        .background(statusBarColor),
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
        }
    }
}