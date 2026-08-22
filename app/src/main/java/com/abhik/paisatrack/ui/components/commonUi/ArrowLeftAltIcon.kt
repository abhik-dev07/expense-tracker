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
val ArrowLeftAltIconVector: ImageVector
    get() {
        if (_arrowLeftAltIconVector != null) {
            return _arrowLeftAltIconVector!!
        }
        _arrowLeftAltIconVector =
            ImageVector.Builder(
                name = "arrow_left_alt",
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
                        moveTo(7.85f, 13f)
                        lineToRelative(2.85f, 2.85f)
                        quadToRelative(0.3f, 0.3f, 0.29f, 0.7f)
                        reflectiveQuadToRelative(-0.29f, 0.7f)
                        quadToRelative(-0.3f, 0.3f, -0.71f, 0.31f)
                        reflectiveQuadTo(9.28f, 17.27f)
                        lineTo(4.7f, 12.7f)
                        quadTo(4.4f, 12.4f, 4.4f, 12f)
                        reflectiveQuadTo(4.7f, 11.3f)
                        lineTo(9.28f, 6.72f)
                        quadTo(9.58f, 6.43f, 9.99f, 6.44f)
                        reflectiveQuadTo(10.7f, 6.75f)
                        quadToRelative(0.28f, 0.3f, 0.29f, 0.7f)
                        reflectiveQuadTo(10.7f, 8.15f)
                        lineTo(7.85f, 11f)
                        horizontalLineTo(19f)
                        quadToRelative(0.43f, 0f, 0.71f, 0.29f)
                        reflectiveQuadTo(20f, 12f)
                        reflectiveQuadToRelative(-0.29f, 0.71f)
                        reflectiveQuadTo(19f, 13f)
                        horizontalLineTo(7.85f)
                        close()
                    }
                }
                .build()
        return _arrowLeftAltIconVector!!
    }

private var _arrowLeftAltIconVector: ImageVector? = null
