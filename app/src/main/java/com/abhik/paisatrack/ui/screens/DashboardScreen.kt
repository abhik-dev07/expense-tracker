package com.abhik.paisatrack.ui.screens

import android.annotation.SuppressLint
import com.abhik.paisatrack.data.AuthManager
import kotlinx.coroutines.launch
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abhik.paisatrack.data.model.TransactionEntity
import com.abhik.paisatrack.ui.FinanceViewModel
import java.text.DecimalFormat
import com.abhik.paisatrack.ui.components.*
import com.abhik.paisatrack.ui.components.commonUi.AddCollectionDialog
import com.abhik.paisatrack.ui.components.commonUi.DeleteTransactionConfirmDialog
import com.abhik.paisatrack.ui.components.commonUi.PlusMenuDialog
import com.abhik.paisatrack.ui.components.commonUi.TransactionDetailBottomSheet
import com.abhik.paisatrack.ui.components.commonUi.EditTransactionBottomSheet
import com.abhik.paisatrack.ui.components.dashboard.CollectionsPanel
import com.abhik.paisatrack.ui.components.dashboard.DashboardHeader
import com.abhik.paisatrack.ui.components.dashboard.InsightsPanel
import com.abhik.paisatrack.ui.components.dashboard.SettingsBottomSheet
import com.abhik.paisatrack.ui.components.dashboard.TransactionsPanel
import com.abhik.paisatrack.ui.components.dashboard.VisualSummaryHeader
import com.swmansion.pulsar.Pulsar

