package com.abhik.paisatrack.ui.components.commonUi

import androidx.compose.foundation.background
import com.abhik.paisatrack.R
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.abhik.paisatrack.ui.utils.findActivity
import com.abhik.paisatrack.ui.utils.getSafePresets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    sheetState: SheetState,
    activeSortOrder: String,
    activeTypeFilter: String,
    activeTimeFilter: String,
    onApplyFilters: (sortOrder: String, typeFilter: String, timeFilter: String) -> Unit,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val pulsar = remember(context) { Pulsar(context.findActivity() ?: context) }
    val presets = remember(pulsar) { pulsar.getSafePresets() }
    val scope = rememberCoroutineScope()

    var tempSortOrder by remember(activeSortOrder) { mutableStateOf(activeSortOrder) }
    var tempTypeFilter by remember(activeTypeFilter) { mutableStateOf(activeTypeFilter) }
    var tempTimeFilter by remember(activeTimeFilter) { mutableStateOf(activeTimeFilter) }

    val isAnyFilterActive = tempSortOrder != "Newest" || tempTypeFilter != "All" || tempTimeFilter != "All"

    var isApplied by remember { mutableStateOf(false) }

    fun closeAndApply() {
        if (!isApplied) {
            isApplied = true
            onApplyFilters(tempSortOrder, tempTypeFilter, tempTimeFilter)
            onDismissRequest()
        }
    }

    ModalBottomSheet(
        onDismissRequest = { closeAndApply() },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 24.dp, end = 24.dp, bottom = 24.dp, top = 4.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header of Filters: Close button when no filter active, replaced by Clear All when active
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filter Transactions",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (isAnyFilterActive) {
                    TextButton(
                        onClick = {
                            presets.boulder()
                            tempSortOrder = "Newest"
                            tempTypeFilter = "All"
                            tempTimeFilter = "All"
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text(
                            text = "Clear All",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable {
                                presets.plunk()
                                scope.launch {
                                    sheetState.hide()
                                    closeAndApply()
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_close_small),
                            contentDescription = "Close Filters",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(16.dp)
                        )
                    }
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
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    sortOrders.forEachIndexed { index, order ->
                        val selected = tempSortOrder == order
                        SegmentedButton(
                            selected = selected,
                            onClick = {
                                presets.boulder()
                                tempSortOrder = order
                            },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = sortOrders.size),
                            icon = {
                                if (selected) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_tick),
                                        contentDescription = "Check",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            },
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

            // Record Type Filters block (Middle)
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
                            "Cash In" -> tempTypeFilter.uppercase() == "INCOME"
                            "Cash Out" -> tempTypeFilter.uppercase() == "EXPENSE"
                            else -> tempTypeFilter.uppercase() == "ALL"
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
                                tempTypeFilter = mappedValue
                            },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = types.size),
                            icon = {
                                if (selected) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_tick),
                                        contentDescription = "Check",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            },
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
                            "Daily" -> tempTimeFilter == "Today"
                            "Weekly" -> tempTimeFilter == "This Week"
                            "Monthly" -> tempTimeFilter == "This Month"
                            else -> tempTimeFilter == "All"
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
                                tempTimeFilter = actualFilter
                            },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = times.size),
                            icon = {
                                if (selected) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_tick),
                                        contentDescription = "Check",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            },
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

            Spacer(modifier = Modifier.height(6.dp))

            // Apply Filters Button
            Button(
                onClick = {
                    presets.boulder()
                    scope.launch {
                        sheetState.hide()
                        closeAndApply()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = "Apply Filters",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
