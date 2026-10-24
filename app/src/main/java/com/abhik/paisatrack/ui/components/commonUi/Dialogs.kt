package com.abhik.paisatrack.ui.components.commonUi

import androidx.compose.material3.Icon
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.toShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.res.painterResource
import com.abhik.paisatrack.R
import androidx.compose.ui.window.DialogProperties
import com.abhik.paisatrack.data.model.TransactionEntity
import com.swmansion.pulsar.Pulsar
import java.text.DecimalFormat
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.ui.platform.LocalDensity
import com.abhik.paisatrack.ui.utils.findActivity
import com.abhik.paisatrack.ui.utils.getSafePresets

private enum class DialogSubmitState {
    Idle,
    Loading,
    Success
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AddCollectionDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, colorHex: String, iconName: String, budget: Double?) -> Unit
) {
    AddCollectionBottomSheet(
        onDismiss = onDismiss,
        onConfirm = onConfirm
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PlusMenuDialog(
    onDismiss: () -> Unit,
    onAddTransaction: () -> Unit,
    onAddCollection: () -> Unit
) {
    val isDark = com.abhik.paisatrack.ui.theme.isAppInDarkTheme()
    val context = LocalContext.current
    val pulsar = remember(context) { Pulsar(context.findActivity() ?: context) }
    val presets = remember(pulsar) { pulsar.getSafePresets() }
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
                        painter = painterResource(id = R.drawable.ic_plus_custom),
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
                    text = "Create a new collection or add a spending entry.",
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
                                .size(44.dp)
                                .clip(MaterialShapes.Flower.toShape())
                                .background(Color(0xFFB7DAAE).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                                contentDescription = null,
                                tint = Color(0xFFB7DAAE),
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Spending Record",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Add incoming or outgoing entries",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }

                        Icon(
                            imageVector = ArrowCircleRightIconVector,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(22.dp)
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
                                .size(44.dp)
                                .clip(MaterialShapes.Cookie4Sided.toShape())
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
                                text = "Organize entries into custom groups",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }

                        Icon(
                            imageVector = ArrowCircleRightIconVector,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(22.dp)
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DeleteTransactionConfirmDialog(
    transaction: TransactionEntity,
    dollarFormat: DecimalFormat,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val context = LocalContext.current
    val pulsar = remember(context) { Pulsar(context.findActivity() ?: context) }
    val presets = remember(pulsar) { pulsar.getSafePresets() }
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
                    painter = painterResource(id = R.drawable.ic_delete),
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(48.dp)
                )

                Text(
                    text = "Delete Record?",
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

                    val deleteScope = rememberCoroutineScope()
                    var deleteSubmitState by remember { mutableStateOf(DialogSubmitState.Idle) }
                    val isDeleteSubmitting = deleteSubmitState == DialogSubmitState.Loading || deleteSubmitState == DialogSubmitState.Success

                    val deleteBgColor by animateColorAsState(
                        targetValue = MaterialTheme.colorScheme.error,
                        animationSpec = tween(350), label = "deleteBg"
                    )
                    val deleteContentColor by animateColorAsState(
                        targetValue = MaterialTheme.colorScheme.onError,
                        animationSpec = tween(350), label = "deleteContent"
                    )
                    val deleteBorderWidth by animateDpAsState(
                        targetValue = 0.dp,
                        animationSpec = tween(350), label = "deleteBorder"
                    )
                    val deleteBorderColor by animateColorAsState(
                        targetValue = Color.Transparent,
                        animationSpec = tween(350), label = "deleteBorderColor"
                    )

                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val totalWidth = maxWidth
                        val idleButtonWidth = (totalWidth - 12.dp) / 2
                        val actionButtonWidth by animateDpAsState(
                            targetValue = if (isDeleteSubmitting) 52.dp else idleButtonWidth,
                            animationSpec = spring(
                                dampingRatio = 0.8f,
                                stiffness = Spring.StiffnessMedium
                            ),
                            label = "actionButtonWidth"
                        )
                        val rowSpacing by animateDpAsState(
                            targetValue = if (isDeleteSubmitting) 0.dp else 12.dp,
                            animationSpec = spring(
                                dampingRatio = 0.8f,
                                stiffness = Spring.StiffnessMedium
                            ),
                            label = "rowSpacing"
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(rowSpacing, Alignment.CenterHorizontally),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AnimatedVisibility(
                                visible = !isDeleteSubmitting,
                                enter = fadeIn(tween(250)) + expandHorizontally(spring(stiffness = Spring.StiffnessMedium)),
                                exit = fadeOut(tween(200)) + shrinkHorizontally(spring(stiffness = Spring.StiffnessMedium))
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        presets.boulder()
                                        onDismiss()
                                    },
                                    modifier = Modifier.width(idleButtonWidth).height(52.dp),
                                    shape = CircleShape
                                ) {
                                    Text("Cancel", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .width(actionButtonWidth)
                                    .height(52.dp)
                                    .clip(CircleShape)
                                    .border(deleteBorderWidth, deleteBorderColor, CircleShape)
                                    .background(deleteBgColor)
                                    .clickable(enabled = deleteSubmitState == DialogSubmitState.Idle) {
                                        presets.ping()
                                        deleteScope.launch {
                                            deleteSubmitState = DialogSubmitState.Loading
                                            delay(1500)
                                            deleteSubmitState = DialogSubmitState.Success
                                            presets.systemNotificationSuccess()
                                            delay(800)
                                            onConfirm()
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                AnimatedContent(
                                    targetState = deleteSubmitState,
                                    transitionSpec = {
                                        (fadeIn(tween(300)) + scaleIn(tween(300), initialScale = 0.8f))
                                            .togetherWith(fadeOut(tween(200)) + scaleOut(tween(200), targetScale = 0.6f))
                                    },
                                    label = "deleteContent"
                                ) { state ->
                                    when (state) {
                                        DialogSubmitState.Idle -> {
                                            Text("Delete", fontWeight = FontWeight.Bold, color = deleteContentColor)
                                        }
                                        DialogSubmitState.Loading -> {
                                            val density = LocalDensity.current
                                            val strokeWidthPx = with(density) { 2.5.dp.toPx() }
                                            val amplitudePx = with(density) { 3.dp.toPx() }
                                            CircularWavyProgressIndicator(
                                                modifier = Modifier.size(24.dp),
                                                color = MaterialTheme.colorScheme.onError,
                                                trackColor = MaterialTheme.colorScheme.onError.copy(alpha = 0.25f),
                                                stroke = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidthPx),
                                                trackStroke = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidthPx),
                                                amplitude = amplitudePx,
                                                wavelength = 6.dp
                                            )
                                        }
                                        DialogSubmitState.Success -> {
                                            Icon(
                                                painter = painterResource(id = R.drawable.ic_tick),
                                                contentDescription = "Check",
                                                tint = MaterialTheme.colorScheme.onError,
                                                modifier = Modifier.size(26.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
            }
        }
    }
}

private val AddIconVector: ImageVector
    get() {
        if (_addIconVector != null) {
            return _addIconVector!!
        }
        _addIconVector = ImageVector.Builder(
            name = "add",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                fillAlpha = 1f,
                stroke = null,
                strokeAlpha = 1f,
                strokeLineWidth = 1f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Bevel,
                strokeLineMiter = 1f,
                pathFillType = PathFillType.Companion.NonZero
            ) {
                moveTo(11f, 13f)
                horizontalLineTo(6f)
                quadTo(5.58f, 13f, 5.29f, 12.71f)
                quadTo(5f, 12.43f, 5f, 12f)
                reflectiveQuadTo(5.29f, 11.29f)
                reflectiveQuadTo(6f, 11f)
                horizontalLineToRelative(5f)
                verticalLineTo(6f)
                quadTo(11f, 5.57f, 11.29f, 5.29f)
                reflectiveQuadTo(12f, 5f)
                reflectiveQuadToRelative(0.71f, 0.29f)
                reflectiveQuadTo(13f, 6f)
                verticalLineToRelative(5f)
                horizontalLineToRelative(5f)
                quadToRelative(0.43f, 0f, 0.71f, 0.29f)
                reflectiveQuadTo(19f, 12f)
                reflectiveQuadToRelative(-0.29f, 0.71f)
                reflectiveQuadTo(18f, 13f)
                horizontalLineTo(13f)
                verticalLineToRelative(5f)
                quadToRelative(0f, 0.43f, -0.29f, 0.71f)
                reflectiveQuadTo(12f, 19f)
                reflectiveQuadTo(11.29f, 18.71f)
                quadTo(11f, 18.43f, 11f, 18f)
                verticalLineTo(13f)
                close()
            }
        }.build()
        return _addIconVector!!
    }

private var _addIconVector: ImageVector? = null

private val ArrowCircleRightIconVector: ImageVector
    get() {
        if (_arrowCircleRightIconVector != null) {
            return _arrowCircleRightIconVector!!
        }
        _arrowCircleRightIconVector = ImageVector.Builder(
            name = "arrow_circle_right",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                fillAlpha = 1f,
                stroke = null,
                strokeAlpha = 1f,
                strokeLineWidth = 1f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Bevel,
                strokeLineMiter = 1f,
                pathFillType = PathFillType.Companion.NonZero
            ) {
                moveTo(12.2f, 13f)
                lineToRelative(-0.9f, 0.9f)
                quadToRelative(-0.28f, 0.28f, -0.28f, 0.7f)
                reflectiveQuadToRelative(0.28f, 0.7f)
                quadToRelative(0.28f, 0.28f, 0.7f, 0.28f)
                reflectiveQuadTo(12.7f, 15.3f)
                lineToRelative(2.6f, -2.6f)
                quadTo(15.6f, 12.4f, 15.6f, 12f)
                reflectiveQuadTo(15.3f, 11.3f)
                lineTo(12.7f, 8.7f)
                quadTo(12.43f, 8.42f, 12f, 8.42f)
                reflectiveQuadTo(11.3f, 8.7f)
                reflectiveQuadTo(11.03f, 9.4f)
                reflectiveQuadToRelative(0.28f, 0.7f)
                lineTo(12.2f, 11f)
                horizontalLineTo(9f)
                quadTo(8.58f, 11f, 8.29f, 11.29f)
                reflectiveQuadTo(8f, 12f)
                reflectiveQuadToRelative(0.29f, 0.71f)
                quadTo(8.58f, 13f, 9f, 13f)
                horizontalLineToRelative(3.2f)
                close()
                moveTo(12f, 22f)
                quadTo(9.93f, 22f, 8.1f, 21.21f)
                quadTo(6.28f, 20.43f, 4.93f, 19.08f)
                quadTo(3.58f, 17.73f, 2.79f, 15.9f)
                reflectiveQuadTo(2f, 12f)
                quadTo(2f, 9.92f, 2.79f, 8.1f)
                quadTo(3.58f, 6.27f, 4.93f, 4.93f)
                quadTo(6.28f, 3.57f, 8.1f, 2.79f)
                quadTo(9.93f, 2f, 12f, 2f)
                reflectiveQuadToRelative(3.9f, 0.79f)
                reflectiveQuadToRelative(3.17f, 2.14f)
                quadToRelative(1.35f, 1.35f, 2.14f, 3.17f)
                quadTo(22f, 9.92f, 22f, 12f)
                reflectiveQuadToRelative(-0.79f, 3.9f)
                reflectiveQuadToRelative(-2.14f, 3.17f)
                quadToRelative(-1.35f, 1.35f, -3.17f, 2.14f)
                reflectiveQuadTo(12f, 22f)
                close()
                moveToRelative(0f, -2f)
                quadToRelative(3.35f, 0f, 5.68f, -2.32f)
                reflectiveQuadTo(20f, 12f)
                reflectiveQuadTo(17.68f, 6.32f)
                reflectiveQuadTo(12f, 4f)
                reflectiveQuadTo(6.33f, 6.32f)
                reflectiveQuadTo(4f, 12f)
                reflectiveQuadToRelative(2.33f, 5.68f)
                reflectiveQuadTo(12f, 20f)
                close()
                moveToRelative(0f, -8f)
                close()
            }
        }.build()
        return _arrowCircleRightIconVector!!
    }

private var _arrowCircleRightIconVector: ImageVector? = null


