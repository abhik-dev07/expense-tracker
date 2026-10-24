package com.abhik.paisatrack.screens.insights.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.abhik.paisatrack.utils.CollectionSummary
import com.abhik.paisatrack.utils.safeParseColor

@Composable
fun ExpenseDonutChart(expenseSummaries: List<CollectionSummary>) {
    var animTrigger by remember { mutableStateOf(false) }
    val animProgress by animateFloatAsState(
        targetValue = if (animTrigger) 1f else 0f,
        animationSpec = tween(1000),
        label = "DonutChartScale"
    )

    LaunchedEffect(expenseSummaries) {
        animTrigger = true
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val totalExp = expenseSummaries.sumOf { it.totalExpense }
        if (totalExp <= 0.0) return@Canvas

        val gapDegrees = if (expenseSummaries.size > 1) 16f else 0f
        var startAngle = -90f

        expenseSummaries.forEach { summary ->
            val sweepAngle = (360f * (summary.totalExpense / totalExp)).toFloat()
            val col = safeParseColor(summary.collection.hexColor)

            val drawSweep = (sweepAngle - gapDegrees).coerceAtLeast(1f) * animProgress
            val drawStart = startAngle + (gapDegrees / 2f)

            drawArc(
                color = col,
                startAngle = drawStart,
                sweepAngle = drawSweep,
                useCenter = false,
                style = Stroke(width = 24.dp.toPx(), cap = StrokeCap.Round)
            )
            startAngle += sweepAngle
        }
    }
}
