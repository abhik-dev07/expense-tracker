package com.abhik.paisatrack.ui.screens.addtransaction

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abhik.paisatrack.R
import com.abhik.paisatrack.ui.FinanceViewModel
import com.abhik.paisatrack.ui.screens.addtransaction.components.AmountQuickAddChips
import com.abhik.paisatrack.ui.screens.addtransaction.components.CategoryDropdownSelector
import com.abhik.paisatrack.ui.screens.addtransaction.components.SubmitState
import com.abhik.paisatrack.ui.components.commonUi.TransactionTypeSelector
import com.abhik.paisatrack.ui.screens.addtransaction.utils.AddTransactionUtils
import com.abhik.paisatrack.ui.utils.findActivity
import com.abhik.paisatrack.ui.utils.getSafePresets
import com.swmansion.pulsar.Pulsar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AddTransactionScreen(
    viewModel: FinanceViewModel,
    initialCollectionId: String = "",
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val pulsar = remember(context) { Pulsar(context.findActivity() ?: context) }
    val presets = remember(pulsar) { pulsar.getSafePresets() }
    val isDark = com.abhik.paisatrack.ui.theme.isAppInDarkTheme()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val collections = uiState.collections
    val scope = rememberCoroutineScope()
    var submitState by remember { mutableStateOf(SubmitState.Idle) }

    var description by remember { mutableStateOf("") }
    var amountTextFieldValue by remember { mutableStateOf(TextFieldValue("")) }
    var transactionType by remember { mutableStateOf("EXPENSE") } // "EXPENSE" or "INCOME"
    var selectedCollectionId by remember { mutableStateOf(if (initialCollectionId.isNotEmpty()) initialCollectionId else (collections.firstOrNull()?.id ?: "")) }

    var errorText by remember { mutableStateOf("") }

    val parsedAmount = remember(amountTextFieldValue.text) {
        amountTextFieldValue.text.replace(",", "").toDoubleOrNull()
    }
    val isFormValid = remember(description, parsedAmount, selectedCollectionId, collections) {
        description.trim().isNotEmpty() &&
            parsedAmount != null && parsedAmount > 0.0 &&
            selectedCollectionId.isNotEmpty() &&
            collections.any { it.id == selectedCollectionId }
    }

    // Fallback selection update when collections load
    LaunchedEffect(collections) {
        if (selectedCollectionId.isEmpty() && collections.isNotEmpty()) {
            selectedCollectionId = if (initialCollectionId.isNotEmpty()) initialCollectionId else (collections.firstOrNull()?.id ?: "")
        }
    }

    val scrollState = rememberScrollState()

    Scaffold(
        modifier = Modifier.fillMaxSize().imePadding(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    })
                }
        ) {
            // Fixed Header Layer
            val isHeaderScrolled by remember { derivedStateOf { scrollState.value > 10 } }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .zIndex(1f)
                    .background(
                        if (isHeaderScrolled) {
                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.background,
                                    MaterialTheme.colorScheme.background.copy(alpha = 0.95f),
                                    MaterialTheme.colorScheme.background.copy(alpha = 0.7f),
                                    Color.Transparent
                                )
                            )
                        } else {
                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Transparent
                                )
                            )
                        }
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Circular back button
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (isDark) MaterialTheme.colorScheme.surfaceVariant else Color.White)
                            .border(
                                androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.1f)
                                ),
                                CircleShape
                            )
                            .clickable(enabled = submitState == SubmitState.Idle) {
                                onBack()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_chevron_back),
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Text(
                        text = "Add Record",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 44.dp), // Balance the back button
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }

            // Scrollable Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp)
                    .padding(bottom = innerPadding.calculateBottomPadding() + 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.statusBarsPadding().height(68.dp)) // Compact offset for header

                // 2. Transaction Type Tab Selector with fluid slide and hold drag
                TransactionTypeSelector(
                    transactionType = transactionType,
                    onTypeSelected = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        transactionType = it
                    },
                    enabled = submitState == SubmitState.Idle
                )

                // 3. Description field
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = description,
                        onValueChange = {
                            if (it.length <= 40) {
                                description = it
                                if (it.length == 40) {
                                    focusManager.clearFocus()
                                }
                            }
                        },
                        label = { Text("Description") },
                        placeholder = { Text("e.g. Starbucks Coffee, Office Rent") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        enabled = submitState == SubmitState.Idle,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (description.length == 40) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    )
                    // Char limit indicator + Progress
                    val progressDesc = description.length / 40f
                    val isLimitDesc = description.length == 40
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp, start = 4.dp, end = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LinearWavyProgressIndicator(
                            progress = { progressDesc },
                            modifier = Modifier.weight(1f).height(10.dp),
                            color = if (isLimitDesc) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Text(
                            text = "${description.length}/40",
                            fontSize = 11.sp,
                            color = if (isLimitDesc) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            fontWeight = if (isLimitDesc) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }

                // 4. Amount Box
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = amountTextFieldValue,
                        onValueChange = { newValue ->
                            if (newValue.text == amountTextFieldValue.text) {
                                amountTextFieldValue = newValue
                            } else {
                                val cleanInput = newValue.text.replace(",", "")
                                val digitCount = cleanInput.count { it != '.' }
                                if (digitCount <= 8 && (cleanInput.isEmpty() || cleanInput.toDoubleOrNull() != null || cleanInput.last() == '.')) {
                                    val formattedText = AddTransactionUtils.formatAmountWithCommas(cleanInput)
                                    val newStart = AddTransactionUtils.mapOffset(newValue.text, formattedText, newValue.selection.start)
                                    val newEnd = AddTransactionUtils.mapOffset(newValue.text, formattedText, newValue.selection.end)

                                    amountTextFieldValue = newValue.copy(
                                        text = formattedText,
                                        selection = TextRange(newStart, newEnd)
                                    )
                                    if (digitCount == 8) {
                                        focusManager.clearFocus()
                                    }
                                }
                            }
                        },
                        label = { Text("Amount") },
                        placeholder = { Text("0.00") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        leadingIcon = {
                            Text(
                                text = "₹",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(start = 12.dp, end = 2.dp)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        enabled = submitState == SubmitState.Idle,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (amountTextFieldValue.text.count { it != ',' && it != '.' } == 8) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    )
                    // Char limit indicator + Progress
                    val progressAmount = amountTextFieldValue.text.count { it != ',' && it != '.' } / 8f
                    val isLimitAmount = amountTextFieldValue.text.count { it != ',' && it != '.' } == 8
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp, start = 4.dp, end = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LinearWavyProgressIndicator(
                            progress = { progressAmount },
                            modifier = Modifier.weight(1f).height(10.dp),
                            color = if (isLimitAmount) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Text(
                            text = "${amountTextFieldValue.text.count { it != ',' && it != '.' }}/8",
                            fontSize = 11.sp,
                            color = if (isLimitAmount) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            fontWeight = if (isLimitAmount) FontWeight.Bold else FontWeight.Normal
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    AmountQuickAddChips(
                        onAddAmount = { amountToAdd ->
                            presets.ping()
                            val currentAmt = amountTextFieldValue.text.replace(",", "").toDoubleOrNull() ?: 0.0
                            val targetAmt = currentAmt + amountToAdd
                            val targetStr = if (targetAmt % 1.0 == 0.0) {
                                targetAmt.toLong().toString()
                            } else {
                                String.format(java.util.Locale.US, "%.2f", targetAmt)
                            }
                            val digitCount = targetStr.count { it != '.' }
                            if (digitCount <= 8) {
                                val formatted = AddTransactionUtils.formatAmountWithCommas(targetStr)
                                amountTextFieldValue = TextFieldValue(
                                    text = formatted,
                                    selection = TextRange(formatted.length)
                                )
                            }
                        }
                    )
                }

                // 5. Category dropdown selector
                CategoryDropdownSelector(
                    collections = collections,
                    selectedCollectionId = selectedCollectionId,
                    onSelectCollection = { selectedCollectionId = it },
                    enabled = submitState == SubmitState.Idle
                )

                // Error Text Output
                if (errorText.isNotEmpty()) {
                    Text(
                        text = errorText,
                        color = Color.Red,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                val isButtonEnabled = isFormValid && submitState == SubmitState.Idle

                // 7. Action Button Panel (Animates to a circle on submit, maintains primary background and onPrimary foreground)
                val buttonBgColor by animateColorAsState(
                    targetValue = when {
                        submitState == SubmitState.Loading || submitState == SubmitState.Success -> MaterialTheme.colorScheme.primary
                        isFormValid -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    },
                    animationSpec = tween(350),
                    label = "buttonBg"
                )
                val buttonContentColor by animateColorAsState(
                    targetValue = when {
                        submitState == SubmitState.Loading || submitState == SubmitState.Success -> MaterialTheme.colorScheme.onPrimary
                        isFormValid -> MaterialTheme.colorScheme.onPrimary
                        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    },
                    animationSpec = tween(350),
                    label = "buttonContent"
                )
                val buttonBorderWidth by animateDpAsState(
                    targetValue = when {
                        submitState == SubmitState.Loading || submitState == SubmitState.Success -> 0.dp
                        isFormValid -> 0.dp
                        else -> 1.dp
                    },
                    animationSpec = tween(350),
                    label = "buttonBorder"
                )
                val buttonBorderColor by animateColorAsState(
                    targetValue = when {
                        submitState == SubmitState.Loading || submitState == SubmitState.Success -> Color.Transparent
                        isFormValid -> Color.Transparent
                        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                    },
                    animationSpec = tween(350),
                    label = "buttonBorderColor"
                )

                BoxWithConstraints(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    val isCircle = submitState == SubmitState.Loading || submitState == SubmitState.Success
                    val buttonWidth by animateDpAsState(
                        targetValue = if (isCircle) 52.dp else maxWidth,
                        animationSpec = spring(
                            dampingRatio = 0.8f,
                            stiffness = Spring.StiffnessMedium
                        ),
                        label = "buttonWidth"
                    )

                    Box(
                        modifier = Modifier
                            .width(buttonWidth)
                            .height(52.dp)
                            .clip(CircleShape)
                            .border(buttonBorderWidth, buttonBorderColor, CircleShape)
                            .background(buttonBgColor)
                            .clickable(enabled = isButtonEnabled) {
                                val amt = parsedAmount
                                val desc = description.trim()

                                if (amt == null || amt <= 0.0) {
                                    presets.systemNotificationError()
                                    errorText = "Enter a valid positive transaction amount!"
                                } else if (collections.isEmpty() || selectedCollectionId.isEmpty()) {
                                    presets.systemNotificationError()
                                    errorText = "Create a category collection first!"
                                } else {
                                    presets.ping()
                                    viewModel.addTransaction(desc, amt, transactionType, selectedCollectionId)
                                    scope.launch {
                                        submitState = SubmitState.Loading
                                        delay(1500)
                                        submitState = SubmitState.Success
                                        presets.systemNotificationSuccess()
                                        delay(800)
                                        Toast.makeText(context, "Record created successfully", Toast.LENGTH_SHORT).show()
                                        onBack()
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        AnimatedContent(
                            targetState = submitState,
                            transitionSpec = {
                                (fadeIn(tween(250)) + scaleIn(tween(250), initialScale = 0.7f))
                                    .togetherWith(fadeOut(tween(150)) + scaleOut(tween(150), targetScale = 0.7f))
                            },
                            label = "submitContent"
                        ) { state ->
                            when (state) {
                                SubmitState.Idle -> {
                                    Text(
                                        text = "Add",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = buttonContentColor
                                    )
                                }
                                SubmitState.Loading -> {
                                    val density = androidx.compose.ui.platform.LocalDensity.current
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
                                SubmitState.Success -> {
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
