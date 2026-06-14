package it.polimi.rewatch.common

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Build
import android.view.RoundedCorner
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyGridItemSpanScope
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.currentStateAsState
import it.polimi.rewatch.R
import it.polimi.rewatch.model.MediaId
import it.polimi.rewatch.model.PosterInfo
import it.polimi.rewatch.screens.home.HomeTabItem
import it.polimi.rewatch.screens.media.MediaPage
import java.text.SimpleDateFormat
import java.util.Locale

/*
    In this file we put some functions and composables that we reuse in many other components
 */

/**
 * Background color of all top bars in the application
 */
val headerColor = Color.Black

/**
 * This function turns the input string, formatted as "yyyy-MM-dd", into whatever format is passed as parameter
 */
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

/**
 * Applies a negative vertical offset to the component it is applied to
 */
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

/**
 * Element that displays a button for expandable sections. Based on the expansion status, it shows different labels and arrow icons
 */
@Composable
fun ExpandBtn(
    compressedString: String,
    expandedString: String,
    isExpanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val string = if (isExpanded) expandedString else compressedString
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(15.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(string, style = MaterialTheme.typography.labelLarge)
            if (isExpanded) Icon(Icons.Default.KeyboardArrowUp, contentDescription = null)
            else Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
        }
    }
}

/**
 * Element that encloses the given [content] in a slightly rounded gray rectangle, and allows to add an optional [onClick] callback
 */
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

/**
 * Function that applies on a given [Double] value, returning a string that represents the same number
 * in a shorter and more human-readable format
 */
fun Double.getShortNotation(): String {
    return when {
        this >= 1_000_000_000 -> String.format(Locale.US, "%.1f billion", this / 1_000_000_000)
        this >= 1_000_000 -> String.format(Locale.US, "%.1f million", this / 1_000_000)
        this >= 1_000 -> String.format(Locale.US, "%.1fk", this / 1_000)
        this > 0 -> this.toInt().toString()
        else -> " - " // TMDB spesso restituisce 0 se il budget è sconosciuto
    }.replace(".0", "") // Toglie il decimale se è zero (es: 50.0 -> 50)
}

/**
 * Basic block of all skeleton screens, displays a pulsating rectangle with entirely customizable size and shape
 */
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

/**
 * Custom animation effect applied to every [SkeletonPart] in this application
 */
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

/**
 * Modifier that allow its component to ignore the horizontal padding of its father
 */
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

/**
 * Data class to manage information about tabs in [MediaPage]
 */
@OptIn(ExperimentalFoundationApi::class)
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

/**
 * Initially displays a subset of all the elements passed, and allows to see them all after clicking on a [ExpandBtn]
 */
@OptIn(ExperimentalFoundationApi::class)
fun <T> LazyListScope.expandableSection(
    title: String,
    itemsList: List<T>,
    expandedSectionIds: Set<String>,
    onToggleSection: (String) -> Unit,
    initialVisibleCount: Int = 3,
    showDividers: Boolean = false,
    divider: @Composable LazyItemScope.() -> Unit = { HorizontalDivider() },
    itemContent: @Composable LazyItemScope.(T) -> Unit,
) {
    val isExpanded = expandedSectionIds.contains(title)
    val visibleItems = if (isExpanded) itemsList else itemsList.take(initialVisibleCount)

    itemsIndexed(
        items = visibleItems,
    ) { idx, item ->
        Column(Modifier.animateItem()) {
            itemContent(item)
            if (showDividers && idx != visibleItems.lastIndex) {
                divider()
            }
        }
    }

    if (itemsList.size > initialVisibleCount || isExpanded) {
        item {
            val remaining = itemsList.size - visibleItems.size
            ExpandBtn(
                "Show $remaining more",
                "Hide $title",
                isExpanded,
                { onToggleSection(title) },
                Modifier.animateItem(),
            )
        }
    }
}

/**
 * Text box that displays up to 3 lines of text, and shows the full content after clicking on it
 */
