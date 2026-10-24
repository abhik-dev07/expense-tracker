package com.abhik.paisatrack.ui.screens.addtransaction.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhik.paisatrack.R
import com.abhik.paisatrack.data.model.CollectionEntity
import com.abhik.paisatrack.ui.components.commonUi.ConnectedButtonGroup
import com.abhik.paisatrack.ui.components.getIconByName
import com.abhik.paisatrack.ui.utils.findActivity
import com.abhik.paisatrack.ui.utils.getSafePresets
import com.abhik.paisatrack.ui.utils.safeParseColor
import com.airbnb.lottie.compose.*
import com.swmansion.pulsar.Pulsar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CategoryDropdownSelector(
    collections: List<CollectionEntity>,
    selectedCollectionId: String,
    onSelectCollection: (String) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    if (collections.isEmpty()) return

    val context = LocalContext.current
    val pulsar = remember(context) { Pulsar(context.findActivity() ?: context) }
    val presets = remember(pulsar) { pulsar.getSafePresets() }

    val lottieComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.nothing))
    val lottieProgress by animateLottieCompositionAsState(
        composition = lottieComposition,
        iterations = LottieConstants.IterateForever
    )

    var dropdownExpanded by remember { mutableStateOf(false) }
    var categoryFilter by remember { mutableStateOf("All") }
    val currentSelection = collections.find { it.id == selectedCollectionId } ?: collections.first()
    val colColor = safeParseColor(currentSelection.hexColor, fallback = MaterialTheme.colorScheme.primary)

    Column(modifier = modifier.fillMaxWidth()) {
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
                    .clickable(enabled = enabled) {
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

            // Beautiful Custom Dropdown Menu design
            DropdownMenu(
                expanded = dropdownExpanded,
                onDismissRequest = { dropdownExpanded = false },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                // Fixed top filter connected button group
                val filters = listOf("All", "Prebuild", "Owned")
                ConnectedButtonGroup(
                    options = filters,
                    selectedOption = categoryFilter,
                    onOptionSelected = { filter ->
                        presets.plunk()
                        categoryFilter = filter
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) { filter, isSelected ->
                    Text(
                        text = filter,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
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
                            .heightIn(max = 260.dp) // 5 items * 52.dp = 260.dp
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

                                        if (isSelected) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.ic_tick),
                                                contentDescription = "Check",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    presets.ping()
                                    onSelectCollection(col.id)
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
