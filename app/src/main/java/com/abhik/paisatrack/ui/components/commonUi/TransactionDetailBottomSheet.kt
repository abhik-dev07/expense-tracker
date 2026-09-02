package com.abhik.paisatrack.ui.components.commonUi

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhik.paisatrack.data.model.TransactionEntity
import com.abhik.paisatrack.ui.components.dashboard.DetailItemRow
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.swmansion.pulsar.Pulsar
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.toShape
import com.abhik.paisatrack.ui.utils.findActivity
import com.abhik.paisatrack.ui.utils.getSafePresets

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TransactionDetailBottomSheet(
    transaction: TransactionEntity,
    collectionName: String,
    collectionColor: Color,
    collectionIcon: ImageVector,
    dollarFormat: DecimalFormat,
    onDismiss: () -> Unit,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    val isIncome = transaction.type.uppercase() == "INCOME"
    val isDark = isSystemInDarkTheme()
    val context = LocalContext.current
    val pulsar = remember(context) { Pulsar(context.findActivity() ?: context) }
    val presets = remember(pulsar) { pulsar.getSafePresets() }
    // Match exact date & time logic from the mockup
    val todayStr = SimpleDateFormat("dd MMM yyyy", LocalLocale.current.platformLocale).format(Date())
    val yesterdayStr = SimpleDateFormat("dd MMM yyyy", LocalLocale.current.platformLocale).format(Date(System.currentTimeMillis() - 86400000L))
    val txDateStr = SimpleDateFormat("dd MMM yyyy", LocalLocale.current.platformLocale).format(Date(transaction.timestamp))
    val displayDate = when (txDateStr) {
        todayStr -> "Today"
        yesterdayStr -> "Yesterday"
        else -> txDateStr
    }
    val displayTimeStr = remember(transaction.timestamp) {
        val sdfStr = SimpleDateFormat("h:mm a", Locale.getDefault())
        sdfStr.format(Date(transaction.timestamp)).lowercase()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = null,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, bottom = 48.dp, top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
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

            // Title centered matching mockup style
            Text(
                text = "Record Details",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Centered beautifully rounded icon
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(MaterialShapes.Cookie4Sided.toShape())
                    .background(collectionColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = collectionIcon,
                    contentDescription = collectionName,
                    tint = collectionColor,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Centered amount matching type green/red color
            Text(
                text = "${if (isIncome) "+" else "-"}${dollarFormat.format(transaction.amount)}",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = if (isIncome) Color(0xFF10B981) else Color(0xFFEF4444)
            )

            // Description placed UNDER the Amount, centered with proper font & color
            if (transaction.description.isNotEmpty()) {
                Text(
                    text = transaction.description,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 2.dp, bottom = 4.dp)
                )
            }

            // Single thin horizontal divider (mockup table style)
            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f),
                modifier = Modifier.padding(vertical = 4.dp)
            )

            // Metadata detailed fields stacked nicely
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                DetailItemRow(
                    label = "Type",
                    value = if (isIncome) "Cash In" else "Cash Out"
                )
                DetailItemRow(
                    label = "Collection Name",
                    value = collectionName
                )
                DetailItemRow(
                    label = "Created Date",
                    value = displayDate
                )
                DetailItemRow(
                    label = "Created Time",
                    value = displayTimeStr
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Side-by-side Edit and Close Buttons with Haptics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = {
                        presets.ping()
                        onEditClick()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = EditRoundedIconVector,
                        contentDescription = "Edit Transaction",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Edit",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                FilledTonalButton(
                    onClick = {
                        presets.boulder()
                        onDismiss()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = CircleShape
                ) {
                    Text(
                        text = "Close",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
