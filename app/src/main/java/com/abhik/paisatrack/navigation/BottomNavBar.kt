package com.abhik.paisatrack.navigation

import androidx.annotation.DrawableRes
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhik.paisatrack.R
import com.abhik.paisatrack.utils.findActivity
import com.abhik.paisatrack.utils.getSafePresets
import com.swmansion.pulsar.Pulsar
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private data class TabSpec(
    val label: String,
    @param:DrawableRes val icon: Int,
    @param:DrawableRes val activeIcon: Int
)

private val Tabs = listOf(
    TabSpec("Home", R.drawable.home, R.drawable.home_active),
    TabSpec("Analysis", R.drawable.analysis, R.drawable.analysis_active),
    TabSpec("Collection", R.drawable.folder, R.drawable.folder_active)
)

/**
 * Glassmorphic bottom bar.
 * - Tap: indicator moves instantly (optimistic), navigation follows.
 * - Long-press the indicator, drag across tabs, release: lands on that tab and navigates.
 */
@Composable
fun BottomNavBar(
    activeTab: String,
    onTabSelected: (String) -> Unit,
    showPlusMenu: Boolean,
    onPlusClick: () -> Unit,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val pulsar = remember(context) { Pulsar(context.findActivity() ?: context) }
    val presets = remember(pulsar) { pulsar.getSafePresets() }
    val scope = rememberCoroutineScope()

    val activeIndex = when (activeTab) {
        "Home", "Dashboard", "Transactions" -> 0
        "Analysis", "Insights" -> 1
        "Collection", "Collections" -> 2
        else -> 0
    }

    // Optimistic selection: UI reacts on touch, not after the route recomposes.
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
    val currentOnTabSelected by rememberUpdatedState(onTabSelected)

    fun select(index: Int) {
        if (index == currentActiveIndex) {
            pendingIndex = null
            return
        }
        pendingIndex = index
        currentOnTabSelected(Tabs[index].label)
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
        targetValue = if (isDragging) 1.08f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "IndicatorScale"
    )
    val indicatorAlpha by animateFloatAsState(
        targetValue = if (isDragging) 0.20f else 0.12f,
        animationSpec = tween(150),
        label = "IndicatorAlpha"
    )

    val plusRotation by animateFloatAsState(
        targetValue = if (showPlusMenu) 135f else 0f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "PlusIconRotation"
    )
    val plusPainter = painterResource(id = R.drawable.ic_plus_custom)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp, start = 16.dp, end = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .height(72.dp)
                .background(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = if (isDark) 0.98f else 0.90f),
                    shape = CircleShape
                )
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = if (isDark) 0.40f else 0.25f),
                    shape = CircleShape
                )
                .padding(6.dp)
        ) {
            val tabWidth = maxWidth / 3
            val tabWidthPx = constraints.maxWidth / 3f

            // Indicator: moved via graphicsLayer so it never triggers relayout.
            Box(
                modifier = Modifier
                    .width(tabWidth)
                    .fillMaxHeight()
                    .graphicsLayer {
                        translationX = tabWidthPx * (if (isDragging) dragPos else indicator.value)
                        scaleX = indicatorScale
                        scaleY = indicatorScale
                    }
                    .background(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = indicatorAlpha),
                        shape = CircleShape
                    )
            )

            val navActiveTint = if (isDark) Color(0xFF15915F) else Color(0xFF1FB47B)
            val navInactiveTint = if (isDark) Color(0xFFF5F5F5) else Color(0xFF000000)

            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Tabs.forEachIndexed { index, tab ->
                    val isSelected = index == selectedIndex
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .semantics(mergeDescendants = true) {
                                role = Role.Tab
                                selected = isSelected
                                onClick(label = tab.label) { select(index); true }
                            },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Crossfade(
                            targetState = isSelected,
                            animationSpec = tween(durationMillis = 150),
                            label = "${tab.label}IconCrossfade"
                        ) { active ->
                            Icon(
                                painter = painterResource(id = if (active) tab.activeIcon else tab.icon),
                                contentDescription = null,
                                tint = if (active) navActiveTint else navInactiveTint,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = tab.label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            }
                        )
                    }
                }
            }

            // Gesture overlay: handles tap and long-press-drag in one place so they can't conflict.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(tabWidthPx) {
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            val downIndex = (down.position.x / tabWidthPx).toInt().coerceIn(0, 2)

                            // 0 = ignore (moved / cancelled), 1 = tap, 2 = long press
                            var outcome = 2
                            withTimeoutOrNull(viewConfiguration.longPressTimeoutMillis) {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    val change = event.changes.firstOrNull { it.id == down.id }
                                    if (change == null) { outcome = 0; break }
                                    if (change.changedToUp()) { change.consume(); outcome = 1; break }
                                    if ((change.position - down.position).getDistance() > viewConfiguration.touchSlop) {
                                        outcome = 0; break
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

                                        val pos = ((change.position.x - tabWidthPx / 2f) / tabWidthPx)
                                            .coerceIn(0f, (Tabs.size - 1).toFloat())
                                        dragPos = pos
                                        val hover = pos.roundToInt()
                                        if (hover != hoverIndex) {
                                            hoverIndex = hover
                                            presets.ping()
                                        }
                                    }

                                    // Drop: hand off to the animatable from where the finger left it
                                    val target = hoverIndex.coerceIn(0, Tabs.size - 1)
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

        val plusInteraction = remember { MutableInteractionSource() }
        val plusPressed by plusInteraction.collectIsPressedAsState()
        val plusAlpha by animateFloatAsState(
            targetValue = if (plusPressed) 0.72f else 1f,
            animationSpec = tween(durationMillis = 120),
            label = "PlusButtonPressAlpha"
        )

        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .alpha(plusAlpha)
                .background(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = if (isDark) 0.98f else 0.90f),
                    shape = CircleShape
                )
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = if (isDark) 0.40f else 0.25f),
                    shape = CircleShape
                )
                .clickable(
                    interactionSource = plusInteraction,
                    indication = ripple(bounded = true, radius = 36.dp)
                ) {
                    presets.plunk()
                    onPlusClick()
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = plusPainter,
                contentDescription = "Add Menu",
                tint = if (isDark) Color(0xFFF2F2F2) else Color(0xFF000000),
                modifier = Modifier
                    .size(28.dp)
                    .rotate(plusRotation)
            )
        }
    }
}