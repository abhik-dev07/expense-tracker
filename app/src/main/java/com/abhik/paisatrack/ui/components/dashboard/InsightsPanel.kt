package com.abhik.paisatrack.ui.components.dashboard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhik.paisatrack.ui.CollectionSummary
import com.abhik.paisatrack.ui.DailySum
import com.abhik.paisatrack.ui.FinanceUiState
import java.text.DecimalFormat

@Composable
fun InsightsPanel(
    uiState: FinanceUiState,
    aiInsights: String,
    aiLoading: Boolean,
    onRefreshInsights: () -> Unit,
    dollarFormat: DecimalFormat,
    onScrollProgressChanged: (Boolean) -> Unit
) {
    val scrollState = rememberScrollState()
    
    LaunchedEffect(scrollState) {
        snapshotFlow { scrollState.value }
            .collect { currentValue ->
                if (currentValue == 0) {
                    onScrollProgressChanged(false)
                } else {
                    onScrollProgressChanged(true)
                }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        
        // 1. Double Rounded Bar Chart
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Weekly Cash Flow Activity",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Synchronized income vs expenditure logs (7-Day window)",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(20.dp))

                // Render Bar Chart
                DailyBarChart(dailySums = uiState.dailyTransactionSums)
            }
        }

        // 2. Beautiful Donut Slice breakdown of expenses
        val expenseSummaries = remember(uiState.collectionSummaries) {
            uiState.collectionSummaries.filter { it.totalExpense > 0.0 }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text(
                text = "Expense Allocation Breakdown",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Proportional spending index of collections",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (!expenseSummaries.isEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total Spent",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = dollarFormat.format(uiState.totalExpense),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            if (expenseSummaries.isEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Register expenses with collection values to draw allocation graphs.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Give the donut bar chart some vertical gap from the total spent
                    Spacer(modifier = Modifier.height(28.dp))

                    // Center the circular donut chart on the screen
                    Box(
                        modifier = Modifier
                            .size(170.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        ExpenseDonutChart(expenseSummaries = expenseSummaries)
                    }

                    // Separation gap between donut chart and list under it
                    Spacer(modifier = Modifier.height(24.dp))

                    // Legend list of card collections below the chart
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val totalSpentVal = expenseSummaries.sumOf { it.totalExpense }
                        expenseSummaries.take(5).forEach { summary ->
                            val rgb = Color(android.graphics.Color.parseColor(summary.collection.hexColor))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(rgb)
                                )
                                Text(
                                    text = "${summary.collection.name} (${String.format("%.0f%%", (summary.totalExpense / totalSpentVal) * 100)})",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------- CHARTS IMPLEMENTATIONS ----------------

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

        var startAngle = -90f
        expenseSummaries.forEach { summary ->
            val angle = 360f * (summary.totalExpense / totalExp).toFloat()
            val col = Color(android.graphics.Color.parseColor(summary.collection.hexColor))
            
            drawArc(
                color = col,
                startAngle = startAngle,
                sweepAngle = angle * animProgress,
                useCenter = false,
                style = Stroke(width = 30.dp.toPx(), cap = StrokeCap.Round)
            )
            startAngle += angle
        }
    }
}

@Composable
fun DailyBarChart(dailySums: List<DailySum>) {
    var animTrigger by remember { mutableStateOf(false) }
    val animProgress by animateFloatAsState(
        targetValue = if (animTrigger) 1f else 0f,
        animationSpec = tween(1000),
        label = "BarChartHeight"
    )

    LaunchedEffect(dailySums) {
        animTrigger = true
    }

    val maxAmount = remember(dailySums) {
        val maxInc = dailySums.maxOfOrNull { it.totalIncome } ?: 0.0
        val maxExp = dailySums.maxOfOrNull { it.totalExpense } ?: 0.0
        maxOf(maxInc, maxExp, 100.0) // fallback base max is 100
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        dailySums.forEach { item ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Dual column containers
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        // Income Bar (Green)
                        val incomeHeightFraction = ((item.totalIncome / maxAmount) * animProgress).toFloat().coerceIn(0.01f, 1f)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(incomeHeightFraction)
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(Color(0xFF10B981))
                        )

                        // Expense Bar (Red)
                        val expenseHeightFraction = ((item.totalExpense / maxAmount) * animProgress).toFloat().coerceIn(0.01f, 1f)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(expenseHeightFraction)
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(Color(0xFFFFB8A9))
                        )
                    }
                }

                // X-Axis Text Label
                Text(
                    text = item.dateString,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
