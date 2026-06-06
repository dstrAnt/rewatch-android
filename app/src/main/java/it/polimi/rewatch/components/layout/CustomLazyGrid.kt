package it.polimi.rewatch.components.layout

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.lazy.grid.GridCells.Fixed
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import kotlin.math.max

/**
 * Component to display a customizable [LazyVerticalGrid]
 */
@Composable
fun CustomLazyGrid(
    modifier: Modifier = Modifier,
    state: LazyGridState = rememberLazyGridState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    userScrollEnabled: Boolean = true,
    content: LazyGridScope.(Int) -> Unit,
) = BoxWithConstraints {
    // 1. Define your adaptive minimum size
    val minCellSize = 90.dp
    // 2. Calculate the columns (Available Width / Min Cell Size)
    // We use max(1, ...) to ensure it never crashes with 0 columns on tiny screens
    val calculatedColumnCount = max(1, (maxWidth / minCellSize).toInt())
    // Now you have the exact integer to use however you want!
    LazyVerticalGrid(
        columns = Fixed(calculatedColumnCount),
        state = state,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp),
        contentPadding = PaddingValues(
            top = 10.dp, bottom = 5.dp, start = 5.dp, end = 5.dp,
        ).plus(contentPadding),
        userScrollEnabled = userScrollEnabled,
        modifier = modifier
            .fillMaxSize()
            .animateContentSize()
            .padding(horizontal = 7.dp)
            .testTag("lazyGrid"),
        content = { content.invoke(this, calculatedColumnCount) },
    )
}