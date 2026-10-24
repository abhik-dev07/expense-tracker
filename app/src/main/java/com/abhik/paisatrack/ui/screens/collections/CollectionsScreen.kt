package com.abhik.paisatrack.ui.screens.collections

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.abhik.paisatrack.R
import com.abhik.paisatrack.data.AuthManager
import com.abhik.paisatrack.ui.FinanceViewModel
import com.abhik.paisatrack.ui.components.commonUi.AddCollectionBottomSheet
import com.abhik.paisatrack.ui.components.dashboard.VisualSummaryHeader
import com.abhik.paisatrack.ui.screens.OfflineBanner
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import com.abhik.paisatrack.ui.screens.collections.components.CollectionsFilterButtonGroup
import com.abhik.paisatrack.ui.screens.collections.components.CollectionsPanel
import com.abhik.paisatrack.ui.screens.dashboard.components.DashboardBackToTopButton
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.lerp
import com.abhik.paisatrack.ui.theme.*
import com.abhik.paisatrack.ui.utils.findActivity
import com.abhik.paisatrack.ui.utils.getSafePresets
import com.swmansion.pulsar.Pulsar
import java.text.DecimalFormat
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CollectionsScreen(
    viewModel: FinanceViewModel,
    isOnline: Boolean = true,
    onNavigateToCollectionTransactions: (String, Rect?) -> Unit,
    onBack: () -> Unit,
    onNavigateToSearch: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onNavigateToAddTransaction: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddCollectionDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val pulsar = remember(context) { Pulsar(context.findActivity() ?: context) }
    val presets = remember(pulsar) { pulsar.getSafePresets() }
    val coroutineScope = rememberCoroutineScope()

    val userName = remember { AuthManager.getUserName(context) }
    val profilePicUrl = remember { AuthManager.getProfilePicUrl(context) }
    val dollarFormat = remember { DecimalFormat("₹#,##0.00") }

    val isDark = com.abhik.paisatrack.ui.theme.isAppInDarkTheme()
    val headerTextPrimary = if (isDark) HeaderTextPrimaryDark else HeaderTextPrimaryLight
    val scrollState = rememberScrollState()

    val entranceAlpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        entranceAlpha.animateTo(1f, animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing))
    }

    var isScrollingUp by remember { mutableStateOf(false) }
    var previousOffset by remember { mutableIntStateOf(0) }
    var accumulatedDelta by remember { mutableIntStateOf(0) }
    var isAutoScrollingToTop by remember { mutableStateOf(false) }

    LaunchedEffect(scrollState.value) {
        if (isAutoScrollingToTop) return@LaunchedEffect
        val currentOffset = scrollState.value
        val delta = currentOffset - previousOffset
        if (delta < 0) {
            if (accumulatedDelta > 0) accumulatedDelta = 0
            accumulatedDelta += delta
            if (accumulatedDelta < -20) {
                isScrollingUp = true
            }
        } else if (delta > 0) {
            if (accumulatedDelta < 0) accumulatedDelta = 0
            accumulatedDelta += delta
            if (accumulatedDelta > 20) {
                isScrollingUp = false
            }
        }
        previousOffset = currentOffset
    }

    val showBackToTop by remember {
        derivedStateOf {
            isScrollingUp && scrollState.value > 300
        }
    }

    val density = LocalDensity.current
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val defaultHeaderHeightPx = with(density) { (statusBarHeight + 62.dp).toPx() }
    var headerBarHeightPx by remember { mutableFloatStateOf(defaultHeaderHeightPx) }
    val defaultFilterInitialY = with(density) { (statusBarHeight + 72.dp + 190.dp).toPx() }
    var filterInitialY by remember { mutableFloatStateOf(defaultFilterInitialY) }
    var isFilterInitialYLocked by remember { mutableStateOf(false) }

    val isFilterSticky by remember {
        derivedStateOf {
            filterInitialY > 0f && headerBarHeightPx > 0f &&
                scrollState.value >= (filterInitialY - headerBarHeightPx)
        }
    }

    val activeColTab by viewModel.activeCollectionTab.collectAsStateWithLifecycle()
    val collapseRange = with(density) { 100.dp.toPx() }
    val collapseProgress = (scrollState.value.toFloat() / collapseRange).coerceIn(0f, 1f)
    val pageBgColor = if (isDark) Color(0xFF202027) else Color(0xFFFFFFFF)

    val headerGradient = Brush.verticalGradient(
        colorStops = if (isDark) arrayOf(
            0.0f to lerp(HeaderGradient0Dark, pageBgColor, collapseProgress),
            0.18f to lerp(HeaderGradient18Dark, pageBgColor, collapseProgress),
            0.38f to lerp(HeaderGradient38Dark, pageBgColor, collapseProgress),
            0.58f to lerp(HeaderGradient58Dark, pageBgColor, collapseProgress),
            0.75f to lerp(HeaderGradient75Dark, pageBgColor, collapseProgress),
            0.90f to lerp(HeaderGradient90Dark, pageBgColor, collapseProgress),
            1.0f to lerp(HeaderGradient100Dark, pageBgColor, collapseProgress)
        ) else arrayOf(
            0.0f to lerp(HeaderGradient0Light, pageBgColor, collapseProgress),
            0.18f to lerp(HeaderGradient18Light, pageBgColor, collapseProgress),
            0.38f to lerp(HeaderGradient38Light, pageBgColor, collapseProgress),
            0.58f to lerp(HeaderGradient58Light, pageBgColor, collapseProgress),
            0.75f to lerp(HeaderGradient75Light, pageBgColor, collapseProgress),
            0.90f to lerp(HeaderGradient90Light, pageBgColor, collapseProgress),
            1.0f to lerp(HeaderGradient100Light, pageBgColor, collapseProgress)
        )
    )

    Surface(
        modifier = modifier
            .fillMaxSize()
            .alpha(entranceAlpha.value),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Main scrollable content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                // Header spacer (status bar + 72dp)
                val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
                Spacer(modifier = Modifier.height(72.dp + statusBarHeight))

                // Floating offline banner
                OfflineBanner(
                    isVisible = !isOnline,
                    modifier = Modifier
                        .fillMaxWidth()
                        .zIndex(10f)
                )

                // Visual summary balance card
                VisualSummaryHeader(uiState = uiState, dollarFormat = dollarFormat)

                // Dedicated Collections panel
                CollectionsPanel(
                    uiState = uiState,
                    viewModel = viewModel,
                    dollarFormat = dollarFormat,
                    onScrollProgressChanged = {},
                    onCollectionClick = onNavigateToCollectionTransactions,
                    onBackToTop = { scrollAction ->
                        coroutineScope.launch {
                            try {
                                scrollAction()
                            } catch (_: Exception) {}
                        }
                    },
                    isFilterSticky = isFilterSticky,
                    onFilterPositioned = { rootY ->
                        if (!isFilterInitialYLocked) {
                            val initialY = rootY + scrollState.value
                            if (initialY > 100f) {
                                filterInitialY = initialY
                                if (scrollState.value == 0) {
                                    isFilterInitialYLocked = true
                                }
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.navigationBarsPadding().height(100.dp))
            }

            // Top Header Bar with Navigation Back
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .zIndex(20f)
                    .background(headerGradient)
                    .onGloballyPositioned { coordinates ->
                        headerBarHeightPx = coordinates.boundsInRoot().bottom
                    }
                    .statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 8.dp, bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        Text(
                            text = "Collections & Vaults",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = headerTextPrimary
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        // Search button
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isDark) Color.Black.copy(alpha = 0.25f)
                                    else Color.White.copy(alpha = 0.8f)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isDark) Color.White.copy(alpha = 0.15f)
                                    else Color.Black.copy(alpha = 0.1f),
                                    shape = CircleShape
                                )
                                .clickable {
                                    presets.plunk()
                                    onNavigateToSearch()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_search),
                                contentDescription = "Search",
                                tint = if (isDark) Color.White else Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Profile section
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(MaterialShapes.Clover8Leaf.toShape())
                                .clickable {
                                    onProfileClick()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (!profilePicUrl.isNullOrEmpty()) {
                                AsyncImage(
                                    model = profilePicUrl,
                                    contentDescription = "Profile Picture",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text(
                                    text = userName.take(1).uppercase(),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) HeaderTextPrimaryDark else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // Fixed sticky filter button group beneath top header bar
            AnimatedVisibility(
                visible = isFilterSticky,
                enter = fadeIn(tween(150)),
                exit = fadeOut(tween(100)),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .zIndex(15f)
                    .padding(top = with(density) { headerBarHeightPx.toDp() })
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(pageBgColor)
                            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 6.dp)
                    ) {
                        CollectionsFilterButtonGroup(
                            activeColTab = activeColTab,
                            onTabSelected = { option ->
                                viewModel.setActiveCollectionTab(option)
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        pageBgColor,
                                        pageBgColor.copy(alpha = 0.9f),
                                        pageBgColor.copy(alpha = 0.5f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                }
            }

            // Floating Back to Top Button
            val backToTopTopPadding = with(density) {
                (headerBarHeightPx + (if (isFilterSticky) 60.dp.toPx() else 0f)).toDp() + 10.dp
            }

            DashboardBackToTopButton(
                visible = showBackToTop,
                isDark = isDark,
                onClick = {
                    presets.ping()
                    isScrollingUp = false
                    coroutineScope.launch {
                        isAutoScrollingToTop = true
                        try {
                            scrollState.animateScrollTo(0)
                        } finally {
                            isAutoScrollingToTop = false
                        }
                    }
                },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .zIndex(25f)
                    .padding(top = backToTopTopPadding)
            )

        }
    }

    if (showAddCollectionDialog) {
        AddCollectionBottomSheet(
            onDismiss = { showAddCollectionDialog = false },
            onConfirm = { name, color, icon, budget ->
                viewModel.addCollection(name, color, icon, budget)
                showAddCollectionDialog = false
            }
        )
    }
}
