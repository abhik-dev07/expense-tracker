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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailBottomSheet(
    transaction: TransactionEntity,
    collectionName: String,
    collectionColor: Color,
    collectionIcon: ImageVector,
    dollarFormat: DecimalFormat,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {

    val isIncome = transaction.type.uppercase() == "INCOME"
    val isDark = isSystemInDarkTheme()
    val context = LocalContext.current
    val pulsar = remember { Pulsar(context) }
    val presets = remember { pulsar.getPresets() }
    // Match exact date & time logic from the mockup
    val todayStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
    val yesterdayStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(System.currentTimeMillis() - 86400000L))
    val txDateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(transaction.timestamp))
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
                text = "Transaction Details",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Centered beautifully rounded icon
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
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
                    value = transaction.type.lowercase().replaceFirstChar { it.uppercase() }
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

            // Standard pill shape Rounded Close Button with Haptic
            Button(
                onClick = {
                    presets.boulder()
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDark) MaterialTheme.colorScheme.primaryContainer else Color(0xFFE4F6E6),
                    contentColor = if (isDark) MaterialTheme.colorScheme.onPrimaryContainer else Color(0xFF1C2C1D)
                )
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
