package com.abhik.paisatrack.ui.screens

import com.abhik.paisatrack.data.AuthManager
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.BorderStroke
import kotlinx.coroutines.launch
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.items
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

// Icon mapper helper
fun getIconByName(iconName: String): ImageVector {
    return when (iconName.lowercase()) {
        "restaurant" -> Icons.Default.Restaurant
        "directions_car" -> Icons.Default.DirectionsCar
        "movie" -> Icons.Default.Movie
        "account_balance_wallet" -> Icons.Default.AccountBalanceWallet
        "local_hospital" -> Icons.Default.LocalHospital
        "flight" -> Icons.Default.Flight
        "school" -> Icons.Default.School
        "shopping_cart" -> Icons.Default.ShoppingCart
        "home" -> Icons.Default.Home
        "fitness_center" -> Icons.Default.FitnessCenter
        "work" -> Icons.Default.Work
        "category" -> Icons.Default.Category
        else -> Icons.Default.Category
    }
}

// Available custom colors for Collections
val CollectionColors = listOf(
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

// Available custom icons for Collections
val CollectionIcons = listOf(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: FinanceViewModel,
    onNavigateToAddTransaction: () -> Unit,
    onNavigateToCollectionTransactions: (Long) -> Unit,
    onLogout: () -> Unit
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
    var showLogoutConfirmDialog by remember { mutableStateOf(false) }
    var showSettingsBottomSheet by remember { mutableStateOf(false) }
    var showDeleteAccountConfirmDialog by remember { mutableStateOf(false) }
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
                // 1. Sleek Mockup Header (Hello, David & Notification Circle) shown on Transactions / Dashboard State
                if (activeTab == "Transactions") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 24.dp, top = 20.dp, end = 24.dp, bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // High-contrast profile picture from Google or letter badge
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
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
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = "Hello,",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = Color(0xFF9CA3AF)
                                )
                                Text(
                                    text = firstName,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }

                        // Settings button instead of logout button
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    showSettingsBottomSheet = true
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Horizontal filter pill state is rendered cleanly inside the Transactions tab below
                } else {
                    // Header for secondary screens (Collections, Insights)
                    val headerText = when (activeTab) {
                        "Collections" -> "Collections & Vaults"
                        "Insights" -> "Insights & Analytics"
                        else -> ""
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 24.dp, top = 20.dp, end = 24.dp, bottom = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = headerText,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

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
                            onTransactionClick = { tx -> txDetailToShow = tx }
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

    if (showLogoutConfirmDialog) {
        Dialog(
            onDismissRequest = { showLogoutConfirmDialog = false },
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
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "Logout",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(48.dp)
                    )

                    Text(
                        text = "Sign Out?",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Are you sure you want to log out of your current finance database session?",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                showLogoutConfirmDialog = false
                            },
                            modifier = Modifier.weight(1f),
                            shape = CircleShape
                        ) {
                            Text("Cancel", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Button(
                            onClick = {
                                showLogoutConfirmDialog = false
                                coroutineScope.launch {
                                    AuthManager.signOut(context)
                                    Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
                                    onLogout()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            ),
                            modifier = Modifier.weight(1f),
                            shape = CircleShape
                        ) {
                            Text("Log Out", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Settings Bottom Sheet
    if (showSettingsBottomSheet) {
        val sheetState = rememberModalBottomSheetState()
        val scope = rememberCoroutineScope()
        
        ModalBottomSheet(
            onDismissRequest = { showSettingsBottomSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 28.dp, start = 20.dp, end = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Settings",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                )
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                ) {
                    Column {
                        ListItem(
                            headlineContent = { Text("Sign Out", fontWeight = FontWeight.Bold) },
                            supportingContent = { Text("Safely close current finance database session") },
                            leadingContent = {
                                Icon(
                                    imageVector = Icons.Default.ExitToApp,
                                    contentDescription = "Sign Out",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            modifier = Modifier.clickable {
                                scope.launch { sheetState.hide() }.invokeOnCompletion {
                                    showSettingsBottomSheet = false
                                    showLogoutConfirmDialog = true
                                }
                            }
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))
                        ListItem(
                            headlineContent = { Text("Delete Account", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error) },
                            supportingContent = { Text("Permanently erase all your data and collections", color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)) },
                            leadingContent = {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Account",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            modifier = Modifier.clickable {
                                scope.launch { sheetState.hide() }.invokeOnCompletion {
                                    showSettingsBottomSheet = false
                                    showDeleteAccountConfirmDialog = true
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // Wide Delete Account Confirmation Dialog
    if (showDeleteAccountConfirmDialog) {
        val coroutineScope = rememberCoroutineScope()
        Dialog(
            onDismissRequest = { showDeleteAccountConfirmDialog = false },
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
                        contentDescription = "Delete Account",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(48.dp)
                    )

                    Text(
                        text = "Delete Account?",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Are you sure you want to delete this account? All data (transactions and collections) will be permanently erased. This action is irreversible.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                showDeleteAccountConfirmDialog = false
                            },
                            modifier = Modifier.weight(1f),
                            shape = CircleShape
                        ) {
                            Text("Cancel", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Button(
                            onClick = {
                                showDeleteAccountConfirmDialog = false
                                viewModel.deleteAccountData {
                                    coroutineScope.launch {
                                        AuthManager.signOut(context)
                                        Toast.makeText(context, "Account deleted successfully", Toast.LENGTH_SHORT).show()
                                        onLogout()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            ),
                            modifier = Modifier.weight(1f),
                            shape = CircleShape
                        ) {
                            Text("Delete", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Wide Delete Confirmation Dialog
    if (txToDelete != null) {
        val tx = txToDelete!!
        Dialog(
            onDismissRequest = { txToDelete = null },
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
                        text = "Delete Transaction?",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Are you sure you want to delete \"${tx.description}\" for ${dollarFormat.format(tx.amount)}? This action cannot be undone.",
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
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                txToDelete = null
                            },
                            modifier = Modifier.weight(1f),
                            shape = CircleShape
                        ) {
                            Text("Cancel", fontWeight = FontWeight.Bold , color = MaterialTheme.colorScheme.onSurfaceVariant )
                        }

                        Button(
                            onClick = {
                                viewModel.deleteTransaction(tx)
                                txToDelete = null
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            ),
                            modifier = Modifier.weight(1f),
                            shape = CircleShape
                        ) {
                            Text("Delete", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Transaction Detail Bottom Sheet
    if (txDetailToShow != null) {
        val tx = txDetailToShow!!
        val isIncome = tx.type.uppercase() == "INCOME"
        val parentCol = uiState.collections.find { it.id == tx.collectionId }
        val colName = parentCol?.name ?: "General"
        val colColor = parentCol?.hexColor?.let { Color(android.graphics.Color.parseColor(it)) } ?: Color(0xFF9CA3AF)
        val colIcon = getIconByName(parentCol?.iconName ?: "category")
        val isDark = isSystemInDarkTheme()

        // Match exact date & time logic from the mockup
        val todayStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
        val yesterdayStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(System.currentTimeMillis() - 86400000L))
        val txDateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(tx.timestamp))
        val displayDate = when (txDateStr) {
            todayStr -> "Today"
            yesterdayStr -> "Yesterday"
            else -> txDateStr
        }
        val displayTimeStr = remember(tx.timestamp) {
            val sdfStr = SimpleDateFormat("h:mm a", Locale.getDefault())
            sdfStr.format(Date(tx.timestamp)).lowercase()
        }

        ModalBottomSheet(
            onDismissRequest = { txDetailToShow = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            dragHandle = null
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, bottom = 48.dp, top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Drag handle (Manual to avoid double / extra visual bar)
                Box(
                    modifier = Modifier
                        .padding(top = 8.dp, bottom = 4.dp)
                        .size(width = 40.dp, height = 4.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                        .align(Alignment.CenterHorizontally)
                )

                // Title centered matching mockup style
                Text(
                    text = "Transaction Details",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Centered beautifully rounded icon
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(colColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = colIcon,
                        contentDescription = colName,
                        tint = colColor,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Centered amount matching type green/red color
                Text(
                    text = "${if (isIncome) "+" else "-"}${dollarFormat.format(tx.amount)}",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isIncome) Color(0xFF10B981) else Color(0xFFEF4444)
                )

                // Description placed UNDER the Amount, centered with proper font & color
                if (tx.description.isNotEmpty()) {
                    Text(
                        text = tx.description,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 2.dp, bottom = 4.dp)
                    )
                }

                // Single thin horizontal divider (mockup table style)
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f),
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                // Metadata detailed fields stacked nicely
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    DetailItemRow(
                        label = "Type",
                        value = tx.type.lowercase().replaceFirstChar { it.uppercase() }
                    )
                    DetailItemRow(
                        label = "Collection Name",
                        value = colName
                    )
                    DetailItemRow(
                        label = "Created Date",
                        value = displayDate
                    )
                    DetailItemRow(
                        label = "Created Time",
                        value = displayTimeStr
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Standard pill shape Rounded Close Button with Haptic
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        txDetailToShow = null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDark) MaterialTheme.colorScheme.primaryContainer else Color(0xFFE4F6E6),
                        contentColor = if (isDark) MaterialTheme.colorScheme.onPrimaryContainer else Color(0xFF1C2C1D)
                    )
                ) {
                    Text(
                        text = "Close",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun DetailItemRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End
        )
    }
}

@Composable
fun RowScope.BottomNavItem(
    selected: Boolean,
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (selected) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (isSystemInDarkTheme()) MaterialTheme.colorScheme.primaryContainer else Color(0xFFE4F6E6))
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.onPrimaryContainer else Color(0xFF1E2541),
                    modifier = Modifier.size(24.dp)
                )
            }
        } else {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color(0xFF9CA3AF),
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) MaterialTheme.colorScheme.onBackground else Color(0xFF9CA3AF)
        )
    }
}

@Composable
fun VisualSummaryHeader(
    uiState: FinanceUiState,
    dollarFormat: DecimalFormat,
    modifier: Modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
) {
    val haptic = LocalHapticFeedback.current
    var showDetailDialog by remember { mutableStateOf(false) }
    
    val netBalance = uiState.totalIncome - uiState.totalExpense
    val incomeStr = dollarFormat.format(uiState.totalIncome)
    val expenseStr = dollarFormat.format(uiState.totalExpense)
    val netStr = dollarFormat.format(netBalance)

    val isDark = isSystemInDarkTheme()
    
    // Tactile parent Card wrapping the entire Balance section
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier)
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                showDetailDialog = true
            },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // "Total Balance" Label
            Text(
                text = "Total Balance",
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
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = "Income icon",
                                tint = if (isDark) Color(0xFFB7DAAE) else Color(0xFF1F4D20),
                                modifier = Modifier
                                    .size(18.dp)
                                    .rotate(45f) // Rotates standard down arrow to point Down-Left (South-West)
                            )
                        }
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Income",
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
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = "Expense icon",
                                tint = if (isDark) Color(0xFFFFB8A9) else Color(0xFF6E261A),
                                modifier = Modifier
                                    .size(18.dp)
                                    .rotate(45f) // Rotates standard up arrow to point Up-Right (North-East)
                            )
                        }
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Expense",
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

    // High Precision uncompressed details Modal / Dialog
    if (showDetailDialog) {
        Dialog(
            onDismissRequest = { showDetailDialog = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
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
                        text = "Balance Summary",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f))

                    // Detail Row 1: Total Net Balance
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Net Balance",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = netStr,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (netBalance >= 0) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.error,
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
                            text = "Total Income",
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
                            text = "Total Expense",
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

                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            showDetailDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = CircleShape,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Got it",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TransactionsPanel(
    uiState: FinanceUiState,
    viewModel: FinanceViewModel,
    dollarFormat: DecimalFormat,
    onScrollProgressChanged: (Boolean) -> Unit,
    onTransactionLongClick: (TransactionEntity) -> Unit,
    onTransactionClick: (TransactionEntity) -> Unit
) {
    val listState = rememberLazyListState()
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.nothing))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )
    
    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset }
            .collect { (currentIndex, currentOffset) ->
                if (currentIndex == 0 && currentOffset == 0) {
                    onScrollProgressChanged(false)
                } else {
                    onScrollProgressChanged(true)
                }
            }
    }

    var showFilters by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    val isAnyFilterActive = remember(uiState.activeTimeFilter, uiState.activeTypeFilter) {
        uiState.activeTimeFilter != "All" || uiState.activeTypeFilter != "All"
    }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .fillMaxWidth(),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // Sticky Header: Recent transactions list header (with integrated filter trigger)
        stickyHeader {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.background
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent transactions",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    // Filter triggers icon button with offset/overflow support for tag badge details
                    Box(
                        modifier = Modifier.size(36.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Interactive core with distinct circular boundary is clipped
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(if (showFilters) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { 
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
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

                        // Tiny active filter feedback dot shifted to sit above the circular button boundary (unclipped)
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

        // Transactions list or empty state
        if (uiState.filteredTransactions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 32.dp, end = 32.dp),
                    contentAlignment = Alignment.TopCenter
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
                            text = "No Transactions Registered",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Change filters or tap the '+' action button below to chronicle a dynamic transaction.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(uiState.filteredTransactions, key = { it.id }) { tx ->
                // Find parent collection for styling information
                val parentCollection = uiState.collections.find { it.id == tx.collectionId }
                val categoryColor = parentCollection?.hexColor?.let {
                    Color(android.graphics.Color.parseColor(it))
                } ?: Color(0xFF9CA3AF)

                TransactionListItem(
                    transaction = tx,
                    categoryName = parentCollection?.name ?: "General",
                    categoryColor = categoryColor,
                    categoryIcon = getIconByName(parentCollection?.iconName ?: "category"),
                    dollarFormat = dollarFormat,
                    onDeleteClick = { viewModel.deleteTransaction(tx) },
                    onLongClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onTransactionLongClick(tx)
                    },
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onTransactionClick(tx)
                    }
                )
            }
        }
    }

    // Elegant Bottom Sheet for filters (Comes under recent transactions trigger)
    if (showFilters) {
        ModalBottomSheet(
            onDismissRequest = { showFilters = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, bottom = 48.dp, top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header of Filters with close (cross) button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Filters",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Filter Transactions",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Circle Close Icon (cross press)
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { 
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                scope.launch {
                                    sheetState.hide()
                                    showFilters = false
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Filters",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                // Time Period Filters block
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Timeframe",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val times = listOf("All", "Daily", "Weekly", "Monthly")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        times.forEach { time ->
                            val selected = when (time) {
                                "Daily" -> uiState.activeTimeFilter == "Today"
                                "Weekly" -> uiState.activeTimeFilter == "This Week"
                                "Monthly" -> uiState.activeTimeFilter == "This Month"
                                else -> uiState.activeTimeFilter == "All"
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        val actualFilter = when (time) {
                                            "Daily" -> "Today"
                                            "Weekly" -> "This Week"
                                            "Monthly" -> "This Month"
                                            else -> "All"
                                        }
                                        viewModel.setTimeFilter(actualFilter)
                                    }
                                    .border(
                                        width = 1.dp,
                                        color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
                                        shape = RoundedCornerShape(50)
                                    )
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = time,
                                    fontSize = 14.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Transaction Direction Filters block
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Transaction Type",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val types = listOf("All", "Income", "Expense")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        types.forEach { type ->
                            val selected = uiState.activeTypeFilter.uppercase() == type.uppercase() ||
                                    (type == "All" && uiState.activeTypeFilter == "All")
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(50))
                                    .background(
                                        if (selected) {
                                            if (type == "Income") Color(0xFFE4F6E6)
                                            else if (type == "Expense") Color(0xFFFEE2E2)
                                            else MaterialTheme.colorScheme.surfaceVariant
                                        } else {
                                            MaterialTheme.colorScheme.surfaceVariant
                                        }
                                    )
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        viewModel.setTypeFilter(if (type == "All") "All" else type.uppercase())
                                    }
                                    .border(
                                        width = 1.dp,
                                        color = if (selected) Color.Transparent else MaterialTheme.colorScheme.outline,
                                        shape = RoundedCornerShape(50)
                                    )
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = type,
                                    fontSize = 14.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selected) {
                                        if (type == "Income") Color(0xFF1FB47B)
                                        else if (type == "Expense") Color(0xFFEF4444)
                                        else MaterialTheme.colorScheme.onSurface
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TransactionListItem(
    transaction: TransactionEntity,
    categoryName: String,
    categoryColor: Color,
    categoryIcon: ImageVector,
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

    var swipeOffsetX by remember { mutableStateOf(0f) }
    val animatedSwipeOffset by animateFloatAsState(
        targetValue = swipeOffsetX,
        animationSpec = tween(150),
        label = "SwipeOffsetDashboard"
    )

    // Determine premium color matching the exact mockup
    val (badgeBg, iconColor) = remember(categoryName, transaction.type) {
        val nameLower = categoryName.lowercase()
        if (transaction.type.uppercase() == "INCOME") {
            Color(0xFFE4F6E6) to Color(0xFF10B981)
        } else if (nameLower.contains("food") || nameLower.contains("dining") || nameLower.contains("restaurant") || nameLower.contains("coffee")) {
            Color(0xFFFFEDD5) to Color(0xFFF97316)
        } else if (nameLower.contains("entertainment") || nameLower.contains("movie") || nameLower.contains("game") || nameLower.contains("ticket")) {
            Color(0xFFF3E8FF) to Color(0xFFA855F7)
        } else if (nameLower.contains("transport") || nameLower.contains("car") || nameLower.contains("travel") || nameLower.contains("flight")) {
            Color(0xFFE0F2FE) to Color(0xFF0EA5E9)
        } else {
            categoryColor.copy(alpha = 0.15f) to categoryColor
        }
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

        // Actual card container that slides to the left
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(animatedSwipeOffset.roundToInt(), 0) }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragStart = {},
                        onDragEnd = {
                            if (swipeOffsetX < -180f) {
                                onDeleteClick()
                            }
                            swipeOffsetX = 0f
                        },
                        onDragCancel = {
                            swipeOffsetX = 0f
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            swipeOffsetX = (swipeOffsetX + dragAmount).coerceIn(-300f, 0f)
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
                // Mockup Icon Badge
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(badgeBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = categoryIcon,
                        contentDescription = categoryName,
                        tint = iconColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Center Details
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = transaction.description,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (transaction.notes.isNotEmpty()) transaction.notes else categoryName,
                        fontSize = 13.sp,
                        color = Color(0xFF9CA3AF),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Right details (Amount and Date)
                Column(
                    modifier = Modifier.padding(start = 8.dp),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.Center
                ) {
                    val isIncome = transaction.type.uppercase() == "INCOME"
                    Text(
                        text = "${if (isIncome) "+" else "-"}${dollarFormat.format(transaction.amount)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = if (isIncome) Color(0xFF10B981) else Color(0xFFEF4444),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = dateStr,
                        fontSize = 12.sp,
                        color = Color(0xFF9CA3AF),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CollectionsPanel(
    uiState: FinanceUiState,
    viewModel: FinanceViewModel,
    dollarFormat: DecimalFormat,
    onScrollProgressChanged: (Boolean) -> Unit,
    onCollectionClick: (Long) -> Unit,
    onBackToTop: (suspend () -> Unit) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val gridState = rememberLazyGridState()
    
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.nothing))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )

    LaunchedEffect(gridState) {
        snapshotFlow { gridState.firstVisibleItemIndex to gridState.firstVisibleItemScrollOffset }
            .collect { (currentIndex, currentOffset) ->
                if (currentIndex == 0 && currentOffset == 0) {
                    onScrollProgressChanged(false)
                } else {
                    onScrollProgressChanged(true)
                }
            }
    }

    var editingCollectionSummary by remember { mutableStateOf<CollectionSummary?>(null) }
    val activeColTab by viewModel.activeCollectionTab.collectAsStateWithLifecycle()

    val showBackToTop by remember {
        derivedStateOf {
            gridState.firstVisibleItemIndex > 0 || gridState.firstVisibleItemScrollOffset > 300
        }
    }

    val filteredSummaries = remember(uiState.collectionSummaries, activeColTab) {
        val sorted = uiState.collectionSummaries.sortedWith(
            compareBy<CollectionSummary> { it.collection.isPrebuilt }
                .thenByDescending { if (!it.collection.isPrebuilt) it.collection.createdTimestamp else 0L }
                .thenBy { if (it.collection.isPrebuilt) it.collection.id else 0L }
        )
        when (activeColTab) {
            "Prebuild" -> sorted.filter { it.collection.isPrebuilt }
            "Owned" -> sorted.filter { !it.collection.isPrebuilt }
            else -> sorted
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Sticky Filtration Pills Row placed outside LazyVerticalGrid
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf("All", "Prebuild", "Owned").forEach { option ->
                val selected = activeColTab == option
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(
                            if (selected) MaterialTheme.colorScheme.primary 
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                        .border(
                            width = 1.dp,
                            color = if (selected) MaterialTheme.colorScheme.primary 
                                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            shape = CircleShape
                        )
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.setActiveCollectionTab(option)
                        }
                        .padding(horizontal = 16.dp, vertical = 6.dp), // Sleeker, smaller height
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = option,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selected) MaterialTheme.colorScheme.onPrimary 
                                else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            LazyVerticalGrid(
                state = gridState,
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 120.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (filteredSummaries.isEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 24.dp, bottom = 48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                LottieAnimation(
                                    composition = composition,
                                    progress = { progress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                )
                                Text(
                                    text = if (activeColTab == "Owned") "No Custom Collections" else "No Collections",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = if (activeColTab == "Owned") "Create custom collections with categories to analyze your financial health efficiently."
                                           else "No collections found.",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(filteredSummaries) { summary ->
                        CollectionGridCard(
                            summary = summary,
                            dollarFormat = dollarFormat,
                            onEditClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                editingCollectionSummary = summary
                            },
                            onClick = {
                                onCollectionClick(summary.collection.id)
                            }
                        )
                    }
                }
            }

            androidx.compose.animation.AnimatedVisibility(
                visible = showBackToTop,
                enter = fadeIn() + slideInVertically { it / 2 },
                exit = fadeOut() + slideOutVertically { it / 2 },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 20.dp)
            ) {
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onBackToTop {
                            gridState.animateScrollToItem(0)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
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

        // Bottom Sheet for Editing / Deleting a collection
        if (editingCollectionSummary != null) {
            val focusManager = LocalFocusManager.current
            val isDark = isSystemInDarkTheme()

            val initialColorIdx = remember(editingCollectionSummary) {
                CollectionColors.indexOfFirst { it.first.lowercase() == (editingCollectionSummary?.collection?.hexColor ?: "").lowercase() }.coerceAtLeast(0)
            }
            val initialIconIdx = remember(editingCollectionSummary) {
                CollectionIcons.indexOfFirst { it.first.lowercase() == (editingCollectionSummary?.collection?.iconName ?: "").lowercase() }.coerceAtLeast(0)
            }

            var editName by remember(editingCollectionSummary) { mutableStateOf(editingCollectionSummary?.collection?.name ?: "") }
            var selectedColorIdx by remember(editingCollectionSummary) { mutableStateOf(initialColorIdx) }
            var selectedIconIdx by remember(editingCollectionSummary) { mutableStateOf(initialIconIdx) }
            var errorText by remember(editingCollectionSummary) { mutableStateOf("") }
            var showDeleteConfirm by remember { mutableStateOf(false) }
            var showSaveConfirm by remember { mutableStateOf(false) }

            ModalBottomSheet(
                onDismissRequest = { editingCollectionSummary = null },
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
                        text = "Edit Collection",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                    Column(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = editName,
                            onValueChange = { 
                                if (it.length <= 20) {
                                    editName = it
                                    if (it.length == 20) {
                                        focusManager.clearFocus()
                                    }
                                }
                            },
                            label = { Text("Collection Name") },
                            placeholder = { Text("e.g. Travel, Gym Expense") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (editName.length == 20) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                        )
                        // Char limit indicator + Progress
                        val progressEditName = editName.length / 20f
                        val isLimitEditName = editName.length == 20
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp, start = 4.dp, end = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            LinearProgressIndicator(
                                progress = progressEditName,
                                modifier = Modifier.weight(1f).height(4.dp).clip(CircleShape),
                                color = if (isLimitEditName) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                            Text(
                                text = "${editName.length}/20",
                                fontSize = 11.sp,
                                color = if (isLimitEditName) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                fontWeight = if (isLimitEditName) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }

                    // Color selections
                    Text("Select Theme Color", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CollectionColors.forEachIndexed { i, pair ->
                            val colorVal = Color(android.graphics.Color.parseColor(pair.first))
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(colorVal)
                                    .clickable {
                                        selectedColorIdx = i
                                        focusManager.clearFocus()
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
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CollectionIcons.forEachIndexed { i, pair ->
                            val selected = selectedIconIdx == i
                            val iconThemeColor = Color(android.graphics.Color.parseColor(CollectionColors[selectedColorIdx].first))

                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (selected) iconThemeColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .clickable {
                                        selectedIconIdx = i
                                        focusManager.clearFocus()
                                    }
                                    .padding(6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = getIconByName(pair.first),
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
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFFEF4444)
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5)),
                            shape = androidx.compose.foundation.shape.CircleShape
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
                                }
                            },
                            modifier = Modifier.weight(1.5f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                            shape = androidx.compose.foundation.shape.CircleShape
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
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
                                        text = "Are you sure you want to delete the collection \"${editingCollectionSummary?.collection?.name}\"? This action cannot be undone.",
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
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                showDeleteConfirm = false
                                            },
                                            modifier = Modifier.weight(1f),
                                            shape = CircleShape
                                        ) {
                                            Text("Cancel", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }

                                        Button(
                                            onClick = {
                                                editingCollectionSummary?.collection?.let {
                                                    viewModel.deleteCollection(it)
                                                }
                                                showDeleteConfirm = false
                                                editingCollectionSummary = null
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.error,
                                                contentColor = MaterialTheme.colorScheme.onError
                                            ),
                                            modifier = Modifier.weight(1f),
                                            shape = CircleShape
                                        ) {
                                            Text("Delete", fontWeight = FontWeight.Bold)
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

                                    val prevName = editingCollectionSummary?.collection?.name ?: ""
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
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                showSaveConfirm = false
                                            },
                                            modifier = Modifier.weight(1f),
                                            shape = CircleShape
                                        ) {
                                            Text("Cancel", fontWeight = FontWeight.Bold , color = MaterialTheme.colorScheme.onSurfaceVariant )
                                        }

                                        Button(
                                            onClick = {
                                                val updatedCol = editingCollectionSummary!!.collection.copy(
                                                    name = editName.trim(),
                                                    hexColor = CollectionColors[selectedColorIdx].first,
                                                    iconName = CollectionIcons[selectedIconIdx].first
                                                )
                                                viewModel.updateCollection(updatedCol)
                                                showSaveConfirm = false
                                                editingCollectionSummary = null
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.primary,
                                                contentColor = MaterialTheme.colorScheme.onPrimary,
                                            ),
                                            modifier = Modifier.weight(1f),
                                            shape = CircleShape
                                        ) {
                                            Text("Save", fontWeight = FontWeight.Bold)
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

@Composable
fun CollectionGridCard(
    summary: CollectionSummary,
    dollarFormat: DecimalFormat,
    onEditClick: () -> Unit,
    onClick: () -> Unit
) {
    val colColor = remember(summary.collection.hexColor) {
        Color(android.graphics.Color.parseColor(summary.collection.hexColor))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Icon representation
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(colColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getIconByName(summary.collection.iconName),
                        contentDescription = null,
                        tint = colColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Edit button
                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Collection",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Info text
            Text(
                text = summary.collection.name,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "${summary.transactionCount} ledger events",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Net balance of the collection card
            val balance = summary.totalIncome - summary.totalExpense
            Column {
                Text(
                    text = "Balance",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = dollarFormat.format(balance),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (balance >= 0.0) Color(0xFF10B981) else Color(0xFFFFB8A9)
                )
            }
        }
    }
}

@Composable
fun InsightsPanel(
    uiState: FinanceUiState,
    aiInsights: String,
    aiLoading: Boolean,
    onRefreshInsights: () -> Unit,
    dollarFormat: DecimalFormat,
    onScrollProgressChanged: (Boolean) -> Unit
) {
    val scrollState = rememberScrollState()
    
    LaunchedEffect(scrollState) {
        snapshotFlow { scrollState.value }
            .collect { currentValue ->
                if (currentValue == 0) {
                    onScrollProgressChanged(false)
                } else {
                    onScrollProgressChanged(true)
                }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        
        // 1. Double Rounded Bar Chart
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Weekly Cash Flow Activity",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Synchronized income vs expenditure logs (7-Day window)",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(20.dp))

                // Render Bar Chart
                DailyBarChart(dailySums = uiState.dailyTransactionSums)
            }
        }

        // 2. Beautiful Donut Slice breakdown of expenses
        val expenseSummaries = remember(uiState.collectionSummaries) {
            uiState.collectionSummaries.filter { it.totalExpense > 0.0 }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text(
                text = "Expense Allocation Breakdown",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Proportional spending index of collections",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (!expenseSummaries.isEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total Spent",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = dollarFormat.format(uiState.totalExpense),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            if (expenseSummaries.isEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Register expenses with collection values to draw allocation graphs.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Give the donut bar chart some vertical gap from the total spent
                    Spacer(modifier = Modifier.height(28.dp))

                    // Center the circular donut chart on the screen
                    Box(
                        modifier = Modifier
                            .size(170.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        ExpenseDonutChart(expenseSummaries = expenseSummaries)
                    }

                    // Separation gap between donut chart and list under it
                    Spacer(modifier = Modifier.height(24.dp))

                    // Legend list of card collections below the chart
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val totalSpentVal = expenseSummaries.sumOf { it.totalExpense }
                        expenseSummaries.take(5).forEach { summary ->
                            val rgb = Color(android.graphics.Color.parseColor(summary.collection.hexColor))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(rgb)
                                )
                                Text(
                                    text = "${summary.collection.name} (${String.format("%.0f%%", (summary.totalExpense / totalSpentVal) * 100)})",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        if (expenseSummaries.size > 5) {
                            Text(
                                text = "+ ${expenseSummaries.size - 5} other categories",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---------------- CHARTS IMPLEMENTATIONS ----------------

@Composable
fun ExpenseDonutChart(expenseSummaries: List<CollectionSummary>) {
    var animTrigger by remember { mutableStateOf(false) }
    val animProgress by animateFloatAsState(
        targetValue = if (animTrigger) 1f else 0f,
        animationSpec = tween(1000),
        label = "DonutChartScale"
    )

    LaunchedEffect(expenseSummaries) {
        animTrigger = true
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val totalExp = expenseSummaries.sumOf { it.totalExpense }
        if (totalExp <= 0.0) return@Canvas

        var startAngle = -90f
        expenseSummaries.forEach { summary ->
            val angle = 360f * (summary.totalExpense / totalExp).toFloat()
            val col = Color(android.graphics.Color.parseColor(summary.collection.hexColor))
            
            drawArc(
                color = col,
                startAngle = startAngle,
                sweepAngle = angle * animProgress,
                useCenter = false,
                style = Stroke(width = 30.dp.toPx(), cap = StrokeCap.Round)
            )
            startAngle += angle
        }
    }
}

@Composable
fun DailyBarChart(dailySums: List<DailySum>) {
    var animTrigger by remember { mutableStateOf(false) }
    val animProgress by animateFloatAsState(
        targetValue = if (animTrigger) 1f else 0f,
        animationSpec = tween(1000),
        label = "BarChartHeight"
    )

    LaunchedEffect(dailySums) {
        animTrigger = true
    }

    val maxAmount = remember(dailySums) {
        val maxInc = dailySums.maxOfOrNull { it.totalIncome } ?: 0.0
        val maxExp = dailySums.maxOfOrNull { it.totalExpense } ?: 0.0
        maxOf(maxInc, maxExp, 100.0) // fallback base max is 100
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        dailySums.forEach { item ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Dual column containers
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        // Income Bar (Green)
                        val incomeHeightFraction = ((item.totalIncome / maxAmount) * animProgress).toFloat().coerceIn(0.01f, 1f)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(incomeHeightFraction)
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(Color(0xFF10B981))
                        )

                        // Expense Bar (Red)
                        val expenseHeightFraction = ((item.totalExpense / maxAmount) * animProgress).toFloat().coerceIn(0.01f, 1f)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(expenseHeightFraction)
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(Color(0xFFFFB8A9))
                        )
                    }
                }

                // X-Axis Text Label
                Text(
                    text = item.dateString,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ---------------- DIALOGS IMPLEMENTATIONS ----------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
    collections: List<CollectionEntity>,
    onDismiss: () -> Unit,
    onConfirm: (description: String, amount: Double, type: String, collectionId: Long) -> Unit
) {
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    var description by remember { mutableStateOf("") }
    var amountString by remember { mutableStateOf("") }
    var transactionType by remember { mutableStateOf("EXPENSE") } // "EXPENSE" or "INCOME"
    var selectedCollectionId by remember { mutableStateOf(collections.firstOrNull()?.id ?: 0L) }
    var selectedPaymentType by remember { mutableStateOf("Cash") } // "Cash", "Credit/Debit Card", "Check"
    
    var errorText by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 20.dp),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // 1. Mockup Header Block (Round Back Button on Left, Centered Title)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Circular back icon matching the style
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .clickable { onDismiss() }
                            .border(1.dp, Color(0xFFE5E7EB), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF1E2541),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = "Add transaction",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E2541),
                        modifier = Modifier.weight(1f)
                    )
                }

                // 2. Transaction Type Selector Tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFF3F4F6))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val isExpense = transactionType == "EXPENSE"
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isExpense) Color.White else Color.Transparent)
                            .clickable { transactionType = "EXPENSE" }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Expense",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isExpense) Color(0xFF1E2541) else Color(0xFF9CA3AF)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (!isExpense) Color.White else Color.Transparent)
                            .clickable { transactionType = "INCOME" }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Income",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (!isExpense) Color(0xFF1E2541) else Color(0xFF9CA3AF)
                        )
                    }
                }

                // 3. Description field (Optional - defaults to collection name)
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Description",
                        color = Color(0xFF9CA3AF),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = description,
                        onValueChange = { 
                            if (it.length <= 40) {
                                description = it
                                if (it.length == 40) {
                                    focusManager.clearFocus()
                                }
                            }
                        },
                        placeholder = { Text("e.g. Starbucks Coffee, Office Rent", color = Color(0xFF9CA3AF)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF1E2541),
                            unfocusedTextColor = Color(0xFF1E2541),
                            focusedContainerColor = Color(0xFFF7F8F9),
                            unfocusedContainerColor = Color(0xFFF7F8F9),
                            focusedBorderColor = if (description.length == 40) MaterialTheme.colorScheme.error else Color(0xFF1E2541),
                            unfocusedBorderColor = Color.Transparent
                        )
                    )
                    // Char limit indicator + Progress
                    val progressDesc = description.length / 40f
                    val isLimitDesc = description.length == 40
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp, start = 4.dp, end = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LinearProgressIndicator(
                            progress = progressDesc,
                            modifier = Modifier.weight(1f).height(4.dp).clip(CircleShape),
                            color = if (isLimitDesc) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Text(
                            text = "${description.length}/40",
                            fontSize = 11.sp,
                            color = if (isLimitDesc) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            fontWeight = if (isLimitDesc) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }

                // 4. Amount Box
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Amount",
                        color = Color(0xFF9CA3AF),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFFF7F8F9))
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AttachMoney,
                            contentDescription = "Dollar Sign",
                            tint = Color(0xFF1E2541),
                            modifier = Modifier.size(24.dp)
                        )
                        TextField(
                            value = amountString,
                            onValueChange = {
                                if (it.length <= 8 && (it.isEmpty() || it.toDoubleOrNull() != null || it.last() == '.')) {
                                    amountString = it
                                    if (it.length == 8) {
                                        focusManager.clearFocus()
                                    }
                                }
                            },
                            placeholder = { Text("0.00", color = Color(0xFF9CA3AF), fontSize = 22.sp, fontWeight = FontWeight.Bold) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = TextStyle(
                                color = Color(0xFF1E2541),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                    }
                    // Char limit indicator + Progress
                    val progressAmount = amountString.length / 8f
                    val isLimitAmount = amountString.length == 8
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp, start = 4.dp, end = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LinearProgressIndicator(
                            progress = progressAmount,
                            modifier = Modifier.weight(1f).height(4.dp).clip(CircleShape),
                            color = if (isLimitAmount) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Text(
                            text = "${amountString.length}/8",
                            fontSize = 11.sp,
                            color = if (isLimitAmount) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            fontWeight = if (isLimitAmount) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }

                // 5. Category Box with collection details spin dropdown
                if (collections.isNotEmpty()) {
                    var dropdownExpanded by remember { mutableStateOf(false) }
                    val currentSelection = collections.find { it.id == selectedCollectionId } ?: collections.first()
                    val colColor = Color(android.graphics.Color.parseColor(currentSelection.hexColor))

                    // Determine background badge and icon color based on selection name
                    val (badgeBg, iconCol) = remember(currentSelection.name) {
                        val nameLower = currentSelection.name.lowercase()
                        if (nameLower.contains("food") || nameLower.contains("coffee") || nameLower.contains("dining")) {
                            Color(0xFFFFEDD5) to Color(0xFFF97316)
                        } else if (nameLower.contains("entertainment") || nameLower.contains("ticket")) {
                            Color(0xFFF3E8FF) to Color(0xFFA855F7)
                        } else if (nameLower.contains("salary") || nameLower.contains("income")) {
                            Color(0xFFE4F6E6) to Color(0xFF10B981)
                        } else {
                            colColor.copy(alpha = 0.15f) to colColor
                        }
                    }

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Category",
                            color = Color(0xFF9CA3AF),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFFF7F8F9))
                                .clickable { dropdownExpanded = true }
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(badgeBg),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = getIconByName(currentSelection.iconName),
                                            contentDescription = null,
                                            tint = iconCol,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Text(
                                        text = currentSelection.name,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E2541)
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Dropdown Chevron",
                                    tint = Color(0xFF1E2541)
                                )
                            }

                            DropdownMenu(
                                expanded = dropdownExpanded,
                                onDismissRequest = { dropdownExpanded = false },
                                modifier = Modifier
                                    .fillMaxWidth(0.9f)
                                    .background(Color.White)
                            ) {
                                collections.forEach { col ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                Icon(
                                                    imageVector = getIconByName(col.iconName),
                                                    contentDescription = null,
                                                    tint = Color(android.graphics.Color.parseColor(col.hexColor)),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Text(
                                                    text = col.name,
                                                    fontWeight = FontWeight.Medium,
                                                    color = Color(0xFF1E2541)
                                                )
                                            }
                                        },
                                        onClick = {
                                            selectedCollectionId = col.id
                                            dropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // 6. Payment Type Radio Selector Stack
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Payment Type",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E2541)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val paymentTypes = listOf("Cash", "Credit / Debit Card", "Check")
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        paymentTypes.forEach { type ->
                            val isSelected = selectedPaymentType == type
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isSelected) Color(0xFFE4F6E6) else Color.Transparent)
                                    .clickable { selectedPaymentType = type }
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) Color(0xFFB7DAAE) else Color(0xFFE5E7EB),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .padding(horizontal = 18.dp, vertical = 14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = type,
                                        fontSize = 15.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = Color(0xFF1E2541)
                                    )

                                    // Mockup checked icon / radio ring dot
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) Color(0xFF10B981) else Color.Transparent)
                                            .border(
                                                width = 1.5.dp,
                                                color = if (isSelected) Color(0xFF10B981) else Color(0xFF9CA3AF),
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Checked",
                                                tint = Color.White,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Error text feedback
                if (errorText.isNotEmpty()) {
                    Text(
                        text = errorText,
                        color = Color.Red,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }

                // 7. Footer Buttons Block ("Draft" and "Add")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Draft action button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(100))
                            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(100))
                            .clickable { onDismiss() }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Draft",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E2541)
                        )
                    }

                    // Confirm action button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(100))
                            .background(Color(0xFFB7DAAE))
                            .clickable {
                                val amt = amountString.toDoubleOrNull()
                                val desc = if (description.trim().isEmpty()) {
                                    val currentSelection = collections.find { it.id == selectedCollectionId }
                                    currentSelection?.name ?: "Expense"
                                } else {
                                    description.trim()
                                }

                                if (amt == null || amt <= 0.0) {
                                    errorText = "Enter a valid positive transaction amount!"
                                } else if (collections.isEmpty()) {
                                    errorText = "Create a collection before adding transaction!"
                                } else {
                                    onConfirm(desc, amt, transactionType, selectedCollectionId)
                                }
                            }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Add",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E2541)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCollectionDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, colorHex: String, iconName: String, budget: Double?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedColorIdx by remember { mutableStateOf(0) }
    var selectedIconIdx by remember { mutableStateOf(0) }

    var errorText by remember { mutableStateOf("") }

    val focusManager = LocalFocusManager.current
    val haptic = LocalHapticFeedback.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Create Collection",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                // Collection Name input
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { 
                            if (it.length <= 20) {
                                name = it
                                if (it.length == 20) {
                                    focusManager.clearFocus()
                                }
                            }
                        },
                        label = { Text("Collection Name") },
                        placeholder = { Text("e.g. Travel, Gym Expense") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (name.length == 20) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    )
                    // Char limit indicator + Progress
                    val progressName = name.length / 20f
                    val isLimitName = name.length == 20
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp, start = 4.dp, end = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LinearProgressIndicator(
                            progress = progressName,
                            modifier = Modifier.weight(1f).height(4.dp).clip(CircleShape),
                            color = if (isLimitName) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Text(
                            text = "${name.length}/20",
                            fontSize = 11.sp,
                            color = if (isLimitName) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            fontWeight = if (isLimitName) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }

                // Color selections
                Text("Select Theme Color", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CollectionColors.forEachIndexed { i, pair ->
                        val colorVal = Color(android.graphics.Color.parseColor(pair.first))
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(colorVal)
                                .clickable {
                                    selectedColorIdx = i
                                    focusManager.clearFocus()
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
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CollectionIcons.forEachIndexed { i, pair ->
                        val selected = selectedIconIdx == i
                        val iconThemeColor = Color(android.graphics.Color.parseColor(CollectionColors[selectedColorIdx].first))
                        
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (selected) iconThemeColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .clickable {
                                    selectedIconIdx = i
                                    focusManager.clearFocus()
                                }
                                .padding(6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getIconByName(pair.first),
                                contentDescription = pair.second,
                                tint = if (selected) iconThemeColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                if (errorText.isNotEmpty()) {
                    Text(errorText, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
                }

                // Footer CTA Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Cancel Text Button
                    TextButton(
                        onClick = {
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text(
                            text = "Cancel",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }

                    Button(
                        onClick = {
                            if (name.trim().isEmpty()) {
                                errorText = "Collection name is required!"
                            } else {
                                onConfirm(
                                    name.trim(),
                                    CollectionColors[selectedColorIdx].first,
                                    CollectionIcons[selectedIconIdx].first,
                                    null
                                )
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Create")
                    }
                }
            }
        }
    }
}

@Composable
fun PlusMenuDialog(
    onDismiss: () -> Unit,
    onAddTransaction: () -> Unit,
    onAddCollection: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Indicator/Icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Text(
                    text = "Add New Entry",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Choose whether you'd like to structure a new wallet group or draft a ledger record.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Option 1: Add Ledger Record (Transaction)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                            onAddTransaction()
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f) else Color(0xFFF1F5F9)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFB7DAAE).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = null,
                                tint = Color(0xFFB7DAAE),
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Transaction Record",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Record incoming funds or outgoing bills",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Option 2: Add Collection Category
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                            onAddCollection()
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f) else Color(0xFFF1F5F9)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFFB8A9).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = null,
                                tint = Color(0xFFFFB8A9),
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Collection Category",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Organize transactions into custom groups",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Cancel Text Button
                TextButton(
                    onClick = {
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        onDismiss()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text(
                        text = "Cancel",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
