package it.polimi.rewatch.components

import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun MediaScaffold(
    title: String?,
    onNavigateBack: () -> Unit,
    scrollState: ScrollableState,
    modifier: Modifier = Modifier,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        topBar = {
            AppearingTopBar(scrollState, title, onNavigateBack)
        },
        modifier = modifier,
    ) { padding ->
        content.invoke(
            padding,
        )
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
