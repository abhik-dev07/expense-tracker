package com.abhik.paisatrack.ui.screens.dashboard

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.animation.graphics.ExperimentalAnimationGraphicsApi
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abhik.paisatrack.data.AuthManager
import com.abhik.paisatrack.data.model.TransactionEntity
import com.abhik.paisatrack.ui.FinanceViewModel
import com.abhik.paisatrack.ui.components.commonUi.DeleteTransactionConfirmDialog
import com.abhik.paisatrack.ui.components.commonUi.EditTransactionBottomSheet
import com.abhik.paisatrack.ui.components.commonUi.TransactionDetailBottomSheet
import com.abhik.paisatrack.ui.components.dashboard.DashboardHeader
import com.abhik.paisatrack.ui.components.dashboard.SettingsBottomSheet
import com.abhik.paisatrack.ui.components.dashboard.TransactionsPanel
import com.abhik.paisatrack.ui.components.dashboard.VisualSummaryHeader
import com.abhik.paisatrack.ui.components.getIconByName
import com.abhik.paisatrack.ui.screens.OfflineBanner
import com.abhik.paisatrack.ui.utils.findActivity
import com.abhik.paisatrack.ui.utils.getSafePresets
import com.abhik.paisatrack.ui.screens.dashboard.components.DashboardBackToTopButton
import com.abhik.paisatrack.ui.screens.dashboard.utils.DashboardUtils
import com.abhik.paisatrack.ui.utils.safeParseColor
import com.swmansion.pulsar.Pulsar
import java.text.DecimalFormat
import kotlinx.coroutines.launch

