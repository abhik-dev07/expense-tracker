package com.abhik.paisatrack.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swmansion.pulsar.Pulsar
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    sheetState: SheetState,
    activeSortOrder: String,
    activeTypeFilter: String,
    activeTimeFilter: String,
    onSortOrderChange: (String) -> Unit,
    onTypeFilterChange: (String) -> Unit,
    onTimeFilterChange: (String) -> Unit,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val pulsar = remember { Pulsar(context) }
    val presets = remember { pulsar.getPresets() }
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, bottom = 48.dp, top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header of Filters with close (cross) button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Filters",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Filter Transactions",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Circle Close Icon (cross press)
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable {
                            presets.plunk()
                            scope.launch {
                                sheetState.hide()
                                onDismissRequest()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Filters",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            // Sort By Filters block (First)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Sort By",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                val sortOrders = listOf("Newest", "Oldest")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    sortOrders.forEach { order ->
                        val selected = activeSortOrder == order
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(50))
                                .background(if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable {
                                    presets.flick()
                                    onSortOrderChange(order)
                                }
                                .border(
                                    width = 1.dp,
                                    color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
                                    shape = RoundedCornerShape(50)
                                )
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = order,
                                fontSize = 12.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                color = if (selected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Transaction Direction Filters block (Middle)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Transaction Type",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                val types = listOf("All", "Income", "Expense")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    types.forEach { type ->
                        val selected = activeTypeFilter.uppercase() == type.uppercase() ||
                                (type == "All" && activeTypeFilter == "All")
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(50))
                                .background(
                                    if (selected) {
                                        if (type == "Income") Color(0xFFE4F6E6)
                                        else if (type == "Expense") Color(0xFFFEE2E2)
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant
                                    }
                                )
                                .clickable {
                                    presets.flick()
                                    onTypeFilterChange(if (type == "All") "All" else type.uppercase())
                                }
                                .border(
                                    width = 1.dp,
                                    color = if (selected) Color.Transparent else MaterialTheme.colorScheme.outline,
                                    shape = RoundedCornerShape(50)
                                )
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = type,
                                fontSize = 12.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                color = if (selected) {
                                    if (type == "Income") Color(0xFF1FB47B)
                                    else if (type == "Expense") Color(0xFFEF4444)
                                    else MaterialTheme.colorScheme.onSurface
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }
                }
            }

            // Time Period Filters block (Last)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Time",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                val times = listOf("All", "Daily", "Weekly", "Monthly")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    times.forEach { time ->
                        val selected = when (time) {
                            "Daily" -> activeTimeFilter == "Today"
                            "Weekly" -> activeTimeFilter == "This Week"
                            "Monthly" -> activeTimeFilter == "This Month"
                            else -> activeTimeFilter == "All"
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(50))
                                .background(if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable {
                                    presets.flick()
                                    val actualFilter = when (time) {
                                        "Daily" -> "Today"
                                        "Weekly" -> "This Week"
                                        "Monthly" -> "This Month"
                                        else -> "All"
                                    }
                                    onTimeFilterChange(actualFilter)
                                }
                                .border(
                                    width = 1.dp,
                                    color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
                                    shape = RoundedCornerShape(50)
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = time,
                                fontSize = 12.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                color = if (selected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
