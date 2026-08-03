package com.abhik.paisatrack.ui.components.commonUi

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp

@Composable
fun AnimatedTuneIcon(
    isActive: Boolean,
    tint: Color,
    modifier: Modifier = Modifier
) {
    val progress by animateFloatAsState(
        targetValue = if (isActive) 1f else 0f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "TuneBarsAnimation"
    )

    Canvas(modifier = modifier.size(18.dp)) {
        val strokeWidth = 1.8f.dp.toPx()
        val cap = StrokeCap.Round

        fun lerp(a: Float, b: Float, t: Float) = a + (b - a) * t

        val scaleX = size.width / 24f
        val scaleY = size.height / 24f

        // Row 0 (Top slider bar)
        val thumbX0 = lerp(16f, 8f, progress)
        val y0 = 6f
        if (thumbX0 - 2.5f > 3.5f) {
            drawLine(
                color = tint,
                start = Offset(4f * scaleX, y0 * scaleY),
                end = Offset((thumbX0 - 2.5f) * scaleX, y0 * scaleY),
                strokeWidth = strokeWidth,
                cap = cap
            )
        }
        if (thumbX0 + 2.5f < 20.5f) {
            drawLine(
                color = tint,
                start = Offset((thumbX0 + 2.5f) * scaleX, y0 * scaleY),
                end = Offset(20f * scaleX, y0 * scaleY),
                strokeWidth = strokeWidth,
                cap = cap
            )
        }
        drawLine(
            color = tint,
            start = Offset(thumbX0 * scaleX, 3.5f * scaleY),
            end = Offset(thumbX0 * scaleX, 8.5f * scaleY),
            strokeWidth = strokeWidth * 1.15f,
            cap = cap
        )

        // Row 1 (Middle slider bar)
        val thumbX1 = lerp(8f, 15f, progress)
        val y1 = 12f
        if (thumbX1 - 2.5f > 3.5f) {
            drawLine(
                color = tint,
                start = Offset(4f * scaleX, y1 * scaleY),
                end = Offset((thumbX1 - 2.5f) * scaleX, y1 * scaleY),
                strokeWidth = strokeWidth,
                cap = cap
            )
        }
        if (thumbX1 + 2.5f < 20.5f) {
            drawLine(
                color = tint,
                start = Offset((thumbX1 + 2.5f) * scaleX, y1 * scaleY),
                end = Offset(20f * scaleX, y1 * scaleY),
                strokeWidth = strokeWidth,
                cap = cap
            )
        }
        drawLine(
            color = tint,
            start = Offset(thumbX1 * scaleX, 9.5f * scaleY),
            end = Offset(thumbX1 * scaleX, 14.5f * scaleY),
            strokeWidth = strokeWidth * 1.15f,
            cap = cap
        )

        // Row 2 (Bottom slider bar)
        val thumbX2 = lerp(12f, 6f, progress)
        val y2 = 18f
        if (thumbX2 - 2.5f > 3.5f) {
            drawLine(
                color = tint,
                start = Offset(4f * scaleX, y2 * scaleY),
                end = Offset((thumbX2 - 2.5f) * scaleX, y2 * scaleY),
                strokeWidth = strokeWidth,
                cap = cap
            )
        }
        if (thumbX2 + 2.5f < 20.5f) {
            drawLine(
                color = tint,
                start = Offset((thumbX2 + 2.5f) * scaleX, y2 * scaleY),
                end = Offset(20f * scaleX, y2 * scaleY),
                strokeWidth = strokeWidth,
                cap = cap
            )
        }
        drawLine(
            color = tint,
            start = Offset(thumbX2 * scaleX, 15.5f * scaleY),
            end = Offset(thumbX2 * scaleX, 20.5f * scaleY),
            strokeWidth = strokeWidth * 1.15f,
            cap = cap
        )
    }
}
