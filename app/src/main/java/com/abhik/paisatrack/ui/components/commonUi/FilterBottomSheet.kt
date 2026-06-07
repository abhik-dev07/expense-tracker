package com.abhik.paisatrack.ui.components.commonUi

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
                .navigationBarsPadding()
                .padding(start = 24.dp, end = 24.dp, bottom = 24.dp, top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sort By",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val isAnyFilterActive = activeSortOrder != "Newest" || activeTypeFilter != "All" || activeTimeFilter != "All"
                    if (isAnyFilterActive) {
                        TextButton(
                            onClick = {
                                presets.boulder()
                                onSortOrderChange("Newest")
                                onTypeFilterChange("All")
                                onTimeFilterChange("All")
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text(
                                text = "Clear",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                val sortOrders = listOf("Newest", "Oldest")
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    sortOrders.forEachIndexed { index, order ->
                        val selected = activeSortOrder == order
                        SegmentedButton(
                            selected = selected,
                            onClick = {
                                presets.boulder()
                                onSortOrderChange(order)
                            },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = sortOrders.size),
                            label = {
                                Text(
                                    text = order,
                                    fontSize = 12.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        )
                    }
                }
            }
 
            // Transaction Direction Filters block (Middle)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Record Type",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                val types = listOf("All", "Cash In", "Cash Out")
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    types.forEachIndexed { index, type ->
                        val selected = when (type) {
                            "Cash In" -> activeTypeFilter.uppercase() == "INCOME"
                            "Cash Out" -> activeTypeFilter.uppercase() == "EXPENSE"
                            else -> activeTypeFilter.uppercase() == "ALL"
                        }
                        SegmentedButton(
                            selected = selected,
                            onClick = {
                                presets.boulder()
                                val mappedValue = when (type) {
                                    "Cash In" -> "INCOME"
                                    "Cash Out" -> "EXPENSE"
                                    else -> "All"
                                }
                                onTypeFilterChange(mappedValue)
                            },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = types.size),
                            label = {
                                Text(
                                    text = type,
                                    fontSize = 12.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        )
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
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    times.forEachIndexed { index, time ->
                        val selected = when (time) {
                            "Daily" -> activeTimeFilter == "Today"
                            "Weekly" -> activeTimeFilter == "This Week"
                            "Monthly" -> activeTimeFilter == "This Month"
                            else -> activeTimeFilter == "All"
                        }
                        SegmentedButton(
                            selected = selected,
                            onClick = {
                                presets.boulder()
                                val actualFilter = when (time) {
                                    "Daily" -> "Today"
                                    "Weekly" -> "This Week"
                                    "Monthly" -> "This Month"
                                    else -> "All"
                                }
                                onTimeFilterChange(actualFilter)
                            },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = times.size),
                            label = {
                                Text(
                                    text = time,
                                    fontSize = 12.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}
