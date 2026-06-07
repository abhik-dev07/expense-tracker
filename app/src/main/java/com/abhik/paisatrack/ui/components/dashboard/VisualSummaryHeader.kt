package com.abhik.paisatrack.ui.components.dashboard

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.abhik.paisatrack.ui.FinanceUiState
import com.swmansion.pulsar.Pulsar
import java.text.DecimalFormat
import kotlinx.coroutines.launch

@Composable
fun DetailItemRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End
        )
    }
}

@Composable
fun VisualSummaryHeader(
    uiState: FinanceUiState,
    dollarFormat: DecimalFormat,
    modifier: Modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
) {

    var showDetailDialog by remember { mutableStateOf(false) }
    
    val netBalance = uiState.totalIncome - uiState.totalExpense
    
    var hasAnimated by rememberSaveable { mutableStateOf(false) }
    val animatedBalance = remember { Animatable(0f) }
    val animatedIncome = remember { Animatable(0f) }
    val animatedExpense = remember { Animatable(0f) }
    val context = LocalContext.current
    val pulsar = remember { Pulsar(context) }
    val presets = remember { pulsar.getPresets() }

    LaunchedEffect(netBalance, uiState.totalIncome, uiState.totalExpense, uiState.isLoading) {
        if (!uiState.isLoading) {
            if (!hasAnimated) {
                launch {
                    animatedBalance.animateTo(
                        targetValue = netBalance.toFloat(),
                        animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing)
                    )
                }
                launch {
                    animatedIncome.animateTo(
                        targetValue = uiState.totalIncome.toFloat(),
                        animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing)
                    )
                }
                launch {
                    animatedExpense.animateTo(
                        targetValue = uiState.totalExpense.toFloat(),
                        animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing)
                    )
                }
                hasAnimated = true
            } else {
                launch {
                    animatedBalance.animateTo(
                        targetValue = netBalance.toFloat(),
                        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
                    )
                }
                launch {
                    animatedIncome.animateTo(
                        targetValue = uiState.totalIncome.toFloat(),
                        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
                    )
                }
                launch {
                    animatedExpense.animateTo(
                        targetValue = uiState.totalExpense.toFloat(),
                        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
                    )
                }
            }
        }
    }

    val displayBalance = if (hasAnimated) animatedBalance.value.toDouble() else 0.0
    val displayIncome = if (hasAnimated) animatedIncome.value.toDouble() else 0.0
    val displayExpense = if (hasAnimated) animatedExpense.value.toDouble() else 0.0

    val incomeStr = dollarFormat.format(displayIncome)
    val expenseStr = dollarFormat.format(displayExpense)
    val netStr = dollarFormat.format(displayBalance)

    val isDark = isSystemInDarkTheme()
    
    // Tactile parent Card wrapping the entire Balance section
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier)
            .clip(RoundedCornerShape(24.dp))
            .clickable {
                presets.ping()
                showDetailDialog = true
            },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // "Total Balance" Label
            Text(
                text = "Overview",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(6.dp))
            
            // Giant Net Balance Center text
            Text(
                text = netStr,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Side-by-side Sub cards inside row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Income Card Item (Left)
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) MaterialTheme.colorScheme.surface.copy(alpha = 0.8f) else Color(0xFFF8FAFC)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Soft clean Green arrow circle using Hex #B7DAAE
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isDark) Color(0xFFB7DAAE).copy(alpha = 0.2f) else Color(0xFFB7DAAE)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = "Cash In icon",
                                tint = if (isDark) Color(0xFFB7DAAE) else Color(0xFF1F4D20),
                                modifier = Modifier
                                    .size(18.dp)
                                    .rotate(45f) // Rotates standard down arrow to point Down-Left (South-West)
                            )
                        }
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Cash In",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = incomeStr,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Expense Card Item (Right)
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) MaterialTheme.colorScheme.surface.copy(alpha = 0.8f) else Color(0xFFF8FAFC)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Soft clean Red arrow circle using Hex #FFB8A9
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isDark) Color(0xFFFFB8A9).copy(alpha = 0.2f) else Color(0xFFFFB8A9)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = "Cash Out icon",
                                tint = if (isDark) Color(0xFFFFB8A9) else Color(0xFF6E261A),
                                modifier = Modifier
                                    .size(18.dp)
                                    .rotate(45f) // Rotates standard up arrow to point Up-Right (North-East)
                            )
                        }
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Cash Out",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = expenseStr,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }

    // High Precision uncompressed details Modal / Dialog
    if (showDetailDialog) {
        Dialog(
            onDismissRequest = { showDetailDialog = false },
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
                    Text(
                        text = "Summary",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f))

                    // Detail Row 1: Net Overview
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Net Amount",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = netStr,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (netBalance >= 0) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f))

                    // Detail Row 2: Total Cash In
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total Cash In",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = incomeStr,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color(0xFFB7DAAE) else Color(0xFF1F4D20)
                        )
                    }

                    // Detail Row 3: Total Cash Out
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total Cash Out",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = expenseStr,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color(0xFFFFB8A9) else Color(0xFF6E261A)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    FilledTonalButton(
                        onClick = {
                            presets.boulder()
                            showDetailDialog = false
                        },
                        shape = CircleShape,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Text(
                            text = "Got it",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}
