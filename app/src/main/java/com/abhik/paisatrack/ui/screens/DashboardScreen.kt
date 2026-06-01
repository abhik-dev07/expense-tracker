package com.abhik.paisatrack.ui.screens

import com.abhik.paisatrack.data.AuthManager
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.boundsInRoot
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abhik.paisatrack.R
import com.abhik.paisatrack.data.model.CollectionEntity
import com.abhik.paisatrack.data.model.TransactionEntity
import com.abhik.paisatrack.ui.CollectionSummary
import com.abhik.paisatrack.ui.DailySum
import com.abhik.paisatrack.ui.FinanceUiState
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
import com.abhik.paisatrack.ui.components.*

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
    var showSettingsBottomSheet by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(activeTab) {
        isScrolling = false
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
    val userName = remember { AuthManager.getUserName(context) }
    val firstName = remember(userName) { userName.split(" ").firstOrNull() ?: userName }
    val profilePicUrl = remember { AuthManager.getProfilePicUrl(context) }
    val dollarFormat = remember { DecimalFormat("₹#,##0.00") }
    val haptic = LocalHapticFeedback.current

    val isDark = isSystemInDarkTheme()
    val view = LocalView.current
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
                if (source == NestedScrollSource.Drag) {
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
        modifier = modifier,
        topBar = {
            // Null topBar, Greeting is drawn inline inside the main body
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .offset(y = bottomBarOffset)
                    .alpha(bottomBarAlpha)
                    .padding(bottom = 16.dp, start = 20.dp, end = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(76.dp)
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(38.dp)),
                    shape = RoundedCornerShape(38.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 1. Home Tab
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        viewModel.setActiveTab("Transactions")
                                    },
                                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                    indication = null
                                ),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "Home",
                                tint = if (activeTab == "Transactions") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Home",
                                fontSize = 11.sp,
                                fontWeight = if (activeTab == "Transactions") FontWeight.Bold else FontWeight.Medium,
                                color = if (activeTab == "Transactions") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }

                        // 2. Analysis Tab
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        viewModel.setActiveTab("Insights")
                                    },
                                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                    indication = null
                                ),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = "Analysis",
                                tint = if (activeTab == "Insights") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Analysis",
                                fontSize = 11.sp,
                                fontWeight = if (activeTab == "Insights") FontWeight.Bold else FontWeight.Medium,
                                color = if (activeTab == "Insights") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }

                        // 3. Collection Tab
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        viewModel.setActiveTab("Collections")
                                    },
                                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                    indication = null
                                ),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = "Collection",
                                tint = if (activeTab == "Collections") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Collection",
                                fontSize = 11.sp,
                                fontWeight = if (activeTab == "Collections") FontWeight.Bold else FontWeight.Medium,
                                color = if (activeTab == "Collections") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }

                        // 4. Plus Action Button (Right of Analysis)
                        Box(
                            modifier = Modifier
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        showPlusMenu = true
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add Menu",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
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
                coroutineScope.launch {
                    AuthManager.signOut(context)
                    Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
                    onLogout()
                }
            },
            onDeleteAccountClick = {
                viewModel.deleteAccountData {
                    coroutineScope.launch {
                        AuthManager.signOut(context)
                        Toast.makeText(context, "Account deleted successfully", Toast.LENGTH_SHORT).show()
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
            onDismiss = { txDetailToShow = null }
        )
    }
}

