package com.abhik.paisatrack.ui.components.dashboard

import androidx.compose.animation.core.Animatable
import com.abhik.paisatrack.R
import androidx.compose.ui.res.painterResource
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.abhik.paisatrack.ui.components.commonUi.shimmerEffect
import com.abhik.paisatrack.ui.theme.VsBgDark
import com.abhik.paisatrack.ui.theme.VsBgLight
import com.abhik.paisatrack.ui.theme.VsContainerDark
import com.abhik.paisatrack.ui.theme.VsContainerLight
import com.abhik.paisatrack.ui.theme.VsTextDark
import com.abhik.paisatrack.ui.theme.VsTextLight
import com.swmansion.pulsar.Pulsar
import java.text.DecimalFormat
import kotlinx.coroutines.launch
import com.abhik.paisatrack.ui.utils.findActivity
import com.abhik.paisatrack.ui.utils.getSafePresets

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

/**
 * Global tracker to ensure VisualSummaryHeader animations (alpha entrance and amount roll)
 * execute once after the initial app open and subsequently only when new data loads.
 */
object VisualSummaryTracker {
    var hasInitialAnimated: Boolean = false
    var lastDataSignature: Any? = null

    fun reset() {
        hasInitialAnimated = false
        lastDataSignature = null
    }
}

private data class HeaderDataSignature(
    val income: Double,
    val expense: Double,
    val txCount: Int,
    val txHash: Int
)

@Composable
fun VisualSummaryHeader(
    uiState: FinanceUiState,
    dollarFormat: DecimalFormat,
    modifier: Modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
) {

    var showDetailDialog by remember { mutableStateOf(false) }
    
    val netBalance = uiState.totalIncome - uiState.totalExpense

    val currentSignature = remember(
        uiState.isLoading,
        uiState.totalIncome,
        uiState.totalExpense,
        uiState.rawTransactions.size,
        uiState.rawTransactions
    ) {
        if (!uiState.isLoading) {
            HeaderDataSignature(
                income = uiState.totalIncome,
                expense = uiState.totalExpense,
                txCount = uiState.rawTransactions.size,
                txHash = uiState.rawTransactions.hashCode()
            )
        } else null
    }

    val isInitialAppOpen = !VisualSummaryTracker.hasInitialAnimated
    val isNewDataLoad = currentSignature != null &&
            VisualSummaryTracker.lastDataSignature != null &&
            VisualSummaryTracker.lastDataSignature != currentSignature

    val shouldAnimateAmounts = isInitialAppOpen || isNewDataLoad

    var hasAnimated by rememberSaveable { mutableStateOf(false) }
    val alphaAnim = remember { Animatable(if (hasAnimated || VisualSummaryTracker.hasInitialAnimated) 1f else 0f) }
    val context = LocalContext.current
    val pulsar = remember(context) { Pulsar(context.findActivity() ?: context) }
    val presets = remember(pulsar) { pulsar.getSafePresets() }

    LaunchedEffect(uiState.isLoading, currentSignature) {
        if (!uiState.isLoading) {
            if (!hasAnimated && !VisualSummaryTracker.hasInitialAnimated) {
                launch {
                    alphaAnim.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(durationMillis = 1000, easing = LinearOutSlowInEasing)
                    )
                }
                hasAnimated = true
                VisualSummaryTracker.hasInitialAnimated = true
            } else {
                alphaAnim.snapTo(1f)
                hasAnimated = true
                VisualSummaryTracker.hasInitialAnimated = true
            }

            if (currentSignature != null) {
                VisualSummaryTracker.lastDataSignature = currentSignature
            }
        }
    }

    // Keep the dollarFormat for the high precision uncompressed details Modal/Dialog
    val incomeStr = dollarFormat.format(uiState.totalIncome)
    val expenseStr = dollarFormat.format(uiState.totalExpense)
    val netStr = dollarFormat.format(netBalance)

    val isDark = com.abhik.paisatrack.ui.theme.isAppInDarkTheme()
    
    // Tactile parent Card wrapping the entire Balance section
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier)
            .clip(RoundedCornerShape(24.dp))
            .clickable(enabled = !uiState.isLoading) {
                presets.ping()
                showDetailDialog = true
            },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) VsBgDark else VsBgLight
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        if (uiState.isLoading) {
            VisualSummaryHeaderSkeleton()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .alpha(alphaAnim.value),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // "Total Balance" Label
                Text(
                    text = "Overview",
                    fontSize = 13.sp,
                    color = if (isDark) VsTextDark.copy(alpha = 0.8f) else VsTextLight.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                // Giant Net Balance Center text
                com.abhik.paisatrack.ui.components.commonUi.AnimatedAmountCounter(
                    amount = netBalance,
                    animateOnInitialMount = shouldAnimateAmounts,
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = if (isDark) VsTextDark else VsTextLight,
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
                            containerColor = if (isDark) VsContainerDark else VsContainerLight
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
                                    painter = painterResource(id = R.drawable.ic_arrow_up),
                                    contentDescription = "Cash In icon",
                                    tint = if (isDark) Color(0xFFB7DAAE) else Color(0xFF1F4D20),
                                    modifier = Modifier
                                        .size(18.dp)
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
                                com.abhik.paisatrack.ui.components.commonUi.AnimatedAmountCounter(
                                    amount = uiState.totalIncome,
                                    animateOnInitialMount = shouldAnimateAmounts,
                                    textStyle = LocalTextStyle.current.copy(
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    }

                    // Expense Card Item (Right)
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDark) VsContainerDark else VsContainerLight
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
                                    painter = painterResource(id = R.drawable.ic_arrow_down),
                                    contentDescription = "Cash Out icon",
                                    tint = if (isDark) Color(0xFFFFB8A9) else Color(0xFF6E261A),
                                    modifier = Modifier
                                        .size(18.dp)
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
                                com.abhik.paisatrack.ui.components.commonUi.AnimatedAmountCounter(
                                    amount = uiState.totalExpense,
                                    animateOnInitialMount = shouldAnimateAmounts,
                                    textStyle = LocalTextStyle.current.copy(
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
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

@Composable
fun VisualSummaryHeaderSkeleton() {
    val isDark = com.abhik.paisatrack.ui.theme.isAppInDarkTheme()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // "Overview" text label shimmer
        Box(
            modifier = Modifier
                .width(70.dp)
                .height(14.dp)
                .clip(RoundedCornerShape(4.dp))
                .shimmerEffect()
        )
        
        Spacer(modifier = Modifier.height(10.dp))
        
        // Net Balance giant shimmer box
        Box(
            modifier = Modifier
                .width(180.dp)
                .height(38.dp)
                .clip(RoundedCornerShape(8.dp))
                .shimmerEffect()
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Side-by-side sub cards skeleton row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Cash In Card skeleton
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
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .shimmerEffect()
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .width(50.dp)
                                .height(11.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .shimmerEffect()
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .width(70.dp)
                                .height(14.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .shimmerEffect()
                        )
                    }
                }
            }

            // Cash Out Card skeleton
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
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .shimmerEffect()
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .width(50.dp)
                                .height(11.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .shimmerEffect()
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .width(70.dp)
                                .height(14.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .shimmerEffect()
                        )
                    }
                }
            }
        }
    }
}


