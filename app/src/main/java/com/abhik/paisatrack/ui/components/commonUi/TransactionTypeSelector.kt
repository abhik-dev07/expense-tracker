package com.abhik.paisatrack.ui.components.commonUi

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhik.paisatrack.ui.theme.isAppInDarkTheme
import com.abhik.paisatrack.utils.findActivity
import com.abhik.paisatrack.utils.getSafePresets
import com.swmansion.pulsar.Pulsar
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Transaction type selector for Cash In (+) and Cash Out (-).
 * Matches BottomNavBar's fluid sliding and hold-drag indicator mechanics:
 * - Tap: indicator moves smoothly via spring animation.
 * - Hold & drag (or horizontal slide on active indicator): picks up with haptic,
 *   tracks touch in real-time with graphicsLayer, provides hover haptic feedback,
 *   and drops with spring physics onto the target tab upon release.
 */
@Composable
fun TransactionTypeSelector(
    transactionType: String,
    onTypeSelected: (String) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val pulsar = remember(context) { Pulsar(context.findActivity() ?: context) }
    val presets = remember(pulsar) { pulsar.getSafePresets() }
    val scope = rememberCoroutineScope()
    val isDark = isAppInDarkTheme()

    val activeIndex = if (transactionType.uppercase() == "EXPENSE") 1 else 0

    // Optimistic selection: UI reacts on touch, not after state changes
    var pendingIndex by remember { mutableStateOf<Int?>(null) }
    var isDragging by remember { mutableStateOf(false) }
    var hoverIndex by remember { mutableIntStateOf(-1) }
    var dragPos by remember { mutableFloatStateOf(activeIndex.toFloat()) }
    val indicator = remember { Animatable(activeIndex.toFloat()) }

    LaunchedEffect(activeIndex) { pendingIndex = null }

    val selectedIndex =
        if (isDragging && hoverIndex >= 0) hoverIndex else pendingIndex ?: activeIndex

    val currentSelectedIndex by rememberUpdatedState(selectedIndex)
    val currentActiveIndex by rememberUpdatedState(activeIndex)
    val currentOnTypeSelected by rememberUpdatedState(onTypeSelected)

    fun select(index: Int) {
        val newType = if (index == 1) "EXPENSE" else "INCOME"
        if (index == currentActiveIndex) {
            pendingIndex = null
            return
        }
        pendingIndex = index
        currentOnTypeSelected(newType)
    }

    // Animate indicator toward the selection whenever we aren't dragging.
    LaunchedEffect(selectedIndex, isDragging) {
        if (!isDragging) {
            indicator.animateTo(
                selectedIndex.toFloat(),
                spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessMedium)
            )
        }
    }

    val indicatorScale by animateFloatAsState(
        targetValue = if (isDragging) 1.06f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "TypeIndicatorScale"
    )
    val indicatorElevation by animateDpAsState(
        targetValue = if (isDragging) 6.dp else 2.dp,
        animationSpec = tween(150),
        label = "TypeIndicatorElevation"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(CircleShape)
            .alpha(if (enabled) 1f else 0.6f)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(4.dp)
    ) {
        val density = LocalDensity.current
        val itemWidth = maxWidth / 2f
        val itemWidthPx = remember(itemWidth, density) {
            with(density) { itemWidth.toPx() }
        }

        // Sliding selection pill background (moved via graphicsLayer so it never triggers relayout)
        Box(
            modifier = Modifier
                .width(itemWidth)
                .fillMaxHeight()
                .graphicsLayer {
                    translationX = itemWidthPx * (if (isDragging) dragPos else indicator.value)
                    scaleX = indicatorScale
                    scaleY = indicatorScale
                }
                .shadow(
                    elevation = indicatorElevation,
                    shape = CircleShape,
                    clip = false
                )
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = CircleShape
                )
        )

        val isIncomeSelected = selectedIndex == 0
        val isExpenseSelected = selectedIndex == 1

        // Option labels row
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .semantics(mergeDescendants = true) {
                        role = Role.Tab
                        selected = isIncomeSelected
                        onClick(label = "Cash In (+)") {
                            if (enabled) {
                                presets.ping()
                                select(0)
                            }
                            true
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Cash In (+)",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isIncomeSelected) MaterialTheme.colorScheme.onSurface else Color(0xFF1FB47B)
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .semantics(mergeDescendants = true) {
                        role = Role.Tab
                        selected = isExpenseSelected
                        onClick(label = "Cash Out (-)") {
                            if (enabled) {
                                presets.ping()
                                select(1)
                            }
                            true
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Cash Out (-)",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isExpenseSelected) MaterialTheme.colorScheme.onSurface else Color(0xFFEF4444)
                )
            }
        }

        // Gesture Overlay: Handles tap and fluid hold/drag in one unified pointer input
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(enabled, itemWidthPx) {
                    if (!enabled) return@pointerInput
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val downIndex = (down.position.x / itemWidthPx).toInt().coerceIn(0, 1)

                        // 0 = ignore (cancelled / vertical scroll), 1 = tap, 2 = hold drag
                        var outcome = 2
                        withTimeoutOrNull(viewConfiguration.longPressTimeoutMillis) {
                            while (true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull { it.id == down.id }
                                if (change == null) {
                                    outcome = 0
                                    break
                                }
                                if (change.changedToUp()) {
                                    change.consume()
                                    outcome = 1
                                    break
                                }
                                val diff = change.position - down.position
                                if (diff.getDistance() > viewConfiguration.touchSlop) {
                                    if (downIndex == currentSelectedIndex && abs(diff.x) > abs(diff.y)) {
                                        outcome = 2
                                        break
                                    } else {
                                        outcome = 0
                                        break
                                    }
                                }
                            }
                        }

                        when (outcome) {
                            1 -> {
                                presets.ping()
                                select(downIndex)
                            }

                            2 -> if (downIndex == currentSelectedIndex) {
                                // Pick up the indicator
                                dragPos = indicator.value
                                hoverIndex = downIndex
                                isDragging = true
                                presets.plunk()

                                while (true) {
                                    val event = awaitPointerEvent()
                                    val change = event.changes.firstOrNull { it.id == down.id } ?: break
                                    change.consume()
                                    if (change.changedToUp() || !change.pressed) break

                                    val pos = ((change.position.x - itemWidthPx / 2f) / itemWidthPx)
                                        .coerceIn(0f, 1f)
                                    dragPos = pos
                                    val hover = pos.roundToInt()
                                    if (hover != hoverIndex) {
                                        hoverIndex = hover
                                        presets.ping()
                                    }
                                }

                                // Drop: hand off to the animatable from where the finger left it
                                val target = hoverIndex.coerceIn(0, 1)
                                val releasedAt = dragPos
                                scope.launch {
                                    indicator.snapTo(releasedAt)
                                    isDragging = false
                                    hoverIndex = -1
                                }
                                select(target)
                            }
                        }
                    }
                }
        )
    }
}
