package com.abhik.paisatrack.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.abhik.paisatrack.R

val UberMoveFontFamily = FontFamily(
    Font(R.font.uber_move_light, FontWeight.Light),
    Font(R.font.uber_move_regular, FontWeight.Normal),
    Font(R.font.uber_move_medium, FontWeight.Medium),
    Font(R.font.uber_move_bold, FontWeight.Bold)
)

private val defaultTypography = Typography()

// Set of Material 3 typography styles using Uber Move font family
val Typography = Typography(
    displayLarge = defaultTypography.displayLarge.copy(fontFamily = UberMoveFontFamily),
    displayMedium = defaultTypography.displayMedium.copy(fontFamily = UberMoveFontFamily),
    displaySmall = defaultTypography.displaySmall.copy(fontFamily = UberMoveFontFamily),
    headlineLarge = defaultTypography.headlineLarge.copy(fontFamily = UberMoveFontFamily),
    headlineMedium = defaultTypography.headlineMedium.copy(fontFamily = UberMoveFontFamily),
    headlineSmall = defaultTypography.headlineSmall.copy(fontFamily = UberMoveFontFamily),
    titleLarge = defaultTypography.titleLarge.copy(fontFamily = UberMoveFontFamily),
    titleMedium = defaultTypography.titleMedium.copy(fontFamily = UberMoveFontFamily),
    titleSmall = defaultTypography.titleSmall.copy(fontFamily = UberMoveFontFamily),
    bodyLarge = defaultTypography.bodyLarge.copy(fontFamily = UberMoveFontFamily),
    bodyMedium = defaultTypography.bodyMedium.copy(fontFamily = UberMoveFontFamily),
    bodySmall = defaultTypography.bodySmall.copy(fontFamily = UberMoveFontFamily),
    labelLarge = defaultTypography.labelLarge.copy(fontFamily = UberMoveFontFamily),
    labelMedium = defaultTypography.labelMedium.copy(fontFamily = UberMoveFontFamily),
    labelSmall = defaultTypography.labelSmall.copy(fontFamily = UberMoveFontFamily)
)
