package com.abhik.paisatrack.ui.components.commonUi

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhik.paisatrack.ui.utils.findActivity
import com.abhik.paisatrack.ui.utils.getSafePresets
import com.swmansion.pulsar.Pulsar
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private val MonthNames = listOf(
    "January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December"
)

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

    val todayUtcDateMillis = remember {
        localMillisToUtcDate(System.currentTimeMillis()) ?: 0L
    }
    val currentCal = remember { Calendar.getInstance() }
    val currentYear = remember { currentCal.get(Calendar.YEAR) }
    val currentMonth = remember { currentCal.get(Calendar.MONTH) }
    val startYear = remember(currentYear) { minOf(2000, currentYear - 10) }

    var selectedDayMillis by remember(activeStart) {
        mutableStateOf(if (!activeIsRange) localMillisToUtcDate(activeStart) else null)
    }
    var rangeStartMillis by remember(activeStart) {
        mutableStateOf(if (activeIsRange) localMillisToUtcDate(activeStart) else null)
    }
    var rangeEndMillis by remember(activeEnd) {
        mutableStateOf(if (activeIsRange) localMillisToUtcDate(activeEnd) else null)
    }

    var displayedYear by remember {
        val initialDate = if (activeIsRange) rangeStartMillis else selectedDayMillis
        val yr = if (initialDate != null) {
            val c = Calendar.getInstance().apply { timeInMillis = utcDateToLocalDayStart(initialDate) }
            c.get(Calendar.YEAR)
        } else currentYear
        mutableStateOf(yr.coerceIn(startYear, currentYear))
    }

    var displayedMonth by remember {
        val initialDate = if (activeIsRange) rangeStartMillis else selectedDayMillis
        val m = if (initialDate != null) {
            val c = Calendar.getInstance().apply { timeInMillis = utcDateToLocalDayStart(initialDate) }
            c.get(Calendar.MONTH)
        } else currentMonth
        mutableStateOf(m)
    }

    var showYearPicker by remember { mutableStateOf(false) }

    val pickerColors = DatePickerDefaults.colors(
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        headlineContentColor = MaterialTheme.colorScheme.onSurface,
        weekdayContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        subheadContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        navigationContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        yearContentColor = MaterialTheme.colorScheme.onSurface,
        disabledYearContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
        currentYearContentColor = MaterialTheme.colorScheme.primary,
        selectedYearContentColor = MaterialTheme.colorScheme.onPrimary,
        selectedYearContainerColor = MaterialTheme.colorScheme.primary,
        dayContentColor = MaterialTheme.colorScheme.onSurface,
        disabledDayContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f),
        selectedDayContentColor = MaterialTheme.colorScheme.onPrimary,
        selectedDayContainerColor = MaterialTheme.colorScheme.primary,
        todayContentColor = MaterialTheme.colorScheme.primary,
        todayDateBorderColor = MaterialTheme.colorScheme.primary,
        dayInSelectionRangeContentColor = MaterialTheme.colorScheme.onSurface,
        dayInSelectionRangeContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        dividerColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    )

    val canApply = if (selectedMode == "Day") {
        selectedDayMillis != null
    } else {
        rangeStartMillis != null
    }

    val dayHeadlineText = remember(selectedDayMillis) {
        if (selectedDayMillis != null) {
            val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
            sdf.format(Date(utcDateToLocalDayStart(selectedDayMillis!!)))
        } else {
            "Select date"
        }
    }

    val rangeHeadlineText = remember(rangeStartMillis, rangeEndMillis) {
        val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        val startStr = rangeStartMillis?.let { sdf.format(Date(utcDateToLocalDayStart(it))) }
        val endStr = rangeEndMillis?.let { sdf.format(Date(utcDateToLocalDayStart(it))) }
        when {
            startStr != null && endStr != null -> "$startStr – $endStr"
            startStr != null -> "$startStr – End date"
            else -> "Start date – End date"
        }
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
                        val picked = selectedDayMillis ?: return@TextButton
                        onConfirm(utcDateToLocalDayStart(picked), utcDateToLocalDayEnd(picked))
                    } else {
                        val start = rangeStartMillis ?: return@TextButton
                        val end = rangeEndMillis ?: start
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
                        selectedDayMillis = null
                        rangeStartMillis = null
                        rangeEndMillis = null
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
        Column(modifier = Modifier.fillMaxWidth()) {
            // Mode toggle (Day | Range) via Material 3 Connected Button Group
            M3ConnectedButtonGroup(
                modes = modes,
                selectedMode = selectedMode,
                onModeSelected = { mode ->
                    selectedMode = mode
                    // Automatically reset date selections when shifting between modes vice-versa
                    selectedDayMillis = null
                    rangeStartMillis = null
                    rangeEndMillis = null
                    showYearPicker = false
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            )

            // Centered Headline with matching 24sp headline font size
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (selectedMode == "Day") dayHeadlineText else rangeHeadlineText,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Normal,
                        fontSize = 24.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            if (showYearPicker) {
                // Year Selection with generous 28dp bottom contentPadding (never cut off!)
                val years = remember(startYear, currentYear) { (startYear..currentYear).toList() }
                val gridState = rememberLazyGridState(
                    initialFirstVisibleItemIndex = maxOf(0, years.indexOf(displayedYear) - 3)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(310.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    presets.ping()
                                    showYearPicker = false
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${MonthNames[displayedMonth]} $displayedYear",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowDropUp,
                                contentDescription = "Close year picker",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        state = gridState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(top = 4.dp, bottom = 28.dp)
                    ) {
                        items(years) { year ->
                            val isSelected = year == displayedYear
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 6.dp, vertical = 6.dp)
                                    .height(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                                    )
                                    .clickable {
                                        presets.boulder()
                                        displayedYear = year
                                        if (displayedYear == currentYear && displayedMonth > currentMonth) {
                                            displayedMonth = currentMonth
                                        }
                                        showYearPicker = false
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = year.toString(),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 15.sp
                                    ),
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            } else {
                // Month navigation row with Horizontal controls
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                presets.ping()
                                showYearPicker = true
                            }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${MonthNames[displayedMonth]} $displayedYear",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Open year picker",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val canGoPrev = displayedYear > startYear || displayedMonth > 0
                        IconButton(
                            onClick = {
                                presets.boulder()
                                if (displayedMonth == 0) {
                                    if (displayedYear > startYear) {
                                        displayedMonth = 11
                                        displayedYear--
                                    }
                                } else {
                                    displayedMonth--
                                }
                            },
                            enabled = canGoPrev
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = "Previous Month",
                                tint = if (canGoPrev) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            )
                        }

                        val canGoNext = displayedYear < currentYear || (displayedYear == currentYear && displayedMonth < currentMonth)
                        IconButton(
                            onClick = {
                                presets.boulder()
                                if (displayedMonth == 11) {
                                    if (displayedYear < currentYear) {
                                        displayedMonth = 0
                                        displayedYear++
                                    }
                                } else {
                                    displayedMonth++
                                }
                            },
                            enabled = canGoNext
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "Next Month",
                                tint = if (canGoNext) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            )
                        }
                    }
                }

                // Weekday Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    listOf("S", "M", "T", "W", "T", "F", "S").forEach { day ->
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = day,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Month Days Grid with Horizontal Swipe Navigation
                val cal = remember(displayedYear, displayedMonth) {
                    Calendar.getInstance().apply {
                        clear()
                        set(Calendar.YEAR, displayedYear)
                        set(Calendar.MONTH, displayedMonth)
                        set(Calendar.DAY_OF_MONTH, 1)
                    }
                }
                val firstDayOfWeek = remember(cal) { cal.get(Calendar.DAY_OF_WEEK) - 1 } // 0 = Sun
                val maxDaysInMonth = remember(cal) { cal.getActualMaximum(Calendar.DAY_OF_MONTH) }
                val totalCells = firstDayOfWeek + maxDaysInMonth
                val numRows = (totalCells + 6) / 7

                var dragAccumulator by remember { mutableStateOf(0f) }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 20.dp)
                        .pointerInput(displayedYear, displayedMonth) {
                            detectHorizontalDragGestures(
                                onDragStart = { dragAccumulator = 0f },
                                onHorizontalDrag = { change, dragAmount ->
                                    change.consume()
                                    dragAccumulator += dragAmount
                                },
                                onDragEnd = {
                                    if (dragAccumulator < -40f) {
                                        val canGoNext = displayedYear < currentYear || (displayedYear == currentYear && displayedMonth < currentMonth)
                                        if (canGoNext) {
                                            presets.boulder()
                                            if (displayedMonth == 11) {
                                                displayedMonth = 0
                                                displayedYear++
                                            } else {
                                                displayedMonth++
                                            }
                                        }
                                    } else if (dragAccumulator > 40f) {
                                        val canGoPrev = displayedYear > startYear || displayedMonth > 0
                                        if (canGoPrev) {
                                            presets.boulder()
                                            if (displayedMonth == 0) {
                                                displayedMonth = 11
                                                displayedYear--
                                            } else {
                                                displayedMonth--
                                            }
                                        }
                                    }
                                    dragAccumulator = 0f
                                }
                            )
                        }
                ) {
                    for (row in 0 until numRows) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (col in 0..6) {
                                val dayNum = row * 7 + col - firstDayOfWeek + 1
                                if (dayNum in 1..maxDaysInMonth) {
                                    val dayUtcMillis = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                                        clear()
                                        set(displayedYear, displayedMonth, dayNum, 0, 0, 0)
                                        set(Calendar.MILLISECOND, 0)
                                    }.timeInMillis

                                    val isFuture = dayUtcMillis > todayUtcDateMillis
                                    val isToday = dayUtcMillis == todayUtcDateMillis

                                    val isStart: Boolean
                                    val isEnd: Boolean
                                    val isInRange: Boolean

                                    if (selectedMode == "Day") {
                                        isStart = selectedDayMillis != null && dayUtcMillis == selectedDayMillis
                                        isEnd = false
                                        isInRange = false
                                    } else {
                                        isStart = rangeStartMillis != null && dayUtcMillis == rangeStartMillis
                                        isEnd = rangeEndMillis != null && dayUtcMillis == rangeEndMillis
                                        isInRange = rangeStartMillis != null && rangeEndMillis != null &&
                                                dayUtcMillis > rangeStartMillis!! && dayUtcMillis < rangeEndMillis!!
                                    }

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        // Range highlight connector
                                        if (isInRange) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f))
                                            )
                                        } else if (isStart && rangeEndMillis != null && rangeEndMillis!! > rangeStartMillis!!) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxHeight()
                                                    .fillMaxWidth(0.5f)
                                                .align(Alignment.CenterEnd)
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f))
                                            )
                                        } else if (isEnd && rangeStartMillis != null && rangeEndMillis!! > rangeStartMillis!!) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxHeight()
                                                    .fillMaxWidth(0.5f)
                                                .align(Alignment.CenterStart)
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f))
                                            )
                                        }

                                        // Day circle & number
                                        val isSelectedEdge = isStart || isEnd
                                        val circleBg = when {
                                            isSelectedEdge -> MaterialTheme.colorScheme.primary
                                            else -> Color.Transparent
                                        }

                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(circleBg)
                                                .then(
                                                    if (isToday && !isSelectedEdge) {
                                                        Modifier.border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                                    } else Modifier
                                                )
                                                .clickable(enabled = !isFuture) {
                                                    presets.ping()
                                                    if (selectedMode == "Day") {
                                                        selectedDayMillis = dayUtcMillis
                                                    } else {
                                                        if (rangeStartMillis == null) {
                                                            rangeStartMillis = dayUtcMillis
                                                            rangeEndMillis = null
                                                        } else if (rangeEndMillis == null) {
                                                            if (dayUtcMillis < rangeStartMillis!!) {
                                                                rangeStartMillis = dayUtcMillis
                                                            } else if (dayUtcMillis == rangeStartMillis) {
                                                                rangeEndMillis = dayUtcMillis
                                                            } else {
                                                                rangeEndMillis = dayUtcMillis
                                                            }
                                                        } else {
                                                            rangeStartMillis = dayUtcMillis
                                                            rangeEndMillis = null
                                                        }
                                                    }
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            val textColor = when {
                                                isFuture -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                                                isSelectedEdge -> MaterialTheme.colorScheme.onPrimary
                                                isInRange -> MaterialTheme.colorScheme.onSurface
                                                isToday -> MaterialTheme.colorScheme.primary
                                                else -> MaterialTheme.colorScheme.onSurface
                                            }

                                            Text(
                                                text = dayNum.toString(),
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = if (isSelectedEdge || isToday) FontWeight.Bold else FontWeight.Normal,
                                                    fontSize = 14.sp
                                                ),
                                                color = textColor
                                            )
                                        }
                                    }
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Material 3 Connected Button Group (https://m3.material.io/components/button-groups/overview)
 * for mutually exclusive mode selection (Day vs Range).
 */
@Composable
private fun M3ConnectedButtonGroup(
    modes: List<String>,
    selectedMode: String,
    onModeSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val pulsar = remember(context) { Pulsar(context.findActivity() ?: context) }
    val presets = remember(pulsar) { pulsar.getSafePresets() }

    Box(
        modifier = modifier
            .height(44.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
            .padding(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            modes.forEach { mode ->
                val selected = selectedMode == mode
                val animBgColor by animateColorAsState(
                    targetValue = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "btnGroupBg_$mode"
                )
                val animContentColor by animateColorAsState(
                    targetValue = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "btnGroupContent_$mode"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(animBgColor)
                        .clickable {
                            presets.boulder()
                            if (selectedMode != mode) {
                                onModeSelected(mode)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        AnimatedCheckIcon(
                            isSelected = selected,
                            tint = animContentColor
                        )

                        Text(
                            text = mode,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 14.sp
                            ),
                            color = animContentColor
                        )
                    }
                }
            }
        }
    }
}

