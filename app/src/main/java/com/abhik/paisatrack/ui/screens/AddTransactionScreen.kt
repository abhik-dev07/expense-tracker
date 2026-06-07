package com.abhik.paisatrack.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abhik.paisatrack.ui.FinanceViewModel
import com.abhik.paisatrack.ui.components.getIconByName
import com.swmansion.pulsar.Pulsar
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith

private enum class SubmitState {
    Idle,
    Loading,
    Success
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AddTransactionScreen(
    viewModel: FinanceViewModel,
    initialCollectionId: String = "",
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val pulsar = remember { Pulsar(context) }
    val presets = remember { pulsar.getPresets() }
    val isDark = isSystemInDarkTheme()
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val collections = uiState.collections
    val scope = rememberCoroutineScope()
    var submitState by remember { mutableStateOf(SubmitState.Idle) }

    var description by remember { mutableStateOf("") }
    var amountString by remember { mutableStateOf("") }
    var transactionType by remember { mutableStateOf("EXPENSE") } // "EXPENSE" or "INCOME"
    var selectedCollectionId by remember { mutableStateOf(if (initialCollectionId.isNotEmpty()) initialCollectionId else (collections.firstOrNull()?.id ?: "")) }
    var selectedPaymentType by remember { mutableStateOf("Cash") } // "Cash", "Credit/Debit Card", "Check"

    var errorText by remember { mutableStateOf("") }

    // Fallback selection update when collections load
    LaunchedEffect(collections) {
        if (selectedCollectionId.isEmpty() && collections.isNotEmpty()) {
            selectedCollectionId = if (initialCollectionId.isNotEmpty()) initialCollectionId else (collections.firstOrNull()?.id ?: "")
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().imePadding(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 1. Sleek Top Bar with Back Arrow and Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (isDark) MaterialTheme.colorScheme.surfaceVariant else Color.White)
                        .border(
                            androidx.compose.foundation.BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f)
                            ),
                            CircleShape
                        )
                        .clickable(enabled = submitState == SubmitState.Idle) {
                            onBack()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = "Add Record",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // 2. Transaction Type Tab Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val isExpense = transactionType == "EXPENSE"


                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(CircleShape)
                        .background(if (!isExpense) MaterialTheme.colorScheme.surface else Color.Transparent)
                        .clickable(enabled = submitState == SubmitState.Idle) {
                            presets.ping()
                            transactionType = "INCOME"
                        }
                        .padding(vertical = 12.dp),
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
                        .clip(CircleShape)
                        .background(if (isExpense) MaterialTheme.colorScheme.surface else Color.Transparent)
                        .clickable(enabled = submitState == SubmitState.Idle) {
                            presets.ping()
                            transactionType = "EXPENSE"
                        }
                        .padding(vertical = 12.dp),
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
                    value = amountString,
                    onValueChange = {
                        if (it.length <= 8 && (it.isEmpty() || it.toDoubleOrNull() != null || it.last() == '.')) {
                            amountString = it
                            if (it.length == 8) {
                                focusManager.clearFocus()
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
                        focusedBorderColor = if (amountString.length == 8) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                )
                // Char limit indicator + Progress
                val progressAmount = amountString.length / 8f
                val isLimitAmount = amountString.length == 8
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
                        text = "${amountString.length}/8",
                        fontSize = 11.sp,
                        color = if (isLimitAmount) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        fontWeight = if (isLimitAmount) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }

            // 5. Category dropdown selector with redesigned beautiful UI
            if (collections.isNotEmpty()) {
                var dropdownExpanded by remember { mutableStateOf(false) }
                var categoryFilter by remember { mutableStateOf("All") }
                val currentSelection = collections.find { it.id == selectedCollectionId } ?: collections.first()
                val colColor = try {
                    Color(android.graphics.Color.parseColor(currentSelection.hexColor))
                } catch (e: Exception) {
                    MaterialTheme.colorScheme.primary
                }

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Category",
                        color = Color(0xFF9CA3AF),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Box(modifier = Modifier.fillMaxWidth()) {
                        // Dropdown Trigger Button
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
                                        .clip(CircleShape)
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

                        // Beautiful Custom Dropdown Menu design
                        DropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .background(MaterialTheme.colorScheme.surface)
                        ) {
                            // Fixed top filter segmented button row
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

                            // Scrollable list showing only 5 items max
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

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 260.dp) // 5 items * 52.dp = 260.dp
                                    .verticalScroll(rememberScrollState())
                            ) {
                                filteredCols.forEach { col ->
                                    val isSelected = col.id == selectedCollectionId
                                    val itemColor = try {
                                        Color(android.graphics.Color.parseColor(col.hexColor))
                                    } catch (e: Exception) {
                                        MaterialTheme.colorScheme.primary
                                    }

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
                                                            .clip(CircleShape)
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
                                                
                                                if (isSelected) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = "Selected",
                                                        tint = itemColor,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
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

            // 6. Payment Type Selector
            // Column(modifier = Modifier.fillMaxWidth()) {
            //     Text(
            //         text = "Payment Type",
            //         fontSize = 16.sp,
            //         fontWeight = FontWeight.Bold,
            //         color = Color(0xFF1E2541)
            //     )
            //     Spacer(modifier = Modifier.height(12.dp))

            //     val paymentTypes = listOf("Cash", "Credit / Debit Card", "Check")
            //     Column(
            //         verticalArrangement = Arrangement.spacedBy(8.dp)
            //     ) {
            //         paymentTypes.forEach { type ->
            //             val isSelected = selectedPaymentType == type
            //             Box(
            //                 modifier = Modifier
            //                     .fillMaxWidth()
            //                     .clip(RoundedCornerShape(16.dp))
            //                     .background(if (isSelected) Color(0xFFE4F6E6) else Color.Transparent)
            //                     .clickable { selectedPaymentType = type }
            //                     .border(
            //                         width = 1.dp,
            //                         color = if (isSelected) Color(0xFFB7DAAE) else Color(0xFFE5E7EB),
            //                         shape = RoundedCornerShape(16.dp)
            //                     )
            //                     .padding(horizontal = 18.dp, vertical = 14.dp)
            //             ) {
            //                 Row(
            //                     modifier = Modifier.fillMaxWidth(),
            //                     horizontalArrangement = Arrangement.SpaceBetween,
            //                     verticalAlignment = Alignment.CenterVertically
            //                 ) {
            //                     Text(
            //                         text = type,
            //                         fontSize = 15.sp,
            //                         fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            //                         color = Color(0xFF1E2541)
            //                     )

            //                     Box(
            //                         modifier = Modifier
            //                             .size(22.dp)
            //                             .clip(CircleShape)
            //                             .background(if (isSelected) Color(0xFF10B981) else Color.Transparent)
            //                             .border(
            //                                 width = 1.5.dp,
            //                                 color = if (isSelected) Color(0xFF10B981) else Color(0xFF9CA3AF),
            //                                 shape = CircleShape
            //                             ),
            //                         contentAlignment = Alignment.Center
            //                     ) {
            //                         if (isSelected) {
            //                             Icon(
            //                                 imageVector = Icons.Default.Check,
            //                                 contentDescription = "Checked",
            //                                 tint = Color.White,
            //                                 modifier = Modifier.size(12.dp)
            //                             )
            //                         }
            //                     }
            //                 }
            //             }
            //         }
            //     }
            // }

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

            // 7. Action Button Panel
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Draft cancellation / exit button
                OutlinedButton(
                    onClick = {
                        presets.boulder()
                        onBack()
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

                // Main Submit Transaction button with animated state transitions
                val buttonBgColor by animateColorAsState(
                    targetValue = when (submitState) {
                        SubmitState.Idle -> MaterialTheme.colorScheme.primary
                        SubmitState.Loading, SubmitState.Success -> MaterialTheme.colorScheme.surface
                    },
                    animationSpec = tween(400),
                    label = "buttonBg"
                )
                val buttonContentColor by animateColorAsState(
                    targetValue = when (submitState) {
                        SubmitState.Idle -> MaterialTheme.colorScheme.onPrimary
                        SubmitState.Loading, SubmitState.Success -> MaterialTheme.colorScheme.primary
                    },
                    animationSpec = tween(400),
                    label = "buttonContent"
                )
                val buttonBorderWidth by animateDpAsState(
                    targetValue = when (submitState) {
                        SubmitState.Idle -> 0.dp
                        SubmitState.Loading, SubmitState.Success -> 1.dp
                    },
                    animationSpec = tween(400),
                    label = "buttonBorder"
                )
                val buttonBorderColor by animateColorAsState(
                    targetValue = when (submitState) {
                        SubmitState.Idle -> Color.Transparent
                        SubmitState.Loading, SubmitState.Success -> MaterialTheme.colorScheme.outline
                    },
                    animationSpec = tween(400),
                    label = "buttonBorderColor"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .clip(CircleShape)
                        .border(buttonBorderWidth, buttonBorderColor, CircleShape)
                        .background(buttonBgColor)
                        .clickable(enabled = submitState == SubmitState.Idle) {
                            val amt = amountString.toDoubleOrNull()
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
                                viewModel.addTransaction(desc, amt, transactionType, selectedCollectionId)
                                scope.launch {
                                    submitState = SubmitState.Loading
                                    delay(1500)
                                    submitState = SubmitState.Success
                                    presets.systemNotificationSuccess()
                                    delay(800)
                                    android.widget.Toast.makeText(context, "Record created successfully", android.widget.Toast.LENGTH_SHORT).show()
                                    onBack()
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
                                    text = "Add",
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
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Success",
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
