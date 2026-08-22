package com.abhik.paisatrack.ui.components.commonUi

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.toShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhik.paisatrack.data.model.CollectionEntity
import com.abhik.paisatrack.data.model.TransactionEntity
import com.abhik.paisatrack.ui.components.getIconByName
import com.swmansion.pulsar.Pulsar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.abhik.paisatrack.ui.utils.safeParseColor
import com.abhik.paisatrack.R
import com.airbnb.lottie.compose.*
import androidx.compose.ui.text.style.TextAlign

private enum class SubmitState {
    Idle,
    Loading,
    Success
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun EditTransactionBottomSheet(
    transaction: TransactionEntity,
    collections: List<CollectionEntity>,
    onDismiss: () -> Unit,
    onSave: (description: String, amount: Double, type: String, collectionId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val pulsar = remember { Pulsar(context) }
    val presets = remember { com.abhik.paisatrack.ui.utils.SafePresets(pulsar.getPresets()) }
    val isDark = isSystemInDarkTheme()
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    var submitState by remember { mutableStateOf(SubmitState.Idle) }

    var description by remember { mutableStateOf(transaction.description) }
    var amountTextFieldValue by remember {
        val initialText = formatAmountWithCommas(transaction.amount.let { if (it % 1.0 == 0.0) it.toLong().toString() else it.toString() })
        mutableStateOf(TextFieldValue(text = initialText, selection = TextRange(initialText.length)))
    }
    var transactionType by remember { mutableStateOf(transaction.type) } // "EXPENSE" or "INCOME"
    var selectedCollectionId by remember { mutableStateOf(transaction.collectionId) }

    val parsedAmount = remember(amountTextFieldValue.text) {
        amountTextFieldValue.text.replace(",", "").toDoubleOrNull()
    }
    val hasChanges = remember(description, parsedAmount, transactionType, selectedCollectionId, transaction) {
        val descChanged = description.trim() != transaction.description.trim()
        val amountChanged = parsedAmount != null && parsedAmount != transaction.amount
        val typeChanged = transactionType.uppercase() != transaction.type.uppercase()
        val collectionChanged = selectedCollectionId != transaction.collectionId
        descChanged || amountChanged || typeChanged || collectionChanged
    }

    var errorText by remember { mutableStateOf("") }

    val lottieComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.nothing))
    val lottieProgress by animateLottieCompositionAsState(
        composition = lottieComposition,
        iterations = LottieConstants.IterateForever
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = null,
        modifier = modifier.imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, bottom = 48.dp, top = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Drag handle (Manual to avoid double / extra visual bar)
            Box(
                modifier = Modifier
                    .padding(top = 8.dp, bottom = 4.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                    .align(Alignment.CenterHorizontally)
            )

            Text(
                text = "Edit Record",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            // 1. Transaction Type Tab Selector with smooth sliding animation
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(4.dp)
            ) {
                val isExpense = transactionType == "EXPENSE"
                val itemWidth = maxWidth / 2f
                val indicatorOffset by animateDpAsState(
                    targetValue = if (isExpense) itemWidth else 0.dp,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    label = "TypeIndicatorOffset"
                )

                // Smoothly sliding selection pill background
                Box(
                    modifier = Modifier
                        .offset(x = indicatorOffset)
                        .width(itemWidth)
                        .height(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                )

                // Option labels row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(CircleShape)
                            .clickable(enabled = submitState == SubmitState.Idle) {
                                presets.ping()
                                transactionType = "INCOME"
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Cash In (+)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (!isExpense) MaterialTheme.colorScheme.onSurface else Color(0xFF1FB47B)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(CircleShape)
                            .clickable(enabled = submitState == SubmitState.Idle) {
                                presets.ping()
                                transactionType = "EXPENSE"
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Cash Out (-)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isExpense) MaterialTheme.colorScheme.onSurface else Color(0xFFEF4444)
                        )
                    }
                }
            }

            // 2. Description field
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
                val progressDesc = description.length / 40f
                val isLimitDesc = description.length == 40
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, start = 4.dp, end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LinearWavyProgressIndicator(
                        progress = { progressDesc },
                        modifier = Modifier
                            .weight(1f)
                            .height(10.dp),
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

            // 3. Amount Box
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
                                val formattedText = formatAmountWithCommas(cleanInput)
                                
                                fun mapOffset(text: String, formattedText: String, offset: Int): Int {
                                    val prefix = text.take(offset)
                                    val digitsBeforeCursor = prefix.count { it != ',' }
                                    var formattedSelectionIndex = 0
                                    var digitsSeen = 0
                                    while (formattedSelectionIndex < formattedText.length && digitsSeen < digitsBeforeCursor) {
                                        if (formattedText[formattedSelectionIndex] != ',') {
                                            digitsSeen++
                                        }
                                        formattedSelectionIndex++
                                    }
                                    while (formattedSelectionIndex < formattedText.length && formattedText[formattedSelectionIndex] == ',') {
                                        formattedSelectionIndex++
                                    }
                                    return formattedSelectionIndex
                                }

                                val newStart = mapOffset(newValue.text, formattedText, newValue.selection.start)
                                val newEnd = mapOffset(newValue.text, formattedText, newValue.selection.end)
                                
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
                val progressAmount = amountTextFieldValue.text.count { it != ',' && it != '.' } / 8f
                val isLimitAmount = amountTextFieldValue.text.count { it != ',' && it != '.' } == 8
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, start = 4.dp, end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LinearWavyProgressIndicator(
                        progress = { progressAmount },
                        modifier = Modifier
                            .weight(1f)
                            .height(10.dp),
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf(100, 500, 1000, 5000).forEach { amountToAdd ->
                        AssistChip(
                            onClick = {
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
                                    val formatted = formatAmountWithCommas(targetStr)
                                    amountTextFieldValue = TextFieldValue(
                                        text = formatted,
                                        selection = TextRange(formatted.length)
                                    )
                                }
                            },
                            label = { Text("+₹$amountToAdd", fontWeight = FontWeight.SemiBold) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        )
                    }
                }
            }

            // 4. Category dropdown selector
            if (collections.isNotEmpty()) {
                var dropdownExpanded by remember { mutableStateOf(false) }
                var categoryFilter by remember { mutableStateOf("All") }
                val currentSelection = collections.find { it.id == selectedCollectionId } ?: collections.first()
                val colColor = safeParseColor(currentSelection.hexColor, fallback = MaterialTheme.colorScheme.primary)

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Category",
                        color = Color(0xFF9CA3AF),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Box(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable(enabled = submitState == SubmitState.Idle) {
                                    presets.plunk()
                                    dropdownExpanded = true
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(MaterialShapes.Cookie4Sided.toShape())
                                        .background(colColor.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = getIconByName(currentSelection.iconName),
                                        contentDescription = null,
                                        tint = colColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = currentSelection.name,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Dropdown Selection Indicator",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .background(MaterialTheme.colorScheme.surface)
                        ) {
                            SingleChoiceSegmentedButtonRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                val filters = listOf("All", "Prebuild", "Owned")
                                filters.forEachIndexed { index, filter ->
                                    val isSelected = categoryFilter == filter
                                    SegmentedButton(
                                        selected = isSelected,
                                        onClick = {
                                            presets.plunk()
                                            categoryFilter = filter
                                        },
                                        shape = SegmentedButtonDefaults.itemShape(index = index, count = filters.size),
                                        icon = {
                                            AnimatedCheckIcon(
                                                isSelected = isSelected,
                                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = filter,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    )
                                }
                            }

                            HorizontalDivider(
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f)
                            )

                            val filteredCols = remember(collections, categoryFilter) {
                                val baseList = when (categoryFilter) {
                                    "Prebuild" -> collections.filter { it.isPrebuilt }
                                    "Owned" -> collections.filter { !it.isPrebuilt }
                                    else -> collections
                                }

                                if (categoryFilter == "All" || categoryFilter == "Owned") {
                                    val newlyCreated = collections.filter { !it.isPrebuilt }
                                        .maxByOrNull { it.createdTimestamp }
                                    if (newlyCreated != null) {
                                        val mutable = baseList.toMutableList()
                                        mutable.removeAll { it.id == newlyCreated.id }
                                        if (mutable.size >= 1) {
                                            mutable.add(1, newlyCreated)
                                        } else {
                                            mutable.add(0, newlyCreated)
                                        }
                                        mutable
                                    } else {
                                        baseList
                                    }
                                } else {
                                    baseList
                                }
                            }

                            if (filteredCols.isEmpty()) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp, horizontal = 16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    LottieAnimation(
                                        composition = lottieComposition,
                                        progress = { lottieProgress },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(120.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "No custom collections",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            } else {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 260.dp)
                                        .verticalScroll(rememberScrollState())
                                ) {
                                    filteredCols.forEach { col ->
                                        val isSelected = col.id == selectedCollectionId
                                        val itemColor = safeParseColor(col.hexColor, fallback = MaterialTheme.colorScheme.primary)

                                        DropdownMenuItem(
                                            text = {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 4.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(32.dp)
                                                                .clip(MaterialShapes.Cookie4Sided.toShape())
                                                                .background(
                                                                    if (isSelected) itemColor
                                                                    else itemColor.copy(alpha = 0.15f)
                                                                ),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Icon(
                                                                imageVector = getIconByName(col.iconName),
                                                                contentDescription = null,
                                                                tint = if (isSelected) Color.White else itemColor,
                                                                modifier = Modifier.size(16.dp)
                                                            )
                                                        }
                                                        Text(
                                                            text = col.name,
                                                            fontSize = 15.sp,
                                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                                        )
                                                    }

                                                    AnimatedCheckIcon(
                                                        isSelected = isSelected,
                                                        tint = itemColor,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            },
                                            onClick = {
                                                presets.ping()
                                                selectedCollectionId = col.id
                                                dropdownExpanded = false
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(52.dp)
                                                .background(
                                                    if (isSelected) itemColor.copy(alpha = 0.08f)
                                                    else Color.Transparent
                                                )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

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

            // 5. Save and Cancel Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        presets.boulder()
                        onDismiss()
                    },
                    enabled = submitState == SubmitState.Idle,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = CircleShape
                ) {
                    Text(
                        text = "Cancel",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                val isButtonEnabled = hasChanges && submitState == SubmitState.Idle

                val buttonBgColor by animateColorAsState(
                    targetValue = when {
                        submitState == SubmitState.Loading || submitState == SubmitState.Success -> MaterialTheme.colorScheme.surface
                        hasChanges -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    },
                    animationSpec = tween(350, easing = FastOutSlowInEasing),
                    label = "buttonBg"
                )
                val buttonContentColor by animateColorAsState(
                    targetValue = when {
                        submitState == SubmitState.Loading || submitState == SubmitState.Success -> MaterialTheme.colorScheme.primary
                        hasChanges -> MaterialTheme.colorScheme.onPrimary
                        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    },
                    animationSpec = tween(350, easing = FastOutSlowInEasing),
                    label = "buttonContent"
                )
                val buttonBorderWidth by animateDpAsState(
                    targetValue = when {
                        submitState == SubmitState.Loading || submitState == SubmitState.Success -> 1.dp
                        hasChanges -> 0.dp
                        else -> 1.dp
                    },
                    animationSpec = tween(350, easing = FastOutSlowInEasing),
                    label = "buttonBorder"
                )
                val buttonBorderColor by animateColorAsState(
                    targetValue = when {
                        submitState == SubmitState.Loading || submitState == SubmitState.Success -> MaterialTheme.colorScheme.outline
                        hasChanges -> Color.Transparent
                        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                    },
                    animationSpec = tween(350, easing = FastOutSlowInEasing),
                    label = "buttonBorderColor"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .clip(CircleShape)
                        .border(buttonBorderWidth, buttonBorderColor, CircleShape)
                        .background(buttonBgColor)
                        .clickable(enabled = isButtonEnabled) {
                            val amt = parsedAmount
                            val desc = if (description.trim().isEmpty()) {
                                val currentSelection = collections.find { it.id == selectedCollectionId }
                                currentSelection?.name ?: "Expense"
                            } else {
                                description.trim()
                            }

                            if (amt == null || amt <= 0.0) {
                                presets.systemNotificationError()
                                errorText = "Enter a valid positive transaction amount!"
                            } else if (collections.isEmpty()) {
                                presets.systemNotificationError()
                                errorText = "Create a category collection first!"
                            } else {
                                presets.ping()
                                scope.launch {
                                    submitState = SubmitState.Loading
                                    delay(1200)
                                    submitState = SubmitState.Success
                                    presets.systemNotificationSuccess()
                                    delay(600)
                                    onSave(desc, amt, transactionType, selectedCollectionId)
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedContent(
                        targetState = submitState,
                        transitionSpec = {
                            (fadeIn(tween(300)) + scaleIn(tween(300), initialScale = 0.8f))
                                .togetherWith(fadeOut(tween(200)) + scaleOut(tween(200), targetScale = 0.6f))
                        },
                        label = "submitContent"
                    ) { state ->
                        when (state) {
                            SubmitState.Idle -> {
                                Text(
                                    text = "Save",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = buttonContentColor
                                )
                            }
                            SubmitState.Loading -> {
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
                            SubmitState.Success -> {
                                AnimatedCheckIcon(
                                    isSelected = true,
                                    tint = buttonContentColor,
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

private fun formatAmountWithCommas(raw: String): String {
    val clean = raw.replace(",", "")
    if (clean.isEmpty()) return ""
    val parts = clean.split(".")
    val integerPart = parts[0]
    
    val formattedInt = if (integerPart.length > 3) {
        val lastThree = integerPart.substring(integerPart.length - 3)
        val rest = integerPart.substring(0, integerPart.length - 3)
        val restFormatted = StringBuilder()
        var count = 0
        for (i in rest.length - 1 downTo 0) {
            if (count > 0 && count % 2 == 0) {
                restFormatted.append(",")
            }
            restFormatted.append(rest[i])
            count++
        }
        restFormatted.reverse().toString() + "," + lastThree
    } else {
        integerPart
    }

    return if (parts.size > 1) {
        formattedInt + "." + parts[1]
    } else if (clean.contains(".")) {
        "$formattedInt."
    } else {
        formattedInt
    }
}
