package com.abhik.paisatrack.ui.components.dashboard

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.abhik.paisatrack.ui.components.commonUi.CloseSmallRoundedIconVector
import com.abhik.paisatrack.ui.components.commonUi.ArrowLeftAltIconVector
import com.abhik.paisatrack.ui.components.commonUi.CalendarMonthIconVector
import com.abhik.paisatrack.ui.components.commonUi.SearchRoundedIconVector
import com.abhik.paisatrack.ui.components.commonUi.SearchDatePickerDialog
import com.abhik.paisatrack.ui.components.commonUi.EditTransactionBottomSheet
import com.abhik.paisatrack.R
import com.abhik.paisatrack.data.model.TransactionEntity
import com.abhik.paisatrack.ui.FinanceUiState
import com.abhik.paisatrack.ui.FinanceViewModel
import com.abhik.paisatrack.ui.components.commonUi.shimmerEffect
import com.abhik.paisatrack.ui.components.commonUi.DeleteRoundedIconVector
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.toShape
import com.airbnb.lottie.compose.*
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt
import android.content.Context
import android.view.inputmethod.InputMethodManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.geometry.Offset
import kotlin.math.absoluteValue
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.foundation.gestures.detectTapGestures
import com.abhik.paisatrack.ui.components.commonUi.AnimatedTuneIcon
import com.abhik.paisatrack.ui.components.commonUi.DeleteTransactionConfirmDialog
import com.abhik.paisatrack.ui.components.commonUi.FilterBottomSheet
import com.abhik.paisatrack.ui.components.getIconByName
import com.abhik.paisatrack.ui.components.formatShortDate
import com.abhik.paisatrack.ui.utils.safeParseColor
import com.abhik.paisatrack.ui.utils.findActivity
import com.abhik.paisatrack.ui.utils.getSafePresets
import com.abhik.paisatrack.ui.utils.getSafeRealtimeComposer
import com.swmansion.pulsar.Pulsar
import com.swmansion.pulsar.types.RealtimeComposerStrategy
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TransactionSkeletonItem(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f)),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(MaterialShapes.Cookie4Sided.toShape())
                    .shimmerEffect()
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .width(120.dp)
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmerEffect()
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .height(12.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmerEffect()
                )
            }
            Column(
                modifier = Modifier.padding(start = 8.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmerEffect()
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .width(50.dp)
                        .height(12.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmerEffect()
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
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

    val context = LocalContext.current
    // This row also renders inside the full-screen search dialog, where LocalContext is the
    // dialog's ContextThemeWrapper. Pulsar casts its context to Activity, so resolve the host.
    val pulsar = remember(context) { Pulsar(context.findActivity() ?: context) }
    val presets = remember(pulsar) { pulsar.getSafePresets() }

    // Match the transaction badge styling to the collection cards by using the collection color directly.
    val (badgeBg, iconColor) = remember(categoryColor) {
        categoryColor.copy(alpha = 0.15f) to categoryColor
    }

    val isIncome = transaction.type.uppercase() == "INCOME"
    val semanticDescription = remember(transaction.description, categoryName, isIncome, transaction.amount, dateStr) {
        "${transaction.description}, $categoryName, ${if (isIncome) "Income" else "Expense"} ${dollarFormat.format(transaction.amount)}, $dateStr"
    }

    @Suppress("DEPRECATION")
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                presets.cleave()
                onDeleteClick()
                false
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            val isSwipingToDelete = dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart
            val iconScale by animateFloatAsState(
                targetValue = if (isSwipingToDelete) 1.15f else 0.85f,
                label = "DeleteIconScale"
            )
            val alpha by animateFloatAsState(
                targetValue = if (isSwipingToDelete) 1f else 0.6f,
                label = "DeleteAlpha"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFEF4444).copy(alpha = 0.95f))
                    .padding(end = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .alpha(alpha)
                        .scale(iconScale)
                ) {
                    Text(
                        text = "Delete",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Icon(
                        imageVector = DeleteRoundedIconVector,
                        contentDescription = "Swipe to delete",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick
                )
                .semantics {
                    contentDescription = semanticDescription
                    customActions = listOf(
                        CustomAccessibilityAction("Delete") {
                            presets.cleave()
                            onDeleteClick()
                            true
                        },
                        CustomAccessibilityAction("View details") {
                            presets.boulder()
                            onClick()
                            true
                        }
                    )
                },
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f)),
            shadowElevation = 1.dp
        ) {
            ListItem(
                headlineContent = {
                    Text(
                        text = transaction.description,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                supportingContent = {
                    Text(
                        text = categoryName,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                leadingContent = {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(MaterialShapes.Cookie4Sided.toShape())
                            .background(badgeBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = categoryIcon,
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                trailingContent = {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "${if (isIncome) "+" else "-"}${dollarFormat.format(transaction.amount)}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            color = if (isIncome) Color(0xFF10B981) else Color(0xFFEF4444),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = dateStr,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                colors = ListItemDefaults.colors(
                    containerColor = Color.Transparent
                )
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TransactionsPanel(
    uiState: FinanceUiState,
    viewModel: FinanceViewModel,
    dollarFormat: DecimalFormat,
    onScrollProgressChanged: (Boolean) -> Unit,
    onSearchActiveChanged: (Boolean) -> Unit = {},
    onTransactionLongClick: (TransactionEntity) -> Unit,
    onTransactionClick: (TransactionEntity) -> Unit,
    onBackToTop: (suspend () -> Unit) -> Unit
) {
    val context = LocalContext.current
    val pulsar = remember(context) { Pulsar(context.findActivity() ?: context) }
    val presets = remember(pulsar) { pulsar.getSafePresets() }
    val listState = rememberLazyListState()
    var visibleLimit by rememberSaveable { mutableStateOf(20) }
    var animationStartLimit by rememberSaveable { mutableStateOf(0) }
    var isLoadingMore by remember { mutableStateOf(false) }
    var txToDelete by remember { mutableStateOf<TransactionEntity?>(null) }
    var searchTransactionToEdit by remember { mutableStateOf<TransactionEntity?>(null) }

    LaunchedEffect(uiState.activeCollectionFilter, uiState.activeTimeFilter, uiState.activeTypeFilter, uiState.activeSortOrder, uiState.searchQuery) {
        visibleLimit = 20
        animationStartLimit = 0
    }
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

    // Material 3 full-screen search. The input is driven by a TextFieldState which we mirror
    // into the ViewModel's search query (debounced downstream to drive filtering + summary).
    val searchBarState = rememberSearchBarState()
    val textFieldState = rememberTextFieldState()
    var showSearchDatePicker by remember { mutableStateOf(false) }

    val view = LocalView.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val searchSuggestionsListState = rememberLazyListState()
    val searchResultsListState = rememberLazyListState()

    val searchBarColors = SearchBarDefaults.colors(
        containerColor = MaterialTheme.colorScheme.background,
        dividerColor = Color.Transparent,
        inputFieldColors = SearchBarDefaults.inputFieldColors(
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedLeadingIconColor = MaterialTheme.colorScheme.onSurface,
            unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurface,
            focusedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unfocusedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    )

    LaunchedEffect(searchSuggestionsListState.isScrollInProgress) {
        if (searchSuggestionsListState.isScrollInProgress) {
            keyboardController?.hide()
            focusManager.clearFocus()
        }
    }

    LaunchedEffect(searchResultsListState.isScrollInProgress) {
        if (searchResultsListState.isScrollInProgress) {
            keyboardController?.hide()
            focusManager.clearFocus()
        }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { textFieldState.text.toString() }
            .collect { viewModel.setSearchQuery(it) }
    }
    // When the search view collapses, reset the query and the date filter so neither leaks
    // into the dashboard list.
    LaunchedEffect(searchBarState.targetValue) {
        if (searchBarState.targetValue == SearchBarValue.Collapsed) {
            keyboardController?.hide()
            focusManager.clearFocus()
            textFieldState.clearText()
            viewModel.clearSearchDate()
            showSearchDatePicker = false
        }
    }
    val isAnyFilterActive = remember(uiState.activeTimeFilter, uiState.activeTypeFilter, uiState.activeSortOrder) {
        uiState.activeTimeFilter != "All" || uiState.activeTypeFilter != "All" || uiState.activeSortOrder != "Newest"
    }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    // Looping typewriter reveal animation for dashboard search placeholder: "entry", "record", "cash in", "cash out"
    val searchKeywords = remember { listOf("entry", "record", "cash in", "cash out") }
    var currentKeywordIndex by remember { mutableIntStateOf(0) }
    var displayedKeyword by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        while (true) {
            val word = searchKeywords[currentKeywordIndex]
            for (i in 1..word.length) {
                displayedKeyword = word.substring(0, i)
                delay(90L)
            }
            delay(1300L)
            for (i in (word.length - 1) downTo 0) {
                displayedKeyword = word.substring(0, i)
                delay(50L)
            }
            delay(200L)
            currentKeywordIndex = (currentKeywordIndex + 1) % searchKeywords.size
        }
    }

    // Extended full-screen search placeholder typewriter loop: "name", "date", "title"
    val extendedSearchKeywords = remember { listOf("name", "date", "title") }
    var currentExtendedIndex by remember { mutableIntStateOf(0) }
    var displayedExtendedKeyword by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        while (true) {
            val word = extendedSearchKeywords[currentExtendedIndex]
            for (i in 1..word.length) {
                displayedExtendedKeyword = word.substring(0, i)
                delay(90L)
            }
            delay(1300L)
            for (i in (word.length - 1) downTo 0) {
                displayedExtendedKeyword = word.substring(0, i)
                delay(50L)
            }
            delay(200L)
            currentExtendedIndex = (currentExtendedIndex + 1) % extendedSearchKeywords.size
        }
    }

    val showBackToTop by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 300
        }
    }

    val paginatedTransactions = remember(uiState.filteredTransactions, visibleLimit) {
        uiState.filteredTransactions.take(visibleLimit)
    }

    val isScrolled by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 20
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
            // Sticky Header: Recent transactions list header with Scroll-Driven Expanding Search Bar
            stickyHeader {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 1. "Recent Activity" Title (Collapses & fades out when scrolled)
                        AnimatedVisibility(
                            visible = !isScrolled,
                            enter = fadeIn(tween(220)) + expandHorizontally(spring(stiffness = Spring.StiffnessMediumLow)),
                            exit = fadeOut(tween(180)) + shrinkHorizontally(spring(stiffness = Spring.StiffnessMediumLow))
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Recent Activity",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                            }
                        }

                        // 2. Search Container (Expands from 36.dp circular icon into full M3 pill search bar)
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = if (isScrolled) Alignment.CenterStart else Alignment.CenterEnd
                        ) {
                            AnimatedContent(
                                targetState = isScrolled,
                                transitionSpec = {
                                    (fadeIn(tween(200)) + expandHorizontally(spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow))) togetherWith
                                        (fadeOut(tween(150)) + shrinkHorizontally(spring(stiffness = Spring.StiffnessMediumLow)))
                                },
                                label = "SearchIconPillTransition"
                            ) { scrolled ->
                                if (scrolled) {
                                    // Expanded M3 Pill-shaped Search Bar
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(44.dp)
                                            .clip(CircleShape)
                                            .clickable {
                                                presets.plunk()
                                                scope.launch { searchBarState.animateToExpanded() }
                                            },
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f)),
                                        shadowElevation = 1.dp
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(start = 12.dp, end = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                modifier = Modifier.weight(1f),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = SearchRoundedIconVector,
                                                    contentDescription = "Search",
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(19.dp)
                                                )

                                                Spacer(modifier = Modifier.width(10.dp))

                                                val dateActive = uiState.searchDateStart != null
                                                val dateLabel = uiState.searchDateLabel
                                                if (dateActive && dateLabel != null) {
                                                    Surface(
                                                        shape = CircleShape,
                                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                                    ) {
                                                        Row(
                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Icon(
                                                                imageVector = CalendarMonthIconVector,
                                                                contentDescription = null,
                                                                tint = MaterialTheme.colorScheme.primary,
                                                                modifier = Modifier.size(12.dp)
                                                            )
                                                            Spacer(modifier = Modifier.width(4.dp))
                                                            Text(
                                                                text = dateLabel,
                                                                fontSize = 11.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = MaterialTheme.colorScheme.primary
                                                            )
                                                            Spacer(modifier = Modifier.width(3.dp))
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(16.dp)
                                                                    .clip(CircleShape)
                                                                    .clickable {
                                                                        presets.plunk()
                                                                        viewModel.clearSearchDate()
                                                                    },
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                Icon(
                                                                    imageVector = CloseSmallRoundedIconVector,
                                                                    contentDescription = "Clear date",
                                                                    tint = MaterialTheme.colorScheme.primary,
                                                                    modifier = Modifier.size(12.dp)
                                                                )
                                                            }
                                                        }
                                                    }
                                                } else {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            text = "Search ",
                                                            fontSize = 13.sp,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                                            maxLines = 1
                                                        )
                                                        Text(
                                                            text = displayedKeyword,
                                                            fontSize = 13.sp,
                                                            fontWeight = FontWeight.Medium,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                                                            maxLines = 1
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    // Initial State: Compact 36.dp Circular Search Button
                                    Box(
                                        modifier = Modifier.size(36.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                                .clickable {
                                                    presets.plunk()
                                                    scope.launch { searchBarState.animateToExpanded() }
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = SearchRoundedIconVector,
                                                contentDescription = "Search",
                                                tint = MaterialTheme.colorScheme.onBackground,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // 3. Filter triggers icon button (Always available on the right)
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
                                AnimatedTuneIcon(
                                    isActive = showFilters,
                                    tint = if (showFilters) MaterialTheme.colorScheme.background else if (isAnyFilterActive) Color(0xFF10B981) else MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Tiny active filter feedback dot shifted to sit above the circular button boundary
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
            if (uiState.isLoading) {
                items(5) {
                    TransactionSkeletonItem()
                }
            } else if (uiState.filteredTransactions.isEmpty()) {
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
                                text = "No Activity Yet",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Change filters or tap the “+” button below to add a new entry.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                itemsIndexed(paginatedTransactions, key = { _, tx -> tx.id }) { index, tx ->
                    // Find parent collection for styling information
                    val parentCollection = uiState.collections.find { it.id == tx.collectionId }
                    val categoryColor = safeParseColor(parentCollection?.hexColor)

                    val alpha = remember(tx.id) { Animatable(if (index >= animationStartLimit) 0f else 1f) }
                    LaunchedEffect(tx.id) {
                        if (index >= animationStartLimit) {
                            delay((index - animationStartLimit) * 25L)
                            alpha.animateTo(1f, tween(150))
                        }
                    }

                    Box(modifier = Modifier.alpha(alpha.value)) {
                        TransactionListItem(
                            transaction = tx,
                            categoryName = parentCollection?.name ?: "General",
                            categoryColor = categoryColor,
                            categoryIcon = getIconByName(parentCollection?.iconName ?: "category"),
                            dollarFormat = dollarFormat,
                            onDeleteClick = {
                                txToDelete = tx
                            },
                            onLongClick = {
                                presets.bassDrop()
                                onTransactionLongClick(tx)
                            },
                            onClick = {
                                presets.boulder()
                                onTransactionClick(tx)
                            }
                        )
                    }
                }

                if (uiState.filteredTransactions.size > visibleLimit) {
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
                                            delay(800)
                                            animationStartLimit = visibleLimit
                                            visibleLimit += 20
                                            isLoadingMore = false
                                        }
                                    },
                                    shape = CircleShape,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 24.dp)
                                ) {
                                    Text(
                                        text = "Load More (${uiState.filteredTransactions.size - visibleLimit} remaining)",
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

        AnimatedVisibility(
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
                    onBackToTop {
                        if (listState.firstVisibleItemIndex > 2) {
                            listState.scrollToItem(2)
                        }
                        listState.animateScrollToItem(0)
                    }
                },
                elevation = ButtonDefaults.filledTonalButtonElevation(defaultElevation = 6.dp),
                shape = CircleShape,
                modifier = Modifier.height(40.dp)
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

        val isSearchExpanded = searchBarState.targetValue == SearchBarValue.Expanded || searchBarState.currentValue == SearchBarValue.Expanded

        LaunchedEffect(isSearchExpanded) {
            onSearchActiveChanged(isSearchExpanded)
        }

        BackHandler(enabled = isSearchExpanded) {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(view.windowToken, 0)
            keyboardController?.hide()
            focusManager.clearFocus()
            scope.launch { searchBarState.animateToCollapsed() }
        }

        // Full-screen Search Overlay with smooth bottom-to-top slide-in and reverse slide-out
        AnimatedVisibility(
            visible = isSearchExpanded,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = spring(
                    dampingRatio = 0.82f,
                    stiffness = Spring.StiffnessMediumLow
                )
            ) + fadeIn(animationSpec = tween(220)),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = spring(
                    dampingRatio = 0.85f,
                    stiffness = Spring.StiffnessMediumLow
                )
            ) + fadeOut(animationSpec = tween(180)),
            modifier = Modifier
                .fillMaxSize()
                .zIndex(100f)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val searchContext = LocalContext.current
                    val searchView = LocalView.current
                    val searchKeyboard = LocalSoftwareKeyboardController.current
                    val searchFocus = LocalFocusManager.current

                    val hideKeyboardNow = remember(searchContext, searchView, searchKeyboard, searchFocus) {
                        {
                            searchKeyboard?.hide()
                            searchFocus.clearFocus(force = true)
                            val imm = searchContext.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                            imm?.hideSoftInputFromWindow(searchView.windowToken, 0)
                            imm?.hideSoftInputFromWindow(searchView.applicationWindowToken, 0)
                        }
                    }

                    val searchScrollConnection = remember(hideKeyboardNow) {
                        object : NestedScrollConnection {
                            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                                if (source == NestedScrollSource.UserInput && available.y.absoluteValue > 0.5f) {
                                    hideKeyboardNow()
                                }
                                return Offset.Zero
                            }
                        }
                    }

                    // Top Search Bar Input Field
                    SearchBarDefaults.InputField(
                        textFieldState = textFieldState,
                        searchBarState = searchBarState,
                        onSearch = {
                            hideKeyboardNow()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        shape = CircleShape,
                        colors = searchBarColors.inputFieldColors,
                        placeholder = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Search by ",
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = displayedExtendedKeyword,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
                                )
                            }
                        },
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        presets.plunk()
                                        hideKeyboardNow()
                                        scope.launch { searchBarState.animateToCollapsed() }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = ArrowLeftAltIconVector,
                                    contentDescription = "Close search",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        },
                        trailingIcon = {
                            if (textFieldState.text.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .clickable {
                                            presets.plunk()
                                            textFieldState.clearText()
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = CloseSmallRoundedIconVector,
                                        contentDescription = "Clear search",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            } else {
                                val dateActive = uiState.searchDateStart != null
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .clickable {
                                            presets.plunk()
                                            showSearchDatePicker = true
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = CalendarMonthIconVector,
                                        contentDescription = "Filter by date",
                                        tint = if (dateActive) MaterialTheme.colorScheme.primary
                                               else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    )

                    // Search Content Body with Tap & Scroll Keyboard Dismissal
                    val query = uiState.searchQuery
                    val dateLabel = uiState.searchDateLabel

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .nestedScroll(searchScrollConnection)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onPress = {
                                        hideKeyboardNow()
                                    }
                                )
                            }
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {

                            if (query.isBlank() && uiState.searchDateStart == null) {
                                LazyColumn(
                                    state = searchSuggestionsListState,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .nestedScroll(searchScrollConnection)
                                        .imePadding(),
                                    contentPadding = PaddingValues(bottom = 24.dp)
                                ) {
                                    if (uiState.frequentNames.isEmpty()) {
                                        item {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(32.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "Start typing to search your records by name, or pick a date.",
                                                    fontSize = 14.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        }
                                    } else {
                                        item {
                                            Text(
                                                text = "Frequent",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(start = 24.dp, top = 12.dp, bottom = 4.dp)
                                            )
                                        }
                                        items(uiState.frequentNames) { name ->
                                            ListItem(
                                                headlineContent = {
                                                    Text(
                                                        text = name,
                                                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
                                                        color = MaterialTheme.colorScheme.onSurface,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                },
                                                leadingContent = {
                                                    Icon(
                                                        imageVector = SearchRoundedIconVector,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                },
                                                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        presets.plunk()
                                                        hideKeyboardNow()
                                                        textFieldState.setTextAndPlaceCursorAtEnd(name)
                                                    }
                                                    .padding(horizontal = 8.dp)
                                            )
                                        }
                                    }
                                }
                            } else {
                                val results = uiState.searchResults
                                LazyColumn(
                                    state = searchResultsListState,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .nestedScroll(searchScrollConnection)
                                        .imePadding(),
                                    contentPadding = PaddingValues(bottom = 24.dp)
                                ) {
                                    uiState.searchSummary?.let { s ->
                                        item {
                                            Surface(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 24.dp, vertical = 8.dp),
                                                shape = RoundedCornerShape(16.dp),
                                                color = MaterialTheme.colorScheme.surfaceVariant
                                            ) {
                                                Column(modifier = Modifier.padding(16.dp)) {
                                                    Text(
                                                        text = (if (s.query.isBlank()) dateLabel ?: "All time"
                                                                else "\"${s.query}\"") +
                                                            " — ${s.matchCount} " +
                                                            if (s.matchCount == 1) "record" else "records",
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = buildString {
                                                            if (s.totalSent > 0) append("Cash out: ${dollarFormat.format(s.totalSent)}")
                                                            if (s.totalSent > 0 && s.totalReceived > 0) append("  ·  ")
                                                            if (s.totalReceived > 0) append("Cash in: ${dollarFormat.format(s.totalReceived)}")
                                                        },
                                                        fontSize = 13.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    if (results.isEmpty()) {
                                        item {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 32.dp, vertical = 24.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                ) {
                                                    LottieAnimation(
                                                        composition = composition,
                                                        progress = { progress },
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .height(200.dp)
                                                    )
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    Text(
                                                        text = if (query.isBlank()) "No records on ${dateLabel ?: "this date"}"
                                                               else "No results for \"${query.trim()}\"",
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onBackground,
                                                        textAlign = TextAlign.Center
                                                    )
                                                }
                                            }
                                        }
                                    } else {
                                        itemsIndexed(results, key = { _, tx -> tx.id }) { _, tx ->
                                            val parentCollection = uiState.collections.find { it.id == tx.collectionId }
                                            val categoryColor = safeParseColor(parentCollection?.hexColor)
                                            TransactionListItem(
                                                transaction = tx,
                                                categoryName = parentCollection?.name ?: "General",
                                                categoryColor = categoryColor,
                                                categoryIcon = getIconByName(parentCollection?.iconName ?: "category"),
                                                dollarFormat = dollarFormat,
                                                onDeleteClick = {
                                                    hideKeyboardNow()
                                                    txToDelete = tx
                                                },
                                                onLongClick = {
                                                    presets.bassDrop()
                                                    hideKeyboardNow()
                                                    searchTransactionToEdit = tx
                                                },
                                                onClick = {
                                                    presets.boulder()
                                                    hideKeyboardNow()
                                                    searchTransactionToEdit = tx
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
        }
    }

    if (showSearchDatePicker) {
        SearchDatePickerDialog(
            activeStart = uiState.searchDateStart,
            activeEnd = uiState.searchDateEnd,
            onConfirm = { start, end ->
                viewModel.setSearchDateRange(start, end)
                showSearchDatePicker = false
            },
            onClear = {
                viewModel.clearSearchDate()
                showSearchDatePicker = false
            },
            onDismiss = { showSearchDatePicker = false }
        )
    }

    // Shared Filter Bottom Sheet
    if (showFilters) {
        FilterBottomSheet(
            sheetState = sheetState,
            activeSortOrder = uiState.activeSortOrder,
            activeTypeFilter = uiState.activeTypeFilter,
            activeTimeFilter = uiState.activeTimeFilter,
            onSortOrderChange = { viewModel.setSortOrder(it) },
            onTypeFilterChange = { viewModel.setTypeFilter(it) },
            onTimeFilterChange = { viewModel.setTimeFilter(it) },
            onDismissRequest = { showFilters = false }
        )
    }

    // Delete Confirmation Dialog
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

    // Edit Transaction Bottom Sheet in Search Results
    if (searchTransactionToEdit != null) {
        val tx = searchTransactionToEdit!!
        EditTransactionBottomSheet(
            transaction = tx,
            collections = uiState.collections,
            onDismiss = { searchTransactionToEdit = null },
            onSave = { desc, amount, type, collectionId ->
                viewModel.updateTransaction(
                    id = tx.id,
                    description = desc,
                    amount = amount,
                    type = type,
                    collectionId = collectionId,
                    timestamp = tx.timestamp
                )
                searchTransactionToEdit = null
                Toast.makeText(context, "Record updated successfully", Toast.LENGTH_SHORT).show()
            }
        )
    }
}
