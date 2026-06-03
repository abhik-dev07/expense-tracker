package com.abhik.paisatrack.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.*
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abhik.paisatrack.R
import com.abhik.paisatrack.ui.CollectionSummary
import com.abhik.paisatrack.ui.FinanceUiState
import com.abhik.paisatrack.ui.FinanceViewModel
import com.abhik.paisatrack.ui.screens.LoadingIndicator
import com.airbnb.lottie.compose.*
import java.text.DecimalFormat
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CollectionsPanel(
    uiState: FinanceUiState,
    viewModel: FinanceViewModel,
    dollarFormat: DecimalFormat,
    onScrollProgressChanged: (Boolean) -> Unit,
    onCollectionClick: (String, androidx.compose.ui.geometry.Rect?) -> Unit,
    onBackToTop: (suspend () -> Unit) -> Unit
) {
    val aiInsights by viewModel.aiInsights.collectAsStateWithLifecycle()
    val aiLoading by viewModel.aiLoading.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current
    val gridState = rememberLazyGridState()
    val scope = rememberCoroutineScope()
    
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
                kotlinx.coroutines.delay(16L)
            }
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
                            val now = System.currentTimeMillis()
                            if (selected || now - lastFilterTapAt < 180L) return@clickable
                            lastFilterTapAt = now
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
                    itemsIndexed(paginatedSummaries) { index, summary ->
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

                        Box(modifier = Modifier.alpha(alpha)) {
                            CollectionGridCard(
                                summary = summary,
                                dollarFormat = dollarFormat,
                                onEditClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    editingCollectionSummary = summary
                                },
                                onClick = { rect ->
                                    onCollectionClick(summary.collection.id, rect)
                                }
                            )
                        }
                    }

                    if (filteredSummaries.size > visibleLimit) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isLoadingMore) {
                                    LoadingIndicator(
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(32.dp)
                                    )
                                } else {
                                    Button(
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            isLoadingMore = true
                                            scope.launch {
                                                kotlinx.coroutines.delay(800)
                                                animationStartLimit = visibleLimit
                                                visibleLimit += 6
                                                isLoadingMore = false
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        shape = RoundedCornerShape(50),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 24.dp)
                                            .height(48.dp)
                                    ) {
                                        Text(
                                            text = "Load More (${filteredSummaries.size - visibleLimit} remaining)",
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
                            border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
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
                                }
                            },
                            modifier = Modifier.weight(1.5f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                            shape = CircleShape
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
                                            Text("Cancel", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
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

@Composable
fun CollectionGridCard(
    summary: CollectionSummary,
    dollarFormat: DecimalFormat,
    onEditClick: () -> Unit,
    onClick: (androidx.compose.ui.geometry.Rect?) -> Unit
) {
    val colColor = remember(summary.collection.hexColor) {
        Color(android.graphics.Color.parseColor(summary.collection.hexColor))
    }
    val haptic = LocalHapticFeedback.current

    var coordinates by remember { mutableStateOf<androidx.compose.ui.layout.LayoutCoordinates?>(null) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned { coordinates = it }
            .clip(RoundedCornerShape(20.dp))
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                val rect = coordinates?.let { coords ->
                    if (coords.isAttached) coords.boundsInRoot() else null
                }
                onClick(rect)
            },
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
