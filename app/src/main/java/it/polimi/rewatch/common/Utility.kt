package it.polimi.rewatch.common

import android.content.Intent
import android.os.Build
import android.view.RoundedCorner
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.grid.GridCells.Fixed
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.scene.Scene
import androidx.navigation3.ui.NavDisplay
import it.polimi.rewatch.model.MediaType
import it.polimi.rewatch.screens.media.expandableSection
import java.text.SimpleDateFormat
import java.util.Locale

fun String.formatDate(format: String): String {
    return try {
        val inputFormat =
            SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val date = inputFormat.parse(this)
        val outputFormat =
            SimpleDateFormat(format, Locale.US)

        if (date != null) outputFormat.format(date) else this
    } catch (_: Exception) {
        this
    }
}

fun Modifier.relativeMove(y: Dp) =
    this.layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        layout(
            placeable.width,
            placeable.height + y.roundToPx(),
        ) {
            // Placing it at the negative Y offset
            placeable.placeRelative(0, y.roundToPx())
        }
    }

@Composable
fun ExpandBtn(
    string: String,
    isExpanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(15.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(string, style = MaterialTheme.typography.labelLarge, modifier = modifier)
            if (isExpanded) Icon(Icons.Default.KeyboardArrowUp, contentDescription = null)
            else Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
        }
    }
}

@Composable
fun BorderBox(
    onClick: () -> Unit = {},
    content: @Composable (() -> Unit),
) {
    val shape = RoundedCornerShape(5.dp)

    Box(
        modifier = Modifier
            .clip(shape) // Set the corner radius
            .border(
                BorderStroke(
                    1.dp,
                    Color.Gray,
                ),
                shape,
            ) // Add border with rounded corners
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onClick,
            ),
    ) {
        content()
    }
}


fun movieFromIntent(intent: Intent): Pair<String, MediaType> {
    val mediaId = intent.getStringExtra("EXTRA_MEDIA_ID") ?: "-1"
    val typeString = intent.getStringExtra("EXTRA_MEDIA_TYPE")
    val mediaType = try {
        typeString?.let { enumValueOf<MediaType>(it) } ?: MediaType.MOVIE
    } catch (_: IllegalArgumentException) {
        MediaType.MOVIE
    }

    return Pair(mediaId, mediaType)
}
fun onMediaClickNav(
    mediaId: String,
    navController: NavHostController,
) = navController.navigate("movie_page/$mediaId")

fun Double.getShortNotation(): String {
    return when {
        this >= 1_000_000_000 -> String.format(Locale.US, "%.1f billion", this / 1_000_000_000)
        this >= 1_000_000 -> String.format(Locale.US, "%.1f million", this / 1_000_000)
        this >= 1_000 -> String.format(Locale.US, "%.1fk", this / 1_000)
        this > 0 -> this.toInt().toString()
        else -> " - " // TMDB spesso restituisce 0 se il budget è sconosciuto
    }.replace(".0", "") // Toglie il decimale se è zero (es: 50.0 -> 50)
}

@Composable
fun SkeletonPart(
    modifier: Modifier = Modifier,
    width: Dp? = null,
    height: Dp? = null,
    shape: Shape = RoundedCornerShape(4.dp),
) {
    Box(
        modifier = modifier
            .then(if (width != null) Modifier.width(width) else Modifier)
            .then(if (height != null) Modifier.height(height) else Modifier)
            .clip(shape)
            .pulseEffect(), // Updated here
    )
}

fun Modifier.pulseEffect(): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "pulse")
    // Animate alpha from 0.4 to 0.8 and back
    val alpha by transition.animateFloat(
        initialValue = 0.2f, targetValue = 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse, // This creates the "breathing" pulse
        ),
        label = "pulseAlpha",
    )
    // Use a solid color with the animated alpha
    background(Color.LightGray.copy(alpha = alpha))
}

fun Modifier.layoutEvade(
    padding: Dp = 7.dp,
) = this.layout { measurable, constraints ->
    val paddingPx = padding.roundToPx()
    // Tell the header it is allowed to be wider than the grid allows
    val expandedConstraints = constraints.copy(
        maxWidth = constraints.maxWidth + (paddingPx * 2),
    )
    val placeable = measurable.measure(expandedConstraints)
    // Shift the header to the left so it perfectly aligns with screen edges
    layout(placeable.width, placeable.height) {
        placeable.place(0, 0)
    }
}

data class TabItem(
    val title: String,
    val isAvailable: () -> Boolean,
    // We now pass the volatile state directly into the lambda when it executes
    val content: LazyListScope.() -> Unit,
) {
    companion object {
        fun <T> expandable(
            title: String,
            itemsList: List<T>,
            expandedSectionIds: Set<String>,
            onToggleSection: (String) -> Unit,
            initialVisibleCount: Int = 3,
            showDividers: Boolean = false,
            itemContent: @Composable LazyItemScope.(T) -> Unit,
        ) = TabItem(
            title = title,
            isAvailable = itemsList::isNotEmpty,
            content = {
                expandableSection(
                    title = title,
                    itemsList = itemsList,
                    expandedSectionIds = expandedSectionIds,
                    onToggleSection = onToggleSection,
                    initialVisibleCount = initialVisibleCount,
                    showDividers = showDividers,
                    itemContent = itemContent,
                )
            },
        )
    }
}

