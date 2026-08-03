package com.abhik.paisatrack.ui.components.dashboard

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhik.paisatrack.R
import com.abhik.paisatrack.data.model.TransactionEntity
import com.abhik.paisatrack.ui.FinanceUiState
import com.abhik.paisatrack.ui.FinanceViewModel
import com.abhik.paisatrack.ui.components.commonUi.shimmerEffect
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import com.airbnb.lottie.compose.*
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import com.abhik.paisatrack.ui.components.commonUi.AnimatedTuneIcon
import com.abhik.paisatrack.ui.components.commonUi.FilterBottomSheet
import com.abhik.paisatrack.ui.components.getIconByName
import com.swmansion.pulsar.Pulsar
import com.swmansion.pulsar.types.RealtimeComposerStrategy
import kotlinx.coroutines.delay

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
                    .clip(RoundedCornerShape(16.dp))
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
                Spacer(modifier = Modifier.height(8.dp))
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
                Spacer(modifier = Modifier.height(8.dp))
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

    val context = LocalContext.current
    val pulsar = remember { Pulsar(context) }
    val presets = remember { com.abhik.paisatrack.ui.utils.SafePresets(pulsar.getPresets()) }
    val realtime = remember { com.abhik.paisatrack.ui.utils.SafeRealtimeComposer(pulsar.getRealtimeComposer(RealtimeComposerStrategy.PRIMITIVE_TICK)) }

    var swipeOffsetX by remember { mutableStateOf(0f) }
    val animatedSwipeOffset by animateFloatAsState(
        targetValue = swipeOffsetX,
        animationSpec = tween(150),
        label = "SwipeOffsetDashboard"
    )

    // Match the transaction badge styling to the collection cards by using the collection color directly.
    val (badgeBg, iconColor) = remember(categoryColor) {
        categoryColor.copy(alpha = 0.15f) to categoryColor
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
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f)),
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
                        text = categoryName,
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

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TransactionsPanel(
    uiState: FinanceUiState,
    viewModel: FinanceViewModel,
    dollarFormat: DecimalFormat,
    onScrollProgressChanged: (Boolean) -> Unit,
    onTransactionLongClick: (TransactionEntity) -> Unit,
    onTransactionClick: (TransactionEntity) -> Unit,
    onBackToTop: (suspend () -> Unit) -> Unit
) {
    val context = LocalContext.current
    val pulsar = remember { Pulsar(context) }
    val presets = remember { com.abhik.paisatrack.ui.utils.SafePresets(pulsar.getPresets()) }
    val listState = rememberLazyListState()
    var visibleLimit by rememberSaveable { mutableStateOf(20) }
    var animationStartLimit by rememberSaveable { mutableStateOf(0) }
    var isLoadingMore by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.activeCollectionFilter, uiState.activeTimeFilter, uiState.activeTypeFilter, uiState.activeSortOrder) {
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
    val isAnyFilterActive = remember(uiState.activeTimeFilter, uiState.activeTypeFilter, uiState.activeSortOrder) {
        uiState.activeTimeFilter != "All" || uiState.activeTypeFilter != "All" || uiState.activeSortOrder != "Newest"
    }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    val showBackToTop by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 300
        }
    }

    val paginatedTransactions = remember(uiState.filteredTransactions, visibleLimit) {
        uiState.filteredTransactions.take(visibleLimit)
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
                            text = "Recent Activity",
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
                    val categoryColor = parentCollection?.hexColor?.let {
                        Color(android.graphics.Color.parseColor(it))
                    } ?: Color(0xFF9CA3AF)

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
                                viewModel.deleteTransaction(tx)
                                Toast.makeText(context, "Record deleted successfully", Toast.LENGTH_SHORT).show()
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
                                        .height(52.dp)
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
}
