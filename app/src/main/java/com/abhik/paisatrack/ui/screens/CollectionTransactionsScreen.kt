package com.abhik.paisatrack.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalContext
import com.abhik.paisatrack.R
import com.abhik.paisatrack.data.model.TransactionEntity
import com.abhik.paisatrack.ui.FinanceViewModel
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.abhik.paisatrack.ui.components.commonUi.DeleteTransactionConfirmDialog
import com.abhik.paisatrack.ui.components.commonUi.FilterBottomSheet
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import com.abhik.paisatrack.ui.components.commonUi.TransactionDetailBottomSheet
import androidx.compose.ui.zIndex
import com.abhik.paisatrack.ui.components.commonUi.EditTransactionBottomSheet
import com.swmansion.pulsar.Pulsar

private enum class CollectionSubmitState {
    Idle,
    Loading,
    Success
}

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

    LaunchedEffect(collectionId, uiState.collActiveTimeFilter, uiState.collActiveTypeFilter, uiState.collActiveSortOrder) {
        visibleLimit = 20
        animationStartLimit = 0
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
    val isDark = isSystemInDarkTheme()
    var showDetailDialog by remember { mutableStateOf(false) }
    var txToDelete by remember { mutableStateOf<TransactionEntity?>(null) }
    var txDetailToShow by remember { mutableStateOf<TransactionEntity?>(null) }
    var transactionToEdit by remember { mutableStateOf<TransactionEntity?>(null) }
    var showEditCollectionModal by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val listState = rememberLazyListState()

    var showFilters by remember { mutableStateOf(false) }
    val isAnyFilterActive = remember(uiState.collActiveTimeFilter, uiState.collActiveTypeFilter, uiState.collActiveSortOrder) {
        uiState.collActiveTimeFilter != "All" || uiState.collActiveTypeFilter != "All" || uiState.collActiveSortOrder != "Newest"
    }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    var isScrolling by remember { mutableStateOf(false) }

    val showBackToTop by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 300
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset }
            .collect { (currentIndex, currentOffset) ->
                if (currentIndex == 0 && currentOffset == 0) {
                    isScrolling = false
                } else {
                    isScrolling = true
                }
            }
    }

    val colColor = remember(collection?.hexColor) {
        collection?.hexColor?.let {
            try {
                Color(android.graphics.Color.parseColor(it))
            } catch (e: Exception) {
                Color(0xFF3F51B5)
            }
        } ?: Color(0xFF3F51B5)
    }

    val view = LocalView.current
    val context = LocalContext.current
    val pulsar = remember { Pulsar(context) }
    val presets = remember { com.abhik.paisatrack.ui.utils.SafePresets(pulsar.getPresets()) }
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
            listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset <= 10
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
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Transaction",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    text = {
                        Text(
                            text = "Add Record",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    },
                    expanded = isAtTop,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.padding(bottom = 12.dp, end = 8.dp)
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            // 1. Fixed Visually Rich Header (Title Row)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .zIndex(10f)
                    .background(MaterialTheme.colorScheme.background)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                colColor.copy(alpha = if (isDark) 0.15f else 0.08f),
                                colColor.copy(alpha = if (isDark) 0.05f else 0.02f)
                            )
                        )
                    )
                    .padding(
                        top = innerPadding.calculateTopPadding() + 20.dp,
                        bottom = 12.dp,
                        start = 24.dp,
                        end = 24.dp
                    )
            ) {
                // Back and Title Navigation Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Circular back button
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (isDark) MaterialTheme.colorScheme.surfaceVariant else Color.White)
                            .border(
                                androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f)
                                ),
                                CircleShape
                            )
                            .clickable {
                                onBack()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Back to dashboard",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Centered Column for Title & Subtitle (ledger records registered)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = collection?.name ?: "Collection details",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${transactions.size} records registered",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                    }

                    // Circular Edit Button with horizontal three dots (...)
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (isDark) MaterialTheme.colorScheme.surfaceVariant else Color.White)
                            .border(
                                androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f)
                                ),
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
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Floating Offline Banner placed below top title header row
            com.abhik.paisatrack.ui.screens.OfflineBanner(
                isVisible = !isOnline,
                modifier = Modifier
                    .fillMaxWidth()
                    .zIndex(10f)
            )

            // 2. Main Parallax Scrollable Content
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    // Non-collapsing Fixed Balance Card with Parallax Translation
                    if (summary != null) {
                        item(key = "balance_card") {
                            val parallaxTranslation = remember {
                                derivedStateOf {
                                    if (listState.firstVisibleItemIndex == 0) {
                                        listState.firstVisibleItemScrollOffset * 0.45f
                                    } else 0f
                                }
                            }
                            val fadeAlpha = remember {
                                derivedStateOf {
                                    if (listState.firstVisibleItemIndex == 0) {
                                        (1f - (listState.firstVisibleItemScrollOffset / 400f) * 0.45f).coerceIn(0.2f, 1f)
                                    } else 0.55f
                                }
                            }
                            val scale = remember {
                                derivedStateOf {
                                    if (listState.firstVisibleItemIndex == 0) {
                                        (1f - (listState.firstVisibleItemScrollOffset / 400f) * 0.04f).coerceIn(0.96f, 1f)
                                    } else 0.96f
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .graphicsLayer {
                                        translationY = parallaxTranslation.value
                                        alpha = fadeAlpha.value
                                        scaleX = scale.value
                                        scaleY = scale.value
                                    }
                            ) {
                                val netBalance = summary.totalIncome - summary.totalExpense
                                
                                var hasAnimated by rememberSaveable { mutableStateOf(false) }
                                val animatedBalance = remember { Animatable(netBalance.toFloat()) }
                                val animatedIncome = remember { Animatable(summary.totalIncome.toFloat()) }
                                val animatedExpense = remember { Animatable(summary.totalExpense.toFloat()) }
                                val alphaAnim = remember { Animatable(if (hasAnimated) 1f else 0f) }

                                LaunchedEffect(netBalance, summary.totalIncome, summary.totalExpense, uiState.isLoading) {
                                    if (!uiState.isLoading) {
                                        if (!hasAnimated) {
                                            animatedBalance.snapTo((netBalance * 0.88).toFloat())
                                            animatedIncome.snapTo((summary.totalIncome * 0.88).toFloat())
                                            animatedExpense.snapTo((summary.totalExpense * 0.88).toFloat())

                                            launch {
                                                alphaAnim.animateTo(
                                                    targetValue = 1f,
                                                    animationSpec = tween(durationMillis = 1000, easing = LinearOutSlowInEasing)
                                                )
                                            }
                                            launch {
                                                animatedBalance.animateTo(
                                                    targetValue = netBalance.toFloat(),
                                                    animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
                                                )
                                            }
                                            launch {
                                                animatedIncome.animateTo(
                                                    targetValue = summary.totalIncome.toFloat(),
                                                    animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
                                                )
                                            }
                                            launch {
                                                animatedExpense.animateTo(
                                                    targetValue = summary.totalExpense.toFloat(),
                                                    animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
                                                )
                                            }
                                            hasAnimated = true
                                        } else {
                                            alphaAnim.snapTo(1f)
                                            launch {
                                                animatedBalance.animateTo(
                                                    targetValue = netBalance.toFloat(),
                                                    animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
                                                )
                                            }
                                            launch {
                                                animatedIncome.animateTo(
                                                    targetValue = summary.totalIncome.toFloat(),
                                                    animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
                                                )
                                            }
                                            launch {
                                                animatedExpense.animateTo(
                                                    targetValue = summary.totalExpense.toFloat(),
                                                    animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
                                                )
                                            }
                                        }
                                    }
                                }

                                val displayBalance = if (hasAnimated) animatedBalance.value.toDouble() else netBalance
                                val displayIncome = if (hasAnimated) animatedIncome.value.toDouble() else summary.totalIncome
                                val displayExpense = if (hasAnimated) animatedExpense.value.toDouble() else summary.totalExpense

                                val netStr = dollarFormat.format(displayBalance)
                                val incomeStr = dollarFormat.format(displayIncome)
                                val expenseStr = dollarFormat.format(displayExpense)

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 24.dp, vertical = 8.dp)
                                        .clip(RoundedCornerShape(24.dp))
                                        .clickable {
                                            presets.ping()
                                            showDetailDialog = true
                                        },
                                    shape = RoundedCornerShape(24.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f) else Color.White
                                    ),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                                    border = androidx.compose.foundation.BorderStroke(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f)
                                    )
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
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                            fontWeight = FontWeight.Medium,
                                            textAlign = TextAlign.Center
                                        )

                                        Spacer(modifier = Modifier.height(6.dp))

                                        // Giant Net Balance Center text
                                        Text(
                                            text = netStr,
                                            fontSize = 32.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onBackground,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            textAlign = TextAlign.Center,
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
                                                    containerColor = if (isDark) MaterialTheme.colorScheme.surface.copy(alpha = 0.8f) else Color(0xFFF8FAFC)
                                                ),
                                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                                                border = androidx.compose.foundation.BorderStroke(
                                                    width = 1.dp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.05f)
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
                                                            imageVector = Icons.Default.ArrowDownward,
                                                            contentDescription = "Income icon",
                                                            tint = if (isDark) Color(0xFFB7DAAE) else Color(0xFF1F4D20),
                                                            modifier = Modifier
                                                                .size(18.dp)
                                                                .rotate(45f)
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
                                                        Text(
                                                            text = incomeStr,
                                                            fontSize = 14.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.onBackground,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    }
                                                }
                                            }

                                            // Expense Card Item (Right)
                                            Card(
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(16.dp),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = if (isDark) MaterialTheme.colorScheme.surface.copy(alpha = 0.8f) else Color(0xFFF8FAFC)
                                                ),
                                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                                                border = androidx.compose.foundation.BorderStroke(
                                                    width = 1.dp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.05f)
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
                                                            imageVector = Icons.Default.ArrowUpward,
                                                            contentDescription = "Expense icon",
                                                            tint = if (isDark) Color(0xFFFFB8A9) else Color(0xFF6E261A),
                                                            modifier = Modifier
                                                                .size(18.dp)
                                                                .rotate(45f)
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
                                                        Text(
                                                            text = expenseStr,
                                                            fontSize = 14.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.onBackground,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Sticky Header: Recent Transactions Title and Filter triggers
                    stickyHeader {
                        val isStuck = remember {
                            derivedStateOf {
                                listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 40
                            }
                        }
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.background.copy(alpha = if (isStuck.value) 0.96f else 1f),
                            tonalElevation = if (isStuck.value) 4.dp else 0.dp,
                            shadowElevation = if (isStuck.value) 3.dp else 0.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Recent Activity",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )

                                Box(
                                    modifier = Modifier.size(36.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                            .background(if (showFilters) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.surfaceVariant)
                                            .clickable {
                                                presets.plunk()
                                                showFilters = !showFilters
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Tune,
                                            contentDescription = "Show Filters",
                                            tint = if (showFilters) MaterialTheme.colorScheme.background else if (isAnyFilterActive) Color(0xFF10B981) else MaterialTheme.colorScheme.onBackground,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    if (isAnyFilterActive && !showFilters) {
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .offset(x = 1.dp, y = (-1).dp)
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF10B981))
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Transactions list items or empty state item
                    if (transactions.isEmpty()) {
                        item {
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
                        itemsIndexed(paginatedTransactions, key = { _, tx -> tx.id }) { index, tx ->
                            val alpha = remember(tx.id) { androidx.compose.animation.core.Animatable(if (index >= animationStartLimit) 0f else 1f) }
                            LaunchedEffect(tx.id) {
                                if (index >= animationStartLimit) {
                                    kotlinx.coroutines.delay((index - animationStartLimit) * 25L)
                                    alpha.animateTo(1f, tween(150))
                                }
                            }

                            val itemParallaxY = remember(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset, index) {
                                val itemOffset = index - listState.firstVisibleItemIndex
                                if (itemOffset in 0..6) {
                                    (listState.firstVisibleItemScrollOffset * 0.03f * (itemOffset + 1)).coerceIn(0f, 12f)
                                } else 0f
                            }

                            Box(
                                modifier = Modifier
                                    .alpha(alpha.value)
                                    .graphicsLayer {
                                        translationY = -itemParallaxY
                                    }
                            ) {
                                TransactionListItemDetailed(
                                    transaction = tx,
                                    colColor = colColor,
                                    dollarFormat = dollarFormat,
                                    onDeleteClick = {
                                        viewModel.deleteTransaction(tx)
                                        android.widget.Toast.makeText(context, "Record deleted successfully", android.widget.Toast.LENGTH_SHORT).show()
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

                        if (transactions.size > visibleLimit) {
                            item {
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
                                                .padding(horizontal = 24.dp)
                                                .height(52.dp)
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
                        .padding(top = 70.dp)
                ) {
                    FilledTonalButton(
                        onClick = {
                            presets.ping()
                            scope.launch {
                                if (listState.firstVisibleItemIndex > 2) {
                                    listState.scrollToItem(2)
                                }
                                listState.animateScrollToItem(0)
                            }
                        },
                        elevation = ButtonDefaults.filledTonalButtonElevation(defaultElevation = 6.dp),
                        shape = CircleShape,
                        modifier = Modifier
                            .height(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = "Back to Top",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Back to Top", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
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
        val colName = collection?.name ?: "General"
        val colIcon = getIconByNameLocal(collection?.iconName ?: "category")

        TransactionDetailBottomSheet(
            transaction = tx,
            collectionName = colName,
            collectionColor = colColor,
            collectionIcon = colIcon,
            dollarFormat = dollarFormat,
            onDismiss = { txDetailToShow = null },
            onEditClick = {
                transactionToEdit = tx
                txDetailToShow = null
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
            onSortOrderChange = { viewModel.setCollectionSortOrder(it) },
            onTypeFilterChange = { viewModel.setCollectionTypeFilter(it) },
            onTimeFilterChange = { viewModel.setCollectionTimeFilter(it) },
            onDismissRequest = { showFilters = false }
        )
    }

    // Localized Edit Collection Bottom Sheet
    if (showEditCollectionModal && collection != null) {
        val initialColorIdx = remember(collection.id) {
            LocalCollectionColors.indexOfFirst { it.first.lowercase() == collection.hexColor.lowercase() }.coerceAtLeast(0)
        }
        val initialIconIdx = remember(collection.id) {
            LocalCollectionIcons.indexOfFirst { it.first.lowercase() == collection.iconName.lowercase() }.coerceAtLeast(0)
        }

        var editName by remember(collection.id) { mutableStateOf(collection.name) }
        var selectedColorIdx by remember(collection.id) { mutableStateOf(initialColorIdx) }
        var selectedIconIdx by remember(collection.id) { mutableStateOf(initialIconIdx) }
        var errorText by remember(collection.id) { mutableStateOf("") }
        var showDeleteConfirm by remember { mutableStateOf(false) }
        var showSaveConfirm by remember { mutableStateOf(false) }

        ModalBottomSheet(
            onDismissRequest = { showEditCollectionModal = false },
            containerColor = MaterialTheme.colorScheme.surface,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, bottom = 48.dp, top = 8.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Edit Collection Settings",
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { 
                            if (it.length <= 25) {
                                editName = it
                                if (it.length == 25) {
                                    focusManager.clearFocus()
                                }
                            }
                        },
                        label = { Text("Collection Name") },
                        placeholder = { Text("e.g. Travel, Gym Expense") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (editName.length == 25) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    )
                    // Char limit indicator + Progress
                    val progressEditName = editName.length / 25f
                    val isLimitEditName = editName.length == 25
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp, start = 4.dp, end = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LinearWavyProgressIndicator(
                            progress = { progressEditName },
                            modifier = Modifier.weight(1f).height(10.dp),
                            color = if (isLimitEditName) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Text(
                            text = "${editName.length}/25",
                            fontSize = 11.sp,
                            color = if (isLimitEditName) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            fontWeight = if (isLimitEditName) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }

                // Theme color selections
                Text("Select Theme Color", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LocalCollectionColors.forEachIndexed { i, pair ->
                        val colorVal = Color(android.graphics.Color.parseColor(pair.first))
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(colorVal)
                                .clickable {
                                    selectedColorIdx = i
                                    focusManager.clearFocus()
                                    presets.boulder()
                                }
                                .padding(2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedColorIdx == i) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                )
                            }
                        }
                    }
                }

                // Icon selections
                Text("Select Collection Icon", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LocalCollectionIcons.forEachIndexed { i, pair ->
                        val selected = selectedIconIdx == i
                        val iconThemeColor = Color(android.graphics.Color.parseColor(LocalCollectionColors[selectedColorIdx].first))

                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (selected) iconThemeColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .clickable {
                                    selectedIconIdx = i
                                    focusManager.clearFocus()
                                    presets.boulder()
                                }
                                .padding(6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getIconByNameLocal(pair.first),
                                contentDescription = pair.second,
                                tint = if (selected) iconThemeColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                if (errorText.isNotEmpty()) {
                    Text(
                        text = errorText,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Delete Button
                    OutlinedButton(
                        onClick = {
                            showDeleteConfirm = true
                            presets.bassDrop()
                        },
                        modifier = Modifier.weight(1.5f).height(52.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFEF4444)
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5)),
                        shape = CircleShape
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Delete", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }

                    // Save Button
                    Button(
                        onClick = {
                            if (editName.trim().isEmpty()) {
                                errorText = "Collection name is required!"
                            } else {
                                showSaveConfirm = true
                                presets.ping()
                            }
                        },
                        modifier = Modifier.weight(2f).height(52.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = CircleShape
                    ) {
                        Text("Save Changes", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (showDeleteConfirm) {
                    Dialog(
                        onDismissRequest = { showDeleteConfirm = false },
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
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Warning",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(48.dp)
                                )

                                Text(
                                    text = "Delete Collection?",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                )

                                Text(
                                    text = "Are you sure you want to delete the collection \"${collection.name}\"? This action cannot be undone.",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            presets.boulder()
                                            showDeleteConfirm = false
                                        },
                                        modifier = Modifier.weight(1f).height(52.dp),
                                        shape = CircleShape
                                    ) {
                                        Text("Cancel", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }

                                        val delScope = rememberCoroutineScope()
                                        var delSubmitState by remember { mutableStateOf(CollectionSubmitState.Idle) }
                                        val delBgColor by animateColorAsState(
                                            targetValue = when (delSubmitState) {
                                                CollectionSubmitState.Idle -> MaterialTheme.colorScheme.error
                                                CollectionSubmitState.Loading, CollectionSubmitState.Success -> MaterialTheme.colorScheme.surface
                                            },
                                            animationSpec = tween(400), label = "delBg"
                                        )
                                        val delContentColor by animateColorAsState(
                                            targetValue = when (delSubmitState) {
                                                CollectionSubmitState.Idle -> MaterialTheme.colorScheme.onError
                                                CollectionSubmitState.Loading, CollectionSubmitState.Success -> MaterialTheme.colorScheme.error
                                            },
                                            animationSpec = tween(400), label = "delContent"
                                        )
                                        val delBorderWidth by animateDpAsState(
                                            targetValue = if (delSubmitState == CollectionSubmitState.Idle) 0.dp else 1.dp,
                                            animationSpec = tween(400), label = "delBorder"
                                        )
                                        val delBorderColor by animateColorAsState(
                                            targetValue = if (delSubmitState == CollectionSubmitState.Idle) Color.Transparent else MaterialTheme.colorScheme.error.copy(alpha = 0.5f),
                                            animationSpec = tween(400), label = "delBorderColor"
                                        )

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(52.dp)
                                                .clip(CircleShape)
                                                .border(delBorderWidth, delBorderColor, CircleShape)
                                                .background(delBgColor)
                                                .clickable(enabled = delSubmitState == CollectionSubmitState.Idle) {
                                                    presets.ping()
                                                    delScope.launch {
                                                        delSubmitState = CollectionSubmitState.Loading
                                                        delay(1500)
                                                        delSubmitState = CollectionSubmitState.Success
                                                        presets.systemNotificationSuccess()
                                                        delay(800)
                                                        viewModel.deleteCollection(collection)
                                                        Toast.makeText(context, "Collection deleted successfully", Toast.LENGTH_SHORT).show()
                                                        showDeleteConfirm = false
                                                        showEditCollectionModal = false
                                                        onBack()
                                                    }
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            AnimatedContent(
                                                targetState = delSubmitState,
                                                transitionSpec = {
                                                    (fadeIn(tween(300)) + scaleIn(tween(300), initialScale = 0.8f))
                                                        .togetherWith(fadeOut(tween(200)) + scaleOut(tween(200), targetScale = 0.6f))
                                                },
                                                label = "delContent"
                                            ) { state ->
                                                when (state) {
                                                    CollectionSubmitState.Idle -> {
                                                        Text("Delete", fontWeight = FontWeight.Bold, color = delContentColor)
                                                    }
                                                    CollectionSubmitState.Loading -> {
                                                        val density = androidx.compose.ui.platform.LocalDensity.current
                                                        val strokeWidthPx = with(density) { 2.dp.toPx() }
                                                        val amplitudePx = with(density) { 4.dp.toPx() }
                                                        CircularWavyProgressIndicator(
                                                            modifier = Modifier.size(24.dp),
                                                            color = MaterialTheme.colorScheme.error,
                                                            trackColor = MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                                                            stroke = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidthPx),
                                                            trackStroke = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidthPx),
                                                            amplitude = amplitudePx,
                                                            wavelength = 6.dp
                                                        )
                                                    }
                                                    CollectionSubmitState.Success -> {
                                                        Icon(imageVector = Icons.Default.Check, contentDescription = "Success", tint = delContentColor, modifier = Modifier.size(24.dp))
                                                    }
                                                }
                                            }
                                        }
                                }
                            }
                        }
                    }
                }

                if (showSaveConfirm) {
                    Dialog(
                        onDismissRequest = { showSaveConfirm = false },
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
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Confirmation",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(48.dp)
                                )

                                Text(
                                    text = "Save Changes?",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                )

                                val prevName = collection.name
                                val finalName = editName.trim()
                                Text(
                                    text = if (prevName != finalName) {
                                        "Are you sure you want to update the collection \"$prevName\" to \"$finalName\"?"
                                    } else {
                                        "Are you sure you want to save changes to the collection \"$finalName\"?"
                                    },
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            presets.ping()
                                            showSaveConfirm = false
                                        },
                                        modifier = Modifier.weight(1f).height(52.dp),
                                        shape = CircleShape
                                    ) {
                                        Text("Cancel", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }

                                    val saveScope = rememberCoroutineScope()
                                    var saveSubmitState by remember { mutableStateOf(CollectionSubmitState.Idle) }
                                    val saveBgColor by animateColorAsState(
                                        targetValue = when (saveSubmitState) {
                                            CollectionSubmitState.Idle -> MaterialTheme.colorScheme.primary
                                            CollectionSubmitState.Loading, CollectionSubmitState.Success -> MaterialTheme.colorScheme.surface
                                        },
                                        animationSpec = tween(400), label = "saveBg"
                                    )
                                    val saveContentColor by animateColorAsState(
                                        targetValue = when (saveSubmitState) {
                                            CollectionSubmitState.Idle -> MaterialTheme.colorScheme.onPrimary
                                            CollectionSubmitState.Loading, CollectionSubmitState.Success -> MaterialTheme.colorScheme.primary
                                        },
                                        animationSpec = tween(400), label = "saveContent"
                                    )
                                    val saveBorderWidth by animateDpAsState(
                                        targetValue = if (saveSubmitState == CollectionSubmitState.Idle) 0.dp else 1.dp,
                                        animationSpec = tween(400), label = "saveBorder"
                                    )
                                    val saveBorderColor by animateColorAsState(
                                        targetValue = if (saveSubmitState == CollectionSubmitState.Idle) Color.Transparent else MaterialTheme.colorScheme.outline,
                                        animationSpec = tween(400), label = "saveBorderColor"
                                    )

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(52.dp)
                                            .clip(CircleShape)
                                            .border(saveBorderWidth, saveBorderColor, CircleShape)
                                            .background(saveBgColor)
                                            .clickable(enabled = saveSubmitState == CollectionSubmitState.Idle) {
                                                presets.ping()
                                                saveScope.launch {
                                                    saveSubmitState = CollectionSubmitState.Loading
                                                    delay(1500)
                                                    saveSubmitState = CollectionSubmitState.Success
                                                    presets.systemNotificationSuccess()
                                                    delay(800)
                                                    val updatedCol = collection.copy(
                                                        name = editName.trim(),
                                                        hexColor = LocalCollectionColors[selectedColorIdx].first,
                                                        iconName = LocalCollectionIcons[selectedIconIdx].first
                                                    )
                                                    viewModel.updateCollection(updatedCol)
                                                    Toast.makeText(context, "Collection updated successfully", Toast.LENGTH_SHORT).show()
                                                    showSaveConfirm = false
                                                    showEditCollectionModal = false
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        AnimatedContent(
                                            targetState = saveSubmitState,
                                            transitionSpec = {
                                                (fadeIn(tween(300)) + scaleIn(tween(300), initialScale = 0.8f))
                                                    .togetherWith(fadeOut(tween(200)) + scaleOut(tween(200), targetScale = 0.6f))
                                            },
                                            label = "saveContent"
                                        ) { state ->
                                            when (state) {
                                                CollectionSubmitState.Idle -> {
                                                    Text("Save", fontWeight = FontWeight.Bold, color = saveContentColor)
                                                }
                                                CollectionSubmitState.Loading -> {
                                                    val density = androidx.compose.ui.platform.LocalDensity.current
                                                    val strokeWidthPx = with(density) { 2.dp.toPx() }
                                                    val amplitudePx = with(density) { 4.dp.toPx() }
                                                    CircularWavyProgressIndicator(
                                                        modifier = Modifier.size(24.dp),
                                                        color = MaterialTheme.colorScheme.primary,
                                                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                                        stroke = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidthPx),
                                                        trackStroke = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidthPx),
                                                        amplitude = amplitudePx,
                                                        wavelength = 6.dp
                                                    )
                                                }
                                                CollectionSubmitState.Success -> {
                                                    Icon(imageVector = Icons.Default.Check, contentDescription = "Success", tint = saveContentColor, modifier = Modifier.size(24.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (transactionToEdit != null) {
        val tx = transactionToEdit!!
        EditTransactionBottomSheet(
            transaction = tx,
            collections = uiState.collections,
            onDismiss = { transactionToEdit = null },
            onSave = { desc, amount, type, collectionId ->
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



fun getIconByNameLocal(iconName: String): ImageVector {
    return when (iconName.lowercase().trim()) {
        "restaurant", "coffee", "utensils", "food", "fast-food", "food and drinks", "food-and-drinks", "hamburger" -> Icons.Default.Restaurant
        "directions_car", "car", "bus", "transport", "automobile", "carfront", "car-front" -> Icons.Default.DirectionsCar
        "movie", "film", "clapperboard", "play", "tv", "entertainment" -> Icons.Default.Movie
        "account_balance_wallet", "wallet", "dollar-sign", "trending-up", "savings", "cash", "bills", "receiptindianrupee", "receipt-indian-rupee" -> Icons.Default.AccountBalanceWallet
        "local_hospital", "heart", "activity", "stethoscope", "health", "medical", "health care", "health-care", "hospital" -> Icons.Default.LocalHospital
        "flight", "plane", "travel", "airplane" -> Icons.Default.Flight
        "school", "book", "book-open", "graduation-cap", "graduationcap", "education", "study" -> Icons.Default.School
        "shopping_cart", "shopping-cart", "shopping-bag", "shopping", "gift", "gifts", "groceries", "shoppingbasket", "shopping-basket" -> Icons.Default.ShoppingCart
        "home", "home-bills", "house", "rent" -> Icons.Default.Home
        "fitness_center", "dumbbell", "sports", "gym", "workout" -> Icons.Default.FitnessCenter
        "work", "briefcase", "job", "business" -> Icons.Default.Work
        "category", "general", "pet", "pawprint", "paw-print", "others", "ellipsis" -> Icons.Default.Category
        else -> Icons.Default.Category
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TransactionListItemDetailed(
    transaction: TransactionEntity,
    colColor: Color,
    dollarFormat: DecimalFormat,
    onDeleteClick: () -> Unit,
    onLongClick: () -> Unit,
    onClick: () -> Unit
) {
    val dateStr = remember(transaction.timestamp) {
        val sdfKey = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val todayStr = sdfKey.format(Date())
        val yesterdayStr = sdfKey.format(Date(System.currentTimeMillis() - 86400000L))
        val txDateStr = sdfKey.format(Date(transaction.timestamp))
        when (txDateStr) {
            todayStr -> "Today"
            yesterdayStr -> "Yesterday"
            else -> SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(transaction.timestamp))
        }
    }

    val isIncome = transaction.type.uppercase() == "INCOME"

    val context = LocalContext.current
    val pulsar = remember { Pulsar(context) }
    val presets = remember { com.abhik.paisatrack.ui.utils.SafePresets(pulsar.getPresets()) }
    val realtime = remember { com.abhik.paisatrack.ui.utils.SafeRealtimeComposer(pulsar.getRealtimeComposer(com.swmansion.pulsar.types.RealtimeComposerStrategy.PRIMITIVE_TICK)) }

    var swipeOffsetX by remember { mutableStateOf(0f) }
    val animatedSwipeOffset by animateFloatAsState(
        targetValue = swipeOffsetX,
        animationSpec = tween(150),
        label = "SwipeOffsetDetailed"
    )

    val badgeBg = remember(isIncome, colColor) {
        if (isIncome) Color(0xFFE4F6E6) else Color(0xFFFFB8A9).copy(alpha = 0.15f)
    }
    val iconColor = remember(isIncome, colColor) {
        if (isIncome) Color(0xFF10B981) else Color(0xFFFFB8A9)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFEF4444).copy(alpha = 0.9f))
    ) {
        // Red Delete Underlay visual helper on vertical center end
        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "Swipe to delete",
            tint = Color.White,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 20.dp)
                .size(24.dp)
        )

        // Slideable Surface container
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(animatedSwipeOffset.roundToInt(), 0) }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragStart = {
                            realtime.start()
                        },
                        onDragEnd = {
                            realtime.stop()
                            if (swipeOffsetX < -180f) {
                                presets.cleave()
                                onDeleteClick()
                            }
                            swipeOffsetX = 0f
                        },
                        onDragCancel = {
                            realtime.stop()
                            swipeOffsetX = 0f
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            swipeOffsetX = (swipeOffsetX + dragAmount).coerceIn(-300f, 0f)
                            val progress = (-swipeOffsetX / 180f).coerceIn(0f, 1f)
                            val amplitude = 0.1f + 0.9f * progress
                            val frequency = 0.2f + 0.8f * progress
                            realtime.set(amplitude, frequency, startIfNeeded = true)
                        }
                    )
                }
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick
                ),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f)),
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Simple type indicators
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(badgeBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isIncome) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Details
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = transaction.description,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = dateStr,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }

                // Amount
                Spacer(modifier = Modifier.width(16.dp))
                Column(
                    modifier = Modifier.padding(start = 8.dp),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "${if (isIncome) "+" else "-"}${dollarFormat.format(transaction.amount)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = if (isIncome) Color(0xFF10B981) else Color(0xFFEF4444),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

private val LocalCollectionColors = listOf(
    "#3F51B5" to "Indigo",
    "#009688" to "Teal",
    "#4CAF50" to "Green",
    "#FF9800" to "Orange",
    "#E91E63" to "Pink",
    "#9C27B0" to "Purple",
    "#FFEB3B" to "Yellow",
    "#00BCD4" to "Cyan",
    "#F44336" to "Red"
)

private val LocalCollectionIcons = listOf(
    "category" to "General",
    "restaurant" to "Food & Dining",
    "directions_car" to "Transport",
    "movie" to "Entertainment",
    "account_balance_wallet" to "Wallet",
    "local_hospital" to "Health",
    "flight" to "Travel",
    "school" to "Education",
    "shopping_cart" to "Shopping",
    "home" to "Home Bills",
    "fitness_center" to "Sports & Fitness",
    "work" to "Work/Office"
)
