package com.abhik.paisatrack.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.unit.Density

internal val DarkColorScheme =
  darkColorScheme(
    primary = SleekPrimaryDark,
    onPrimary = SleekOnPrimaryDark,
    primaryContainer = SleekPrimaryContainerDark,
    onPrimaryContainer = SleekOnPrimaryContainerDark,
    secondary = SleekSecondaryDark,
    onSecondary = SleekOnSecondaryDark,
    secondaryContainer = SleekSecondaryContainerDark,
    onSecondaryContainer = SleekOnSecondaryContainerDark,
    background = SleekBackgroundDark,
    onBackground = SleekOnBackgroundDark,
    surface = SleekSurfaceDark,
    onSurface = SleekOnSurfaceDark,
    surfaceVariant = SleekSurfaceVariantDark,
    onSurfaceVariant = SleekOnSurfaceVariantDark,
    outline = SleekOutlineDark
  )

internal val LightColorScheme =
  lightColorScheme(
    primary = SleekPrimary,
    onPrimary = SleekOnPrimary,
    primaryContainer = SleekPrimaryContainer,
    onPrimaryContainer = SleekOnPrimaryContainer,
    secondary = SleekSecondary,
    onSecondary = SleekOnSecondary,
    secondaryContainer = SleekSecondaryContainer,
    onSecondaryContainer = SleekOnSecondaryContainer,
    background = SleekBackground,
    onBackground = SleekOnBackground,
    surface = SleekSurface,
    onSurface = SleekOnSurface,
    surfaceVariant = SleekSurfaceVariant,
    onSurfaceVariant = SleekOnSurfaceVariant,
    outline = SleekOutline
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isAppInDarkTheme(),
  // Force user-designed color palette for Sleek theme
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val targetColorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }
    
  val animationSpec = androidx.compose.animation.core.tween<androidx.compose.ui.graphics.Color>(durationMillis = 400)
  
  val primary by androidx.compose.animation.animateColorAsState(targetColorScheme.primary, animationSpec, label = "primary")
  val onPrimary by androidx.compose.animation.animateColorAsState(targetColorScheme.onPrimary, animationSpec, label = "onPrimary")
  val primaryContainer by androidx.compose.animation.animateColorAsState(targetColorScheme.primaryContainer, animationSpec, label = "primaryContainer")
  val onPrimaryContainer by androidx.compose.animation.animateColorAsState(targetColorScheme.onPrimaryContainer, animationSpec, label = "onPrimaryContainer")
  val secondary by androidx.compose.animation.animateColorAsState(targetColorScheme.secondary, animationSpec, label = "secondary")
  val onSecondary by androidx.compose.animation.animateColorAsState(targetColorScheme.onSecondary, animationSpec, label = "onSecondary")
  val secondaryContainer by androidx.compose.animation.animateColorAsState(targetColorScheme.secondaryContainer, animationSpec, label = "secondaryContainer")
  val onSecondaryContainer by androidx.compose.animation.animateColorAsState(targetColorScheme.onSecondaryContainer, animationSpec, label = "onSecondaryContainer")
  val background by androidx.compose.animation.animateColorAsState(targetColorScheme.background, animationSpec, label = "background")
  val onBackground by androidx.compose.animation.animateColorAsState(targetColorScheme.onBackground, animationSpec, label = "onBackground")
  val surface by androidx.compose.animation.animateColorAsState(targetColorScheme.surface, animationSpec, label = "surface")
  val onSurface by androidx.compose.animation.animateColorAsState(targetColorScheme.onSurface, animationSpec, label = "onSurface")
  val surfaceVariant by androidx.compose.animation.animateColorAsState(targetColorScheme.surfaceVariant, animationSpec, label = "surfaceVariant")
  val onSurfaceVariant by androidx.compose.animation.animateColorAsState(targetColorScheme.onSurfaceVariant, animationSpec, label = "onSurfaceVariant")
  val outline by androidx.compose.animation.animateColorAsState(targetColorScheme.outline, animationSpec, label = "outline")

  val colorScheme = targetColorScheme.copy(
      primary = primary,
      onPrimary = onPrimary,
      primaryContainer = primaryContainer,
      onPrimaryContainer = onPrimaryContainer,
      secondary = secondary,
      onSecondary = onSecondary,
      secondaryContainer = secondaryContainer,
      onSecondaryContainer = onSecondaryContainer,
      background = background,
      onBackground = onBackground,
      surface = surface,
      onSurface = onSurface,
      surfaceVariant = surfaceVariant,
      onSurfaceVariant = onSurfaceVariant,
      outline = outline
  )

  val currentDensity = LocalDensity.current
  val configuration = LocalConfiguration.current
  
  // Base display layout target of 375dp. If system display scaling is set too high
  // (which drops the screen's dpWidth under 375dp), we adjust the density factor
  // proportionally to scale everything down gracefully so the layouts are identical.
  val targetWidthDp = 375f
  val screenWidthPx = currentDensity.density * configuration.screenWidthDp
  val adjustedDensity = if (configuration.screenWidthDp < targetWidthDp) {
    screenWidthPx / targetWidthDp
  } else {
    currentDensity.density
  }

  val customDensity = Density(
    density = adjustedDensity,
    fontScale = 1.0f // Prevent enlarged system fonts from breaking UI labels & container layout boundaries
  )

  CompositionLocalProvider(LocalDensity provides customDensity) {
    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
  }
}

@Composable
fun isAppInDarkTheme(): Boolean {
  val themeMode by com.abhik.paisatrack.ui.theme.ThemeManager.themeMode.collectAsState()
  return when (themeMode) {
      com.abhik.paisatrack.ui.theme.ThemeMode.Light -> false
      com.abhik.paisatrack.ui.theme.ThemeMode.Dark -> true
      com.abhik.paisatrack.ui.theme.ThemeMode.System -> isSystemInDarkTheme()
  }
}
