package com.abhik.paisatrack.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.abhik.paisatrack.data.model.CollectionEntity
import com.abhik.paisatrack.data.model.TransactionEntity
import com.swmansion.pulsar.Pulsar
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
    collections: List<CollectionEntity>,
    onDismiss: () -> Unit,
    onConfirm: (description: String, amount: Double, type: String, collectionId: String) -> Unit
) {
    val context = LocalContext.current
    val pulsar = remember { Pulsar(context) }
    val presets = remember { pulsar.getPresets() }
    val focusManager = LocalFocusManager.current
    var description by remember { mutableStateOf("") }
    var amountString by remember { mutableStateOf("") }
    var transactionType by remember { mutableStateOf("EXPENSE") } // "EXPENSE" or "INCOME"
    var selectedCollectionId by remember { mutableStateOf(collections.firstOrNull()?.id ?: "") }
    var selectedPaymentType by remember { mutableStateOf("Cash") } // "Cash", "Credit/Debit Card", "Check"
    
    var errorText by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 20.dp),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // 1. Mockup Header Block (Round Back Button on Left, Centered Title)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Circular back icon matching the style
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .clickable {
                                presets.boulder()
                                onDismiss()
                            }
                            .border(1.dp, Color(0xFFE5E7EB), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF1E2541),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = "Add transaction",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E2541),
                        modifier = Modifier.weight(1f)
                    )
                }

                // 2. Transaction Type Selector Tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFF3F4F6))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val isExpense = transactionType == "EXPENSE"
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isExpense) Color.White else Color.Transparent)
                            .clickable {
                                presets.plunk()
                                transactionType = "EXPENSE"
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Expense",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isExpense) Color(0xFF1E2541) else Color(0xFF9CA3AF)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (!isExpense) Color.White else Color.Transparent)
                            .clickable {
                                presets.plunk()
                                transactionType = "INCOME"
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Income",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (!isExpense) Color(0xFF1E2541) else Color(0xFF9CA3AF)
                        )
                    }
                }

                // 3. Description field (Optional - defaults to collection name)
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Description",
                        color = Color(0xFF9CA3AF),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
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
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF1E2541),
                            unfocusedTextColor = Color(0xFF1E2541),
                            focusedContainerColor = Color(0xFFF7F8F9),
                            unfocusedContainerColor = Color(0xFFF7F8F9),
                            focusedBorderColor = if (description.length == 40) MaterialTheme.colorScheme.error else Color(0xFF1E2541),
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
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFFF7F8F9))
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AttachMoney,
                            contentDescription = "Dollar Sign",
                            tint = Color(0xFF1E2541),
                            modifier = Modifier.size(24.dp)
                        )
                        TextField(
                            value = amountString,
                            onValueChange = {
                                if (it.length <= 8 && (it.isEmpty() || it.toDoubleOrNull() != null || it.last() == '.')) {
                                    amountString = it
                                    if (it.length == 8) {
                                        focusManager.clearFocus()
                                    }
                                }
                            },
                            placeholder = { Text("0.00", color = Color(0xFF9CA3AF), fontSize = 22.sp, fontWeight = FontWeight.Bold) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = TextStyle(
                                color = Color(0xFF1E2541),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                    }
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

                // 5. Category Box with collection details spin dropdown
                if (collections.isNotEmpty()) {
                    var dropdownExpanded by remember { mutableStateOf(false) }
                    val currentSelection = collections.find { it.id == selectedCollectionId } ?: collections.first()
                    val colColor = Color(android.graphics.Color.parseColor(currentSelection.hexColor))

                    // Determine background badge and icon color based on selection name
                    val (badgeBg, iconCol) = remember(currentSelection.name) {
                        val nameLower = currentSelection.name.lowercase()
                        if (nameLower.contains("food") || nameLower.contains("coffee") || nameLower.contains("dining")) {
                            Color(0xFFFFEDD5) to Color(0xFFF97316)
                        } else if (nameLower.contains("entertainment") || nameLower.contains("ticket")) {
                            Color(0xFFF3E8FF) to Color(0xFFA855F7)
                        } else if (nameLower.contains("salary") || nameLower.contains("income")) {
                            Color(0xFFE4F6E6) to Color(0xFF10B981)
                        } else {
                            colColor.copy(alpha = 0.15f) to colColor
                        }
                    }

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Category",
                            color = Color(0xFF9CA3AF),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFFF7F8F9))
                                .clickable {
                                    presets.ping()
                                    dropdownExpanded = true
                                }
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(badgeBg),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = getIconByName(currentSelection.iconName),
                                            contentDescription = null,
                                            tint = iconCol,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Text(
                                        text = currentSelection.name,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E2541)
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Dropdown Chevron",
                                    tint = Color(0xFF1E2541)
                                )
                            }

                            DropdownMenu(
                                expanded = dropdownExpanded,
                                onDismissRequest = { dropdownExpanded = false },
                                modifier = Modifier
                                    .fillMaxWidth(0.9f)
                                    .background(Color.White)
                            ) {
                                collections.forEach { col ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                Icon(
                                                    imageVector = getIconByName(col.iconName),
                                                    contentDescription = null,
                                                    tint = Color(android.graphics.Color.parseColor(col.hexColor)),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Text(
                                                    text = col.name,
                                                    fontWeight = FontWeight.Medium,
                                                    color = Color(0xFF1E2541)
                                                )
                                            }
                                        },
                                        onClick = {
                                            presets.plunk()
                                            selectedCollectionId = col.id
                                            dropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // 6. Payment Type Radio Selector Stack
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Payment Type",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E2541)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val paymentTypes = listOf("Cash", "Credit / Debit Card", "Check")
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        paymentTypes.forEach { type ->
                            val isSelected = selectedPaymentType == type
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isSelected) Color(0xFFE4F6E6) else Color.Transparent)
                                    .clickable {
                                        presets.plunk()
                                        selectedPaymentType = type
                                    }
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) Color(0xFFB7DAAE) else Color(0xFFE5E7EB),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .padding(horizontal = 18.dp, vertical = 14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = type,
                                        fontSize = 15.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = Color(0xFF1E2541)
                                    )

                                    // Mockup checked icon / radio ring dot
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) Color(0xFF10B981) else Color.Transparent)
                                            .border(
                                                width = 1.5.dp,
                                                color = if (isSelected) Color(0xFF10B981) else Color(0xFF9CA3AF),
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Checked",
                                                tint = Color.White,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Error text feedback
                if (errorText.isNotEmpty()) {
                    Text(
                        text = errorText,
                        color = Color.Red,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }

                // 7. Footer Buttons Block ("Draft" and "Add")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Draft action button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(100))
                            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(100))
                            .clickable {
                                presets.boulder()
                                onDismiss()
                            }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Draft",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E2541)
                        )
                    }

                    // Confirm action button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(100))
                            .background(Color(0xFFB7DAAE))
                            .clickable {
                                val amt = amountString.toDoubleOrNull()
                                val desc = if (description.trim().isEmpty()) {
                                    val currentSelection = collections.find { it.id == selectedCollectionId }
                                    currentSelection?.name ?: "Expense"
                                } else {
                                    description.trim()
                                }

                                if (amt == null || amt <= 0.0) {
                                    errorText = "Enter a valid positive transaction amount!"
                                    presets.boulder()
                                } else if (collections.isEmpty()) {
                                    errorText = "Create a collection before adding transaction!"
                                } else {
                                    presets.bassDrop()
                                    onConfirm(desc, amt, transactionType, selectedCollectionId)
                                }
                            }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Add",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E2541)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCollectionDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, colorHex: String, iconName: String, budget: Double?) -> Unit
) {
    val context = LocalContext.current
    val pulsar = remember { Pulsar(context) }
    val presets = remember { pulsar.getPresets() }
    var name by remember { mutableStateOf("") }
    var selectedColorIdx by remember { mutableStateOf(0) }
    var selectedIconIdx by remember { mutableStateOf(0) }

    var errorText by remember { mutableStateOf("") }

    val focusManager = LocalFocusManager.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Create Collection",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                // Collection Name input
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { 
                            if (it.length <= 20) {
                                name = it
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
                            focusedBorderColor = if (name.length == 20) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    )
                    // Char limit indicator + Progress
                    val progressName = name.length / 20f
                    val isLimitName = name.length == 20
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp, start = 4.dp, end = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LinearProgressIndicator(
                            progress = progressName,
                            modifier = Modifier.weight(1f).height(4.dp).clip(CircleShape),
                            color = if (isLimitName) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Text(
                            text = "${name.length}/20",
                            fontSize = 11.sp,
                            color = if (isLimitName) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            fontWeight = if (isLimitName) FontWeight.Bold else FontWeight.Normal
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
                                    presets.plunk()
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
                                    presets.plunk()
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
                    Text(errorText, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
                }

                // Footer CTA Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Cancel Text Button
                    TextButton(
                        onClick = {
                            presets.boulder()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text(
                            text = "Cancel",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }

                    Button(
                        onClick = {
                            if (name.trim().isEmpty()) {
                                errorText = "Collection name is required!"
                                presets.boulder()
                            } else {
                                presets.bassDrop()
                                onConfirm(
                                    name.trim(),
                                    CollectionColors[selectedColorIdx].first,
                                    CollectionIcons[selectedIconIdx].first,
                                    null
                                )
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Create")
                    }
                }
            }
        }
    }
}

@Composable
fun PlusMenuDialog(
    onDismiss: () -> Unit,
    onAddTransaction: () -> Unit,
    onAddCollection: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val context = LocalContext.current
    val pulsar = remember { Pulsar(context) }
    val presets = remember { pulsar.getPresets() }
    Dialog(
        onDismissRequest = onDismiss,
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
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Indicator/Icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Text(
                    text = "Add New Entry",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Choose whether you'd like to structure a new wallet group or draft a ledger record.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Option 1: Add Ledger Record (Transaction)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable {
                            presets.ping()
                            onAddTransaction()
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f) else Color(0xFFF1F5F9)
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFB7DAAE).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = null,
                                tint = Color(0xFFB7DAAE),
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Transaction Record",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Record incoming funds or outgoing bills",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Option 2: Add Collection Category
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable {
                            presets.ping()
                            onAddCollection()
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f) else Color(0xFFF1F5F9)
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFFB8A9).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = null,
                                tint = Color(0xFFFFB8A9),
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Collection Category",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Organize transactions into custom groups",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Cancel Text Button
                TextButton(
                    onClick = {
                        presets.boulder()
                        onDismiss()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text(
                        text = "Cancel",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun DeleteTransactionConfirmDialog(
    transaction: TransactionEntity,
    dollarFormat: DecimalFormat,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val context = LocalContext.current
    val pulsar = remember { Pulsar(context) }
    val presets = remember { pulsar.getPresets() }
    Dialog(
        onDismissRequest = onDismiss,
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
                    text = "Delete Transaction?",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Are you sure you want to delete \"${transaction.description}\" for ${dollarFormat.format(transaction.amount)}? This action cannot be undone.",
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
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        shape = CircleShape
                    ) {
                        Text("Cancel", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Button(
                        onClick = {
                            presets.bassDrop()
                            onConfirm()
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