@Composable
fun ExpandableText(
    text: String,
    modifier: Modifier = Modifier,
    maxLines: Int = 3,
) {
    var isExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .animateContentSize() // 1. Internal: Handle animation first
            .clickable { isExpanded = !isExpanded } // 2. Internal: Handle click logic
            .then(modifier), // 3. Argument: Apply user modifiers (padding, background, etc.) last    )
    ) {
        Text(
            text = text,
            // When expanded, we set maxLines to Int.MAX_VALUE
            maxLines = if (isExpanded) Int.MAX_VALUE else maxLines,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun <T> CustomTabRow(
    availableTabs: List<T>,
    onClick: (T) -> Unit,
    title: (T) -> String,
    tabState: T?,
) = PrimaryScrollableTabRow(
    edgePadding = 8.dp,
    selectedTabIndex = availableTabs.indexOf(
        tabState,
    ),
) {
    availableTabs.forEach {
        Tab(
            modifier = Modifier.height(50.dp),
            selected = it == tabState,
            onClick = { onClick(it) },
            text = {
                Text(
                    text = title(it),
                    style = MaterialTheme.typography.labelLarge, // Explicitly setting it here
//                                    style = MaterialTheme.typography.labelMedium,  // Use the Large Title style here
                )
            },
        )
    }
}

@Composable
fun CustomLazyGrid(
    state: LazyGridState = rememberLazyGridState(),
    contentPadding: PaddingValues = PaddingValues(top = 10.dp, bottom = 5.dp, start = 5.dp, end = 5.dp),
    content: LazyGridScope.() -> Unit,
) = LazyVerticalGrid(
    columns = Fixed(4),
    state = state,
    horizontalArrangement = Arrangement.spacedBy(7.dp),
    verticalArrangement = Arrangement.spacedBy(7.dp),
    contentPadding = contentPadding,
    modifier = Modifier
        .fillMaxSize()
        .animateContentSize()
        .padding(horizontal = 7.dp),
    content = content,
)

@Composable
fun getDeviceCornerRadius(): Dp {
    val view = LocalView.current
    val density = LocalDensity.current
    // Only attempt to read hardware corners on Android 12+
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val windowInsets = view.rootWindowInsets
        val corner = windowInsets?.getRoundedCorner(RoundedCorner.POSITION_TOP_LEFT)
        val radiusPx = corner?.radius ?: 0

        if (radiusPx > 0) {
            // Convert the raw pixels to Compose Dp
            return with(density) { radiusPx.toDp() }
        }
    }
    // Fallback for Android 11 and below, or if the device has square corners
    return 32.dp
}

class NavigationActions(
    val onNavigateToProfile: (Int) -> Unit,
    val onNavigateToMedia: (String, MediaType) -> Unit,
    val onNavigateBack: () -> Unit,
    val onFullscreenImage: (String?) -> Modifier,
)

@Composable
fun <T : Any> AnimatedNavDisplay(
    backStack: List<T>,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {
        if (backStack is MutableList<T>) {
            backStack.removeLastOrNull()
        }
    },
    entryProvider: (key: T) -> NavEntry<T>,
) = NavDisplay(
    backStack = backStack,
    onBack = onBack,
    transitionSpec = {
        // Forward Navigation:
        // New screen slides in from the right TOGETHER WITH old screen pushing left
        slideInHorizontally(
            animationSpec = tween(400),
            initialOffsetX = { fullWidth -> fullWidth },
        ) togetherWith slideOutHorizontally(
            animationSpec = tween(400),
            targetOffsetX = { fullWidth -> -fullWidth / 4 },
        )
    },
    popTransitionSpec = {
        // Backward Navigation (Predictive Back):
        // Old screen pulls in from the left TOGETHER WITH top screen sliding right
        slideInHorizontally(
            animationSpec = tween(
                durationMillis = 400,
                easing = EaseInOutCubic,
            ),
            initialOffsetX = { fullWidth -> -fullWidth / 4 },
        ) togetherWith slideOutHorizontally(
            animationSpec = tween(
                durationMillis = 400,
                easing = EaseInOutCubic,
            ),
            targetOffsetX = { fullWidth -> fullWidth },
        ).apply {
            // CRITICAL FOR PARALLAX:
            // This forces the returning bottom screen to render underneath the exiting top screen.
            // Without this, the bottom screen will pop on top and hide your slide/shadow.
            //targetContentZIndex = -1f
        }
    },
    predictivePopTransitionSpec = {
        // PREDICTIVE BACK: Triggered ONLY by the edge-swipe gesture.
        // Using a standard tween here allows the OS to seamlessly map your
        // finger's physical swipe distance to the X-offset of the slide.
        slideInHorizontally(
            animationSpec = tween(400),
            initialOffsetX = { fullWidth -> -fullWidth / 4 },
        ) togetherWith slideOutHorizontally(
            animationSpec = tween(400),
            targetOffsetX = { fullWidth -> fullWidth },
        ).apply {
            // Keeps the exiting screen hovering ON TOP of the returning screen
            //   targetContentZIndex = -1f
        }
    },
    entryProvider = entryProvider,
)

fun getRawNavKey(state: Any?): String {
    return when (state) {
        // Digs into the Scene wrapper and grabs our overridden contentKey
        is Scene<*> -> state.entries.lastOrNull()?.contentKey
        is NavEntry<*> -> state
        else -> state
    }.toString().substringAfterLast(".").substringBefore("(")
}