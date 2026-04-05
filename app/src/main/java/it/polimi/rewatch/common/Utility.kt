package it.polimi.rewatch.common

import android.content.res.Configuration
import android.os.Build
import android.view.RoundedCorner
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.grid.GridCells.Fixed
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import it.polimi.rewatch.R
import it.polimi.rewatch.model.MediaId
import it.polimi.rewatch.model.PosterInfo
import it.polimi.rewatch.screens.media.expandableSection
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.abs
import kotlin.math.max

/*
    In this file we put some functions and composables that we reuse in many other components
 */

val headerColor = Color.Black

fun String.capitalize(): String = this.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }

/**
 * Aggiunge un bordo sul lato inferiore del componente.
 * @param color Il colore del bordo (default grigio chiaro)
 * @param width Lo spessore del bordo (default 1dp)
 */
fun Modifier.addBorderBottom(
    color: Color = Color.LightGray,
    width: Dp = 1.dp,
): Modifier = drawWithContent {
    // Disegna il contenuto originale del composable
    drawContent()

    // Converte lo spessore da Dp a Pixel
    val strokeWidth = width.toPx()

    // Calcola la coordinata Y (il fondo del componente)
    // Sottraiamo metà dello spessore per assicurarci che la linea sia dentro i bordi
    val y = size.height - strokeWidth / 2

    drawLine(
        color = color,
        start = Offset(x = 0f, y = y),
        end = Offset(x = size.width, y = y),
        strokeWidth = strokeWidth,
    )
}


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

//fun movieFromIntent(intent: Intent): Pair<String, MediaType> {
//    val mediaId = intent.getStringExtra("EXTRA_MEDIA_ID") ?: "-1"
//    val typeString = intent.getStringExtra("EXTRA_MEDIA_TYPE")
//    val mediaType = try {
//        typeString?.let { enumValueOf<MediaType>(it) } ?: MediaType.MOVIE
//    } catch (_: IllegalArgumentException) {
//        MediaType.MOVIE
//    }
//
//    return Pair(mediaId, mediaType)
//}
//
//fun onMediaClickNav(
//    mediaId: String,
//    navController: NavHostController,
//) = navController.navigate("movie_page/$mediaId")
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
                .animateContentSize()
                .then(
                    if (canExpand) Modifier.clickable { isExpanded = !isExpanded }
                    else Modifier,
                )
                .then(modifier),
        )
    }
}

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
            .padding(horizontal = 7.dp),
        content = { content.invoke(this, calculatedColumnCount) },
    )
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
    val onNavigateToProfile: () -> Unit,
    val onClearAndNavigateToSettings: () -> Unit,
    val onBackToHome: () -> Unit,
)

private const val ANIM_DURATION = 400
const val PARALLAX_FACTOR = 4 // For the -fullWidth / 4 effect

// 2. Create reusable animation specs (Note the <IntOffset> type required for slides)
val standardTween = tween<IntOffset>(ANIM_DURATION, easing = EaseOut)
private val cubicTween = tween<IntOffset>(ANIM_DURATION, easing = EaseInOutCubic)

// 3. Create a helper function for the pop transitions
fun popContentTransform(): ContentTransform {
    return (slideInHorizontally(cubicTween) { fullWidth -> -fullWidth / PARALLAX_FACTOR } togetherWith
            slideOutHorizontally(cubicTween) { fullWidth -> fullWidth }).apply {
        // Keeps the exiting screen hovering ON TOP of the returning screen
        targetContentZIndex = -1f
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
        } catch (_: CancellationException) {
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
                .fillMaxByOrientation() // <--- Your custom modifier
                .graphicsLayer {
                    translationY = animatedOffsetY
                    scaleX = animatedScale
                    scaleY = animatedScale
                }
                .clip(RoundedCornerShape(5.dp)) // Set the corner radius
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

@Composable
fun OverlapIcon(main: ImageVector, secondary: ImageVector) {
    Box(
        modifier = Modifier
            .width(35.dp)
            .height(30.dp),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            secondary,
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(20.dp),
            tint = Color.Gray,
        )

        Icon(
            main,
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(20.dp),
            tint = Color.White,
        )
    }
}

@Composable
fun Logo() {
    Icon(
        painterResource(R.drawable.logo_monochrome),
        null,
        Modifier.size(40.dp),
        tint = Color.White,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BasicTopBar(
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

fun Modifier.fillMaxByOrientation(): Modifier = composed {
    val orientation = LocalConfiguration.current.orientation

    if (orientation != Configuration.ORIENTATION_LANDSCAPE)
        fillMaxWidth()
    else
        fillMaxHeight()
}


fun LazyGridScope.itemFullSpan(
    content: @Composable (LazyGridItemScope.() -> Unit),
): Unit = item(
    span = { GridItemSpan(maxLineSpan) },
    content = content,
)