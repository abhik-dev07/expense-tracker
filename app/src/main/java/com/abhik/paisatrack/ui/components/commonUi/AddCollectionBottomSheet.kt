package com.abhik.paisatrack.ui.components.commonUi

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhik.paisatrack.R
import com.abhik.paisatrack.utils.CollectionColors
import com.abhik.paisatrack.utils.CollectionIcons
import com.abhik.paisatrack.utils.getIconByName
import com.abhik.paisatrack.utils.findActivity
import com.abhik.paisatrack.utils.getSafePresets
import com.abhik.paisatrack.utils.safeParseColor
import com.swmansion.pulsar.Pulsar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private enum class CollectionSubmitState {
    Idle,
    Loading,
    Success
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AddCollectionBottomSheet(
    onDismiss: () -> Unit,
    onConfirm: (name: String, colorHex: String, iconName: String, budget: Double?) -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val pulsar = remember(context) { Pulsar(context.findActivity() ?: context) }
    val presets = remember(pulsar) { pulsar.getSafePresets() }
    val createScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current

    // Single place to drop focus + hide the keyboard.
    val dismissInput: () -> Unit = {
        focusManager.clearFocus()
        keyboard?.hide()
    }

    var name by remember { mutableStateOf("") }
    var selectedColorIdx by remember { mutableStateOf(0) }
    var selectedIconIdx by remember { mutableStateOf(0) }
    var isForwardIconSelection by remember { mutableStateOf(true) }

    var errorText by remember { mutableStateOf("") }

    var createSubmitState by remember { mutableStateOf(CollectionSubmitState.Idle) }
    val isCreateSubmitting = createSubmitState == CollectionSubmitState.Loading || createSubmitState == CollectionSubmitState.Success

    val selectedColor = safeParseColor(CollectionColors[selectedColorIdx].first)
    val selectedIcon = CollectionIcons[selectedIconIdx].first

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = null,
        modifier = modifier // imePadding moved onto the scroll Column below
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()                       // viewport ends exactly at the keyboard's top edge
                .verticalScroll(rememberScrollState())
                .pointerInput(Unit) {               // tap on empty space -> dismiss keyboard + focus
                    detectTapGestures(onTap = { dismissInput() })
                }
                .navigationBarsPadding()            // inside the scroll content, so no clipped cutout
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

            // Header Title
            Text(
                text = "Create Collection",
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
                    value = name,
                    onValueChange = {
                        if (it.length <= 25) {
                            name = it
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
                        focusedBorderColor = if (name.length == 25) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                )
                // Char limit indicator + Progress
                val progressName = name.length / 25f
                val isLimitName = name.length == 25
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp, start = 4.dp, end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LinearWavyProgressIndicator(
                        progress = { progressName },
                        modifier = Modifier.weight(1f).height(10.dp),
                        color = if (isLimitName) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Text(
                        text = "${name.length}/25",
                        fontSize = 11.sp,
                        color = if (isLimitName) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        fontWeight = if (isLimitName) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }

            // Color selections
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
                                presets.plunk()
                                selectedColorIdx = i
                                dismissInput()
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
                                presets.plunk()
                                if (i != selectedIconIdx) {
                                    isForwardIconSelection = i > selectedIconIdx
                                    selectedIconIdx = i
                                }
                                dismissInput()
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

            val createBgColor by animateColorAsState(
                targetValue = MaterialTheme.colorScheme.primary,
                animationSpec = tween(350), label = "createBg"
            )
            val createContentColor by animateColorAsState(
                targetValue = MaterialTheme.colorScheme.onPrimary,
                animationSpec = tween(350), label = "createContent"
            )
            val createBorderWidth by animateDpAsState(
                targetValue = 0.dp,
                animationSpec = tween(350), label = "createBorder"
            )
            val createBorderColor by animateColorAsState(
                targetValue = Color.Transparent,
                animationSpec = tween(350), label = "createBorderColor"
            )

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(52.dp),
                contentAlignment = Alignment.Center
            ) {
                val totalWidth = maxWidth
                val idleButtonWidth = (totalWidth - 12.dp) / 2
                val actionButtonWidth by animateDpAsState(
                    targetValue = if (isCreateSubmitting) 52.dp else idleButtonWidth,
                    animationSpec = spring(
                        dampingRatio = 0.8f,
                        stiffness = Spring.StiffnessMedium
                    ),
                    label = "actionButtonWidth"
                )
                val rowSpacing by animateDpAsState(
                    targetValue = if (isCreateSubmitting) 0.dp else 12.dp,
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
                        visible = !isCreateSubmitting,
                        enter = fadeIn(tween(250)) + expandHorizontally(spring(stiffness = Spring.StiffnessMedium)),
                        exit = fadeOut(tween(200)) + shrinkHorizontally(spring(stiffness = Spring.StiffnessMedium))
                    ) {
                        OutlinedButton(
                            onClick = {
                                presets.boulder()
                                dismissInput()
                                createScope.launch {
                                    sheetState.hide()
                                    onDismiss()
                                }
                            },
                            enabled = createSubmitState == CollectionSubmitState.Idle,
                            modifier = Modifier
                                .width(idleButtonWidth)
                                .height(52.dp),
                            shape = CircleShape
                        ) {
                            Text(
                                text = "Cancel",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .width(actionButtonWidth)
                            .height(52.dp)
                            .clip(CircleShape)
                            .border(createBorderWidth, createBorderColor, CircleShape)
                            .background(createBgColor)
                            .clickable(enabled = createSubmitState == CollectionSubmitState.Idle) {
                                dismissInput()
                                if (name.trim().isEmpty()) {
                                    errorText = "Collection name is required!"
                                    presets.boulder()
                                } else {
                                    presets.ping()
                                    createScope.launch {
                                        createSubmitState = CollectionSubmitState.Loading
                                        delay(1500)
                                        createSubmitState = CollectionSubmitState.Success
                                        presets.systemNotificationSuccess()
                                        delay(800)
                                        Toast.makeText(context, "Collection created successfully", Toast.LENGTH_SHORT).show()
                                        sheetState.hide()
                                        onConfirm(
                                            name.trim(),
                                            CollectionColors[selectedColorIdx].first,
                                            CollectionIcons[selectedIconIdx].first,
                                            null
                                        )
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        AnimatedContent(
                            targetState = createSubmitState,
                            transitionSpec = {
                                (fadeIn(tween(300)) + scaleIn(tween(300), initialScale = 0.8f))
                                    .togetherWith(fadeOut(tween(200)) + scaleOut(tween(200), targetScale = 0.6f))
                            },
                            label = "createContent"
                        ) { state ->
                            when (state) {
                                CollectionSubmitState.Idle -> {
                                    Text(text = "Create", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = createContentColor)
                                }
                                CollectionSubmitState.Loading -> {
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
                                CollectionSubmitState.Success -> {
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