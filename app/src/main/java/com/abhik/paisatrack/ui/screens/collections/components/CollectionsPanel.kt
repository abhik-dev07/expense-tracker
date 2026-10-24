package com.abhik.paisatrack.ui.screens.collections.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import com.abhik.paisatrack.ui.screens.search.components.PaginationFooter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abhik.paisatrack.R
import com.abhik.paisatrack.ui.CollectionSummary
import com.abhik.paisatrack.ui.FinanceUiState
import com.abhik.paisatrack.ui.FinanceViewModel
import com.abhik.paisatrack.ui.components.commonUi.ConnectedButtonGroup
import com.abhik.paisatrack.ui.components.commonUi.EditCollectionBottomSheet
import com.abhik.paisatrack.ui.utils.findActivity
import com.abhik.paisatrack.ui.utils.getSafePresets
import com.airbnb.lottie.compose.*
import com.swmansion.pulsar.Pulsar
import java.text.DecimalFormat
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CollectionsFilterButtonGroup(
    activeColTab: String,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val options = listOf("All", "Prebuild", "Owned")
    ConnectedButtonGroup(
        options = options,
        selectedOption = activeColTab,
        onOptionSelected = onTabSelected,
        modifier = modifier
    ) { option, selected ->
        Text(
            text = option,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CollectionsPanel(
    uiState: FinanceUiState,
    viewModel: FinanceViewModel,
    dollarFormat: DecimalFormat,
    onScrollProgressChanged: (Boolean) -> Unit,
    onCollectionClick: (String, Rect?) -> Unit,
    onBackToTop: (suspend () -> Unit) -> Unit,
    isFilterSticky: Boolean = false,
    onFilterPositioned: (Float) -> Unit = {}
) {
    val context = LocalContext.current
    val pulsar = remember(context) { Pulsar(context.findActivity() ?: context) }
    val presets = remember(pulsar) { pulsar.getSafePresets() }
    val scope = rememberCoroutineScope()
    
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.nothing))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )

    // Scroll progress handled by DashboardScreen

    var editingCollectionSummary by remember { mutableStateOf<CollectionSummary?>(null) }
    val activeColTab by viewModel.activeCollectionTab.collectAsStateWithLifecycle()
    var visibleLimit by rememberSaveable { mutableStateOf(6) }
    var animationStartLimit by rememberSaveable { mutableStateOf(0) }
    var isLoadingMore by remember { mutableStateOf(false) }
    val revealedCollectionIds = remember(activeColTab) { mutableStateListOf<String>() }
    var lastFilterTapAt by rememberSaveable { mutableStateOf(0L) }

    LaunchedEffect(activeColTab) {
        visibleLimit = 6
        animationStartLimit = 0
        revealedCollectionIds.clear()
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

    val paginatedSummaries = remember(filteredSummaries, visibleLimit) {
        filteredSummaries.take(visibleLimit)
    }

    LaunchedEffect(paginatedSummaries, animationStartLimit) {
        val toReveal = paginatedSummaries
            .drop(animationStartLimit)
            .map { it.collection.id }
            .filterNot { revealedCollectionIds.contains(it) }

        if (toReveal.isNotEmpty()) {
            toReveal.forEach { collectionId ->
                revealedCollectionIds.add(collectionId)
                delay(16L)
            }
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Sticky Filtration Pills Connected Button Group placed outside LazyVerticalGrid
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(if (isFilterSticky) 0f else 1f)
                .onGloballyPositioned { coordinates ->
                    onFilterPositioned(coordinates.positionInRoot().y)
                }
        ) {
            CollectionsFilterButtonGroup(
                activeColTab = activeColTab,
                onTabSelected = { option ->
                    val now = System.currentTimeMillis()
                    if (now - lastFilterTapAt >= 180L) {
                        lastFilterTapAt = now
                        presets.boulder()
                        viewModel.setActiveCollectionTab(option)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 12.dp)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (isFilterSticky) Modifier.defaultMinSize(minHeight = 450.dp)
                    else Modifier
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 16.dp,
                        top = 8.dp,
                        end = 16.dp,
                        bottom = if (filteredSummaries.isEmpty()) 24.dp else 120.dp
                    ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (filteredSummaries.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 8.dp),
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
                                    .height(170.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (activeColTab == "Owned") "No Custom Collections" else "No Collections",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (activeColTab == "Owned") "Create custom collections with categories to analyze your spending efficiently."
                                       else "No collections found. You deleted all prebuild collections.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    val rows = paginatedSummaries.chunked(2)
                    for ((rowIndex, rowItems) in rows.withIndex()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            for ((colIndex, summary) in rowItems.withIndex()) {
                                val index = rowIndex * 2 + colIndex
                                val shouldBeVisible =
                                    // Avoid "empty/vanish" state when rapid tab presses keep resetting reveal list.
                                    revealedCollectionIds.isEmpty() ||
                                    index < animationStartLimit ||
                                    revealedCollectionIds.contains(summary.collection.id)
                                val alpha by animateFloatAsState(
                                    targetValue = if (shouldBeVisible) 1f else 0f,
                                    animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
                                    label = "CollectionCardFadeIn"
                                )
        
                                Box(modifier = Modifier.weight(1f).alpha(alpha)) {
                                    CollectionGridCard(
                                        summary = summary,
                                        dollarFormat = dollarFormat,
                                        onEditClick = {
                                            presets.plunk()
                                            editingCollectionSummary = summary
                                        },
                                        onClick = { rect ->
                                            onCollectionClick(summary.collection.id, rect)
                                        }
                                    )
                                }
                            }
                            if (rowItems.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }

                    if (filteredSummaries.size > visibleLimit) {
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
                                        presets.flick()
                                        isLoadingMore = true
                                        scope.launch {
                                            delay(800)
                                            animationStartLimit = visibleLimit
                                            visibleLimit += 6
                                            isLoadingMore = false
                                        }
                                    },
                                    shape = CircleShape,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                ) {
                                    Text(
                                        text = "Load More (${filteredSummaries.size - visibleLimit} remaining)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    } else if (filteredSummaries.isNotEmpty()) {
                        PaginationFooter(
                            isLoading = false,
                            error = null,
                            isEndOfResults = true,
                            onRetry = {},
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                        )
                    }
                    }
                }
            }

        // Bottom Sheet for Editing / Deleting a collection
        if (editingCollectionSummary != null) {
            EditCollectionBottomSheet(
                collection = editingCollectionSummary!!.collection,
                onDismiss = { editingCollectionSummary = null },
                onSave = { updatedCol ->
                    viewModel.updateCollection(updatedCol)
                    editingCollectionSummary = null
                },
                onDelete = { col ->
                    viewModel.deleteCollection(col)
                    editingCollectionSummary = null
                }
            )
        }
    }
}
