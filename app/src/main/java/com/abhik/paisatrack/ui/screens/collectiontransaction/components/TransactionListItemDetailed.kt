package com.abhik.paisatrack.ui.screens.collectiontransaction.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.toShape
import androidx.compose.material3.MaterialShapes
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhik.paisatrack.R
import com.abhik.paisatrack.data.model.TransactionEntity
import com.abhik.paisatrack.ui.screens.collectiontransaction.utils.CollectionTransactionUtils
import com.abhik.paisatrack.ui.utils.findActivity
import com.abhik.paisatrack.ui.utils.getSafePresets
import com.swmansion.pulsar.Pulsar
import com.abhik.paisatrack.ui.theme.SearchBorderDark
import com.abhik.paisatrack.ui.theme.SearchBorderLight
import com.abhik.paisatrack.ui.theme.isAppInDarkTheme
import java.text.DecimalFormat

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TransactionListItemDetailed(
    transaction: TransactionEntity,
    colColor: Color,
    dollarFormat: DecimalFormat,
    onDeleteClick: () -> Unit,
    onLongClick: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateStr = remember(transaction.timestamp) {
        CollectionTransactionUtils.formatTransactionDate(transaction.timestamp)
    }

    val isIncome = transaction.type.uppercase() == "INCOME"

    val context = LocalContext.current
    val pulsar = remember(context) { Pulsar(context.findActivity() ?: context) }
    val presets = remember(pulsar) { pulsar.getSafePresets() }

    val (badgeBg, iconColor) = remember(isIncome, colColor) {
        if (isIncome) Color(0xFFE4F6E6) to Color(0xFF10B981) else Color(0xFFFFB8A9).copy(alpha = 0.15f) to Color(0xFFFFB8A9)
    }

    val semanticDescription = remember(transaction.description, isIncome, transaction.amount, dateStr, dollarFormat) {
        val formattedAmount = dollarFormat.format(transaction.amount)
        val amountWithRupee = if (formattedAmount.startsWith("₹")) formattedAmount else "₹$formattedAmount"
        "${transaction.description}, ${if (isIncome) "Income" else "Expense"} $amountWithRupee, $dateStr"
    }

    @Suppress("DEPRECATION")
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                presets.cleave()
                onDeleteClick()
                false
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            val isSwipingToDelete = dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart
            val iconScale by animateFloatAsState(
                targetValue = if (isSwipingToDelete) 1.15f else 0.85f,
                label = "DeleteIconScaleDetailed"
            )
            val alpha by animateFloatAsState(
                targetValue = if (isSwipingToDelete) 1f else 0.6f,
                label = "DeleteAlphaDetailed"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFEF4444).copy(alpha = 0.95f))
                    .padding(end = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .alpha(alpha)
                        .scale(iconScale)
                ) {
                    Text(
                        text = "Delete",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.ic_delete),
                        contentDescription = "Swipe to delete",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = {
                        presets.ping()
                        onClick()
                    },
                    onLongClick = {
                        presets.plunk()
                        onLongClick()
                    }
                )
                .semantics {
                    contentDescription = semanticDescription
                    customActions = listOf(
                        CustomAccessibilityAction("Delete") {
                            presets.cleave()
                            onDeleteClick()
                            true
                        },
                        CustomAccessibilityAction("View details") {
                            presets.boulder()
                            onClick()
                            true
                        }
                    )
                },
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, if (isAppInDarkTheme()) SearchBorderDark else SearchBorderLight),
            shadowElevation = 1.dp
        ) {
            ListItem(
                headlineContent = {
                    Text(
                        text = transaction.description,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                supportingContent = {
                    Text(
                        text = dateStr,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                leadingContent = {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(MaterialShapes.VerySunny.toShape())
                            .background(badgeBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = if (isIncome) painterResource(id = R.drawable.ic_arrow_down) else painterResource(id = R.drawable.ic_arrow_up),
                            contentDescription = if (isIncome) "Income" else "Expense",
                            tint = iconColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                trailingContent = {
                    val formattedAmount = dollarFormat.format(transaction.amount)
                    val displayAmount = if (formattedAmount.startsWith("₹")) {
                        "${if (isIncome) "+" else "-"}$formattedAmount"
                    } else {
                        "${if (isIncome) "+₹" else "-₹"}$formattedAmount"
                    }
                    Text(
                        text = displayAmount,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = if (isIncome) Color(0xFF10B981) else Color(0xFFEF4444),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                colors = ListItemDefaults.colors(
                    containerColor = Color.Transparent
                )
            )
        }
    }
}
