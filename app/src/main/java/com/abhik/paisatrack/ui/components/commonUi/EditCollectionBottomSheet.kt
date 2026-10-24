package com.abhik.paisatrack.ui.components.commonUi

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.toShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.abhik.paisatrack.R
import com.abhik.paisatrack.data.model.CollectionEntity
import com.abhik.paisatrack.ui.components.CollectionColors
import com.abhik.paisatrack.ui.components.CollectionIcons
import com.abhik.paisatrack.ui.components.getIconByName
import com.abhik.paisatrack.ui.utils.findActivity
import com.abhik.paisatrack.ui.utils.getSafePresets
import com.abhik.paisatrack.ui.utils.safeParseColor
import com.swmansion.pulsar.Pulsar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private enum class EditCollectionSubmitState {
    Idle,
    Loading,
    Success
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun EditCollectionBottomSheet(
    collection: CollectionEntity,
    onDismiss: () -> Unit,
    onSave: (updatedCollection: CollectionEntity) -> Unit,
    onDelete: (collection: CollectionEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val pulsar = remember(context) { Pulsar(context.findActivity() ?: context) }
    val presets = remember(pulsar) { pulsar.getSafePresets() }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current

    val dismissInput: () -> Unit = {
        focusManager.clearFocus()
        keyboard?.hide()
    }

    val initialColorIdx = remember(collection.id) {
        CollectionColors.indexOfFirst { it.first.lowercase() == collection.hexColor.lowercase() }.coerceAtLeast(0)
    }
    val initialIconIdx = remember(collection.id) {
        CollectionIcons.indexOfFirst { it.first.lowercase() == collection.iconName.lowercase() }.coerceAtLeast(0)
    }

    var editName by remember(collection.id) { mutableStateOf(collection.name) }
    var selectedColorIdx by remember(collection.id) { mutableStateOf(initialColorIdx) }
    var selectedIconIdx by remember(collection.id) { mutableStateOf(initialIconIdx) }
    var isForwardIconSelection by remember(collection.id) { mutableStateOf(true) }
    var errorText by remember(collection.id) { mutableStateOf("") }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showSaveConfirm by remember { mutableStateOf(false) }

    val hasChanges = remember(editName, selectedColorIdx, selectedIconIdx, collection, initialColorIdx, initialIconIdx) {
        val isNameChanged = editName.trim() != collection.name.trim()
        val isColorChanged = selectedColorIdx != initialColorIdx
        val isIconChanged = selectedIconIdx != initialIconIdx
        (isNameChanged || isColorChanged || isIconChanged) && editName.trim().isNotEmpty()
    }

    val selectedColor = safeParseColor(CollectionColors[selectedColorIdx].first)
    val selectedIcon = CollectionIcons[selectedIconIdx].first

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = null,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { dismissInput() })
                }
                .navigationBarsPadding()
                .padding(top = 8.dp, bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Drag handle pill
            Box(
                modifier = Modifier
                    .padding(top = 8.dp, bottom = 4.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                    .align(Alignment.CenterHorizontally)
            )

            Text(
                text = "Edit Collection",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(horizontal = 24.dp)
            )

            // Live Preview Badge
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(MaterialShapes.Cookie4Sided.toShape())
                    .background(selectedColor.copy(alpha = 0.18f))
                    .align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getIconByName(selectedIcon),
                    contentDescription = null,
                    tint = selectedColor,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Collection Name input
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                OutlinedTextField(
                    value = editName,
                    onValueChange = {
                        if (it.length <= 25) {
                            editName = it
                            if (errorText.isNotEmpty()) {
                                errorText = ""
                            }
                            if (it.length == 25) {
                                dismissInput()
                            }
                        }
                    },
                    label = { Text("Collection Name") },
                    placeholder = { Text("e.g. Travel, Gym Expense") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { dismissInput() }),
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, start = 4.dp, end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LinearWavyProgressIndicator(
                        progress = { progressEditName },
                        modifier = Modifier
                            .weight(1f)
                            .height(10.dp),
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

            // Theme color selections
            Text(
                text = "Select Theme Color",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(CollectionColors) { i, pair ->
                    val colorVal = safeParseColor(pair.first)
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(colorVal)
                            .clickable {
                                selectedColorIdx = i
                                dismissInput()
                                presets.plunk()
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
            Text(
                text = "Select Collection Icon",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(CollectionIcons) { i, pair ->
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
                                dismissInput()
                                presets.plunk()
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
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(horizontal = 24.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons Row: Delete + Save Changes
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Delete Button
                OutlinedButton(
                    onClick = {
                        dismissInput()
                        showDeleteConfirm = true
                        presets.bassDrop()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFEF4444)
                    ),
                    border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                    shape = CircleShape
                ) {
                    Icon(painterResource(id = R.drawable.ic_delete), contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Delete", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
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
                            dismissInput()
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
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = saveBtnContentColor
                    )
                }
            }
        }
    }

    // Confirmation Dialog for Delete
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
                        painter = painterResource(id = R.drawable.ic_delete),
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
                        text = "Are you sure you want to delete the collection \"${collection.name}\"? This action cannot be undone.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    val delScope = rememberCoroutineScope()
                    var delSubmitState by remember { mutableStateOf(EditCollectionSubmitState.Idle) }
                    val isDelSubmitting = delSubmitState == EditCollectionSubmitState.Loading || delSubmitState == EditCollectionSubmitState.Success

                    val delBgColor by animateColorAsState(
                        targetValue = MaterialTheme.colorScheme.error,
                        animationSpec = tween(350), label = "delBg"
                    )
                    val delContentColor by animateColorAsState(
                        targetValue = MaterialTheme.colorScheme.onError,
                        animationSpec = tween(350), label = "delContent"
                    )
                    val delBorderWidth by animateDpAsState(
                        targetValue = 0.dp,
                        animationSpec = tween(350), label = "delBorder"
                    )
                    val delBorderColor by animateColorAsState(
                        targetValue = Color.Transparent,
                        animationSpec = tween(350), label = "delBorderColor"
                    )

                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val totalWidth = maxWidth
                        val idleButtonWidth = (totalWidth - 12.dp) / 2
                        val actionButtonWidth by animateDpAsState(
                            targetValue = if (isDelSubmitting) 52.dp else idleButtonWidth,
                            animationSpec = spring(
                                dampingRatio = 0.8f,
                                stiffness = Spring.StiffnessMedium
                            ),
                            label = "actionButtonWidth"
                        )
                        val rowSpacing by animateDpAsState(
                            targetValue = if (isDelSubmitting) 0.dp else 12.dp,
                            animationSpec = spring(
                                dampingRatio = 0.8f,
                                stiffness = Spring.StiffnessMedium
                            ),
                            label = "rowSpacing"
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(rowSpacing, Alignment.CenterHorizontally),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AnimatedVisibility(
                                visible = !isDelSubmitting,
                                enter = fadeIn(tween(250)) + expandHorizontally(spring(stiffness = Spring.StiffnessMedium)),
                                exit = fadeOut(tween(200)) + shrinkHorizontally(spring(stiffness = Spring.StiffnessMedium))
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        presets.boulder()
                                        showDeleteConfirm = false
                                    },
                                    modifier = Modifier.width(idleButtonWidth).height(52.dp),
                                    shape = CircleShape
                                ) {
                                    Text("Cancel", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .width(actionButtonWidth)
                                    .height(52.dp)
                                    .clip(CircleShape)
                                    .border(delBorderWidth, delBorderColor, CircleShape)
                                    .background(delBgColor)
                                    .clickable(enabled = delSubmitState == EditCollectionSubmitState.Idle) {
                                        presets.ping()
                                        delScope.launch {
                                            delSubmitState = EditCollectionSubmitState.Loading
                                            delay(1500)
                                            delSubmitState = EditCollectionSubmitState.Success
                                            presets.systemNotificationSuccess()
                                            delay(800)
                                            Toast.makeText(context, "Collection deleted successfully", Toast.LENGTH_SHORT).show()
                                            showDeleteConfirm = false
                                            sheetState.hide()
                                            onDelete(collection)
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
                                        EditCollectionSubmitState.Idle -> {
                                            Text("Delete", fontWeight = FontWeight.Bold, color = delContentColor)
                                        }
                                        EditCollectionSubmitState.Loading -> {
                                            val density = LocalDensity.current
                                            val strokeWidthPx = with(density) { 2.5.dp.toPx() }
                                            val amplitudePx = with(density) { 3.dp.toPx() }
                                            CircularWavyProgressIndicator(
                                                modifier = Modifier.size(24.dp),
                                                color = MaterialTheme.colorScheme.onError,
                                                trackColor = MaterialTheme.colorScheme.onError.copy(alpha = 0.25f),
                                                stroke = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidthPx),
                                                trackStroke = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidthPx),
                                                amplitude = amplitudePx,
                                                wavelength = 6.dp
                                            )
                                        }
                                        EditCollectionSubmitState.Success -> {
                                            Icon(
                                                painter = painterResource(id = R.drawable.ic_tick),
                                                contentDescription = "Check",
                                                tint = MaterialTheme.colorScheme.onError,
                                                modifier = Modifier.size(26.dp)
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

    // Confirmation Dialog for Save
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

                    val prevName = collection.name
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

                    val saveScope = rememberCoroutineScope()
                    var saveSubmitState by remember { mutableStateOf(EditCollectionSubmitState.Idle) }
                    val isSaveSubmitting = saveSubmitState == EditCollectionSubmitState.Loading || saveSubmitState == EditCollectionSubmitState.Success

                    val saveBgColor by animateColorAsState(
                        targetValue = MaterialTheme.colorScheme.primary,
                        animationSpec = tween(350), label = "saveBg"
                    )
                    val saveContentColor by animateColorAsState(
                        targetValue = MaterialTheme.colorScheme.onPrimary,
                        animationSpec = tween(350), label = "saveContent"
                    )
                    val saveBorderWidth by animateDpAsState(
                        targetValue = 0.dp,
                        animationSpec = tween(350), label = "saveBorder"
                    )
                    val saveBorderColor by animateColorAsState(
                        targetValue = Color.Transparent,
                        animationSpec = tween(350), label = "saveBorderColor"
                    )

                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val totalWidth = maxWidth
                        val idleButtonWidth = (totalWidth - 12.dp) / 2
                        val actionButtonWidth by animateDpAsState(
                            targetValue = if (isSaveSubmitting) 52.dp else idleButtonWidth,
                            animationSpec = spring(
                                dampingRatio = 0.8f,
                                stiffness = Spring.StiffnessMedium
                            ),
                            label = "actionButtonWidth"
                        )
                        val rowSpacing by animateDpAsState(
                            targetValue = if (isSaveSubmitting) 0.dp else 12.dp,
                            animationSpec = spring(
                                dampingRatio = 0.8f,
                                stiffness = Spring.StiffnessMedium
                            ),
                            label = "rowSpacing"
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(rowSpacing, Alignment.CenterHorizontally),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AnimatedVisibility(
                                visible = !isSaveSubmitting,
                                enter = fadeIn(tween(250)) + expandHorizontally(spring(stiffness = Spring.StiffnessMedium)),
                                exit = fadeOut(tween(200)) + shrinkHorizontally(spring(stiffness = Spring.StiffnessMedium))
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        presets.boulder()
                                        showSaveConfirm = false
                                    },
                                    modifier = Modifier.width(idleButtonWidth).height(52.dp),
                                    shape = CircleShape
                                ) {
                                    Text("Cancel", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .width(actionButtonWidth)
                                    .height(52.dp)
                                    .clip(CircleShape)
                                    .border(saveBorderWidth, saveBorderColor, CircleShape)
                                    .background(saveBgColor)
                                    .clickable(enabled = saveSubmitState == EditCollectionSubmitState.Idle) {
                                        presets.ping()
                                        saveScope.launch {
                                            saveSubmitState = EditCollectionSubmitState.Loading
                                            delay(1500)
                                            saveSubmitState = EditCollectionSubmitState.Success
                                            presets.systemNotificationSuccess()
                                            delay(800)
                                            val updatedCol = collection.copy(
                                                name = editName.trim(),
                                                hexColor = CollectionColors[selectedColorIdx].first,
                                                iconName = CollectionIcons[selectedIconIdx].first
                                            )
                                            Toast.makeText(context, "Collection updated successfully", Toast.LENGTH_SHORT).show()
                                            showSaveConfirm = false
                                            sheetState.hide()
                                            onSave(updatedCol)
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
                                        EditCollectionSubmitState.Idle -> {
                                            Text("Save", fontWeight = FontWeight.Bold, color = saveContentColor)
                                        }
                                        EditCollectionSubmitState.Loading -> {
                                            val density = LocalDensity.current
                                            val strokeWidthPx = with(density) { 2.5.dp.toPx() }
                                            val amplitudePx = with(density) { 3.dp.toPx() }
                                            CircularWavyProgressIndicator(
                                                modifier = Modifier.size(24.dp),
                                                color = MaterialTheme.colorScheme.onPrimary,
                                                trackColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f),
                                                stroke = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidthPx),
                                                trackStroke = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidthPx),
                                                amplitude = amplitudePx,
                                                wavelength = 6.dp
                                            )
                                        }
                                        EditCollectionSubmitState.Success -> {
                                            Icon(
                                                painter = painterResource(id = R.drawable.ic_tick),
                                                contentDescription = "Check",
                                                tint = MaterialTheme.colorScheme.onPrimary,
                                                modifier = Modifier.size(26.dp)
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
