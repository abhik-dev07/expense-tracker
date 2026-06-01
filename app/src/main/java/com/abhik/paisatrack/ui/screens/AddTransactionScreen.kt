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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abhik.paisatrack.ui.FinanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    viewModel: FinanceViewModel,
    initialCollectionId: String = "",
    onBack: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val isDark = isSystemInDarkTheme()
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val collections = uiState.collections

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
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onBack()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowLeft,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = "Add transaction",
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
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            transactionType = "INCOME"
                        }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Cash in",
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
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            transactionType = "EXPENSE"
                        }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Cash out",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isExpense) MaterialTheme.colorScheme.onSurface else Color(0xFFEF4444)
                    )
                }
            }

            // 3. Description field
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Description",
                    color = Color(0xFF9CA3AF),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
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
                    placeholder = { Text("e.g. Starbucks Coffee, Office Rent", color = Color(0xFF9CA3AF)) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedBorderColor = if (description.length == 40) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.Transparent
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
                    LinearProgressIndicator(
                        progress = progressDesc,
                        modifier = Modifier.weight(1f).height(4.dp).clip(CircleShape),
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
                Text(
                    text = "Amount",
                    color = Color(0xFF9CA3AF),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
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
                    placeholder = { Text("0.00", color = Color(0xFF9CA3AF)) },
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
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedBorderColor = if (amountString.length == 8) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.Transparent
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
                    LinearProgressIndicator(
                        progress = progressAmount,
                        modifier = Modifier.weight(1f).height(4.dp).clip(CircleShape),
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
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
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
                            // Fixed top filter pills
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val filters = listOf("All", "Prebuild", "Owned")
                                filters.forEach { filter ->
                                    val isSelected = categoryFilter == filter
                                    val containerColor = if (isSelected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    }
                                    val contentColor = if (isSelected) {
                                        MaterialTheme.colorScheme.onPrimary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                    
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(CircleShape)
                                            .background(containerColor)
                                            .border(
                                                width = 1.dp,
                                                color = if (isSelected) Color.Transparent else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f),
                                                shape = CircleShape
                                            )
                                            .clickable {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                categoryFilter = filter
                                            }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = filter,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = contentColor
                                        )
                                    }
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
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
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
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(100))
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(100))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onBack()
                        }
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Cancel",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Main Submit Transaction button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(100))
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            val amt = amountString.toDoubleOrNull()
                            val desc = if (description.trim().isEmpty()) {
                                val currentSelection = collections.find { it.id == selectedCollectionId }
                                currentSelection?.name ?: "Expense"
                            } else {
                                description.trim()
                            }

                            if (amt == null || amt <= 0.0) {
                                errorText = "Enter a valid positive transaction amount!"
                            } else if (collections.isEmpty()) {
                                errorText = "Create a category collection first!"
                            } else {
                                viewModel.addTransaction(desc, amt, transactionType, selectedCollectionId)
                                onBack()
                            }
                        }
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Add",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}