@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationGraphicsApi::class)
@Composable
fun DashboardScreen(
    viewModel: FinanceViewModel,
    onNavigateToAddTransaction: () -> Unit,
    onNavigateToCollectionTransactions: (String, androidx.compose.ui.geometry.Rect?) -> Unit,
    onLogout: () -> Unit,
    onProfileClick: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToInsights: () -> Unit = {},
    onNavigateToCollections: () -> Unit = {},
    modifier: Modifier = Modifier,
    isOnline: Boolean = true
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()

    var isScrolling by remember { mutableStateOf(false) }
    var isSearchOpen by rememberSaveable { mutableStateOf(false) }
    var txToDelete by remember { mutableStateOf<TransactionEntity?>(null) }
    var txDetailToShow by remember { mutableStateOf<TransactionEntity?>(null) }
    var transactionToEdit by remember { mutableStateOf<TransactionEntity?>(null) }
    var txDetailUpdateTrigger by remember { mutableLongStateOf(0L) }
    var showSettingsBottomSheet by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var isScrollingUp by remember { mutableStateOf(false) }
    var isAutoScrollingToTop by remember { mutableStateOf(false) }
    var isNavBarVisible by remember { mutableStateOf(true) }

    LaunchedEffect(activeTab) {
        isScrolling = false
        isScrollingUp = false
        isNavBarVisible = true
    }

    // Entrance fade-in animation
    val entranceAlpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        entranceAlpha.animateTo(1f, animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing))
    }

    val scrollState = rememberScrollState()

    LaunchedEffect(scrollState) {
        var previousOffset = scrollState.value
        var accumulatedDelta = 0
        snapshotFlow { scrollState.value }
            .collect { currentOffset ->
                val delta = currentOffset - previousOffset
                if (isAutoScrollingToTop) {
                    isScrollingUp = false
                    isNavBarVisible = true
                    accumulatedDelta = 0
                } else if (currentOffset <= 80) {
                    isNavBarVisible = true
                    accumulatedDelta = 0
                    if (currentOffset <= 400) {
                        isScrollingUp = false
                    }
                } else if (delta < 0) {
                    // Scrolling up
                    if (accumulatedDelta > 0) accumulatedDelta = 0
                    accumulatedDelta += delta
                    if (accumulatedDelta < -20) {
                        isNavBarVisible = true
                        if (currentOffset > 400) {
                            isScrollingUp = true
                        }
                    }
                } else if (delta > 0) {
                    // Scrolling down
                    if (accumulatedDelta < 0) accumulatedDelta = 0
                    accumulatedDelta += delta
                    if (accumulatedDelta > 20) {
                        isScrollingUp = false
                        isNavBarVisible = false
                    }
                }
                previousOffset = currentOffset
            }
    }

    val context = LocalContext.current
    val pulsar = remember(context) { Pulsar(context.findActivity() ?: context) }
    val presets = remember(pulsar) { pulsar.getSafePresets() }
    val userName = remember { AuthManager.getUserName(context) }
    val firstName = remember(userName) { userName.split(" ").firstOrNull() ?: userName }
    val profilePicUrl = remember { AuthManager.getProfilePicUrl(context) }
    val dollarFormat = remember { DecimalFormat("₹#,##0.00") }

    val isDark = com.abhik.paisatrack.ui.theme.isAppInDarkTheme()
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

    var triggerSearch by remember { mutableStateOf(false) }
    var showFilters by remember { mutableStateOf(false) }
    val isAnyFilterActive = remember(uiState.activeTimeFilter, uiState.activeTypeFilter, uiState.activeSortOrder) {
        uiState.activeTimeFilter != "All" || uiState.activeTypeFilter != "All" || uiState.activeSortOrder != "Newest"
    }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .alpha(entranceAlpha.value),
        color = MaterialTheme.colorScheme.background
    ) {
            LaunchedEffect(scrollState.isScrollInProgress) {
                isScrolling = scrollState.isScrollInProgress
            }

            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    val targetHeaderHeight = 136.dp
                    val statusBarHeight = androidx.compose.foundation.layout.WindowInsets.statusBars
                        .asPaddingValues().calculateTopPadding()
                    Spacer(modifier = Modifier.height(targetHeaderHeight + statusBarHeight))

                    AnimatedVisibility(
                        visible = !isSearchOpen,
                        enter = fadeIn(tween(200)) + expandVertically(spring(stiffness = Spring.StiffnessMediumLow)),
                        exit = fadeOut(tween(150)) + shrinkVertically(spring(stiffness = Spring.StiffnessMediumLow))
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Floating Offline Banner below DashboardHeader and above VisualSummaryHeader
                            OfflineBanner(
                                isVisible = !isOnline,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .zIndex(10f)
                            )

                            // Balance Card container
                            VisualSummaryHeader(uiState = uiState, dollarFormat = dollarFormat)
                        }
                    }

                    // Transactions Panel
                    TransactionsPanel(
                        uiState = uiState,
                        viewModel = viewModel,
                        dollarFormat = dollarFormat,
                        onScrollProgressChanged = { isScrolling = it },
                        onSearchActiveChanged = { isSearchOpen = it },
                        onTransactionLongClick = { tx -> txToDelete = tx },
                        onTransactionClick = { tx -> txDetailToShow = tx },
                        onBackToTop = { scrollAction ->
                            coroutineScope.launch {
                                isAutoScrollingToTop = true
                                isScrollingUp = false
                                isNavBarVisible = true
                                try {
                                    scrollAction()
                                } finally {
                                    isAutoScrollingToTop = false
                                }
                            }
                        },
                        triggerSearch = triggerSearch,
                        onSearchTriggered = { triggerSearch = false },
                        showFilters = showFilters,
                        isAnyFilterActive = isAnyFilterActive,
                        onToggleFilters = { showFilters = !showFilters },
                        isScrolled = scrollState.value > with(LocalDensity.current) { 250.dp.toPx() }
                    )
                }

                // Sticky Dashboard Header
                AnimatedVisibility(
                    visible = !isSearchOpen,
                    enter = fadeIn(tween(200)),
                    exit = fadeOut(tween(150))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .zIndex(20f)
                    ) {
                        DashboardHeader(
                            activeTab = "Transactions",
                            userName = userName,
                            firstName = firstName,
                            profilePicUrl = profilePicUrl,
                            onSettingsClick = onProfileClick,
                            isSettingsOpen = false,
                            scrollOffset = scrollState.value.toFloat(),
                            onSearchClick = {
                                onNavigateToSearch()
                            },
                            onFilterClick = { showFilters = !showFilters },
                            showFilters = showFilters,
                            isAnyFilterActive = isAnyFilterActive
                        )
                    }
                }

                val showBackToTop by remember {
                    derivedStateOf {
                        DashboardUtils.shouldShowBackToTop(isScrollingUp, scrollState.value)
                    }
                }

                DashboardBackToTopButton(
                    visible = showBackToTop && !isSearchOpen,
                    isDark = isDark,
                    onClick = {
                        presets.ping()
                        isScrollingUp = false
                        isNavBarVisible = true
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
                        .zIndex(15f)
                        .padding(top = 72.dp + 16.dp + androidx.compose.foundation.layout.WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
                )
            }
        }

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
                Toast.makeText(context, "Record deleted successfully", Toast.LENGTH_SHORT).show()
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
            updateTrigger = txDetailUpdateTrigger,
            onDismiss = { txDetailToShow = null },
            onEditClick = {
                transactionToEdit = tx
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
