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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swmansion.pulsar.Pulsar
import kotlinx.coroutines.launch
import com.abhik.paisatrack.ui.theme.isAppInDarkTheme
import com.abhik.paisatrack.ui.theme.SleekSurfaceDark
import com.abhik.paisatrack.ui.utils.findActivity
import com.abhik.paisatrack.ui.utils.getSafePresets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
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

    var isDismissed by remember { mutableStateOf(false) }

    fun dismissSheet() {
        if (!isDismissed) {
            isDismissed = true
            onDismissRequest()
        }
    }

    fun applyAndDismiss() {
        if (!isDismissed) {
            isDismissed = true
            onApplyFilters(tempSortOrder, tempTypeFilter, tempTimeFilter)
            onDismissRequest()
        }
    }

    val isDark = isAppInDarkTheme()
    val closeButtonBg = if (isDark) MaterialTheme.colorScheme.surfaceVariant else SleekSurfaceDark
    val isFilterChangedFromDefault = tempSortOrder != "Newest" || tempTypeFilter != "All" || tempTimeFilter != "All"
    val hasChangedFromActive = tempSortOrder != activeSortOrder ||
        tempTypeFilter != activeTypeFilter ||
        tempTimeFilter != activeTimeFilter
    val isApplyEnabled = isFilterChangedFromDefault && hasChangedFromActive

    ModalBottomSheet(
        onDismissRequest = { dismissSheet() },
        sheetState = sheetState,
        containerColor = Color.Transparent,
        dragHandle = null,
        shape = RoundedCornerShape(0.dp),
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Floating Circular Close Button above the sheet
            CompositionLocalProvider(LocalContentColor provides Color.White) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(closeButtonBg)
                        .clickable {
                            presets.plunk()
                            scope.launch {
                                sheetState.hide()
                                dismissSheet()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_close_small),
                        contentDescription = "Close Filters",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main sheet surface container
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(start = 24.dp, end = 24.dp, bottom = 24.dp, top = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Header of Filters: Title on the left, Clear All on the right when filters are active
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

                        if (isFilterChangedFromDefault) {
                            TextButton(
                                onClick = {
                                    presets.boulder()
                                    tempSortOrder = "Newest"
                                    tempTypeFilter = "All"
                                    tempTimeFilter = "All"
                                    scope.launch {
                                        sheetState.hide()
                                        if (!isDismissed) {
                                            isDismissed = true
                                            onApplyFilters("Newest", "All", "All")
                                            onDismissRequest()
                                        }
                                    }
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
                        ConnectedButtonGroup(
                            options = sortOrders,
                            selectedOption = tempSortOrder,
                            onOptionSelected = { order ->
                                presets.boulder()
                                tempSortOrder = order
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { order, selected ->
                            Text(
                                text = order,
                                fontSize = 12.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            )
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
                        val selectedTypeString = when (tempTypeFilter.uppercase()) {
                            "INCOME" -> "Cash In"
                            "EXPENSE" -> "Cash Out"
                            else -> "All"
                        }
                        ConnectedButtonGroup(
                            options = types,
                            selectedOption = selectedTypeString,
                            onOptionSelected = { type ->
                                presets.boulder()
                                tempTypeFilter = when (type) {
                                    "Cash In" -> "INCOME"
                                    "Cash Out" -> "EXPENSE"
                                    else -> "All"
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { type, selected ->
                            Text(
                                text = type,
                                fontSize = 12.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            )
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
                        val selectedTimeString = when (tempTimeFilter) {
                            "Today" -> "Daily"
                            "This Week" -> "Weekly"
                            "This Month" -> "Monthly"
                            else -> "All"
                        }
                        ConnectedButtonGroup(
                            options = times,
                            selectedOption = selectedTimeString,
                            onOptionSelected = { time ->
                                presets.boulder()
                                tempTimeFilter = when (time) {
                                    "Daily" -> "Today"
                                    "Weekly" -> "This Week"
                                    "Monthly" -> "This Month"
                                    else -> "All"
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { time, selected ->
                            Text(
                                text = time,
                                fontSize = 12.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Apply Filters Button - Pill shape, disabled until filter changes from default
                    Button(
                        onClick = {
                            presets.boulder()
                            scope.launch {
                                sheetState.hide()
                                applyAndDismiss()
                            }
                        },
                        enabled = isApplyEnabled,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            disabledContainerColor = if (isDark) Color(0xFF2A2A2D) else Color(0xFFE2E8F0),
                            disabledContentColor = if (isDark) Color(0xFF6E6E73) else Color(0xFF94A3B8)
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
    }
}
