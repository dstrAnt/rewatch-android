package it.polimi.rewatch.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import it.polimi.rewatch.R

@OptIn(ExperimentalTextApi::class)
val GoogleSans =
    FontFamily(
        // Map Thin (W100)
        Font(
            resId = R.font.google,
            weight = FontWeight.W100,
            variationSettings = FontVariation.Settings(
                FontVariation.weight(100),
            ),
        ),
        // Map Regular / Normal (W400)
        Font(
            resId = R.font.google,
            weight = FontWeight.Normal,
            variationSettings = FontVariation.Settings(
                FontVariation.weight(400),
            ),
        ),
        // Map Medium (W500)
        Font(
            resId = R.font.google,
            weight = FontWeight.Medium,
            variationSettings = FontVariation.Settings(
                FontVariation.weight(500),
            ),
        ),
        // Map Bold (W700)
        Font(
            resId = R.font.google,
            weight = FontWeight.Bold,
            variationSettings = FontVariation.Settings(
                FontVariation.weight(700),
            ),
        ),
    )

// Grab the default Material 3 baseline typography
private val baseline = Typography()

// Map the font family to all styles and only override custom sizes
val AppTypography = Typography(
    // Display
    displayLarge = baseline.displayLarge.copy(fontFamily = GoogleSans),
    displayMedium = baseline.displayMedium.copy(fontFamily = GoogleSans),
    displaySmall = baseline.displaySmall.copy(fontFamily = GoogleSans),
    // Headline
    headlineLarge = baseline.headlineLarge.copy(fontFamily = GoogleSans),
    headlineMedium = baseline.headlineMedium.copy(fontFamily = GoogleSans),
    headlineSmall = baseline.headlineSmall.copy(fontFamily = GoogleSans),
    // Title
    titleLarge = baseline.titleLarge.copy(fontFamily = GoogleSans),
    titleMedium = baseline.titleMedium.copy(fontFamily = GoogleSans),
    titleSmall = baseline.titleSmall.copy(fontFamily = GoogleSans),
    // Body
    bodyLarge = baseline.bodyLarge.copy(
        fontFamily = GoogleSans,
        fontSize = 14.sp,
    ),
    bodyMedium = baseline.bodyMedium.copy(fontFamily = GoogleSans),
    bodySmall = baseline.bodySmall.copy(fontFamily = GoogleSans),
    // Label
    labelLarge = baseline.labelLarge.copy(
        fontFamily = GoogleSans,
        fontSize = 15.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp,
    ),
    labelMedium = baseline.labelMedium.copy(fontFamily = GoogleSans),
    labelSmall = baseline.labelSmall.copy(fontFamily = GoogleSans),
)
