package com.abhik.paisatrack.ui.utils

import androidx.compose.ui.graphics.Color
import android.graphics.Color as AndroidColor

/**
 * Safely parses a color hex string into a Compose Color object.
 * Returns the fallback color if the string is invalid, null, or cannot be parsed.
 */
fun safeParseColor(hexString: String?, fallback: Color = Color(0xFF9CA3AF)): Color {
    if (hexString.isNullOrBlank()) return fallback
    return try {
        Color(AndroidColor.parseColor(hexString))
    } catch (e: Exception) {
        fallback
    }
}
