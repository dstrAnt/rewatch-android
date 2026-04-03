package it.polimi.rewatch.components

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun StarRating(
    rating: Float,
    modifier: Modifier = Modifier,
    onRatingChanged: ((Float) -> Unit)? = null,
    maxRating: Int = 5,
    starSize: Dp = 15.dp,
) {
    var rowWidth by remember { mutableFloatStateOf(0f) }
    // 1. Safely remember the latest callback so we don't restart gestures on recomposition
    val currentOnRatingChanged by rememberUpdatedState(onRatingChanged)
    val interactiveModifier = if (onRatingChanged != null) {
        Modifier
            // 2. Use Unit as the key so the gesture detector never restarts mid-swipe
            .pointerInput(Unit) {
                detectTapGestures(
                    // 3. Use onTap instead of onPress. It waits for finger lift.
                    // If the user scrolls vertically, this tap is gracefully cancelled!
                    onTap = { offset ->
                        if (rowWidth > 0f) {
                            val x = offset.x.coerceIn(0f, rowWidth)
                            val calculatedRating = (x / rowWidth) * maxRating
                            val snappedRating = (calculatedRating * 2).roundToInt() / 2f
                            currentOnRatingChanged?.invoke(snappedRating)
                        }
                    },
                )
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onHorizontalDrag = { change, _ ->
                        // 4. Consume the gesture ONLY when moving horizontally
                        change.consume()
                        if (rowWidth > 0f) {
                            val x = change.position.x.coerceIn(0f, rowWidth)
                            val calculatedRating = (x / rowWidth) * maxRating
                            val snappedRating = (calculatedRating * 2).roundToInt() / 2f
                            currentOnRatingChanged?.invoke(snappedRating)
                        }
                    },
                )
            }
    } else {
        Modifier
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .onSizeChanged { rowWidth = it.width.toFloat() }
            .then(interactiveModifier),
    ) {
        repeat(maxRating) { index ->
            val fillFraction = (rating - index).coerceIn(0f, 1f)

            Box(modifier = Modifier.size(starSize)) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.matchParentSize(),
                )

                if (fillFraction > 0f) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = Color.LightGray,
                        modifier = Modifier
                            .matchParentSize()
                            .clip(FractionalClip(fillFraction)),
                    )
                }
            }
        }
    }
}

class FractionalClip(private val fraction: Float) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        return Outline.Rectangle(
            Rect(0f, 0f, size.width * fraction, size.height),
        )
    }
}