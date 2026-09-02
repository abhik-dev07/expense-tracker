package com.abhik.paisatrack.ui.components.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.swmansion.pulsar.Pulsar
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateDpAsState
import com.abhik.paisatrack.ui.components.commonUi.AnimatedCheckIcon
import com.abhik.paisatrack.ui.components.commonUi.DeleteRoundedIconVector
import com.abhik.paisatrack.ui.components.commonUi.ExitToAppRoundedIconVector
import com.abhik.paisatrack.ui.utils.findActivity
import com.abhik.paisatrack.ui.utils.getSafePresets

private enum class SettingsSubmitState {
    Idle,
    Loading,
    Success
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsBottomSheet(
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onSignOutClick: () -> Unit,
    onDeleteAccountClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    var showLogoutConfirmDialog by remember { mutableStateOf(false) }
    var showDeleteAccountConfirmDialog by remember { mutableStateOf(false) }
    var showDeleteAccountVerifyDialog by remember { mutableStateOf(false) }
    var deleteVerificationText by remember { mutableStateOf("") }
    val context = LocalContext.current
    val pulsar = remember(context) { Pulsar(context.findActivity() ?: context) }
    val presets = remember(pulsar) { pulsar.getSafePresets() }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 28.dp, start = 20.dp, end = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Settings",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
            )
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            ) {
                Column {
                    ListItem(
                        headlineContent = { Text("Sign Out", fontWeight = FontWeight.Bold) },
                        supportingContent = { Text("Safely close current finance database session") },
                        leadingContent = {
                            Icon(
                                imageVector = ExitToAppRoundedIconVector,
                                contentDescription = "Sign Out",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        modifier = Modifier.clickable {
                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                showLogoutConfirmDialog = true
                                presets.ping()

                            }
                        }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))
                    ListItem(
                        headlineContent = { Text("Delete Account", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error) },
                        supportingContent = { Text("Permanently erase all your data and collections", color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)) },
                        leadingContent = {
                            Icon(
                                imageVector = DeleteRoundedIconVector,
                                contentDescription = "Delete Account",
                                tint = MaterialTheme.colorScheme.error
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        modifier = Modifier.clickable {
                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                  presets.bassDrop()
                                showDeleteAccountConfirmDialog = true
                            }
                        }
                    )
                }
            }
        }
    }

    if (showLogoutConfirmDialog) {
        Dialog(
            onDismissRequest = { 
                showLogoutConfirmDialog = false
                onDismiss()
            },
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
                        imageVector = ExitToAppRoundedIconVector,
                        contentDescription = "Logout",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(48.dp)
                    )

                    Text(
                        text = "Sign Out?",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Are you sure you want to log out of your current finance database session?",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                presets.boulder()
                                showLogoutConfirmDialog = false
                                onDismiss()
                            },
                            modifier = Modifier.weight(1f).height(52.dp),
                            shape = CircleShape
                        ) {
                            Text("Cancel", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        val logoutScope = rememberCoroutineScope()
                        var logoutSubmitState by remember { mutableStateOf(SettingsSubmitState.Idle) }
                        val logoutBgColor by animateColorAsState(
                            targetValue = when (logoutSubmitState) {
                                SettingsSubmitState.Idle -> MaterialTheme.colorScheme.error
                                SettingsSubmitState.Loading, SettingsSubmitState.Success -> MaterialTheme.colorScheme.surface
                            },
                            animationSpec = tween(400), label = "logoutBg"
                        )
                        val logoutContentColor by animateColorAsState(
                            targetValue = when (logoutSubmitState) {
                                SettingsSubmitState.Idle -> MaterialTheme.colorScheme.onError
                                SettingsSubmitState.Loading, SettingsSubmitState.Success -> MaterialTheme.colorScheme.error
                            },
                            animationSpec = tween(400), label = "logoutContent"
                        )
                        val logoutBorderWidth by animateDpAsState(
                            targetValue = if (logoutSubmitState == SettingsSubmitState.Idle) 0.dp else 1.dp,
                            animationSpec = tween(400), label = "logoutBorder"
                        )
                        val logoutBorderColor by animateColorAsState(
                            targetValue = if (logoutSubmitState == SettingsSubmitState.Idle) Color.Transparent else MaterialTheme.colorScheme.error.copy(alpha = 0.5f),
                            animationSpec = tween(400), label = "logoutBorderColor"
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .clip(CircleShape)
                                .border(logoutBorderWidth, logoutBorderColor, CircleShape)
                                .background(logoutBgColor)
                                .clickable(enabled = logoutSubmitState == SettingsSubmitState.Idle) {
                                    presets.ping()
                                    logoutScope.launch {
                                        logoutSubmitState = SettingsSubmitState.Loading
                                        delay(1500)
                                        logoutSubmitState = SettingsSubmitState.Success
                                        presets.systemNotificationSuccess()
                                        delay(800)
                                        showLogoutConfirmDialog = false
                                        onDismiss()
                                        onSignOutClick()
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            AnimatedContent(
                                targetState = logoutSubmitState,
                                transitionSpec = {
                                    (fadeIn(tween(300)) + scaleIn(tween(300), initialScale = 0.8f))
                                        .togetherWith(fadeOut(tween(200)) + scaleOut(tween(200), targetScale = 0.6f))
                                },
                                label = "logoutContent"
                            ) { state ->
                                when (state) {
                                    SettingsSubmitState.Idle -> {
                                        Text("Log Out", fontWeight = FontWeight.Bold, color = logoutContentColor)
                                    }
                                    SettingsSubmitState.Loading -> {
                                        val density = androidx.compose.ui.platform.LocalDensity.current
                                        val strokeWidthPx = with(density) { 2.dp.toPx() }
                                        val amplitudePx = with(density) { 4.dp.toPx() }
                                        CircularWavyProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            color = MaterialTheme.colorScheme.error,
                                            trackColor = MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                                            stroke = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidthPx),
                                            trackStroke = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidthPx),
                                            amplitude = amplitudePx,
                                            wavelength = 6.dp
                                        )
                                    }
                                    SettingsSubmitState.Success -> {
                                        AnimatedCheckIcon(
                                            isSelected = true,
                                            tint = logoutContentColor,
                                            modifier = Modifier.size(24.dp)
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

    if (showDeleteAccountConfirmDialog) {
        Dialog(
            onDismissRequest = { 
                showDeleteAccountConfirmDialog = false
                onDismiss()
            },
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
                        imageVector = DeleteRoundedIconVector,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(48.dp)
                    )

                    Text(
                        text = "Delete Account?",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Are you sure you want to delete your account? This will erase all your transaction history and collections from the database. This action cannot be undone.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                presets.boulder()
                                showDeleteAccountConfirmDialog = false
                                onDismiss()
                            },
                            modifier = Modifier.weight(1f).height(52.dp),
                            shape = CircleShape
                        ) {
                            Text("Cancel", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Button(
                            onClick = {
                                showDeleteAccountConfirmDialog = false
                                deleteVerificationText = ""
                                showDeleteAccountVerifyDialog = true
                                presets.ping()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            ),
                            modifier = Modifier.weight(1f).height(52.dp),
                            shape = CircleShape
                        ) {
                            Text("Delete", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showDeleteAccountVerifyDialog) {
        Dialog(
            onDismissRequest = { 
                showDeleteAccountVerifyDialog = false
                onDismiss()
            },
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
                        imageVector = DeleteRoundedIconVector,
                        contentDescription = "Confirm Delete",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(48.dp)
                    )

                    Text(
                        text = "Confirm Account Deletion",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "To confirm deletion, please type \"Delete\" in the field below.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    OutlinedTextField(
                        value = deleteVerificationText,
                        onValueChange = { deleteVerificationText = it },
                        placeholder = { Text("Delete", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedBorderColor = MaterialTheme.colorScheme.error,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                presets.boulder()
                                showDeleteAccountVerifyDialog = false
                                onDismiss()
                            },
                            modifier = Modifier.weight(1f).height(52.dp),
                            shape = CircleShape
                        ) {
                            Text("Cancel", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        val deleteScope = rememberCoroutineScope()
                        var deleteSubmitState by remember { mutableStateOf(SettingsSubmitState.Idle) }
                        val deleteBgColor by animateColorAsState(
                            targetValue = when {
                                deleteSubmitState == SettingsSubmitState.Loading || deleteSubmitState == SettingsSubmitState.Success -> MaterialTheme.colorScheme.surface
                                deleteVerificationText == "Delete" -> MaterialTheme.colorScheme.error
                                else -> Color.Transparent
                            },
                            animationSpec = tween(400), label = "deleteBg"
                        )
                        val deleteContentColor by animateColorAsState(
                            targetValue = when {
                                deleteSubmitState == SettingsSubmitState.Loading || deleteSubmitState == SettingsSubmitState.Success -> MaterialTheme.colorScheme.error
                                deleteVerificationText == "Delete" -> MaterialTheme.colorScheme.onError
                                else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            },
                            animationSpec = tween(400), label = "deleteContent"
                        )
                        val deleteBorderWidth by animateDpAsState(
                            targetValue = when {
                                deleteSubmitState == SettingsSubmitState.Loading || deleteSubmitState == SettingsSubmitState.Success -> 1.dp
                                deleteVerificationText == "Delete" -> 0.dp
                                else -> 1.dp
                            },
                            animationSpec = tween(400), label = "deleteBorder"
                        )
                        val deleteBorderColor by animateColorAsState(
                            targetValue = when {
                                deleteSubmitState == SettingsSubmitState.Loading || deleteSubmitState == SettingsSubmitState.Success -> MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                                deleteVerificationText == "Delete" -> Color.Transparent
                                else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                            },
                            animationSpec = tween(400), label = "deleteBorderColor"
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .clip(CircleShape)
                                .border(deleteBorderWidth, deleteBorderColor, CircleShape)
                                .background(deleteBgColor)
                                .clickable(enabled = deleteSubmitState == SettingsSubmitState.Idle && deleteVerificationText == "Delete") {
                                    presets.ping()
                                    deleteScope.launch {
                                        deleteSubmitState = SettingsSubmitState.Loading
                                        delay(1500)
                                        deleteSubmitState = SettingsSubmitState.Success
                                        presets.systemNotificationSuccess()
                                        delay(800)
                                        showDeleteAccountVerifyDialog = false
                                        onDismiss()
                                        onDeleteAccountClick()
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
                                    SettingsSubmitState.Idle -> {
                                        Text("Delete", fontWeight = FontWeight.Bold, color = deleteContentColor)
                                    }
                                    SettingsSubmitState.Loading -> {
                                        val density = androidx.compose.ui.platform.LocalDensity.current
                                        val strokeWidthPx = with(density) { 2.dp.toPx() }
                                        val amplitudePx = with(density) { 4.dp.toPx() }
                                        CircularWavyProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            color = MaterialTheme.colorScheme.error,
                                            trackColor = MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                                            stroke = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidthPx),
                                            trackStroke = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidthPx),
                                            amplitude = amplitudePx,
                                            wavelength = 6.dp
                                        )
                                    }
                                    SettingsSubmitState.Success -> {
                                        AnimatedCheckIcon(
                                            isSelected = true,
                                            tint = deleteContentColor,
                                            modifier = Modifier.size(24.dp)
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
