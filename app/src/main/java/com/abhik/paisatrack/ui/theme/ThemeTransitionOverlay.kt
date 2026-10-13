package com.abhik.paisatrack.ui.theme

import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhik.paisatrack.R
import kotlinx.coroutines.delay

private const val COVER_DURATION = 200
private const val HOLD_DURATION = 450
private const val REVEAL_FADE_DURATION = 150
private const val REVEAL_EXPAND_DURATION = 500

@Composable
fun ThemeTransitionOverlay() {
    val requestedTheme by ThemeManager.requestedTheme.collectAsState()
    
    val activeRequest = requestedTheme
    if (activeRequest == null) return

    val context = LocalContext.current
    val isSystemDark = isSystemInDarkTheme()
    
    val resolvedTargetIsDark = when (activeRequest) {
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
        ThemeMode.System -> isSystemDark
    }
    
    val targetScheme = if (resolvedTargetIsDark) DarkColorScheme else LightColorScheme
    
    val backgroundColor = targetScheme.background
    val surfaceColor = targetScheme.surface
    val onBackgroundColor = targetScheme.onBackground
    
    val label = when (activeRequest) {
        ThemeMode.Light -> "Switching to Light"
        ThemeMode.Dark -> "Switching to Dark"
        ThemeMode.System -> "System default"
    }
    
    val subtitle = if (activeRequest == ThemeMode.System) {
        "Following device: ${if (resolvedTargetIsDark) "Dark" else "Light"}"
    } else null
    
    val iconRes = if (resolvedTargetIsDark) R.drawable.ic_moon_custom else R.drawable.ic_sun_custom
    
    var phase by rememberSaveable { mutableIntStateOf(0) }
    
    val animatorScale = Settings.Global.getFloat(
        context.contentResolver,
        Settings.Global.ANIMATOR_DURATION_SCALE,
        1f
    )
    
    val isAnimationDisabled = animatorScale == 0f
    
    LaunchedEffect(activeRequest) {
        if (isAnimationDisabled) {
            ThemeManager.setTheme(context, activeRequest)
            ThemeManager.clearRequest()
            return@LaunchedEffect
        }
        
        phase = 0
        delay(COVER_DURATION.toLong())
        
        phase = 1
        ThemeManager.setTheme(context, activeRequest)
        delay(HOLD_DURATION.toLong())
        
        phase = 2
        delay(REVEAL_FADE_DURATION.toLong())
        delay(REVEAL_EXPAND_DURATION.toLong())
        
        phase = 3
        ThemeManager.clearRequest()
    }
    
    if (phase == 3) return
    
    BackHandler(true) {}
    
    val coverAlpha by animateFloatAsState(
        targetValue = if (phase >= 0) 1f else 0f,
        animationSpec = tween(if (phase == 0) 200 else 0),
        label = "coverAlpha"
    )

    val contentAlpha by animateFloatAsState(
        targetValue = if (phase < 2) 1f else 0f,
        animationSpec = tween(if (phase == 2) 150 else 0),
        label = "contentAlpha"
    )
    
    val density = LocalDensity.current
    val maxRadiusPx = with(density) { 1500.dp.toPx() }
    
    val holeRadiusPx by animateFloatAsState(
        targetValue = if (phase >= 2) maxRadiusPx else 0f,
        animationSpec = tween(if (phase >= 2) 500 else 0, delayMillis = if (phase >= 2) 150 else 0),
        label = "holeRadius"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                alpha = coverAlpha
                compositingStrategy = CompositingStrategy.Offscreen
            }
            .pointerInput(Unit) {
                awaitEachGesture {
                    val event = awaitPointerEvent()
                    event.changes.forEach { it.consume() }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(color = backgroundColor)
            if (holeRadiusPx > 0f) {
                drawCircle(
                    color = Color.Black,
                    radius = holeRadiusPx,
                    center = Offset(size.width / 2f, size.height / 2f),
                    blendMode = BlendMode.Clear
                )
            }
        }
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .graphicsLayer { alpha = contentAlpha }
                .semantics { liveRegion = LiveRegionMode.Polite }
        ) {
            Box(
                modifier = Modifier
                    .size(136.dp)
                    .background(surfaceColor, CircleShape)
                    .border(1.dp, onBackgroundColor.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = "Theme Icon",
                    tint = onBackgroundColor,
                    modifier = Modifier.size(52.dp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(text = label, color = onBackgroundColor, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
