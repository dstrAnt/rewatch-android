package it.polimi.rewatch.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun CollapsingLayout(
    collapsingTopHeight: Dp,
    pinnedBottomHeight: Dp,
    modifier: Modifier = Modifier,
    listState: LazyGridState = rememberLazyGridState(),
    statusBarColor: Color = Color.Transparent,
    collapsingTopBar: @Composable () -> Unit,
    pinnedBottomBar: @Composable () -> Unit = {},
    // THE UPGRADE: A Composable lambda that passes values down to your screen!
    content: @Composable (listState: LazyGridState, spacerHeight: Dp) -> Unit,
) {
    WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val collapsingHeightPx = with(LocalDensity.current) { collapsingTopHeight.toPx() }
    // We calculate the required gap here so you don't have to do the math on your screen
    val spacerHeight = collapsingTopHeight + pinnedBottomHeight - 7.dp
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

    Box(modifier = modifier.fillMaxSize()) {
        // 1. YOUR CUSTOM SCROLLABLE CONTENT
        // We pass the state and the required gap height to your lambda
        content(listState, spacerHeight)
        // 2. THE TOP BAR CONTAINER
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(statusBarColor)
                    .windowInsetsTopHeight(WindowInsets.statusBars),
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clipToBounds(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset { IntOffset(x = 0, y = topBarOffsetHeightPx.roundToInt()) },
                ) {
                    Box(
                        modifier = Modifier
                            .height(collapsingTopHeight)
                            .background(statusBarColor)
                            .fillMaxWidth(),
                    ) {
                        collapsingTopBar()
                    }

                    Box(
                        modifier = Modifier
                            .height(pinnedBottomHeight)
                            .fillMaxWidth(),
                    ) {
                        pinnedBottomBar()
                    }
                }
            }
        }
    }
}