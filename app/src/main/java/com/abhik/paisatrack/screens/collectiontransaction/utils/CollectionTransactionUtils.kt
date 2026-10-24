package com.abhik.paisatrack.screens.collectiontransaction.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CollectionTransactionUtils {

    val dollarFormat = DecimalFormat("₹#,##0.00")

    fun formatTransactionDate(timestamp: Long): String {
        val sdfKey = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val todayStr = sdfKey.format(Date())
        val yesterdayStr = sdfKey.format(Date(System.currentTimeMillis() - 86400000L))
        val txDateStr = sdfKey.format(Date(timestamp))
        return when (txDateStr) {
            todayStr -> "Today"
            yesterdayStr -> "Yesterday"
            else -> SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(timestamp))
        }
    }

    val LocalCollectionColors = listOf(
        "#3F51B5" to "Indigo",
        "#009688" to "Teal",
        "#4CAF50" to "Green",
        "#FF9800" to "Orange",
        "#E91E63" to "Pink",
        "#9C27B0" to "Purple",
        "#FFEB3B" to "Yellow",
        "#00BCD4" to "Cyan",
        "#F44336" to "Red"
    )

    val LocalCollectionIcons = listOf(
        "category" to "General",
        "restaurant" to "Food & Dining",
        "directions_car" to "Transport",
        "movie" to "Entertainment",
        "account_balance_wallet" to "Wallet",
        "local_hospital" to "Health",
        "flight" to "Travel",
        "school" to "Education",
        "shopping_cart" to "Shopping",
        "home" to "Home Bills",
        "fitness_center" to "Sports & Fitness",
        "work" to "Work/Office"
    )

    val AddIconVector: ImageVector
        get() {
            if (_addIconVector != null) {
                return _addIconVector!!
            }
            _addIconVector = ImageVector.Builder(
                name = "add",
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
                    pathFillType = PathFillType.Companion.NonZero
                ) {
                    moveTo(11f, 13f)
                    horizontalLineTo(6f)
                    quadTo(5.58f, 13f, 5.29f, 12.71f)
                    quadTo(5f, 12.43f, 5f, 12f)
                    reflectiveQuadTo(5.29f, 11.29f)
                    reflectiveQuadTo(6f, 11f)
                    horizontalLineToRelative(5f)
                    verticalLineTo(6f)
                    quadTo(11f, 5.57f, 11.29f, 5.29f)
                    reflectiveQuadTo(12f, 5f)
                    reflectiveQuadToRelative(0.71f, 0.29f)
                    reflectiveQuadTo(13f, 6f)
                    verticalLineToRelative(5f)
                    horizontalLineToRelative(5f)
                    quadToRelative(0.43f, 0f, 0.71f, 0.29f)
                    reflectiveQuadTo(19f, 12f)
                    reflectiveQuadToRelative(-0.29f, 0.71f)
                    reflectiveQuadTo(18f, 13f)
                    horizontalLineTo(13f)
                    verticalLineToRelative(5f)
                    quadToRelative(0f, 0.43f, -0.29f, 0.71f)
                    reflectiveQuadTo(12f, 19f)
                    reflectiveQuadTo(11.29f, 18.71f)
                    quadTo(11f, 18.43f, 11f, 18f)
                    verticalLineTo(13f)
                    close()
                }
            }.build()
            return _addIconVector!!
        }

    private var _addIconVector: ImageVector? = null
}
