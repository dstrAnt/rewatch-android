package it.polimi.rewatch.theme

import android.os.Build
import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import it.polimi.rewatch.R

val GoogleSans = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
    FontFamily(
        Font(R.font.google, FontWeight.Thin),   // W100
        Font(R.font.google, FontWeight.Normal), // W400
        Font(R.font.google, FontWeight.Medium), // W500
        Font(R.font.google, FontWeight.Bold),    // W700
    )
} else {
    FontFamily.Default // Fallback for Android 7 and below
}

// 1. Grab the default Material 3 baseline typography
private val baseline = Typography()

// 2. Map your font family to all styles, and only override your custom sizes
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
        fontSize = 14.sp, // From your original code (M3 default is 16.sp)
    ),
    bodyMedium = baseline.bodyMedium.copy(fontFamily = GoogleSans),
    bodySmall = baseline.bodySmall.copy(fontFamily = GoogleSans),
    // Label
    labelLarge = baseline.labelLarge.copy(
        fontFamily = GoogleSans,
        fontSize = 15.sp, // From your original code (M3 default is 14.sp)
        lineHeight = 28.sp,
        letterSpacing = 0.sp,
    ),
    labelMedium = baseline.labelMedium.copy(fontFamily = GoogleSans),
    labelSmall = baseline.labelSmall.copy(fontFamily = GoogleSans),
)