package com.abhik.paisatrack.ui.components.commonUi

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import com.abhik.paisatrack.R
import androidx.compose.ui.res.painterResource
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
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.toShape
import com.abhik.paisatrack.ui.theme.SleekSurfaceDark
import com.abhik.paisatrack.ui.theme.isAppInDarkTheme
import com.abhik.paisatrack.ui.utils.findActivity
import com.abhik.paisatrack.ui.utils.getSafePresets
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
    modifier: Modifier = Modifier,
    updateTrigger: Long = 0L
) {

    val isIncome = transaction.type.uppercase() == "INCOME"
    val isDark = isAppInDarkTheme()
    val context = LocalContext.current
    val pulsar = remember(context) { Pulsar(context.findActivity() ?: context) }
    val presets = remember(pulsar) { pulsar.getSafePresets() }
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var isDismissed by remember { mutableStateOf(false) }

    fun dismissSheet() {
        if (!isDismissed) {
            isDismissed = true
            onDismiss()
        }
    }

    val closeButtonBg = if (isDark) MaterialTheme.colorScheme.surfaceVariant else SleekSurfaceDark

    var isShimmering by remember { mutableStateOf(false) }
    var lastProcessedTransaction by remember { mutableStateOf(transaction) }

    LaunchedEffect(transaction, updateTrigger) {
        if (updateTrigger > 0L || transaction != lastProcessedTransaction) {
            lastProcessedTransaction = transaction
            isShimmering = true
            delay(650)
            isShimmering = false
        }
    }

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
        onDismissRequest = { dismissSheet() },
        sheetState = sheetState,
        containerColor = Color.Transparent,
        shape = RoundedCornerShape(0.dp),
        dragHandle = null,
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Floating Circular Close Button above the sheet
            CompositionLocalProvider(LocalContentColor provides Color.White) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(closeButtonBg)
                        .clickable {
                            presets.plunk()
                            scope.launch {
                                sheetState.hide()
                                dismissSheet()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_close_small),
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main sheet surface container
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(start = 24.dp, end = 24.dp, bottom = 24.dp, top = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Title centered matching mockup style
                    Text(
                        text = "Record Details",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Centered beautifully rounded icon
                    if (isShimmering) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(MaterialShapes.VerySunny.toShape())
                                .shimmerEffect()
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(MaterialShapes.VerySunny.toShape())
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
                    }

                    // Formatted Amount
                    if (isShimmering) {
                        Box(
                            modifier = Modifier
                                .width(170.dp)
                                .height(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .shimmerEffect()
                        )
                    } else {
                        val formattedAmount = dollarFormat.format(transaction.amount)
                        val displayAmount = if (formattedAmount.startsWith("₹")) {
                            "${if (isIncome) "+" else "-"}$formattedAmount"
                        } else {
                            "${if (isIncome) "+₹" else "-₹"}$formattedAmount"
                        }
                        Text(
                            text = displayAmount,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isIncome) Color(0xFF10B981) else Color(0xFFEF4444)
                        )
                    }

                    // Description placed UNDER the Amount, centered with proper font & color
                    if (isShimmering) {
                        Box(
                            modifier = Modifier
                                .width(200.dp)
                                .height(18.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .shimmerEffect()
                        )
                    } else if (transaction.description.isNotEmpty()) {
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
                        val metadataItems = listOf(
                            "Type" to (if (isIncome) "Cash In" else "Cash Out") to 76.dp,
                            "Collection Name" to collectionName to 110.dp,
                            "Created Date" to displayDate to 100.dp,
                            "Created Time" to displayTimeStr to 84.dp
                        )
                        metadataItems.forEach { (fieldInfo, shimmerWidth) ->
                            val (label, value) = fieldInfo
                            if (isShimmering) {
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
                                    Box(
                                        modifier = Modifier
                                            .width(shimmerWidth)
                                            .height(16.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .shimmerEffect()
                                    )
                                }
                            } else {
                                DetailItemRow(
                                    label = label,
                                    value = value
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Full-width Edit Button with Haptics (Close button moved to floating top)
                    Button(
                        onClick = {
                            presets.ping()
                            onEditClick()
                        },
                        enabled = !isShimmering,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_edit),
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
                }
            }
        }
    }
}

