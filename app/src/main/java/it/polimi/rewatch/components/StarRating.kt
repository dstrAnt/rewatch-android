package it.polimi.rewatch.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

@Composable
fun StarRating(
    rating: Float,
    maxRating: Int = 5,
    starSize: Dp = 15.dp,
    modifier: Modifier = Modifier,
) {
    // Convertiamo il voto da base 10 a base 5
    val starRating = (rating / 2).coerceIn(0.0f, 5.0f)
    val fullStars = starRating.toInt()
    val partialStarFill = (starRating - fullStars)
    val emptyStars =
        maxRating - fullStars - (if (partialStarFill > 0) 1 else 0)
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        // 1. Stelle Piene
        repeat(fullStars) {
            Icon(
                Icons.Filled.Star,
                contentDescription = null,
                tint = Color.LightGray,
                modifier = Modifier.size(starSize),
            )
        }
        // 2. Stella Parziale (il "pezzettino")
        if (partialStarFill > 0) {
            Box {
                // Sfondo della stella (vuota)
                Icon(
                    Icons.Filled.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(starSize),
                )
                // Parte riempita (ritagliata)
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = Color.LightGray,
                    modifier = Modifier
                        .clip(FractionalClip(partialStarFill))
                        .size(starSize),
                )
            }
        }
        // 3. Stelle Vuote
        repeat(emptyStars) {
            Icon(
                Icons.Filled.Star,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(starSize),
            )
        }
    }
}

class FractionalClip(val fraction: Float) : Shape {
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