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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun AnimatedCheckIcon(
    isSelected: Boolean,
    tint: Color,
    modifier: Modifier = Modifier
) {
    val progress by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
        label = "CheckPathAnimation"
    )

    if (progress > 0.02f) {
        Canvas(modifier = modifier.size(16.dp)) {
            val strokeWidth = 2.2f.dp.toPx()
            val cap = StrokeCap.Round
            val join = StrokeJoin.Round

            val scaleX = size.width / 24f
            val scaleY = size.height / 24f

            val pA = Offset(6.7f * scaleX, 12.4f * scaleY)
            val pB = Offset(10f * scaleX, 15.7f * scaleY)
            val pC = Offset(17.3f * scaleX, 8.4f * scaleY)

            val path = Path()
            path.moveTo(pA.x, pA.y)

            if (progress <= 0.31f) {
                val t = progress / 0.31f
                val curr = Offset(
                    pA.x + (pB.x - pA.x) * t,
                    pA.y + (pB.y - pA.y) * t
                )
                path.lineTo(curr.x, curr.y)
            } else {
                path.lineTo(pB.x, pB.y)
                val t = (progress - 0.31f) / 0.69f
                val curr = Offset(
                    pB.x + (pC.x - pB.x) * t,
                    pB.y + (pC.y - pB.y) * t
                )
                path.lineTo(curr.x, curr.y)
            }

            drawPath(
                path = path,
                color = tint,
                style = Stroke(width = strokeWidth, cap = cap, join = join)
            )
        }
    }
}
