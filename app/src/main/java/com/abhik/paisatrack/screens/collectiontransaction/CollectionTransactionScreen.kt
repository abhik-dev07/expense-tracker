package com.abhik.paisatrack.screens.collectiontransaction
import com.abhik.paisatrack.screens.collectiontransaction.components.*
import com.abhik.paisatrack.ui.components.commonUi.EditCollectionBottomSheet

import android.widget.Toast
import androidx.compose.material3.Icon
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.ExperimentalFoundationApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.foundation.layout.*
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import com.abhik.paisatrack.utils.getIconByName
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalContext
import com.abhik.paisatrack.R
import com.abhik.paisatrack.data.model.TransactionEntity
import com.abhik.paisatrack.utils.FinanceViewModel
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import java.text.DecimalFormat
import com.abhik.paisatrack.ui.components.commonUi.DeleteTransactionConfirmDialog
import com.abhik.paisatrack.ui.components.commonUi.FilterBottomSheet
import com.abhik.paisatrack.screens.dashboard.components.TransactionSkeletonItem
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import com.abhik.paisatrack.ui.components.commonUi.TransactionDetailBottomSheet
import androidx.compose.ui.zIndex
import com.abhik.paisatrack.ui.components.commonUi.OfflineBanner
import com.abhik.paisatrack.ui.components.commonUi.EditTransactionBottomSheet
import com.swmansion.pulsar.Pulsar
import com.abhik.paisatrack.utils.safeParseColor
import com.abhik.paisatrack.utils.findActivity
import com.abhik.paisatrack.utils.getSafePresets
import com.abhik.paisatrack.ui.theme.*


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CollectionTransactionsScreen(
    viewModel: FinanceViewModel,
    collectionId: String,
    onNavigateToAddTransaction: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    isOnline: Boolean = true
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var visibleLimit by rememberSaveable { mutableStateOf(20) }
    var animationStartLimit by rememberSaveable { mutableStateOf(0) }
    var isLoadingMore by remember { mutableStateOf(false) }

    val density = LocalDensity.current
    val headerHeight = 220.dp
    val headerHeightPx = remember(density) { with(density) { headerHeight.toPx() } }
    var scrollOffsetPx by rememberSaveable { mutableStateOf(0f) }

    LaunchedEffect(collectionId, uiState.collActiveTimeFilter, uiState.collActiveTypeFilter, uiState.collActiveSortOrder) {
        visibleLimit = 20
        animationStartLimit = 0
        scrollOffsetPx = 0f
    }
    
    // Find our active collection and summary
    val summary = remember(uiState.collectionSummaries, collectionId) {
        uiState.collectionSummaries.find { it.collection.id == collectionId }
    }
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.nothing))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )
    
    val collection = summary?.collection
    val transactions = remember(uiState.rawTransactions, collectionId, uiState.collActiveTimeFilter, uiState.collActiveTypeFilter, uiState.collActiveSortOrder) {
        val now = System.currentTimeMillis()
        val filter = uiState.collActiveTimeFilter
        val typeFilter = uiState.collActiveTypeFilter.uppercase()
        val sortOrder = uiState.collActiveSortOrder
        
        val cal2 = java.util.Calendar.getInstance().apply { timeInMillis = now }
        val todayYear = cal2.get(java.util.Calendar.YEAR)
        val todayDay = cal2.get(java.util.Calendar.DAY_OF_YEAR)
        val weekYear = cal2.get(java.util.Calendar.YEAR)
        val weekOfYear = cal2.get(java.util.Calendar.WEEK_OF_YEAR)
        val monthYear = cal2.get(java.util.Calendar.YEAR)
        val month = cal2.get(java.util.Calendar.MONTH)

        val filtered = uiState.rawTransactions.filter { tx ->
            val matchesCollection = tx.collectionId == collectionId
            val matchesType = typeFilter == "ALL" || tx.type.uppercase() == typeFilter
            
            val matchesTime = when (filter) {
                "Today" -> {
                    val cal1 = java.util.Calendar.getInstance().apply { timeInMillis = tx.timestamp }
                    cal1.get(java.util.Calendar.YEAR) == todayYear &&
                            cal1.get(java.util.Calendar.DAY_OF_YEAR) == todayDay
                }
                "This Week" -> {
                    val cal1 = java.util.Calendar.getInstance().apply { timeInMillis = tx.timestamp }
                    cal1.get(java.util.Calendar.YEAR) == weekYear &&
                            cal1.get(java.util.Calendar.WEEK_OF_YEAR) == weekOfYear
                }
                "This Month" -> {
                    val cal1 = java.util.Calendar.getInstance().apply { timeInMillis = tx.timestamp }
                    cal1.get(java.util.Calendar.YEAR) == monthYear &&
                            cal1.get(java.util.Calendar.MONTH) == month
                }
                else -> true
            }
            matchesCollection && matchesType && matchesTime
        }

        if (sortOrder == "Newest") {
            filtered.sortedByDescending { it.timestamp }
        } else {
            filtered.sortedBy { it.timestamp }
        }
    }

    val paginatedTransactions = remember(transactions, visibleLimit) {
        transactions.take(visibleLimit)
    }
    
    val dollarFormat = remember { DecimalFormat("₹#,##0.00") }
    val isDark = com.abhik.paisatrack.ui.theme.isAppInDarkTheme()
    var showDetailDialog by remember { mutableStateOf(false) }
    var txToDelete by remember { mutableStateOf<TransactionEntity?>(null) }
    var txDetailToShow by remember { mutableStateOf<TransactionEntity?>(null) }
    var transactionToEdit by remember { mutableStateOf<TransactionEntity?>(null) }
    var txDetailUpdateTrigger by remember { mutableLongStateOf(0L) }
    var showEditCollectionModal by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    var showFilters by remember { mutableStateOf(false) }
    val isAnyFilterActive = remember(uiState.collActiveTimeFilter, uiState.collActiveTypeFilter, uiState.collActiveSortOrder) {
        uiState.collActiveTimeFilter != "All" || uiState.collActiveTypeFilter != "All" || uiState.collActiveSortOrder != "Newest"
    }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var isFilterShimmering by remember { mutableStateOf(false) }

    var isScrolling by remember { mutableStateOf(false) }

    val nestedScrollConnection = remember(headerHeightPx) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                if (source == NestedScrollSource.UserInput) {
                    if (delta < -12f) {
                        isScrolling = true
                    } else if (delta > 12f) {
                        isScrolling = false
                    }
                }
                
                // When scrolling down the page (delta < 0) and header is not fully collapsed
                if (delta < 0 && scrollOffsetPx > -headerHeightPx) {
                    val newOffset = scrollOffsetPx + delta
                    scrollOffsetPx = newOffset.coerceIn(-headerHeightPx, 0f)
                    return Offset(0f, delta)
                }
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                val delta = available.y
                // When scrolling up towards top (delta > 0) and header is collapsed
                if (delta > 0 && scrollOffsetPx < 0f) {
                    val newOffset = scrollOffsetPx + delta
                    scrollOffsetPx = newOffset.coerceIn(-headerHeightPx, 0f)
                    return Offset(0f, delta)
                }
                return Offset.Zero
            }
        }
    }

    var isScrollingUp by remember { mutableStateOf(false) }
    var isAutoScrollingToTop by remember { mutableStateOf(false) }

    LaunchedEffect(scrollState) {
        var previousOffset = scrollState.value
        var accumulatedDelta = 0
        snapshotFlow { scrollState.value }
            .collect { currentOffset ->
                val delta = currentOffset - previousOffset
                if (isAutoScrollingToTop) {
                    isScrollingUp = false
                    accumulatedDelta = 0
                } else if (currentOffset <= 300) {
                    isScrollingUp = false
                    accumulatedDelta = 0
                } else if (delta < 0) {
                    // Scrolling up (offset decreasing towards top)
                    if (accumulatedDelta > 0) accumulatedDelta = 0
                    accumulatedDelta += delta
                    if (accumulatedDelta < -20) {
                        isScrollingUp = true
                    }
                } else if (delta > 0) {
                    // Scrolling down (offset increasing)
                    if (accumulatedDelta < 0) accumulatedDelta = 0
                    accumulatedDelta += delta
                    if (accumulatedDelta > 20) {
                        isScrollingUp = false
                    }
                }
                previousOffset = currentOffset
            }
    }

    val showBackToTop by remember {
        derivedStateOf {
            isScrollingUp && scrollState.value > 300
        }
    }

    

    val colColor = remember(collection?.hexColor) {
        safeParseColor(collection?.hexColor, fallback = Color(0xFF3F51B5))
    }

    val view = LocalView.current
    val context = LocalContext.current
    val pulsar = remember(context) { Pulsar(context.findActivity() ?: context) }
    val presets = remember(pulsar) { pulsar.getSafePresets() }
    @Suppress("DEPRECATION")
    DisposableEffect(isDark) {
        val activity = context as? android.app.Activity
        val window = activity?.window
        if (window != null) {
            val originalStatusColor = window.statusBarColor
            val originalAppearance = androidx.core.view.WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars

            // Use transparent status bar to let the gradient background shine through
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            androidx.core.view.WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDark

            onDispose {
                window.statusBarColor = originalStatusColor
                androidx.core.view.WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = originalAppearance
            }
        } else {
            onDispose {}
        }
    }

    val isAtTop by remember {
        derivedStateOf {
            scrollState.value <= 10
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            if (collection != null) {
                ExtendedFloatingActionButton(
                    onClick = {
                        presets.plunk()
                        onNavigateToAddTransaction(collection.id)
                    },
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_plus_custom),
                            tint = if (isDark) Color.Black else Color.White,
                            contentDescription = "Add Transaction",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    text = {
                        Text(
                            text = "Add Record",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (isDark) Color.Black else Color.White
                        )
                    },
                    expanded = isAtTop,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = CircleShape,
                    modifier = Modifier.padding(bottom = 12.dp, end = 8.dp)
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Fixed Header Layer
            val isHeaderScrolled by remember { derivedStateOf { scrollState.value > 10 } }
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .zIndex(1f)
                    .background(
                        if (isHeaderScrolled) {
                            Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.background,
                                    MaterialTheme.colorScheme.background.copy(alpha = 0.9f),
                                    MaterialTheme.colorScheme.background.copy(alpha = 0.5f),
                                    Color.Transparent
                                )
                            )
                        } else {
                            Brush.verticalGradient(
                                colors = listOf(
                                    colColor.copy(alpha = if (isDark) 0.18f else 0.12f),
                                    colColor.copy(alpha = if (isDark) 0.08f else 0.05f),
                                    Color.Transparent
                                )
                            )
                        }
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top = innerPadding.calculateTopPadding() + 8.dp,
                            bottom = 24.dp
                        )
                ) {
                    // Navigation Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Circular back button
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (isDark) MaterialTheme.colorScheme.surfaceVariant else Color.White)
                                .border(
                                    BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.1f)),
                                    CircleShape
                                )
                                .clickable { onBack() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_chevron_back),
                                contentDescription = "Back to dashboard",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Start-aligned Column for Title & Subtitle inside Pill + Filter Icon
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(CircleShape)
                                .background(if (isDark) MaterialTheme.colorScheme.surfaceVariant else Color.White)
                                .border(
                                    BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.1f)),
                                    CircleShape
                                )
                                .padding(start = 16.dp, end = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.Start,
                                verticalArrangement = Arrangement.spacedBy((-4).dp)
                            ) {
                                Text(
                                    text = (collection?.name ?: "Collection details").let { if (it.length > 18) it.take(18) + "..." else it },
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${transactions.size} records",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                            }
                            
                            // Vertical Divider
                            Box(
                                modifier = Modifier
                                    .height(24.dp)
                                    .width(1.dp)
                                    .background(if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.1f))
                            )
                            
                            Spacer(modifier = Modifier.width(6.dp))
                            
                            // Filter Button
                            val isFilterActive = isAnyFilterActive || showFilters
                            val filterTint = if (isFilterActive) {
                                if (isDark) Color(0xFFFFFFFF) else Color(0xFF1FB47B)
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }

                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .clickable { 
                                        presets.plunk()
                                        showFilters = !showFilters
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = if (isFilterActive) R.drawable.ic_filter_active else R.drawable.ic_filter),
                                    contentDescription = "Filter",
                                    tint = filterTint,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Search Button (Swapped with Three Dots)
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (isDark) MaterialTheme.colorScheme.surfaceVariant else Color.White)
                                .border(
                                    BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.1f)),
                                    CircleShape
                                )
                                .clickable {
                                    presets.plunk()
                                    // search action can be hooked up here
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_search),
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Edit Button (Three dots - Swapped with Search)
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (isDark) MaterialTheme.colorScheme.surfaceVariant else Color.White)
                                .border(
                                    BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.1f)),
                                    CircleShape
                                )
                                .clickable {
                                    presets.plunk()
                                    showEditCollectionModal = true
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreHoriz,
                                contentDescription = "Edit Collection Settings",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Floating Offline Banner
                    OfflineBanner(
                        isVisible = !isOnline,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }
            }

            // Scrollable Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                Spacer(modifier = Modifier.height(innerPadding.calculateTopPadding() + 80.dp))

                // 2. Collapsible Balance Card container
                if (summary != null) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val netBalance = summary.totalIncome - summary.totalExpense
                        
                        var hasAnimated by rememberSaveable { mutableStateOf(false) }
                        val alphaAnim = remember { Animatable(if (hasAnimated) 1f else 0f) }

                        LaunchedEffect(uiState.isLoading) {
                            if (!uiState.isLoading) {
                                if (!hasAnimated) {
                                    launch {
                                        alphaAnim.animateTo(
                                            targetValue = 1f,
                                            animationSpec = tween(durationMillis = 1000, easing = LinearOutSlowInEasing)
                                        )
                                    }
                                    hasAnimated = true
                                } else {
                                    alphaAnim.snapTo(1f)
                                }
                            }
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .clickable {
                                    presets.ping()
                                    showDetailDialog = true
                                },
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isDark) VsBgDark else VsBgLight
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp)
                                    .alpha(alphaAnim.value),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // "Total Balance" Label
                                Text(
                                    text = "Collection Overview",
                                    fontSize = 13.sp,
                                    color = if (isDark) VsTextDark.copy(alpha = 0.8f) else VsTextLight.copy(alpha = 0.8f),
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                // Giant Net Balance Center text
                                com.abhik.paisatrack.ui.components.commonUi.AnimatedAmountCounter(
                                    amount = netBalance,
                                    textStyle = LocalTextStyle.current.copy(
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = if (isDark) VsTextDark else VsTextLight,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                // Side-by-side Sub cards inside row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Income Card Item (Left)
                                    Card(
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isDark) VsContainerDark else VsContainerLight
                                        ),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                                        border = androidx.compose.foundation.BorderStroke(
                                            width = 1.dp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 12.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            // Soft clean Green arrow circle using Hex #B7DAAE
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isDark) Color(0xFFB7DAAE).copy(alpha = 0.2f) else Color(0xFFB7DAAE)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    painter = painterResource(id = R.drawable.ic_arrow_up),
                                                    contentDescription = "Income icon",
                                                    tint = if (isDark) Color(0xFFB7DAAE) else Color(0xFF1F4D20),
                                                    modifier = Modifier
                                                        .size(18.dp)
                                                )
                                            }

                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "Cash In",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                                    fontWeight = FontWeight.Medium
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                com.abhik.paisatrack.ui.components.commonUi.AnimatedAmountCounter(
                                                    amount = summary.totalIncome,
                                                    textStyle = LocalTextStyle.current.copy(
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Bold
                                                    ),
                                                    color = MaterialTheme.colorScheme.onBackground
                                                )
                                            }
                                        }
                                    }

                                    // Expense Card Item (Right)
                                    Card(
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isDark) VsContainerDark else VsContainerLight
                                        ),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                                        border = androidx.compose.foundation.BorderStroke(
                                            width = 1.dp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 12.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            // Soft clean Red arrow circle using Hex #FFB8A9
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isDark) Color(0xFFFFB8A9).copy(alpha = 0.2f) else Color(0xFFFFB8A9)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    painter = painterResource(id = R.drawable.ic_arrow_down),
                                                    contentDescription = "Expense icon",
                                                    tint = if (isDark) Color(0xFFFFB8A9) else Color(0xFF6E261A),
                                                    modifier = Modifier
                                                        .size(18.dp)
                                                )
                                            }

                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "Cash Out",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                                    fontWeight = FontWeight.Medium
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                com.abhik.paisatrack.ui.components.commonUi.AnimatedAmountCounter(
                                                    amount = summary.totalExpense,
                                                    textStyle = LocalTextStyle.current.copy(
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Bold
                                                    ),
                                                    color = MaterialTheme.colorScheme.onBackground
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

            // 3. Scrollable Content
            Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 80.dp)
                ) {
                    // Header: Recent Transactions Title and Filter triggers
                    // Header: Recent Transactions Title
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recent Activity",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    // Transactions list items or empty state item
                    if (isFilterShimmering) {
                        repeat(5) {
                            TransactionSkeletonItem()
                        }
                    } else if (transactions.isEmpty()) {
                        Box {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 32.dp, end = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    LottieAnimation(
                                        composition = composition,
                                        progress = { progress },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(200.dp)
                                    )
                                    Text(
                                        text = "No Records Formed",
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Text(
                                        text = "Any records registered with this specific collection will show up here nicely.",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    } else {
                        for ((index, tx) in paginatedTransactions.withIndex()) {
                            key(tx.id) {
                                val alpha = remember(tx.id) { Animatable(if (index >= animationStartLimit) 0f else 1f) }
                                LaunchedEffect(tx.id) {
                                    if (index >= animationStartLimit) {
                                        delay((index - animationStartLimit) * 25L)
                                        alpha.animateTo(1f, tween(150))
                                    }
                                }
                                Box(modifier = Modifier.alpha(alpha.value)) {
                                    TransactionListItemDetailed(
                                        transaction = tx,
                                        colColor = colColor,
                                        dollarFormat = dollarFormat,
                                        onDeleteClick = {
                                            txToDelete = tx
                                        },
                                        onLongClick = {
                                            presets.bassDrop()
                                            txToDelete = tx
                                        },
                                        onClick = {
                                            presets.boulder()
                                            txDetailToShow = tx
                                        }
                                    )
                                }
                            }
                        }
                        if (transactions.size > visibleLimit) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isLoadingMore) {
                                    ContainedLoadingIndicator(
                                        modifier = Modifier.size(48.dp)
                                    )
                                } else {
                                    FilledTonalButton(
                                        onClick = {
                                            presets.boulder()
                                            isLoadingMore = true
                                            scope.launch {
                                                kotlinx.coroutines.delay(800)
                                                animationStartLimit = visibleLimit
                                                visibleLimit += 20
                                                isLoadingMore = false
                                            }
                                        },
                                        shape = CircleShape,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp)
                                    ) {
                                        Text(
                                            text = "Load More (${transactions.size - visibleLimit} remaining)",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            androidx.compose.animation.AnimatedVisibility(
                    visible = showBackToTop,
                    enter = fadeIn() + slideInVertically { it / 2 },
                    exit = fadeOut() + slideOutVertically { it / 2 },
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .zIndex(2f)
                        .padding(top = innerPadding.calculateTopPadding() + 85.dp)
                ) {
                    FilledTonalButton(
                        onClick = {
                            presets.ping()
                            isScrollingUp = false
                            scope.launch {
                                isAutoScrollingToTop = true
                                try {
                                    scrollOffsetPx = 0f
                                    scrollState.animateScrollTo(0)
                                } finally {
                                    isAutoScrollingToTop = false
                                }
                            }
                        },
                        elevation = ButtonDefaults.filledTonalButtonElevation(defaultElevation = 6.dp),
                        shape = CircleShape,
                        modifier = Modifier
                            .height(40.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_up_arrow),
                            contentDescription = "Back to Top",
                            modifier = Modifier.size(16.dp),
                            tint = if (isDark) androidx.compose.ui.graphics.Color.White else androidx.compose.ui.graphics.Color.Black
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Back to Top", 
                            fontSize = 12.sp, 
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) androidx.compose.ui.graphics.Color.White else androidx.compose.ui.graphics.Color.Black
                        )
                    }
                }
            }
        }

    // Wide Category Balance Detail Dialog
    if (showDetailDialog && summary != null) {
        val netBalance = summary.totalIncome - summary.totalExpense
        val netStr = dollarFormat.format(netBalance)
        val incomeStr = dollarFormat.format(summary.totalIncome)
        val expenseStr = dollarFormat.format(summary.totalExpense)
        val colName = collection?.name ?: "Category Details"

        Dialog(
            onDismissRequest = { showDetailDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .padding(vertical = 16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "$colName Overview",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f))

                    // Detail Row 1: Total Net Balance
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Net Amount",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = netStr,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (netBalance >= 0) Color(0xFF10B981) else Color(0xFFEF4444),
                            textAlign = TextAlign.Center
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f))

                    // Detail Row 2: Total Income
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total Cash In",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = incomeStr,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color(0xFFB7DAAE) else Color(0xFF1F4D20)
                        )
                    }

                    // Detail Row 3: Total Expense
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total Cash Out",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = expenseStr,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color(0xFFFFB8A9) else Color(0xFF6E261A)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    FilledTonalButton(
                        onClick = {
                            presets.boulder()
                            showDetailDialog = false
                        },
                        shape = CircleShape,
                        modifier = Modifier.fillMaxWidth().height(52.dp)
                    ) {
                        Text(
                            text = "Got it",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }

    // Wide Delete Confirmation Dialog
    if (txToDelete != null) {
        val tx = txToDelete!!
        DeleteTransactionConfirmDialog(
            transaction = tx,
            dollarFormat = dollarFormat,
            onDismiss = { txToDelete = null },
            onConfirm = {
                viewModel.deleteTransaction(tx)
                Toast.makeText(context, "Record deleted successfully", Toast.LENGTH_SHORT)
                    .show()
                txToDelete = null
            }
        )
    }

    // Transaction Detail Bottom Sheet
    if (txDetailToShow != null) {
        val tx = txDetailToShow!!
        val parentCol = uiState.collections.find { it.id == tx.collectionId } ?: collection
        val colName = parentCol?.name ?: "General"
        val resolvedColColor = parentCol?.let { safeParseColor(it.hexColor) } ?: colColor
        val resolvedColIcon = getIconByName(parentCol?.iconName ?: "category")

        TransactionDetailBottomSheet(
            transaction = tx,
            collectionName = colName,
            collectionColor = resolvedColColor,
            collectionIcon = resolvedColIcon,
            dollarFormat = dollarFormat,
            updateTrigger = txDetailUpdateTrigger,
            onDismiss = { txDetailToShow = null },
            onEditClick = {
                transactionToEdit = tx
            }
        )
    }

    // Shared Filter Bottom Sheet
    if (showFilters) {
        FilterBottomSheet(
            sheetState = sheetState,
            activeSortOrder = uiState.collActiveSortOrder,
            activeTypeFilter = uiState.collActiveTypeFilter,
            activeTimeFilter = uiState.collActiveTimeFilter,
            onApplyFilters = { sort, type, time ->
                val hasChanged = sort != uiState.collActiveSortOrder ||
                    type != uiState.collActiveTypeFilter ||
                    time != uiState.collActiveTimeFilter
                viewModel.setCollectionSortOrder(sort)
                viewModel.setCollectionTypeFilter(type)
                viewModel.setCollectionTimeFilter(time)
                if (hasChanged) {
                    scope.launch {
                        isFilterShimmering = true
                        delay(550)
                        isFilterShimmering = false
                    }
                }
            },
            onDismissRequest = { showFilters = false }
        )
    }

    // Edit Collection Bottom Sheet
    if (showEditCollectionModal && collection != null) {
        EditCollectionBottomSheet(
            collection = collection,
            onDismiss = { showEditCollectionModal = false },
            onSave = { updatedCol ->
                viewModel.updateCollection(updatedCol)
                showEditCollectionModal = false
            },
            onDelete = { col ->
                viewModel.deleteCollection(col)
                showEditCollectionModal = false
                onBack()
            }
        )
    }

    if (transactionToEdit != null) {
        val tx = transactionToEdit!!
        EditTransactionBottomSheet(
            transaction = tx,
            collections = uiState.collections,
            onDismiss = { transactionToEdit = null },
            onSave = { desc, amount, type, collectionId ->
                val updatedTx = tx.copy(
                    description = desc,
                    amount = amount,
                    type = type,
                    collectionId = collectionId
                )
                if (txDetailToShow?.id == tx.id) {
                    txDetailToShow = updatedTx
                    txDetailUpdateTrigger = System.currentTimeMillis()
                }
                viewModel.updateTransaction(
                    id = tx.id,
                    description = desc,
                    amount = amount,
                    type = type,
                    collectionId = collectionId,
                    timestamp = tx.timestamp
                )
                transactionToEdit = null
                Toast.makeText(context, "Record updated successfully", Toast.LENGTH_SHORT).show()
            }
        )
    }
}


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CollectionTransactionScreen(
    viewModel: FinanceViewModel,
    collectionId: String,
    onNavigateToAddTransaction: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    isOnline: Boolean = true
) {
    CollectionTransactionsScreen(
        viewModel = viewModel,
        collectionId = collectionId,
        onNavigateToAddTransaction = onNavigateToAddTransaction,
        onBack = onBack,
        modifier = modifier,
        isOnline = isOnline
    )
}
