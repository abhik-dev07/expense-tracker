package com.abhik.paisatrack.ui.components.commonUi

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Suppress("CheckReturnValue")
val CloseSmallRoundedIconVector: ImageVector
    get() {
        if (_closeSmallRoundedIconVector != null) {
            return _closeSmallRoundedIconVector!!
        }
        _closeSmallRoundedIconVector =
            ImageVector.Builder(
                name = "close_small_rounded",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f,
            )
                .apply {
                    path(
                        fill = SolidColor(Color.Black),
                        fillAlpha = 1f,
                        stroke = null,
                        strokeAlpha = 1f,
                        strokeLineWidth = 1f,
                        strokeLineCap = StrokeCap.Butt,
                        strokeLineJoin = StrokeJoin.Bevel,
                        strokeLineMiter = 1f,
                        pathFillType = PathFillType.NonZero,
                    ) {
                        moveTo(12f, 13.4f)
                        lineTo(9.1f, 16.3f)
                        quadTo(8.83f, 16.58f, 8.4f, 16.58f)
                        reflectiveQuadTo(7.7f, 16.3f)
                        quadTo(7.43f, 16.02f, 7.43f, 15.6f)
                        reflectiveQuadTo(7.7f, 14.9f)
                        lineTo(10.6f, 12f)
                        lineTo(7.7f, 9.13f)
                        quadTo(7.43f, 8.85f, 7.43f, 8.42f)
                        reflectiveQuadTo(7.7f, 7.72f)
                        reflectiveQuadTo(8.4f, 7.45f)
                        quadToRelative(0.43f, 0f, 0.7f, 0.27f)
                        lineToRelative(2.9f, 2.9f)
                        lineToRelative(2.88f, -2.9f)
                        quadToRelative(0.28f, -0.27f, 0.7f, -0.27f)
                        reflectiveQuadToRelative(0.7f, 0.27f)
                        quadToRelative(0.3f, 0.3f, 0.3f, 0.71f)
                        reflectiveQuadToRelative(-0.3f, 0.69f)
                        lineTo(13.38f, 12f)
                        lineToRelative(2.9f, 2.9f)
                        quadToRelative(0.27f, 0.27f, 0.27f, 0.7f)
                        reflectiveQuadToRelative(-0.27f, 0.7f)
                        quadToRelative(-0.3f, 0.3f, -0.71f, 0.3f)
                        reflectiveQuadTo(14.88f, 16.3f)
                        lineTo(12f, 13.4f)
                        close()
                    }
                }
                .build()
        return _closeSmallRoundedIconVector!!
    }

private var _closeSmallRoundedIconVector: ImageVector? = null
