package com.abhik.paisatrack.ui.components.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhik.paisatrack.ui.CollectionSummary
import com.abhik.paisatrack.ui.DailySum
import com.abhik.paisatrack.ui.FinanceUiState
import com.swmansion.pulsar.Pulsar
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
        
        // 1. Double Rounded Bar Chart with detailed info
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header row with title + legend
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Weekly Spending Activity",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Tap any bar to see details",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Render Enhanced Bar Chart
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
                text = "Spending Overview",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Spending by Category",
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
                        text = "Add spending records to view category charts.",
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
                            .size(190.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        ExpenseDonutChart(expenseSummaries = expenseSummaries)

                        val totalExp = expenseSummaries.sumOf { it.totalExpense }
                        val formattedCenter = if (totalExp >= 100000) {
                            String.format(Locale.US, "%.1fK", totalExp / 1000.0)
                        } else {
                            dollarFormat.format(totalExp)
                        }

                        // Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        //     Text(
                        //         text = formattedCenter,
                        //         fontSize = 22.sp,
                        //         fontWeight = FontWeight.ExtraBold,
                        //         color = MaterialTheme.colorScheme.onBackground
                        //     )
                        //     Text(
                        //         text = "Total Spent",
                        //         fontSize = 11.sp,
                        //         fontWeight = FontWeight.Medium,
                        //         color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        //     )
                        // }
                    }

                    // Separation gap between donut chart and legend row
                    Spacer(modifier = Modifier.height(24.dp))

                    // Legend list matching screenshot showing ALL categories that have data
                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        maxItemsInEachRow = 3
                    ) {
                        val totalSpentVal = expenseSummaries.sumOf { it.totalExpense }
                        expenseSummaries.forEach { summary ->
                            val rgb = Color(android.graphics.Color.parseColor(summary.collection.hexColor))
                            val pct = if (totalSpentVal > 0) ((summary.totalExpense / totalSpentVal) * 100).toInt() else 0

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(rgb)
                                    )
                                    Text(
                                        text = summary.collection.name,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "$pct%",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
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

        val gapDegrees = if (expenseSummaries.size > 1) 16f else 0f
        var startAngle = -90f

        expenseSummaries.forEach { summary ->
            val sweepAngle = (360f * (summary.totalExpense / totalExp)).toFloat()
            val col = Color(android.graphics.Color.parseColor(summary.collection.hexColor))

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

    // Track which day index is selected for tooltip
    var selectedDayIndex by remember { mutableStateOf(-1) }

    val context = LocalContext.current
    val pulsar = remember { Pulsar(context) }
    val presets = remember { com.abhik.paisatrack.ui.utils.SafePresets(pulsar.getPresets()) }

    val maxAmount = remember(dailySums) {
        val maxInc = dailySums.maxOfOrNull { it.totalIncome } ?: 0.0
        val maxExp = dailySums.maxOfOrNull { it.totalExpense } ?: 0.0
        maxOf(maxInc, maxExp, 100.0)
    }

    // Determine today's day key for highlighting
    val todayKey = remember {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        sdf.format(Date())
    }

    // Weekly totals
    val weeklyIncome = remember(dailySums) { dailySums.sumOf { it.totalIncome } }
    val weeklyExpense = remember(dailySums) { dailySums.sumOf { it.totalExpense } }
    val weeklyNet = weeklyIncome - weeklyExpense

    Column(modifier = Modifier.fillMaxWidth()) {

        // Tooltip card for selected day
        AnimatedVisibility(
            visible = selectedDayIndex in dailySums.indices,
            enter = fadeIn(tween(200)) + slideInVertically(tween(200)) { -it / 2 },
            exit = fadeOut(tween(150)) + slideOutVertically(tween(150)) { -it / 2 }
        ) {
            if (selectedDayIndex in dailySums.indices) {
                val selected = dailySums[selectedDayIndex]
                val dayFullFormat = remember(selected.timestamp) {
                    SimpleDateFormat("EEE, dd MMM", Locale.getDefault()).format(Date(selected.timestamp))
                }
                val net = selected.totalIncome - selected.totalExpense
                val compactFormat = remember { DecimalFormat("₹#,##0.00") }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = dayFullFormat,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            // Circle Close Icon (matching FilterBottomSheet style)
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable {
                                        presets.boulder()
                                        selectedDayIndex = -1
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close details",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            // Income detail
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF10B981)))
                                    Text("Cash In", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(
                                    text = compactFormat.format(selected.totalIncome),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF10B981)
                                )
                            }
                            // Expense detail
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFFEF4444)))
                                    Text("Cash Out", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(
                                    text = compactFormat.format(selected.totalExpense),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFFEF4444)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Bar chart
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            dailySums.forEachIndexed { index, item ->
                val isSelected = index == selectedDayIndex
                val isToday = remember(item.timestamp) {
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    sdf.format(Date(item.timestamp)) == todayKey
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f)
                            else Color.Transparent
                        )
                        .clickable {
                            presets.ping()
                            selectedDayIndex = if (selectedDayIndex == index) -1 else index
                        }
                        .padding(horizontal = 2.dp)
                ) {

                    // Dual bar containers
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            // Income Bar (Green)
                            val incomeHeightFraction = ((item.totalIncome / maxAmount) * animProgress).toFloat().coerceIn(0.01f, 1f)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(incomeHeightFraction)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) Color(0xFF059669) else Color(0xFF10B981)
                                    )
                            )

                            // Expense Bar (Red)
                            val expenseHeightFraction = ((item.totalExpense / maxAmount) * animProgress).toFloat().coerceIn(0.01f, 1f)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(expenseHeightFraction)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) Color(0xFFDC2626) else Color(0xFFEF4444)
                                    )
                            )
                        }
                    }

                    // X-Axis day label
                    Text(
                        text = item.dateString,
                        fontSize = 10.sp,
                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                        color = if (isToday) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    // "Today" badge
                    if (isToday) {
                        Box(
                            modifier = Modifier
                                .height(3.dp)
                                .width(16.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    } else {
                        Spacer(modifier = Modifier.height(3.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Weekly summary row
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            thickness = 0.5.dp
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val summaryFormat = remember { DecimalFormat("₹#,##0") }

            // Weekly Income
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Total Cash In",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = summaryFormat.format(weeklyIncome),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF10B981)
                )
            }

            // Weekly Expense
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Total Cash Out",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = summaryFormat.format(weeklyExpense),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFEF4444)
                )
            }
        }
    }
}

/**
 * Formats an amount into a compact human-readable string.
 * e.g. 1500 -> "₹1.5K", 250 -> "₹250", 0 -> "₹0"
 */
private fun formatCompactAmount(amount: Double): String {
    return when {
        amount >= 10_00_000 -> "₹${String.format("%.1f", amount / 10_00_000)}M"
        amount >= 1_000 -> "₹${String.format("%.1f", amount / 1_000)}K"
        amount > 0 -> "₹${amount.toInt()}"
        else -> "₹0"
    }
}
