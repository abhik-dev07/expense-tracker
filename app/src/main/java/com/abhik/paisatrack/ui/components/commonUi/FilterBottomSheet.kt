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

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path

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
    val presets = remember { com.abhik.paisatrack.ui.utils.SafePresets(pulsar.getPresets()) }
    val scope = rememberCoroutineScope()
    val isAnyFilterActive = activeSortOrder != "Newest" || activeTypeFilter != "All" || activeTimeFilter != "All"

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 24.dp, end = 24.dp, bottom = 24.dp, top = 4.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
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
                    // Replace close button with Clear All when filter is active
                    TextButton(
                        onClick = {
                            presets.boulder()
                            onSortOrderChange("Newest")
                            onTypeFilterChange("All")
                            onTimeFilterChange("All")
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
                    // Show Close button when no filter is active
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
                            imageVector = CloseIconVector,
                            contentDescription = "Close Filters",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(14.dp)
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
                        val selected = activeSortOrder == order
                        SegmentedButton(
                            selected = selected,
                            onClick = {
                                presets.boulder()
                                onSortOrderChange(order)
                            },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = sortOrders.size),
                            icon = {
                                AnimatedCheckIcon(
                                    isSelected = selected,
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                                )
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
                            icon = {
                                AnimatedCheckIcon(
                                    isSelected = selected,
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                                )
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
                            icon = {
                                AnimatedCheckIcon(
                                    isSelected = selected,
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                                )
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
        }
    }
}

private val CloseIconVector: ImageVector
    get() {
        if (_closeIconVector != null) return _closeIconVector!!
        _closeIconVector = ImageVector.Builder(
            name = "close",
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
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(12f, 13.4f)
                lineTo(7.1f, 18.3f)
                quadTo(6.83f, 18.58f, 6.4f, 18.58f)
                reflectiveQuadTo(5.7f, 18.3f)
                quadTo(5.43f, 18.02f, 5.43f, 17.6f)
                reflectiveQuadTo(5.7f, 16.9f)
                lineTo(10.6f, 12f)
                lineTo(5.7f, 7.1f)
                quadTo(5.43f, 6.82f, 5.43f, 6.4f)
                reflectiveQuadTo(5.7f, 5.7f)
                reflectiveQuadTo(6.4f, 5.43f)
                reflectiveQuadTo(7.1f, 5.7f)
                lineTo(12f, 10.6f)
                lineTo(16.9f, 5.7f)
                quadTo(17.18f, 5.43f, 17.6f, 5.43f)
                reflectiveQuadTo(18.3f, 5.7f)
                reflectiveQuadToRelative(0.27f, 0.7f)
                reflectiveQuadTo(18.3f, 7.1f)
                lineTo(13.4f, 12f)
                lineToRelative(4.9f, 4.9f)
                quadToRelative(0.27f, 0.28f, 0.27f, 0.7f)
                quadToRelative(0f, 0.42f, -0.27f, 0.7f)
                reflectiveQuadToRelative(-0.7f, 0.27f)
                reflectiveQuadTo(16.9f, 18.3f)
                lineTo(12f, 13.4f)
                close()
            }
        }.build()
        return _closeIconVector!!
    }

private var _closeIconVector: ImageVector? = null