@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: FinanceViewModel,
    onNavigateToAddTransaction: () -> Unit,
    onNavigateToCollectionTransactions: (String, androidx.compose.ui.geometry.Rect?) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val aiInsights by viewModel.aiInsights.collectAsStateWithLifecycle()
    val aiLoading by viewModel.aiLoading.collectAsStateWithLifecycle()

    val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()
    var showAddCollectionDialog by remember { mutableStateOf(false) }
    var showPlusMenu by remember { mutableStateOf(false) }
    var isScrolling by remember { mutableStateOf(false) }
    var txToDelete by remember { mutableStateOf<TransactionEntity?>(null) }
    var txDetailToShow by remember { mutableStateOf<TransactionEntity?>(null) }
    var transactionToEdit by remember { mutableStateOf<TransactionEntity?>(null) }
    var showSettingsBottomSheet by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(activeTab) {
        isScrolling = false
    }

    // Entrance fade-in animation (plays once when Dashboard first appears after LoaderScreen)
    val entranceAlpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        entranceAlpha.animateTo(1f, animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing))
    }

    val density = LocalDensity.current
    val headerHeight = 220.dp
    val headerHeightPx = remember(density) { with(density) { headerHeight.toPx() } }
    var headerOffsetHeightPx by rememberSaveable { mutableStateOf(0f) }
    var previousTab by rememberSaveable { mutableStateOf(activeTab) }

    LaunchedEffect(activeTab) {
        if (activeTab != previousTab) {
            headerOffsetHeightPx = 0f
            previousTab = activeTab
        }
    }

    val context = LocalContext.current
    val pulsar = remember { Pulsar(context) }
    val presets = remember { pulsar.getPresets() }
    val userName = remember { AuthManager.getUserName(context) }
    val firstName = remember(userName) { userName.split(" ").firstOrNull() ?: userName }
    val profilePicUrl = remember { AuthManager.getProfilePicUrl(context) }
    val dollarFormat = remember { DecimalFormat("₹#,##0.00") }

    val isDark = isSystemInDarkTheme()
    val view = LocalView.current
    @Suppress("DEPRECATION")
    DisposableEffect(isDark) {
        val activity = context as? android.app.Activity
        val window = activity?.window
        if (window != null) {
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            androidx.core.view.WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDark
        }
        onDispose {}
    }

    var lockHeaderExpansionInCurrentGesture by remember { mutableStateOf(false) }
    var isScrollingToTop by remember { mutableStateOf(false) }

    val nestedScrollConnection = remember(headerHeightPx) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (isScrollingToTop) return Offset.Zero
                val delta = available.y
                if (source == NestedScrollSource.UserInput) {
                    if (delta < -12f) {
                        isScrolling = true
                    } else if (delta > 12f) {
                        isScrolling = false
                    }
                }
                
                // Update header collapse offset ONLY when scrolling up
                if (delta < 0) {
                    val newOffset = headerOffsetHeightPx + delta
                    headerOffsetHeightPx = newOffset.coerceIn(-headerHeightPx, 0f)
                }
                
                // If scrolling up (delta < 0) and header is not fully collapsed, consume the scroll
                return if (delta < 0 && headerOffsetHeightPx > -headerHeightPx) {
                    Offset(0f, delta)
                } else {
                    Offset.Zero
                }
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (isScrollingToTop) return Offset.Zero
                val delta = available.y
                
                // If the list consumed some scroll down delta, it means it was not at the top at start,
                // so we lock header expansion for the rest of this gesture.
                if (delta > 0 && consumed.y > 0.5f) {
                    lockHeaderExpansionInCurrentGesture = true
                }
                
                // If scrolling down (delta > 0), header is not fully expanded, and we are not locked, expand it
                if (delta > 0 && headerOffsetHeightPx < 0f && !lockHeaderExpansionInCurrentGesture) {
                    val newOffset = headerOffsetHeightPx + delta
                    headerOffsetHeightPx = newOffset.coerceIn(-headerHeightPx, 0f)
                    return Offset(0f, delta)
                }
                return Offset.Zero
            }

            override suspend fun onPreFling(available: androidx.compose.ui.unit.Velocity): androidx.compose.ui.unit.Velocity {
                // Reset gesture lock when the user lifts their finger (fling event starts)
                lockHeaderExpansionInCurrentGesture = false
                return androidx.compose.ui.unit.Velocity.Zero
            }
        }
    }


    val bottomBarOffset by animateDpAsState(
        targetValue = if (isScrolling) 200.dp else 0.dp,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "NavBarOffsetAnimation"
    )

    val bottomBarAlpha by animateFloatAsState(
        targetValue = if (isScrolling) 0f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "NavBarAlphaAnimation"
    )

    Scaffold(
        modifier = modifier.alpha(entranceAlpha.value),
        topBar = {
            // Null topBar, Greeting is drawn inline inside the main body
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .offset(y = bottomBarOffset)
                    .alpha(bottomBarAlpha)
                    .padding(bottom = 16.dp, start = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Glassmorphic Tab Container Pill
                val tabs = remember {
                    listOf(
                        Pair("Transactions", "Home"),
                        Pair("Insights", "Analysis"),
                        Pair("Collections", "Collection")
                    )
                }
                val activeIndex = when (activeTab) {
                    "Transactions" -> 0
                    "Insights" -> 1
                    "Collections" -> 2
                    else -> 0
                }
                val animatedIndex by animateFloatAsState(
                    targetValue = activeIndex.toFloat(),
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "TabIndicatorIndexAnimation"
                )

                BoxWithConstraints(
                    modifier = Modifier
                        .weight(1f)
                        .height(72.dp)
                        .background(
                            color = if (isDark) {
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.98f)
                            } else {
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.90f)
                            },
                            shape = CircleShape
                        )
                        .border(
                            width = 1.dp,
                            color = if (isDark) {
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.40f)
                            } else {
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                            },
                            shape = CircleShape
                        )
                        .padding(6.dp)
                ) {
                    val containerWidth = maxWidth
                    val tabWidth = containerWidth / 3

                    // Slidable Highlight Indicator Box
                    Box(
                        modifier = Modifier
                            .offset(x = tabWidth * animatedIndex)
                            .width(tabWidth)
                            .fillMaxHeight()
                            .background(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                                shape = CircleShape
                            )
                    )

                    // Row of Tab items
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        tabs.forEachIndexed { index, tab ->
                            val isSelected = index == activeIndex
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clickable(
                                        onClick = {
                                            presets.ping()
                                            viewModel.setActiveTab(tab.first)
                                        },
                                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                        indication = null
                                    ),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                val contentColor = if (isSelected) {
                                    MaterialTheme.colorScheme.onSurface
                                } else {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                }

                                // Icon mapping
                                when (tab.first) {
                                    "Transactions" -> HomeIcon(
                                        tint = contentColor,
                                        modifier = Modifier.size(24.dp)
                                    )

                                    "Insights" -> AnalysisIcon(
                                        tint = contentColor,
                                        modifier = Modifier.size(24.dp)
                                    )

                                    "Collections" -> FolderIcon(
                                        tint = contentColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // Label
                                Text(
                                    text = tab.second,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = contentColor
                                )
                            }
                        }
                    }
                }

                val plusButtonInteractionSource = remember { MutableInteractionSource() }
                val plusButtonPressed by plusButtonInteractionSource.collectIsPressedAsState()
                val plusButtonAlpha by animateFloatAsState(
                    targetValue = if (plusButtonPressed) 0.72f else 1f,
                    animationSpec = tween(durationMillis = 120),
                    label = "PlusButtonPressAlpha"
                )

                // Glassmorphic FAB Container
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .alpha(plusButtonAlpha)
                        .background(
                            color = if (isDark) {
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.98f)
                            } else {
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.90f)
                            },
                            shape = CircleShape
                        )
                        .border(
                            width = 1.dp,
                            color = if (isDark) {
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.40f)
                            } else {
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                            },
                            shape = CircleShape
                        )
                        .clickable(
                            interactionSource = plusButtonInteractionSource,
                            indication = ripple(bounded = true, radius = 36.dp)
                        ) {
                            presets.plunk()
                            showPlusMenu = true
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Menu",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding()),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(nestedScrollConnection)
                    .pointerInput(Unit) {
                        detectVerticalDragGestures { _, dragAmount ->
                            if (dragAmount < -8f) {
                                isScrolling = true
                            } else if (dragAmount > 8f) {
                                isScrolling = false
                            }
                        }
                    }
            ) {
                DashboardHeader(
                    activeTab = activeTab,
                    userName = userName,
                    firstName = firstName,
                    profilePicUrl = profilePicUrl,
                    onSettingsClick = { showSettingsBottomSheet = true }
                )

                // Collapsible Balance Card container (for all tabs except Profile)
                if (activeTab != "Profile") {
                    val balanceCardHeightDp = with(LocalDensity.current) { (headerHeightPx + headerOffsetHeightPx).toDp() }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(balanceCardHeightDp)
                            .clipToBounds()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .offset { IntOffset(0, headerOffsetHeightPx.roundToInt()) }
                        ) {
                            VisualSummaryHeader(uiState = uiState, dollarFormat = dollarFormat)
                        }
                    }
                }


                // Tab Switcher Content
                AnimatedContent(
                    targetState = activeTab,
                    transitionSpec = {
                        val fromIndex = when (initialState) {
                            "Transactions" -> 0
                            "Insights" -> 1
                            "Collections" -> 2
                            else -> 0
                        }
                        val toIndex = when (targetState) {
                            "Transactions" -> 0
                            "Insights" -> 1
                            "Collections" -> 2
                            else -> 0
                        }
                        if (toIndex > fromIndex) {
                            (slideInHorizontally(animationSpec = tween(300), initialOffsetX = { it }) + fadeIn(animationSpec = tween(300))) togetherWith
                                (slideOutHorizontally(animationSpec = tween(300), targetOffsetX = { -it }) + fadeOut(animationSpec = tween(300)))
                        } else {
                            (slideInHorizontally(animationSpec = tween(300), initialOffsetX = { -it }) + fadeIn(animationSpec = tween(300))) togetherWith
                                (slideOutHorizontally(animationSpec = tween(300), targetOffsetX = { it }) + fadeOut(animationSpec = tween(300)))
                        }
                    },
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    label = "TabTransition"
                ) { targetTab ->
                    when (targetTab) {
                        "Transactions" -> TransactionsPanel(
                            uiState = uiState,
                            viewModel = viewModel,
                            dollarFormat = dollarFormat,
                            onScrollProgressChanged = { isScrolling = it },
                            onTransactionLongClick = { tx -> txToDelete = tx },
                            onTransactionClick = { tx -> txDetailToShow = tx },
                            onBackToTop = { scrollAction ->
                                coroutineScope.launch {
                                    isScrollingToTop = true
                                    headerOffsetHeightPx = -headerHeightPx
                                    try {
                                        scrollAction()
                                    } finally {
                                        isScrollingToTop = false
                                    }
                                }
                            }
                        )
                        "Collections" -> CollectionsPanel(
                            uiState = uiState,
                            viewModel = viewModel,
                            dollarFormat = dollarFormat,
                            onScrollProgressChanged = { isScrolling = it },
                            onCollectionClick = onNavigateToCollectionTransactions,
                            onBackToTop = { scrollAction ->
                                coroutineScope.launch {
                                    isScrollingToTop = true
                                    headerOffsetHeightPx = -headerHeightPx
                                    try {
                                        scrollAction()
                                    } finally {
                                        isScrollingToTop = false
                                    }
                                }
                            }
                        )
                        "Insights" -> InsightsPanel(
                            uiState = uiState,
                            aiInsights = aiInsights,
                            aiLoading = aiLoading,
                            onRefreshInsights = { viewModel.fetchAiInsights() },
                            dollarFormat = dollarFormat,
                            onScrollProgressChanged = { isScrolling = it }
                        )
                    }
                }
            }
        }
    }

    // Dialogs
    if (showPlusMenu) {
        PlusMenuDialog(
            onDismiss = { showPlusMenu = false },
            onAddTransaction = {
                showPlusMenu = false
                onNavigateToAddTransaction()
            },
            onAddCollection = {
                showPlusMenu = false
                showAddCollectionDialog = true
            }
        )
    }

    if (showAddCollectionDialog) {
        AddCollectionDialog(
            onDismiss = { showAddCollectionDialog = false },
            onConfirm = { name, color, icon, budget ->
                viewModel.addCollection(name, color, icon, budget)
                showAddCollectionDialog = false
            }
        )
    }



    // Settings Bottom Sheet
    if (showSettingsBottomSheet) {
        val sheetState = rememberModalBottomSheetState()
        SettingsBottomSheet(
            sheetState = sheetState,
            onDismiss = { showSettingsBottomSheet = false },
            onSignOutClick = {
                viewModel.signOut(context) {
                    Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
                    onLogout()
                }
            },
            onDeleteAccountClick = {
                viewModel.deleteAccountData {
                    coroutineScope.launch {
                        AuthManager.signOut(context)
                        Toast.makeText(context, "Account deleted successfully", Toast.LENGTH_SHORT)
                            .show()
                        onLogout()
                    }
                }
            }
        )
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
        val parentCol = uiState.collections.find { it.id == tx.collectionId }
        val colName = parentCol?.name ?: "General"
        val colColor = parentCol?.hexColor?.let { Color(android.graphics.Color.parseColor(it)) } ?: Color(0xFF9CA3AF)
        val colIcon = getIconByName(parentCol?.iconName ?: "category")

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

@Composable
private fun HomeIcon(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(24.dp)) {
        val strokeWidth = 2.dp.toPx()
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(5.dp.toPx(), 20.dp.toPx())
            lineTo(5.dp.toPx(), 11.dp.toPx())
            lineTo(12.dp.toPx(), 4.dp.toPx())
            lineTo(19.dp.toPx(), 11.dp.toPx())
            lineTo(19.dp.toPx(), 20.dp.toPx())
            close()
            
            moveTo(10.dp.toPx(), 20.dp.toPx())
            lineTo(10.dp.toPx(), 15.dp.toPx())
            quadraticTo(10.dp.toPx(), 14.dp.toPx(), 11.dp.toPx(), 14.dp.toPx())
            lineTo(13.dp.toPx(), 14.dp.toPx())
            quadraticTo(14.dp.toPx(), 14.dp.toPx(), 14.dp.toPx(), 15.dp.toPx())
            lineTo(14.dp.toPx(), 20.dp.toPx())
        }
        drawPath(
            path = path,
            color = tint,
            style = Stroke(width = strokeWidth, join = androidx.compose.ui.graphics.StrokeJoin.Round, cap = StrokeCap.Round)
        )
    }
}

