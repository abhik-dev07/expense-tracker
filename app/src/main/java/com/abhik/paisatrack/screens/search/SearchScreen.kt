package com.abhik.paisatrack.screens.search

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import kotlin.math.absoluteValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.abhik.paisatrack.data.model.TransactionEntity
import com.abhik.paisatrack.data.repository.RecentSearchManager
import com.abhik.paisatrack.utils.FinanceViewModel
import com.abhik.paisatrack.ui.components.commonUi.DeleteTransactionConfirmDialog
import com.abhik.paisatrack.ui.components.commonUi.EditTransactionBottomSheet
import com.abhik.paisatrack.ui.components.commonUi.SearchDatePickerDialog
import com.abhik.paisatrack.screens.dashboard.components.TransactionListItem
import com.abhik.paisatrack.utils.getIconByName
import com.abhik.paisatrack.utils.safeParseColor
import com.abhik.paisatrack.screens.search.components.FinancialFilterBottomSheet
import com.abhik.paisatrack.screens.search.components.FinancialShortcutsSection
import com.abhik.paisatrack.screens.search.components.FinancialShimmerSkeleton
import com.abhik.paisatrack.screens.search.components.FinancialSummaryStrip
import com.abhik.paisatrack.screens.search.components.MatchingCollectionsSection
import com.abhik.paisatrack.screens.search.components.PaginationFooter
import com.abhik.paisatrack.screens.search.components.RecentSearchesSection
import com.abhik.paisatrack.screens.search.components.SearchPillHeader
import com.abhik.paisatrack.screens.search.components.SearchResultsHeader
import com.abhik.paisatrack.ui.theme.SleekPrimary
import com.abhik.paisatrack.ui.theme.isAppInDarkTheme
import kotlinx.coroutines.delay
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToCollectionTransactions: (String) -> Unit = {},
    viewModel: FinanceViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val isDark = isAppInDarkTheme()

    val dollarFormat = remember { DecimalFormat("₹#,##0.00") }
    val recentSearchManager = remember { RecentSearchManager(context) }
    val recentSearches by recentSearchManager.recentSearches.collectAsState()

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val lazyListState = rememberLazyListState()

    var visibleLimit by rememberSaveable { mutableIntStateOf(20) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showFilters by remember { mutableStateOf(false) }
    var isFilterShimmering by remember { mutableStateOf(false) }
    var txToDelete by remember { mutableStateOf<TransactionEntity?>(null) }
    var txToEdit by remember { mutableStateOf<TransactionEntity?>(null) }

    LaunchedEffect(isFilterShimmering) {
        if (isFilterShimmering) {
            delay(400)
            isFilterShimmering = false
        }
    }

    val isSearchActive = uiState.searchQuery.isNotBlank() || uiState.searchDateStart != null

    val isFilterActive = uiState.activeTypeFilter != "All" ||
            uiState.activeCollectionFilter != null ||
            uiState.activeSortOrder != "Newest"

    val activeFiltersCount = listOfNotNull(
        if (uiState.activeTypeFilter != "All") 1 else null,
        if (uiState.activeCollectionFilter != null) 1 else null,
        if (uiState.activeSortOrder != "Newest") 1 else null
    ).size

    BackHandler {
        onBack()
    }


    // Hide keyboard when user begins scrolling
    LaunchedEffect(lazyListState.isScrollInProgress) {
        if (lazyListState.isScrollInProgress) {
            keyboardController?.hide()
            focusManager.clearFocus()
        }
    }

    // Reset pagination window when query, dates, or filters change
    LaunchedEffect(
        uiState.searchQuery,
        uiState.searchDateStart,
        uiState.searchDateEnd,
        uiState.activeTypeFilter,
        uiState.activeCollectionFilter,
        uiState.activeSortOrder
    ) {
        visibleLimit = 20
    }

    val paginatedRecords = remember(uiState.searchResults, visibleLimit) {
        uiState.searchResults.take(visibleLimit)
    }
    val canLoadMore = uiState.searchResults.size > paginatedRecords.size

    // Infinite scroll listener to reveal more local records
    LaunchedEffect(lazyListState, uiState.searchResults.size, visibleLimit) {
        snapshotFlow { lazyListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastIndex ->
                if (lastIndex != null && lastIndex >= paginatedRecords.size - 4 && canLoadMore) {
                    visibleLimit = minOf(visibleLimit + 20, uiState.searchResults.size)
                }
            }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
        ) {
            // Fixed top search pill header (with calendar icon in trailing position)
            SearchPillHeader(
                query = uiState.searchQuery,
                onQueryChange = { newQuery ->
                    viewModel.setSearchQuery(newQuery)
                },
                onClearQuery = {
                    viewModel.clearSearchQuery()
                },
                onImeSearch = {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                    if (uiState.searchQuery.trim().isNotBlank()) {
                        recentSearchManager.addSearchQuery(uiState.searchQuery.trim())
                    }
                },
                onBack = onBack,
                onCalendarClick = {
                    showDatePicker = true
                },
                isDateFilterActive = uiState.searchDateStart != null,
                isLoading = uiState.isLoading,
                focusRequester = focusRequester,
                modifier = Modifier
                    .fillMaxWidth()
                    .zIndex(10f)
            )

            val searchScrollConnection = remember {
                object : NestedScrollConnection {
                    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                        if (source == NestedScrollSource.UserInput && available.y.absoluteValue > 0.5f) {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                        }
                        return Offset.Zero
                    }
                }
            }

            // Primary vertical scrollable content area with tap-outside and scroll keyboard dismissal
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .nestedScroll(searchScrollConnection)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                keyboardController?.hide()
                                focusManager.clearFocus()
                            }
                        )
                    }
            ) {
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 40.dp)
                ) {
                if (isFilterShimmering || (uiState.isLoading && uiState.rawTransactions.isEmpty())) {
                    // Shimmer skeleton state
                    item {
                        FinancialShimmerSkeleton()
                    }
                } else if (!isSearchActive) {
                    // EMPTY QUERY / DISCOVERY STATE
                    // 1. Recent Searches
                    item {
                        RecentSearchesSection(
                            recentSearches = recentSearches,
                            onSearchClick = { query ->
                                viewModel.setSearchQuery(query)
                                recentSearchManager.addSearchQuery(query)
                                keyboardController?.hide()
                                focusManager.clearFocus()
                            },
                            onClearClick = {
                                recentSearchManager.clearAll()
                            }
                        )
                    }

                    // 2. Financial Shortcuts (Frequent records, Collections, Time presets)
                    item {
                        FinancialShortcutsSection(
                            frequentNames = uiState.frequentNames,
                            collections = uiState.collections,
                            onNameClick = { name ->
                                viewModel.setSearchQuery(name)
                                recentSearchManager.addSearchQuery(name)
                                keyboardController?.hide()
                                focusManager.clearFocus()
                            },
                            onCollectionClick = { col ->
                                onNavigateToCollectionTransactions(col.id)
                            },
                            onDatePresetClick = { start, end ->
                                viewModel.setSearchDateRange(start, end)
                            }
                        )
                    }
                } else {
                    // ACTIVE SEARCH STATE
                    // 1. Results Heading & Filter Count
                    item {
                        SearchResultsHeader(
                            query = uiState.searchQuery,
                            resultCount = uiState.searchResults.size,
                            dateLabel = uiState.searchDateLabel,
                            isFilterActive = isFilterActive,
                            activeFiltersCount = activeFiltersCount,
                            onFilterClick = { showFilters = true },
                            onClearDate = { viewModel.clearSearchDate() }
                        )
                    }

                    // 2. Compact Financial Summary Card
                    uiState.searchSummary?.let { summary ->
                        item {
                            FinancialSummaryStrip(
                                summary = summary,
                                dollarFormat = dollarFormat
                            )
                        }
                    }

                    // 3. Matching Collections (if query matches collections)
                    if (uiState.matchingCollections.isNotEmpty()) {
                        item {
                            MatchingCollectionsSection(
                                collections = uiState.matchingCollections,
                                rawTransactions = uiState.rawTransactions,
                                dollarFormat = dollarFormat,
                                onCollectionClick = { col ->
                                    if (uiState.searchQuery.trim().isNotBlank()) {
                                        recentSearchManager.addSearchQuery(uiState.searchQuery.trim())
                                    }
                                    onNavigateToCollectionTransactions(col.id)
                                }
                            )
                        }
                    }

                    // 4. Matching Transactions or Empty Results
                    if (uiState.searchResults.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 32.dp, vertical = 48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isDark) Color(0xFF222228) else Color(0xFFF1F5F3)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.SearchOff,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }

                                    Text(
                                        text = when {
                                            uiState.searchQuery.isNotBlank() && uiState.searchDateLabel != null ->
                                                "No records found for \"${uiState.searchQuery}\" in ${uiState.searchDateLabel}"
                                            uiState.searchQuery.isNotBlank() ->
                                                "No records found for \"${uiState.searchQuery}\""
                                            uiState.searchDateLabel != null ->
                                                "No records found on ${uiState.searchDateLabel}"
                                            else -> "No matching records found"
                                        },
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        textAlign = TextAlign.Center
                                    )

                                    Text(
                                        text = "Try adjusting your query, clearing filters, or selecting a broader date range.",
                                        fontSize = 13.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        if (uiState.searchQuery.isNotBlank()) {
                                            OutlinedButton(
                                                onClick = { viewModel.clearSearchQuery() },
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Text("Clear query")
                                            }
                                        }

                                        if (isFilterActive || uiState.searchDateStart != null) {
                                            Button(
                                                onClick = {
                                                    viewModel.clearSearchDate()
                                                    viewModel.setTypeFilter("All")
                                                    viewModel.setSortOrder("Newest")
                                                    viewModel.setCollectionFilter(null)
                                                },
                                                shape = RoundedCornerShape(12.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary)
                                            ) {
                                                Text("Reset filters")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        itemsIndexed(
                            items = paginatedRecords,
                            key = { _, tx -> tx.id }
                        ) { _, tx ->
                            val parentCollection = uiState.collections.find { it.id == tx.collectionId }
                            val categoryColor = safeParseColor(parentCollection?.hexColor)

                            TransactionListItem(
                                transaction = tx,
                                categoryName = parentCollection?.name ?: "General",
                                categoryColor = categoryColor,
                                categoryIcon = getIconByName(parentCollection?.iconName ?: "category"),
                                dollarFormat = dollarFormat,
                                onDeleteClick = {
                                    keyboardController?.hide()
                                    focusManager.clearFocus()
                                    txToDelete = tx
                                },
                                onLongClick = {
                                    keyboardController?.hide()
                                    focusManager.clearFocus()
                                    txToEdit = tx
                                },
                                onClick = {
                                    keyboardController?.hide()
                                    focusManager.clearFocus()
                                    if (uiState.searchQuery.trim().isNotBlank()) {
                                        recentSearchManager.addSearchQuery(uiState.searchQuery.trim())
                                    }
                                    txToEdit = tx
                                }
                            )
                        }

                        // 5. Pagination Footer
                        item {
                            PaginationFooter(
                                isLoading = false,
                                error = null,
                                isEndOfResults = !canLoadMore,
                                onRetry = {
                                    visibleLimit = minOf(visibleLimit + 20, uiState.searchResults.size)
                                }
                            )
                        }
                    }
                }
            } // Close LazyColumn
            } // Close Box wrapping LazyColumn
        }

        // Date Picker Dialog
        if (showDatePicker) {
            SearchDatePickerDialog(
                activeStart = uiState.searchDateStart,
                activeEnd = uiState.searchDateEnd,
                onConfirm = { start, end ->
                    isFilterShimmering = true
                    viewModel.setSearchDateRange(start, end)
                    showDatePicker = false
                },
                onClear = {
                    isFilterShimmering = true
                    viewModel.clearSearchDate()
                    showDatePicker = false
                },
                onDismiss = {
                    showDatePicker = false
                }
            )
        }

        // Filters Bottom Sheet
        if (showFilters) {
            FinancialFilterBottomSheet(
                activeSortOrder = uiState.activeSortOrder,
                activeTypeFilter = uiState.activeTypeFilter,
                activeCollectionId = uiState.activeCollectionFilter,
                collections = uiState.collections,
                onApplyFilters = { sort, type, colId ->
                    isFilterShimmering = true
                    viewModel.setSortOrder(sort)
                    viewModel.setTypeFilter(type)
                    viewModel.setCollectionFilter(colId)
                    showFilters = false
                },
                onDismissRequest = {
                    showFilters = false
                }
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
                    txToDelete = null
                }
            )
        }

        // Edit Transaction Bottom Sheet
        if (txToEdit != null) {
            val tx = txToEdit!!
            EditTransactionBottomSheet(
                transaction = tx,
                collections = uiState.collections,
                onDismiss = { txToEdit = null },
                onSave = { desc, amount, type, colId ->
                    viewModel.updateTransaction(
                        id = tx.id,
                        description = desc,
                        amount = amount,
                        type = type,
                        collectionId = colId,
                        timestamp = tx.timestamp
                    )
                    txToEdit = null
                }
            )
        }
    }
}
