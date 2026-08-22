package com.abhik.paisatrack.ui.components.dashboard

import android.widget.Toast
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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
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
import com.abhik.paisatrack.ui.components.CollectionColors
import com.abhik.paisatrack.ui.components.CollectionIcons
import com.abhik.paisatrack.ui.components.getIconByName
import com.airbnb.lottie.compose.*
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.toShape
import com.swmansion.pulsar.Pulsar
import java.text.DecimalFormat
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.abhik.paisatrack.ui.components.commonUi.AnimatedCheckIcon
import com.abhik.paisatrack.ui.components.commonUi.DeleteRoundedIconVector
import com.abhik.paisatrack.ui.components.commonUi.EditRoundedIconVector
import com.abhik.paisatrack.ui.utils.safeParseColor

private enum class PanelSubmitState {
    Idle,
    Loading,
    Success
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CollectionsPanel(
    uiState: FinanceUiState,
    viewModel: FinanceViewModel,
    dollarFormat: DecimalFormat,
    onScrollProgressChanged: (Boolean) -> Unit,
    onCollectionClick: (String, Rect?) -> Unit,
    onBackToTop: (suspend () -> Unit) -> Unit
) {
    val context = LocalContext.current
    val pulsar = remember { Pulsar(context) }
    val presets = remember { com.abhik.paisatrack.ui.utils.SafePresets(pulsar.getPresets()) }
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
                delay(16L)
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Sticky Filtration Pills Segmented Button Row placed outside LazyVerticalGrid
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 12.dp)
        ) {
            val options = listOf("All", "Prebuild", "Owned")
            options.forEachIndexed { index, option ->
                val selected = activeColTab == option
                SegmentedButton(
                    selected = selected,
                    onClick = {
                        val now = System.currentTimeMillis()
                        if (!selected && now - lastFilterTapAt >= 180L) {
                            lastFilterTapAt = now
                            presets.boulder()
                            viewModel.setActiveCollectionTab(option)
                        }
                    },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                    icon = {
                        AnimatedCheckIcon(
                            isSelected = selected,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    },
                    label = {
                        Text(
                            text = option,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                )
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
                                    text = if (activeColTab == "Owned") "Create custom collections with categories to analyze your spending efficiently."
                                           else "No collections found. You deleted all prebuild collections.",
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
                                    presets.plunk()
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
                                            .padding(horizontal = 24.dp)
                                            .height(52.dp)
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
                FilledTonalButton(
                    onClick = {
                        presets.ping()
                        onBackToTop {
                            if (gridState.firstVisibleItemIndex > 2) {
                                gridState.scrollToItem(2)
                            }
                            gridState.animateScrollToItem(0)
                        }
                    },
                    elevation = ButtonDefaults.filledTonalButtonElevation(defaultElevation = 6.dp),
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
            var isForwardIconSelection by remember(editingCollectionSummary) { mutableStateOf(true) }
            var errorText by remember(editingCollectionSummary) { mutableStateOf("") }
            var showDeleteConfirm by remember { mutableStateOf(false) }
            var showSaveConfirm by remember { mutableStateOf(false) }

            val hasChanges = remember(editName, selectedColorIdx, selectedIconIdx, editingCollectionSummary) {
                val origName = editingCollectionSummary?.collection?.name ?: ""
                val origColor = (editingCollectionSummary?.collection?.hexColor ?: "").lowercase()
                val origIcon = (editingCollectionSummary?.collection?.iconName ?: "").lowercase()
                val curColor = CollectionColors.getOrNull(selectedColorIdx)?.first?.lowercase() ?: ""
                val curIcon = CollectionIcons.getOrNull(selectedIconIdx)?.first?.lowercase() ?: ""

                editName.trim() != origName.trim() || curColor != origColor || curIcon != origIcon
            }

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
                                if (it.length <= 25) {
                                    editName = it
                                    if (it.length == 25) {
                                        focusManager.clearFocus()
                                    }
                                }
                            },
                            label = { Text("Collection Name") },
                            placeholder = { Text("e.g. Travel, Gym Expense") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (editName.length == 25) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                        )
                        // Char limit indicator + Progress
                        val progressEditName = editName.length / 25f
                        val isLimitEditName = editName.length == 25
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp, start = 4.dp, end = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            LinearWavyProgressIndicator(
                                progress = { progressEditName },
                                modifier = Modifier.weight(1f).height(10.dp),
                                color = if (isLimitEditName) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                            Text(
                                text = "${editName.length}/25",
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
                            val colorVal = safeParseColor(pair.first)
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(colorVal)
                                    .clickable {
                                        selectedColorIdx = i
                                        focusManager.clearFocus()
                                        presets.boulder()
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
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CollectionIcons.forEachIndexed { i, pair ->
                            val selected = selectedIconIdx == i
                            val iconThemeColor = safeParseColor(CollectionColors[selectedColorIdx].first)
                            val targetRotation = if (selected) (if (isForwardIconSelection) 90f else -90f) else 0f
                            val animatedRotation by animateFloatAsState(
                                targetValue = targetRotation,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                ),
                                label = "IconRotation_$i"
                            )

                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .graphicsLayer { rotationZ = animatedRotation }
                                    .clip(MaterialShapes.Cookie4Sided.toShape())
                                    .background(if (selected) iconThemeColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .clickable {
                                        if (i != selectedIconIdx) {
                                            isForwardIconSelection = i > selectedIconIdx
                                            selectedIconIdx = i
                                        }
                                        focusManager.clearFocus()
                                        presets.boulder()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = getIconByName(pair.first),
                                    contentDescription = pair.second,
                                    tint = if (selected) iconThemeColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier
                                        .size(20.dp)
                                        .graphicsLayer { rotationZ = -animatedRotation }
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
                                presets.bassDrop()
                            },
                            modifier = Modifier.weight(1f).height(52.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFFEF4444)
                            ),
                            border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                            shape = CircleShape
                        ) {
                            Icon(DeleteRoundedIconVector, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Delete", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }

                        // Save Button
                        val saveBtnBgColor by animateColorAsState(
                            targetValue = if (hasChanges) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            animationSpec = tween(350, easing = FastOutSlowInEasing),
                            label = "saveBtnBg"
                        )
                        val saveBtnContentColor by animateColorAsState(
                            targetValue = if (hasChanges) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                            animationSpec = tween(350, easing = FastOutSlowInEasing),
                            label = "saveBtnContent"
                        )
                        val saveBtnBorderWidth by animateDpAsState(
                            targetValue = if (hasChanges) 0.dp else 1.dp,
                            animationSpec = tween(350, easing = FastOutSlowInEasing),
                            label = "saveBtnBorderWidth"
                        )
                        val saveBtnBorderColor by animateColorAsState(
                            targetValue = if (hasChanges) Color.Transparent else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                            animationSpec = tween(350, easing = FastOutSlowInEasing),
                            label = "saveBtnBorderColor"
                        )

                        Box(
                            modifier = Modifier
                                .weight(1.5f)
                                .height(52.dp)
                                .clip(CircleShape)
                                .border(saveBtnBorderWidth, saveBtnBorderColor, CircleShape)
                                .background(saveBtnBgColor)
                                .clickable(enabled = hasChanges) {
                                    if (editName.trim().isEmpty()) {
                                        errorText = "Collection name is required!"
                                    } else {
                                        showSaveConfirm = true
                                    }
                                    presets.ping()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Save Changes",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = saveBtnContentColor
                            )
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
                                        imageVector = DeleteRoundedIconVector,
                                        contentDescription = "Delete",
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
                                                presets.boulder()
                                                showDeleteConfirm = false
                                            },
                                            modifier = Modifier.weight(1f).height(52.dp),
                                            shape = CircleShape
                                        ) {
                                            Text("Cancel", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }

                                        val delScope = rememberCoroutineScope()
                                        var delSubmitState by remember { mutableStateOf(PanelSubmitState.Idle) }
                                        val delBgColor by animateColorAsState(
                                            targetValue = when (delSubmitState) {
                                                PanelSubmitState.Idle -> MaterialTheme.colorScheme.error
                                                PanelSubmitState.Loading, PanelSubmitState.Success -> MaterialTheme.colorScheme.surface
                                            },
                                            animationSpec = tween(400), label = "delBg"
                                        )
                                        val delContentColor by animateColorAsState(
                                            targetValue = when (delSubmitState) {
                                                PanelSubmitState.Idle -> MaterialTheme.colorScheme.onError
                                                PanelSubmitState.Loading, PanelSubmitState.Success -> MaterialTheme.colorScheme.error
                                            },
                                            animationSpec = tween(400), label = "delContent"
                                        )
                                        val delBorderWidth by animateDpAsState(
                                            targetValue = if (delSubmitState == PanelSubmitState.Idle) 0.dp else 1.dp,
                                            animationSpec = tween(400), label = "delBorder"
                                        )
                                        val delBorderColor by animateColorAsState(
                                            targetValue = if (delSubmitState == PanelSubmitState.Idle) Color.Transparent else MaterialTheme.colorScheme.error.copy(alpha = 0.5f),
                                            animationSpec = tween(400), label = "delBorderColor"
                                        )

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(52.dp)
                                                .clip(CircleShape)
                                                .border(delBorderWidth, delBorderColor, CircleShape)
                                                .background(delBgColor)
                                                .clickable(enabled = delSubmitState == PanelSubmitState.Idle) {
                                                    presets.ping()
                                                    delScope.launch {
                                                        delSubmitState = PanelSubmitState.Loading
                                                        delay(1500)
                                                        delSubmitState = PanelSubmitState.Success
                                                        presets.systemNotificationSuccess()
                                                        delay(800)
                                                        editingCollectionSummary?.collection?.let {
                                                            viewModel.deleteCollection(it)
                                                            Toast.makeText(context, "Collection deleted successfully", Toast.LENGTH_SHORT).show()
                                                        }
                                                        showDeleteConfirm = false
                                                        editingCollectionSummary = null
                                                    }
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            AnimatedContent(
                                                targetState = delSubmitState,
                                                transitionSpec = {
                                                    (fadeIn(tween(300)) + scaleIn(tween(300), initialScale = 0.8f))
                                                        .togetherWith(fadeOut(tween(200)) + scaleOut(tween(200), targetScale = 0.6f))
                                                },
                                                label = "delContent"
                                            ) { state ->
                                                when (state) {
                                                    PanelSubmitState.Idle -> {
                                                        Text("Delete", fontWeight = FontWeight.Bold, color = delContentColor)
                                                    }
                                                    PanelSubmitState.Loading -> {
                                                        val density = androidx.compose.ui.platform.LocalDensity.current
                                                        val strokeWidthPx = with(density) { 2.dp.toPx() }
                                                        val amplitudePx = with(density) { 4.dp.toPx() }
                                                        CircularWavyProgressIndicator(
                                                            modifier = Modifier.size(24.dp),
                                                            color = MaterialTheme.colorScheme.error,
                                                            trackColor = MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                                                            stroke = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidthPx),
                                                            trackStroke = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidthPx),
                                                            amplitude = amplitudePx,
                                                            wavelength = 6.dp
                                                        )
                                                    }
                                                    PanelSubmitState.Success -> {
                                                        AnimatedCheckIcon(
                                                            isSelected = true,
                                                            tint = delContentColor,
                                                            modifier = Modifier.size(24.dp)
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
                                                presets.boulder()
                                                showSaveConfirm = false
                                            },
                                            modifier = Modifier.weight(1f).height(52.dp),
                                            shape = CircleShape
                                        ) {
                                            Text("Cancel", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }

                                        val saveScope = rememberCoroutineScope()
                                        var saveSubmitState by remember { mutableStateOf(PanelSubmitState.Idle) }
                                        val saveBgColor by animateColorAsState(
                                            targetValue = when (saveSubmitState) {
                                                PanelSubmitState.Idle -> MaterialTheme.colorScheme.primary
                                                PanelSubmitState.Loading, PanelSubmitState.Success -> MaterialTheme.colorScheme.surface
                                            },
                                            animationSpec = tween(400), label = "saveBg"
                                        )
                                        val saveContentColor by animateColorAsState(
                                            targetValue = when (saveSubmitState) {
                                                PanelSubmitState.Idle -> MaterialTheme.colorScheme.onPrimary
                                                PanelSubmitState.Loading, PanelSubmitState.Success -> MaterialTheme.colorScheme.primary
                                            },
                                            animationSpec = tween(400), label = "saveContent"
                                        )
                                        val saveBorderWidth by animateDpAsState(
                                            targetValue = if (saveSubmitState == PanelSubmitState.Idle) 0.dp else 1.dp,
                                            animationSpec = tween(400), label = "saveBorder"
                                        )
                                        val saveBorderColor by animateColorAsState(
                                            targetValue = if (saveSubmitState == PanelSubmitState.Idle) Color.Transparent else MaterialTheme.colorScheme.outline,
                                            animationSpec = tween(400), label = "saveBorderColor"
                                        )

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(52.dp)
                                                .clip(CircleShape)
                                                .border(saveBorderWidth, saveBorderColor, CircleShape)
                                                .background(saveBgColor)
                                                .clickable(enabled = saveSubmitState == PanelSubmitState.Idle) {
                                                    presets.ping()
                                                    saveScope.launch {
                                                        saveSubmitState = PanelSubmitState.Loading
                                                        delay(1500)
                                                        saveSubmitState = PanelSubmitState.Success
                                                        presets.systemNotificationSuccess()
                                                        delay(800)
                                                        val updatedCol = editingCollectionSummary!!.collection.copy(
                                                            name = editName.trim(),
                                                            hexColor = CollectionColors[selectedColorIdx].first,
                                                            iconName = CollectionIcons[selectedIconIdx].first
                                                        )
                                                        viewModel.updateCollection(updatedCol)
                                                        Toast.makeText(context, "Collection updated successfully", Toast.LENGTH_SHORT).show()
                                                        showSaveConfirm = false
                                                        editingCollectionSummary = null
                                                    }
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            AnimatedContent(
                                                targetState = saveSubmitState,
                                                transitionSpec = {
                                                    (fadeIn(tween(300)) + scaleIn(tween(300), initialScale = 0.8f))
                                                        .togetherWith(fadeOut(tween(200)) + scaleOut(tween(200), targetScale = 0.6f))
                                                },
                                                label = "saveContent"
                                            ) { state ->
                                                when (state) {
                                                    PanelSubmitState.Idle -> {
                                                        Text("Save", fontWeight = FontWeight.Bold, color = saveContentColor)
                                                    }
                                                    PanelSubmitState.Loading -> {
                                                        val density = androidx.compose.ui.platform.LocalDensity.current
                                                        val strokeWidthPx = with(density) { 2.dp.toPx() }
                                                        val amplitudePx = with(density) { 4.dp.toPx() }
                                                        CircularWavyProgressIndicator(
                                                            modifier = Modifier.size(24.dp),
                                                            color = MaterialTheme.colorScheme.primary,
                                                            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                                            stroke = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidthPx),
                                                            trackStroke = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidthPx),
                                                            amplitude = amplitudePx,
                                                            wavelength = 6.dp
                                                        )
                                                    }
                                                    PanelSubmitState.Success -> {
                                                        AnimatedCheckIcon(
                                                            isSelected = true,
                                                            tint = saveContentColor,
                                                            modifier = Modifier.size(24.dp)
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
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CollectionGridCard(
    summary: CollectionSummary,
    dollarFormat: DecimalFormat,
    onEditClick: () -> Unit,
    onClick: (Rect?) -> Unit
) {
    val colColor = remember(summary.collection.hexColor) {
        safeParseColor(summary.collection.hexColor)
    }
    val context = LocalContext.current
    val pulsar = remember { Pulsar(context) }
    val presets = remember { com.abhik.paisatrack.ui.utils.SafePresets(pulsar.getPresets()) }

    var coordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned { coordinates = it }
            .clip(RoundedCornerShape(20.dp))
            .clickable {
                presets.ping()
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
                        .clip(MaterialShapes.Cookie4Sided.toShape())
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
                        imageVector = EditRoundedIconVector,
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
                text = "${summary.transactionCount} records",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))
            // Net balance of the collection card
            val balance = summary.totalIncome - summary.totalExpense

            Column {
                Text(
                    text = "Remaining",
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
