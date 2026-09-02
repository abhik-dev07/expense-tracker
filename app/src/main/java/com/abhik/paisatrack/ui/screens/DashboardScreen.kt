package com.abhik.paisatrack.ui.screens

import android.annotation.SuppressLint
import com.abhik.paisatrack.data.AuthManager
import kotlinx.coroutines.launch
import androidx.compose.animation.*
import androidx.compose.animation.graphics.ExperimentalAnimationGraphicsApi
import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import com.abhik.paisatrack.R
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
import androidx.compose.ui.zIndex
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
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
import com.abhik.paisatrack.ui.utils.safeParseColor
import com.abhik.paisatrack.ui.components.commonUi.EditTransactionBottomSheet
import com.abhik.paisatrack.ui.components.dashboard.CollectionsPanel
import com.abhik.paisatrack.ui.components.dashboard.DashboardHeader
import com.abhik.paisatrack.ui.components.dashboard.InsightsPanel
import com.abhik.paisatrack.ui.components.dashboard.SettingsBottomSheet
import com.abhik.paisatrack.ui.components.dashboard.TransactionsPanel
import com.abhik.paisatrack.ui.components.dashboard.VisualSummaryHeader
import com.swmansion.pulsar.Pulsar
import com.abhik.paisatrack.ui.utils.findActivity
import com.abhik.paisatrack.ui.utils.getSafePresets