@Composable
private fun AnalysisIcon(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(24.dp)) {
        val strokeWidth = 2.dp.toPx()
        
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(4.dp.toPx(), 18.dp.toPx())
            lineTo(9.dp.toPx(), 13.dp.toPx())
            lineTo(14.dp.toPx(), 16.dp.toPx())
            lineTo(20.dp.toPx(), 8.dp.toPx())
        }
        drawPath(
            path = path,
            color = tint,
            style = Stroke(width = strokeWidth, join = androidx.compose.ui.graphics.StrokeJoin.Round, cap = StrokeCap.Round)
        )
        
        val arrowPath = androidx.compose.ui.graphics.Path().apply {
            moveTo(15.dp.toPx(), 8.dp.toPx())
            lineTo(20.dp.toPx(), 8.dp.toPx())
            lineTo(20.dp.toPx(), 13.dp.toPx())
        }
        drawPath(
            path = arrowPath,
            color = tint,
            style = Stroke(width = strokeWidth, join = androidx.compose.ui.graphics.StrokeJoin.Round, cap = StrokeCap.Round)
        )
    }
}

@Composable
private fun FolderIcon(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(24.dp)) {
        val strokeWidth = 2.dp.toPx()
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(2.dp.toPx(), 8.dp.toPx())
            lineTo(2.dp.toPx(), 6.dp.toPx())
            quadraticTo(2.dp.toPx(), 4.dp.toPx(), 4.dp.toPx(), 4.dp.toPx())
            lineTo(8.dp.toPx(), 4.dp.toPx())
            quadraticTo(9.5.dp.toPx(), 4.dp.toPx(), 10.5.dp.toPx(), 6.dp.toPx())
            lineTo(11.5.dp.toPx(), 8.dp.toPx())
            lineTo(20.dp.toPx(), 8.dp.toPx())
            quadraticTo(22.dp.toPx(), 8.dp.toPx(), 22.dp.toPx(), 10.dp.toPx())
            lineTo(22.dp.toPx(), 18.dp.toPx())
            quadraticTo(22.dp.toPx(), 20.dp.toPx(), 20.dp.toPx(), 20.dp.toPx())
            lineTo(4.dp.toPx(), 20.dp.toPx())
            quadraticTo(2.dp.toPx(), 20.dp.toPx(), 2.dp.toPx(), 18.dp.toPx())
            close()
        }
        drawPath(
            path = path,
            color = tint,
            style = Stroke(width = strokeWidth, join = androidx.compose.ui.graphics.StrokeJoin.Round)
        )
    }
}

