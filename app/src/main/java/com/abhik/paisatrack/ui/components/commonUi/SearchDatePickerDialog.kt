package com.abhik.paisatrack.ui.components.commonUi

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhik.paisatrack.ui.utils.findActivity
import com.abhik.paisatrack.ui.utils.getSafePresets
import com.swmansion.pulsar.Pulsar
import java.util.Calendar
import java.util.TimeZone

/**
 * The Material 3 date pickers hand back UTC midnight for the picked calendar date, while
 * transaction timestamps are plain local wall-clock millis. Rebuild the same y/m/d in the
 * device timezone so a day filter lines up with the day the user actually tapped.
 */
private fun utcDateToLocalDayStart(utcMillis: Long): Long {
    val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = utcMillis }
    return Calendar.getInstance().apply {
        clear()
        set(utc.get(Calendar.YEAR), utc.get(Calendar.MONTH), utc.get(Calendar.DAY_OF_MONTH), 0, 0, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

private fun utcDateToLocalDayEnd(utcMillis: Long): Long {
    val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = utcMillis }
    return Calendar.getInstance().apply {
        clear()
        set(utc.get(Calendar.YEAR), utc.get(Calendar.MONTH), utc.get(Calendar.DAY_OF_MONTH), 23, 59, 59)
        set(Calendar.MILLISECOND, 999)
    }.timeInMillis
}

/** Inverse of the above: seed the picker's selection from an already-applied local filter. */
private fun localMillisToUtcDate(localMillis: Long?): Long? {
    if (localMillis == null) return null
    val local = Calendar.getInstance().apply { timeInMillis = localMillis }
    return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        clear()
        set(local.get(Calendar.YEAR), local.get(Calendar.MONTH), local.get(Calendar.DAY_OF_MONTH), 0, 0, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

private fun isSameLocalDay(t1: Long, t2: Long): Boolean {
    val c1 = Calendar.getInstance().apply { timeInMillis = t1 }
    val c2 = Calendar.getInstance().apply { timeInMillis = t2 }
    return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
            c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)
}

/**
 * Date filter for the transaction search. Offers a single day or a custom range and hands back
 * inclusive local-time bounds, so the caller can filter with a plain `timestamp in start..end`.
 *
 * @param activeStart currently applied start bound (local millis), or null when no filter is set.
 * @param activeEnd currently applied end bound (local millis).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchDatePickerDialog(
    activeStart: Long?,
    activeEnd: Long?,
    onConfirm: (startMillis: Long, endMillis: Long) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val pulsar = remember(context) { Pulsar(context.findActivity() ?: context) }
    val presets = remember(pulsar) { pulsar.getSafePresets() }

    val hasActiveFilter = activeStart != null
    val activeIsRange = activeStart != null && activeEnd != null && !isSameLocalDay(activeStart, activeEnd)

    val modes = listOf("Day", "Range")
    var selectedMode by remember { mutableStateOf(if (activeIsRange) "Range" else "Day") }

    val dayState = rememberDatePickerState(
        initialSelectedDateMillis = if (activeIsRange) null else localMillisToUtcDate(activeStart)
    )
    val rangeState = rememberDateRangePickerState(
        initialSelectedStartDateMillis = if (activeIsRange) localMillisToUtcDate(activeStart) else null,
        initialSelectedEndDateMillis = if (activeIsRange) localMillisToUtcDate(activeEnd) else null
    )

    val pickerColors = DatePickerDefaults.colors(
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        headlineContentColor = MaterialTheme.colorScheme.onSurface,
        weekdayContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        subheadContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        navigationContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        yearContentColor = MaterialTheme.colorScheme.onSurface,
        currentYearContentColor = MaterialTheme.colorScheme.primary,
        selectedYearContentColor = MaterialTheme.colorScheme.onPrimary,
        selectedYearContainerColor = MaterialTheme.colorScheme.primary,
        dayContentColor = MaterialTheme.colorScheme.onSurface,
        selectedDayContentColor = MaterialTheme.colorScheme.onPrimary,
        selectedDayContainerColor = MaterialTheme.colorScheme.primary,
        todayContentColor = MaterialTheme.colorScheme.primary,
        todayDateBorderColor = MaterialTheme.colorScheme.primary,
        dayInSelectionRangeContentColor = MaterialTheme.colorScheme.onSurface,
        dayInSelectionRangeContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        dividerColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    )

    val canApply = if (selectedMode == "Day") {
        dayState.selectedDateMillis != null
    } else {
        rangeState.selectedStartDateMillis != null
    }

    DatePickerDialog(
        onDismissRequest = onDismiss,
        colors = pickerColors,
        confirmButton = {
            TextButton(
                enabled = canApply,
                onClick = {
                    presets.boulder()
                    if (selectedMode == "Day") {
                        val picked = dayState.selectedDateMillis ?: return@TextButton
                        onConfirm(utcDateToLocalDayStart(picked), utcDateToLocalDayEnd(picked))
                    } else {
                        val start = rangeState.selectedStartDateMillis ?: return@TextButton
                        // A range with only its first edge picked collapses to that single day.
                        val end = rangeState.selectedEndDateMillis ?: start
                        onConfirm(utcDateToLocalDayStart(start), utcDateToLocalDayEnd(end))
                    }
                }
            ) {
                Text(text = "Apply", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (hasActiveFilter) {
                    TextButton(onClick = {
                        presets.plunk()
                        onClear()
                    }) {
                        Text(
                            text = "Clear",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                TextButton(onClick = {
                    presets.plunk()
                    onDismiss()
                }) {
                    Text(
                        text = "Cancel",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    ) {
        // The dialog's content slot hosts exactly one child (M3 places it in a Box), so the
        // Day/Range toggle rides in the picker's own `title` slot instead of as a sibling.
        val modeToggle: @Composable () -> Unit = {
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 4.dp)
            ) {
                modes.forEachIndexed { index, mode ->
                    val selected = selectedMode == mode
                    SegmentedButton(
                        selected = selected,
                        onClick = {
                            presets.boulder()
                            selectedMode = mode
                        },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = modes.size),
                        icon = {
                            AnimatedCheckIcon(
                                isSelected = selected,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        },
                        label = {
                            Text(
                                text = mode,
                                fontSize = 12.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    )
                }
            }
        }

        if (selectedMode == "Day") {
            DatePicker(
                state = dayState,
                colors = pickerColors,
                title = modeToggle,
                showModeToggle = false
            )
        } else {
            DateRangePicker(
                state = rangeState,
                colors = pickerColors,
                title = modeToggle,
                showModeToggle = false
            )
        }
    }
}