@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationGraphicsApi::class)
@Composable
fun DashboardScreen(
    viewModel: FinanceViewModel,
    onNavigateToAddTransaction: () -> Unit,
    onNavigateToCollectionTransactions: (String, androidx.compose.ui.geometry.Rect?) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    isOnline: Boolean = true
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val aiInsights by viewModel.aiInsights.collectAsStateWithLifecycle()
    val aiLoading by viewModel.aiLoading.collectAsStateWithLifecycle()

    val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()
    var showAddCollectionDialog by remember { mutableStateOf(false) }
    var showPlusMenu by remember { mutableStateOf(false) }
    var isScrolling by remember { mutableStateOf(false) }
    var isSearchOpen by rememberSaveable { mutableStateOf(false) }
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
    var scrollOffsetPx by rememberSaveable { mutableStateOf(0f) }
    var previousTab by rememberSaveable { mutableStateOf(activeTab) }

    LaunchedEffect(activeTab) {
        if (activeTab != previousTab) {
            scrollOffsetPx = 0f
            previousTab = activeTab
        }
    }

    val context = LocalContext.current
    val pulsar = remember(context) { Pulsar(context.findActivity() ?: context) }
    val presets = remember(pulsar) { pulsar.getSafePresets() }
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


    val bottomBarOffset by animateDpAsState(
        targetValue = if (isScrolling || isSearchOpen) 200.dp else 0.dp,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "NavBarOffsetAnimation"
    )

    val bottomBarAlpha by animateFloatAsState(
        targetValue = if (isScrolling || isSearchOpen) 0f else 1f,
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

                val homeAnimatedVector = AnimatedImageVector.animatedVectorResource(R.drawable.avd_home)
                val analysisAnimatedVector = AnimatedImageVector.animatedVectorResource(R.drawable.avd_analysis)
                val collectionAnimatedVector = AnimatedImageVector.animatedVectorResource(R.drawable.avd_collection)
                val plusAnimatedVector = AnimatedImageVector.animatedVectorResource(R.drawable.avd_plus)

                val plusPainter = rememberAnimatedVectorPainter(plusAnimatedVector, atEnd = showPlusMenu)

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

                                // Icon mapping: AnimatedVectorDrawable forward on select, reverse ("back effect") on deselect, then smooth Crossfade into predefined inactive icon
                                when (tab.first) {
                                    "Transactions" -> {
                                        var showStatic by remember { mutableStateOf(!isSelected) }
                                        var atEnd by remember { mutableStateOf(isSelected) }
                                        LaunchedEffect(isSelected) {
                                            if (isSelected) {
                                                showStatic = false
                                                atEnd = true
                                            } else if (!showStatic) {
                                                atEnd = false
                                                kotlinx.coroutines.delay(450)
                                                showStatic = true
                                            }
                                        }
                                        val homePainter = rememberAnimatedVectorPainter(homeAnimatedVector, atEnd = atEnd)
                                        Crossfade(
                                            targetState = showStatic,
                                            animationSpec = tween(durationMillis = 200),
                                            label = "HomeIconCrossfade"
                                        ) { isStatic ->
                                            if (isStatic) {
                                                Icon(
                                                    imageVector = HomeIconVector,
                                                    contentDescription = "Transactions",
                                                    tint = contentColor,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            } else {
                                                Icon(
                                                    painter = homePainter,
                                                    contentDescription = "Transactions",
                                                    tint = contentColor,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                        }
                                    }

                                    "Insights" -> {
                                        var showStatic by remember { mutableStateOf(!isSelected) }
                                        var atEnd by remember { mutableStateOf(isSelected) }
                                        LaunchedEffect(isSelected) {
                                            if (isSelected) {
                                                showStatic = false
                                                atEnd = true
                                            } else if (!showStatic) {
                                                atEnd = false
                                                kotlinx.coroutines.delay(450)
                                                showStatic = true
                                            }
                                        }
                                        val analysisPainter = rememberAnimatedVectorPainter(analysisAnimatedVector, atEnd = atEnd)
                                        Crossfade(
                                            targetState = showStatic,
                                            animationSpec = tween(durationMillis = 200),
                                            label = "AnalysisIconCrossfade"
                                        ) { isStatic ->
                                            if (isStatic) {
                                                Icon(
                                                    imageVector = EqualizerIconVector,
                                                    contentDescription = "Insights",
                                                    tint = contentColor,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            } else {
                                                Icon(
                                                    painter = analysisPainter,
                                                    contentDescription = "Insights",
                                                    tint = contentColor,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                        }
                                    }

                                    "Collections" -> {
                                        var showStatic by remember { mutableStateOf(!isSelected) }
                                        var atEnd by remember { mutableStateOf(isSelected) }
                                        LaunchedEffect(isSelected) {
                                            if (isSelected) {
                                                showStatic = false
                                                atEnd = true
                                            } else if (!showStatic) {
                                                atEnd = false
                                                kotlinx.coroutines.delay(450)
                                                showStatic = true
                                            }
                                        }
                                        val collectionPainter = rememberAnimatedVectorPainter(collectionAnimatedVector, atEnd = atEnd)
                                        Crossfade(
                                            targetState = showStatic,
                                            animationSpec = tween(durationMillis = 200),
                                            label = "CollectionIconCrossfade"
                                        ) { isStatic ->
                                            if (isStatic) {
                                                Icon(
                                                    imageVector = FolderIconVector,
                                                    contentDescription = "Collections",
                                                    tint = contentColor,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            } else {
                                                Icon(
                                                    painter = collectionPainter,
                                                    contentDescription = "Collections",
                                                    tint = contentColor,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                        }
                                    }
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
                        painter = plusPainter,
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
                AnimatedVisibility(
                    visible = !isSearchOpen,
                    enter = fadeIn(tween(200)) + expandVertically(spring(stiffness = Spring.StiffnessMediumLow)),
                    exit = fadeOut(tween(150)) + shrinkVertically(spring(stiffness = Spring.StiffnessMediumLow))
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .zIndex(10f)
                                .background(MaterialTheme.colorScheme.background)
                        ) {
                            DashboardHeader(
                                activeTab = activeTab,
                                userName = userName,
                                firstName = firstName,
                                profilePicUrl = profilePicUrl,
                                onSettingsClick = { showSettingsBottomSheet = true },
                                isSettingsOpen = showSettingsBottomSheet
                            )
                        }

                        // Floating Offline Banner below DashboardHeader and above VisualSummaryHeader
                        com.abhik.paisatrack.ui.screens.OfflineBanner(
                            isVisible = !isOnline,
                            modifier = Modifier
                                .fillMaxWidth()
                                .zIndex(10f)
                        )

                        // Balance Card container with collapsing height + inner parallax translation
                        if (activeTab != "Profile") {
                            val currentHeightDp = with(LocalDensity.current) {
                                (headerHeightPx + scrollOffsetPx).coerceAtLeast(0f).toDp()
                            }
                            val progress = if (headerHeightPx > 0f) (-scrollOffsetPx / headerHeightPx).coerceIn(0f, 1f) else 0f

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(currentHeightDp)
                                    .clipToBounds()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .graphicsLayer {
                                            // Parallax translation (moves at 0.45x speed)
                                            translationY = scrollOffsetPx * 0.45f
                                            val scale = 1f - (progress * 0.04f)
                                            scaleX = scale
                                            scaleY = scale
                                            alpha = (1f - progress * 0.4f).coerceIn(0.2f, 1f)
                                        }
                                ) {
                                    VisualSummaryHeader(uiState = uiState, dollarFormat = dollarFormat)
                                }
                            }
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
                            onSearchActiveChanged = { isSearchOpen = it },
                            onTransactionLongClick = { tx -> txToDelete = tx },
                            onTransactionClick = { tx -> txDetailToShow = tx },
                            onBackToTop = { scrollAction ->
                                coroutineScope.launch {
                                    scrollOffsetPx = 0f
                                    scrollAction()
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
                                    scrollOffsetPx = 0f
                                    scrollAction()
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
        val colColor = safeParseColor(parentCol?.hexColor)
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

private val AddIconVector: ImageVector
    get() {
        if (_addIconVector != null) return _addIconVector!!
        _addIconVector = ImageVector.Builder(
            name = "add",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                fillAlpha = 1f,
                stroke = null,
                strokeAlpha = 1f,
                strokeLineWidth = 1f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Bevel,
                strokeLineMiter = 1f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(11f, 13f)
                horizontalLineTo(6f)
                quadTo(5.58f, 13f, 5.29f, 12.71f)
                quadTo(5f, 12.43f, 5f, 12f)
                reflectiveQuadTo(5.29f, 11.29f)
                reflectiveQuadTo(6f, 11f)
                horizontalLineToRelative(5f)
                verticalLineTo(6f)
                quadTo(11f, 5.57f, 11.29f, 5.29f)
                reflectiveQuadTo(12f, 5f)
                reflectiveQuadToRelative(0.71f, 0.29f)
                reflectiveQuadTo(13f, 6f)
                verticalLineToRelative(5f)
                horizontalLineToRelative(5f)
                quadToRelative(0.43f, 0f, 0.71f, 0.29f)
                reflectiveQuadTo(19f, 12f)
                reflectiveQuadToRelative(-0.29f, 0.71f)
                reflectiveQuadTo(18f, 13f)
                horizontalLineTo(13f)
                verticalLineToRelative(5f)
                quadToRelative(0f, 0.43f, -0.29f, 0.71f)
                reflectiveQuadTo(12f, 19f)
                reflectiveQuadTo(11.29f, 18.71f)
                quadTo(11f, 18.43f, 11f, 18f)
                verticalLineTo(13f)
                close()
            }
        }.build()
        return _addIconVector!!
    }

private var _addIconVector: ImageVector? = null

private val EqualizerIconVector: ImageVector
    get() {
        if (_equalizerIconVector != null) return _equalizerIconVector!!
        _equalizerIconVector = ImageVector.Builder(
            name = "equalizer",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 3.5f,
                strokeLineCap = StrokeCap.Round,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(6f, 19f)
                lineTo(6f, 13f)
                moveTo(12f, 19f)
                lineTo(12f, 5f)
                moveTo(18f, 19f)
                lineTo(18f, 10f)
            }
        }.build()
        return _equalizerIconVector!!
    }

private var _equalizerIconVector: ImageVector? = null

private val FolderIconVector: ImageVector
    get() {
        if (_folderIconVector != null) return _folderIconVector!!
        _folderIconVector = ImageVector.Builder(
            name = "folder",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(4f, 19.5f)
                lineTo(20f, 19.5f)
                quadTo(21.5f, 19.5f, 21.5f, 18f)
                lineTo(21.5f, 7.5f)
                quadTo(21.5f, 6f, 20f, 6f)
                lineTo(12f, 6f)
                lineTo(10.2f, 4.43f)
                quadTo(9.58f, 4f, 9.18f, 4f)
                lineTo(4f, 4f)
                quadTo(2.5f, 4f, 2.5f, 5.5f)
                lineTo(2.5f, 18f)
                quadTo(2.5f, 19.5f, 4f, 19.5f)
                close()
            }
        }.build()
        return _folderIconVector!!
    }

private var _folderIconVector: ImageVector? = null

private val HomeIconVector: ImageVector
    get() {
        if (_homeIconVector != null) return _homeIconVector!!
        _homeIconVector = ImageVector.Builder(
            name = "home",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(12f, 3.5f)
                lineTo(4.5f, 9.5f)
                lineTo(4.5f, 20f)
                quadTo(4.5f, 20.55f, 5.5f, 21f)
                lineTo(9.5f, 21f)
                quadTo(10.5f, 21f, 10.5f, 20f)
                lineTo(10.5f, 14.5f)
                quadTo(10.5f, 13.5f, 11.5f, 13.5f)
                lineTo(12.5f, 13.5f)
                quadTo(13.5f, 13.5f, 13.5f, 14.5f)
                lineTo(13.5f, 20f)
                quadTo(13.5f, 21f, 14.5f, 21f)
                lineTo(18.5f, 21f)
                quadTo(19.5f, 21f, 19.5f, 20f)
                lineTo(19.5f, 9.5f)
                close()
            }
        }.build()
        return _homeIconVector!!
    }

private var _homeIconVector: ImageVector? = null

private val HomeFilledIconVector: ImageVector
    get() {
        if (_homeFilledIconVector != null) return _homeFilledIconVector!!
        _homeFilledIconVector = ImageVector.Builder(
            name = "home_filled",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                fillAlpha = 1f,
                stroke = null,
                strokeAlpha = 1f,
                strokeLineWidth = 1f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Bevel,
                strokeLineMiter = 1f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(4f, 19f)
                verticalLineTo(10f)
                quadTo(4f, 9.52f, 4.21f, 9.1f)
                quadTo(4.43f, 8.67f, 4.8f, 8.4f)
                lineToRelative(6f, -4.5f)
                quadTo(11.33f, 3.5f, 12f, 3.5f)
                reflectiveQuadToRelative(1.2f, 0.4f)
                lineToRelative(6f, 4.5f)
                quadToRelative(0.38f, 0.28f, 0.59f, 0.7f)
                quadTo(20f, 9.52f, 20f, 10f)
                verticalLineToRelative(9f)
                quadToRelative(0f, 0.82f, -0.59f, 1.41f)
                reflectiveQuadTo(18f, 21f)
                horizontalLineTo(15f)
                quadToRelative(-0.42f, 0f, -0.71f, -0.29f)
                quadTo(14f, 20.43f, 14f, 20f)
                verticalLineTo(15f)
                quadToRelative(0f, -0.43f, -0.29f, -0.71f)
                reflectiveQuadTo(13f, 14f)
                horizontalLineTo(11f)
                quadToRelative(-0.42f, 0f, -0.71f, 0.29f)
                reflectiveQuadTo(10f, 15f)
                verticalLineToRelative(5f)
                quadToRelative(0f, 0.43f, -0.29f, 0.71f)
                reflectiveQuadTo(9f, 21f)
                horizontalLineTo(6f)
                quadTo(5.18f, 21f, 4.59f, 20.41f)
                reflectiveQuadTo(4f, 19f)
                close()
            }
        }.build()
        return _homeFilledIconVector!!
    }

private var _homeFilledIconVector: ImageVector? = null

private val FolderFilledIconVector: ImageVector
    get() {
        if (_folderFilledIconVector != null) return _folderFilledIconVector!!
        _folderFilledIconVector = ImageVector.Builder(
            name = "folder_filled",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                fillAlpha = 1f,
                stroke = null,
                strokeAlpha = 1f,
                strokeLineWidth = 1f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Bevel,
                strokeLineMiter = 1f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(4f, 20f)
                quadTo(3.18f, 20f, 2.59f, 19.41f)
                reflectiveQuadTo(2f, 18f)
                verticalLineTo(6f)
                quadTo(2f, 5.18f, 2.59f, 4.59f)
                reflectiveQuadTo(4f, 4f)
                horizontalLineTo(9.18f)
                quadToRelative(0.4f, 0f, 0.76f, 0.15f)
                reflectiveQuadToRelative(0.64f, 0.43f)
                lineTo(12f, 6f)
                horizontalLineToRelative(8f)
                quadToRelative(0.83f, 0f, 1.41f, 0.59f)
                quadTo(22f, 7.18f, 22f, 8f)
                verticalLineTo(18f)
                quadToRelative(0f, 0.82f, -0.59f, 1.41f)
                reflectiveQuadTo(20f, 20f)
                horizontalLineTo(4f)
                close()
            }
        }.build()
        return _folderFilledIconVector!!
    }

private var _folderFilledIconVector: ImageVector? = null