@Composable
fun ExpandableText(
    text: String,
    modifier: Modifier = Modifier,
    maxLines: Int = 3,
    style: TextStyle = LocalTextStyle.current, // Pass style down to ensure measurement matches rendering
) {
    var isExpanded by remember { mutableStateOf(false) }
    val textMeasurer = rememberTextMeasurer()
    // BoxWithConstraints lets us know the exact width we have available to draw the text
    BoxWithConstraints {
        // 1. Measure the text in the background based on the available width
        val textLayoutResult = remember(text, constraints.maxWidth) {
            textMeasurer.measure(
                text = text,
                style = style,
                constraints = Constraints(
                    maxWidth = if (constraints.hasBoundedWidth) constraints.maxWidth else Constraints.Infinity,
                ),
            )
        }
        // 2. Apply your rule: Clickable/Expandable ONLY if it exceeds maxLines + 1
        val canExpand = textLayoutResult.lineCount > maxLines + 1
        val currentMaxLines = when {
            !canExpand -> Int.MAX_VALUE // If it's maxLines + 1 or less, just show it all
            isExpanded -> Int.MAX_VALUE // If the user clicked to expand, show it all
            else -> maxLines            // Otherwise, clip it at maxLines
        }
        // 3. Render the text. It knows exactly what to do on Frame 1.
        Text(
            text = text,
            style = style,
            maxLines = currentMaxLines,
            overflow = if (canExpand && !isExpanded) TextOverflow.Ellipsis else TextOverflow.Clip,
            modifier = Modifier
                // REMOVED .animateContentSize()
                .then(
                    if (canExpand) Modifier.clickable { isExpanded = !isExpanded }
                    else Modifier,
                )
                .then(modifier)
                .testTag("expandableText"),
        )
    }
}

/**
 * Component to display a customizable [PrimaryScrollableTabRow]
 */
