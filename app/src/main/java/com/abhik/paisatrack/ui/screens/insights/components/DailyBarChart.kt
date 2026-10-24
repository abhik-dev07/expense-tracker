package com.abhik.paisatrack.ui.screens.insights.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhik.paisatrack.R
import com.abhik.paisatrack.ui.DailySum
import com.abhik.paisatrack.ui.utils.findActivity
import com.abhik.paisatrack.ui.utils.getSafePresets
import com.swmansion.pulsar.Pulsar
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    val pulsar = remember(context) { Pulsar(context.findActivity() ?: context) }
    val presets = remember(pulsar) { pulsar.getSafePresets() }

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
                            // Circle Close Icon
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
                                    painter = painterResource(id = R.drawable.ic_close_small),
                                    contentDescription = "Close details",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(16.dp)
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
