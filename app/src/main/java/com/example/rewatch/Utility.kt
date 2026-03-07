package com.example.rewatch

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.rewatch.movie.MovieActivity
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

@Composable
fun StarRating(
    rating: Double,
    maxRating: Int = 5
) {
    // Convertiamo il voto da base 10 a base 5
    val starRating = (rating / 2).coerceIn(0.0, 5.0)

    val fullStars = starRating.toInt()
    val partialStarFill = (starRating - fullStars).toFloat()
    val emptyStars =
        maxRating - fullStars - (if (partialStarFill > 0) 1 else 0)

    val starSize = 16.dp
    Row(verticalAlignment = Alignment.CenterVertically) {
        // 1. Stelle Piene
        repeat(fullStars) {
            Icon(
                Icons.Filled.Star,
                contentDescription = null,
                tint = Color(0xFFFFC107),
                modifier = Modifier.size(starSize)
            )
        }

        // 2. Stella Parziale (il "pezzettino")
        if (partialStarFill > 0) {
            Box {
                // Sfondo della stella (vuota)
                Icon(
                    Icons.Filled.Star,
                    contentDescription = null,
                    tint = Color.LightGray,
                    modifier = Modifier.size(starSize)
                )
                // Parte riempita (ritagliata)
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = Color(0xFFFFC107),
                    modifier = Modifier
                        .clip(FractionalClip(partialStarFill))
                        .size(starSize)
                )
            }
        }

        // 3. Stelle Vuote
        repeat(emptyStars) {
            Icon(
                Icons.Filled.Star,
                contentDescription = null,
                tint = Color.LightGray,
                modifier = Modifier.size(starSize)
            )
        }

    }
}

class FractionalClip(val fraction: Float) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        return Outline.Rectangle(
            Rect(0f, 0f, size.width * fraction, size.height)
        )
    }
}

fun Modifier.relativeMove(y: Dp) =
    this.layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        layout(
            placeable.width,
            placeable.height + y.roundToPx()
        ) {
            // Placing it at the negative Y offset
            placeable.placeRelative(0, y.roundToPx())
        }
    }

@Composable
fun ExpandBtn(
    string: String,
    function: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = function)
            .padding(15.dp)
    ) {

        Text(string)
    }
}


@Composable
fun PosterImage(
    onClick: () -> Unit = {},
    content: @Composable (() -> Unit)
) {

    val shape = RoundedCornerShape(5.dp)

    Box(
        modifier = Modifier
            .clip(shape) // Set the corner radius
            .border(
                BorderStroke(1.dp, Color.Gray), shape
            ) // Add border with rounded corners
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onClick
            )
    ) {
        content()
    }
}

fun onMovieClick(
    movieId: Int,
    context: Context
) {
    // Create an Intent to start MovieActivity
    val intent = Intent(
        context, MovieActivity::class.java
    ).apply {
        // Bundle the movieId into the Intent extras
        putExtra(
            "EXTRA_MOVIE_ID", movieId
        )
    }

    // Launch the activity! (This triggers the native OS transition)
    context.startActivity(intent)
}

fun Double.getShortNotation(): String {
    return when {
        this >= 1_000_000_000 -> String.format(Locale.US, "%.1f billion", this / 1_000_000_000)
        this >= 1_000_000 -> String.format(Locale.US, "%.1f million", this / 1_000_000)
        this >= 1_000 -> String.format(Locale.US, "%.1fk", this / 1_000)
        this > 0 -> this.toInt().toString()
        else -> " - " // TMDB spesso restituisce 0 se il budget è sconosciuto
    }.replace(".0", "") // Toglie il decimale se è zero (es: 50.0 -> 50)
}