@Composable
fun <T> CustomTabRow(
    availableTabs: List<T>,
    onClick: (T) -> Unit,
    title: (T) -> String,
    tabState: T?,
    edgePadding: Dp = 0.dp,
    containerColor: Color = Color.Unspecified,
    divider: @Composable () -> Unit = @Composable { HorizontalDivider() },
) = PrimaryScrollableTabRow(
    edgePadding = edgePadding,
    selectedTabIndex = availableTabs.indexOf(
        tabState,
    ),
    divider = divider,
    containerColor = containerColor,
    modifier = Modifier.testTag("Tab_Row")
) {
    availableTabs.forEach {
        Tab(
            modifier = Modifier.height(50.dp).testTag("Tab_${title(it)}").testTag("customTabs"),
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
/**
 * Clips the content to the shape of the device on which the app is running
 */
fun Modifier.clipToDeviceCorners() = composed {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return@composed this
    // 1. Observe the app's lifecycle state
    val lifecycleState by LocalLifecycleOwner.current.lifecycle.currentStateAsState()
    val isResumed = lifecycleState != Lifecycle.State.RESUMED
    // 2. If the app is pushed to Recents, drop the radius to 0 so the OS can handle it
    val radiusDp = if (isResumed) {
        getDeviceCornerRadius()
    } else {
        0.dp
    }

    clip(RoundedCornerShape(radiusDp))
}

/**
 * Encapsulates all navigation and high-level UI actions for the app.
 * Using a class instead of passing individual lambdas helps keep Composable signatures clean.
 */
class NavigationActions(
    val onNavigateToPerson: (Int) -> Unit,
    val onNavigateToMedia: (MediaId) -> Unit,
    val onNavigateBack: () -> Unit,
    val onFullscreenImage: (String) -> Unit,
    val onBottomSheet: (PosterInfo) -> Unit,
    val onNavigateToLogin: () -> Unit,
    val onBackToHome: (HomeTabItem?) -> Unit,
)

/**
 * Standard duration in milliseconds for navigation transitions.
 */
private const val ANIM_DURATION = 400

/**
 * The divisor used to calculate the offset for the background screen during a
 * transition, creating a parallax effect.
 * * For example, a value of 4 means the background screen moves 1/4th of the
 * screen width while the foreground screen moves the full width.
 */
const val PARALLAX_FACTOR = 4 // For the -fullWidth / 4 effect

/**
 * A standard animation spec using [EaseOut] for smooth deceleration.
 * Required specifically for [IntOffset] based animations like slides.
 */
val standardTween = tween<IntOffset>(ANIM_DURATION, easing = EaseOut)

/**
 * A cubic animation spec using [EaseInOutCubic] for a more organic,
 * accelerated/decelerated feel.
 */
private val cubicTween = tween<IntOffset>(ANIM_DURATION, easing = EaseInOutCubic)

/**
 * The alpha value applied to a screen when it is being "scrimmed" or
 * dimmed in the background.
 */
const val SCRIM_ALPHA = 0.3f

/**
 * Defines the [ContentTransform] for "Pop" transitions (e.g., navigating back).
 * * The returning screen slides in from the left with a [PARALLAX_FACTOR] offset
 * and fades in from [SCRIM_ALPHA]. The exiting screen slides out to the right.
 * It also ensures the exiting screen stays
 * visually on top of the returning screen during the movement.
 *
 * @return A [ContentTransform] configured for a "back" or "pop" navigation feel.
 */
fun popContentTransform() = (
        // Returning (back) screen slides in AND fades back up to 1.0 alpha
        (slideInHorizontally(cubicTween) { fullWidth -> -fullWidth / PARALLAX_FACTOR } +
                fadeIn(tween(ANIM_DURATION, easing = EaseInOutCubic), initialAlpha = SCRIM_ALPHA)) togetherWith
                // Exiting screen slides out normally
                slideOutHorizontally(cubicTween) { fullWidth -> fullWidth }
        ).apply {
        // Keeps the exiting screen hovering ON TOP of the returning screen
        targetContentZIndex = -1f
    }

/**
 * Defines the [ContentTransform] for standard forward transitions.
 * * The entering screen slides in from the right to cover the full width,
 * while the exiting screen slides slightly to the left (parallax) and
 * fades out to [SCRIM_ALPHA] to simulate depth.
 *
 * @return A [ContentTransform] configured for a "forward" navigation feel.
 */
fun transitionSpec() = (
        // Returning (back) screen slides in AND fades back up to 1.0 alpha
        slideInHorizontally(standardTween) { it } togetherWith
                // Exiting (back) screen slides back AND fades out to simulate a scrim
                (slideOutHorizontally(standardTween) { -it / PARALLAX_FACTOR } +
                        fadeOut(tween(ANIM_DURATION, easing = EaseOut), targetAlpha = SCRIM_ALPHA)))

/**
 * Component to display two icons slightly overlapped on the same row
 */
@Composable
fun OverlapIcon(main: ImageVector, secondary: ImageVector) {
    Box(
        modifier = Modifier
            .width(35.dp),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            secondary,
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(20.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
        )

        Icon(
            main,
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(20.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
    }
}

/**
 * Displays the monochrome logo of the application
 */
@Composable
fun Logo() {
    Icon(
        painterResource(R.drawable.logo_monochrome),
        null,
        Modifier.size(40.dp),
        tint = Color.White,
    )
}

/**
 * Basic template for application top bar
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IconTopBar(
    title: String,
    icon: Painter,
    clickEnabled: Boolean = true,
    color: Color = headerColor,
    onBack: () -> Unit,
) {
    TopAppBar(
        title = {
            Text(title)
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = color,
            scrolledContainerColor = Color.Unspecified,
            navigationIconContentColor = Color.Unspecified,
            titleContentColor = Color.White,
            actionIconContentColor = Color.Unspecified,
        ),
        navigationIcon = {
            IconButton(onClick = onBack, enabled = clickEnabled) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .size(24.dp)
                        .wrapContentSize(Alignment.Center),
                )
            }
        },
    )
}

/**
 * Applies a conditional max-size constraint based on the current device orientation.
 *
 * In **Portrait** (or any non-landscape) orientation, this applies [Modifier.fillMaxWidth].
 * In **Landscape** orientation, it applies [Modifier.fillMaxHeight].
 *
 * **Note:** This uses `composed` to reactively observe [LocalConfiguration]. While
 * `UnnecessaryComposedModifier` is suppressed, this is necessary because the modifier
 * logic depends on a CompositionLocal value that can change at runtime.
 *
 * @return A [Modifier] that fills the width in portrait and height in landscape.
 */
@SuppressLint("UnnecessaryComposedModifier")
fun Modifier.fillMaxByOrientation(): Modifier = composed {
    val orientation = LocalConfiguration.current.orientation

    if (orientation != Configuration.ORIENTATION_LANDSCAPE)
        fillMaxWidth()
    else
        fillMaxHeight()
}

/**
 * Adds a single item to a [LazyGridScope] that spans the entire width (all columns) of the grid.
 *
 * This is a convenience wrapper around [LazyGridScope.item] that sets the `span` to
 * [LazyGridItemSpanScope.maxLineSpan]. It ensures the item occupies the full row regardless of
 * the grid's configuration (fixed or adaptive).
 *
 * ### Example:
 * ```kotlin
 * LazyVerticalGrid(columns = GridCells.Fixed(3)) {
 *      itemFullSpan {
 *          HeaderContent() // Spans all 3 columns
 *      }
 * }
 * ```
 *
 * @param content The composable content to be rendered within the full-span grid item.
 * @see LazyGridScope.item
 * @see GridItemSpan
 */
fun LazyGridScope.itemFullSpan(
    content: @Composable (LazyGridItemScope.() -> Unit),
): Unit = item(
    span = { GridItemSpan(maxLineSpan) },
    content = content,
)