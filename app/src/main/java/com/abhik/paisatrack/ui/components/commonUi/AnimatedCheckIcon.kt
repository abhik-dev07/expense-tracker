package com.abhik.paisatrack.ui.components.commonUi

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Composable
fun AnimatedCheckIcon(
    isSelected: Boolean = true,
    tint: Color,
    modifier: Modifier = Modifier.size(16.dp)
) {
    val animatable = remember { Animatable(0f) }

    LaunchedEffect(isSelected) {
        if (isSelected) {
            animatable.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
            )
        } else {
            animatable.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing)
            )
        }
    }

    val progress = animatable.value

    if (progress > 0.01f) {
        Canvas(modifier = modifier) {
            val strokeWidth = 2.4f.dp.toPx() * (size.width / 24.dp.toPx()).coerceIn(0.65f, 1.5f)
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

val CheckSmallIconVector: ImageVector
    get() {
        if (_checkSmallIconVector != null) return _checkSmallIconVector!!
        _checkSmallIconVector = ImageVector.Builder(
            name = "check_small",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                fillAlpha = 1f,
                stroke = null,
                strokeAlpha = 1f,
                strokeLineWidth = 1f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Bevel,
                strokeLineMiter = 1f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(10f, 13.6f)
                lineTo(15.9f, 7.7f)
                quadTo(16.18f, 7.43f, 16.6f, 7.43f)
                reflectiveQuadTo(17.3f, 7.7f)
                reflectiveQuadToRelative(0.27f, 0.7f)
                reflectiveQuadTo(17.3f, 9.1f)
                lineToRelative(-6.6f, 6.6f)
                quadTo(10.4f, 16f, 10f, 16f)
                reflectiveQuadTo(9.3f, 15.7f)
                lineTo(6.7f, 13.1f)
                quadTo(6.43f, 12.83f, 6.43f, 12.4f)
                quadToRelative(0f, -0.42f, 0.27f, -0.7f)
                reflectiveQuadTo(7.4f, 11.43f)
                reflectiveQuadTo(8.1f, 11.7f)
                lineTo(10f, 13.6f)
                close()
            }
        }.build()
        return _checkSmallIconVector!!
    }

private var _checkSmallIconVector: ImageVector? = null
