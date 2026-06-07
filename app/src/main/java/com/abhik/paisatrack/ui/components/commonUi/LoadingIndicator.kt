package com.abhik.paisatrack.ui.components.commonUi

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size

@Composable
fun LoadingIndicator(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF1FB47B)
) {
    Box(
        modifier = modifier.shimmerEffect()
    )
}

fun Modifier.shimmerEffect(): Modifier = composed {
    var size by remember { mutableStateOf(IntSize.Zero) }
    val transition = rememberInfiniteTransition(label = "shimmer")
    val startOffsetX by transition.animateFloat(
        initialValue = -2 * size.width.toFloat(),
        targetValue = 2 * size.width.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing)
        ),
        label = "shimmerOffset"
    )

    val isDark = isSystemInDarkTheme()
    val colors = if (isDark) {
        listOf(
            Color(0xFF1E293B),
            Color(0xFF334155),
            Color(0xFF1E293B),
        )
    } else {
        listOf(
            Color(0xFFF1F5F9),
            Color(0xFFE2E8F0),
            Color(0xFFF1F5F9),
        )
    }

    background(
        brush = Brush.linearGradient(
            colors = colors,
            start = Offset(startOffsetX, 0f),
            end = Offset(startOffsetX + size.width.toFloat(), size.height.toFloat())
        )
    ).onGloballyPositioned {
        size = it.size
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomPullToRefreshIndicator(
    state: PullToRefreshState,
    isRefreshing: Boolean,
    modifier: Modifier = Modifier
) {
    val themeColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        if (isRefreshing) {
            LoadingIndicator(
                modifier = Modifier.size(36.dp),
                color = themeColor
            )
        } else {
            val fraction = state.distanceFraction.coerceIn(0f, 1f)
            if (fraction > 0f) {
                Canvas(
                    modifier = Modifier
                        .size(36.dp)
                        .scale(fraction)
                        .rotate(fraction * 360f)
                ) {
                    val sizePx = size.minDimension
                    val rectSize = sizePx * 0.75f
                    val left = (size.width - rectSize) / 2
                    val top = (size.height - rectSize) / 2

                    // Calculate morphing corner radius
                    val maxRadius = rectSize / 2f
                    val minRadius = rectSize * 0.15f
                    val currentRadius = minRadius + (maxRadius - minRadius) * (1f - fraction)

                    drawRoundRect(
                        color = themeColor.copy(alpha = 0.3f + 0.7f * fraction),
                        topLeft = Offset(left, top),
                        size = Size(rectSize, rectSize),
                        cornerRadius = CornerRadius(currentRadius, currentRadius)
                    )
                }
            }
        }
    }
}
