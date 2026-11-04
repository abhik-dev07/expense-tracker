package com.abhik.paisatrack.screens.accountprivacy

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import com.abhik.paisatrack.R
import com.abhik.paisatrack.ui.theme.isAppInDarkTheme

@Composable
fun DeleteAccountFeedbackScreen(
    reason: String,
    onBack: () -> Unit,
    onDeleteAccountConfirmed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isAppInDarkTheme()
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var feedbackText by remember { mutableStateOf("") }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }

    val isHeaderScrolled by remember { derivedStateOf { scrollState.value > 10 } }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                focusManager.clearFocus()
                keyboardController?.hide()
            }
    ) {
        // Sticky Header / Top App Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .zIndex(1f)
                .background(
                    if (isHeaderScrolled) {
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.background,
                                MaterialTheme.colorScheme.background.copy(alpha = 1f),
                                MaterialTheme.colorScheme.background.copy(alpha = 0.7f),
                                Color.Transparent
                            )
                        )
                    } else {
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.background,
                                MaterialTheme.colorScheme.background
                            )
                        )
                    }
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular back button matching design system (no haptic on back press)
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (isDark) MaterialTheme.colorScheme.surfaceVariant else Color.White)
                        .border(
                            BorderStroke(
                                1.dp,
                                if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.10f)
                            ),
                            CircleShape
                        )
                        .clickable {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                            onBack()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_chevron_back),
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Centered "Delete Account" Title
                Text(
                    text = "Delete Account",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 44.dp), // Balance back button
                    textAlign = TextAlign.Center
                )
            }
        }

        // Scrollable Body Content with IME padding for keyboard scrolling
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(scrollState)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                }
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.statusBarsPadding().height(86.dp))

            // Dynamic Heading based on selected reason
            Text(
                text = reason,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Subtitle
            Text(
                text = "Do you have any feedback for us? We would love to hear from you! (optional)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 20.sp,
                color = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Feedback Text Field - Starts compact, auto-expands downward on line break
            OutlinedTextField(
                value = feedbackText,
                onValueChange = { feedbackText = it },
                placeholder = {
                    Text(
                        text = "Please share your feedback (Optional)",
                        fontSize = 14.sp,
                        color = if (isDark) Color(0xFF9CA3AF) else Color(0xFF9CA3AF)
                    )
                },
                singleLine = false,
                minLines = 1,
                maxLines = 8,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Default
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
                    .animateContentSize(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (isDark) Color.White.copy(alpha = 0.25f) else Color.Black.copy(alpha = 0.20f),
                    unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.10f),
                    focusedContainerColor = if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f) else Color.White,
                    unfocusedContainerColor = if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f) else Color.White,
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Pill-shaped Action Button adapting to theme primary and onPrimary
            Button(
                onClick = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                    showDeleteConfirmDialog = true
                },
                shape = CircleShape, // Pill shape
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    text = "Delete my account",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Note Text
            Text(
                text = "Note* : All data associated with this account will be deleted in accordance with our privacy policy. You will not be able to retrieve this information once deleted.",
                fontSize = 12.sp,
                lineHeight = 17.sp,
                fontWeight = FontWeight.Normal,
                color = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280)
            )

            Spacer(modifier = Modifier.navigationBarsPadding().height(100.dp))
        }
    }

    if (showDeleteConfirmDialog) {
        DeleteConfirmationDialog(
            isDeleting = isDeleting,
            onDismiss = {
                if (!isDeleting) {
                    showDeleteConfirmDialog = false
                }
            },
            onConfirm = {
                isDeleting = true
                onDeleteAccountConfirmed()
            }
        )
    }
}

@Composable
private fun DeleteConfirmationDialog(
    isDeleting: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(
        onDismissRequest = { if (!isDeleting) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = modifier
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
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.error.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_delete),
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Text(
                    text = "Request Account Deletion?",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Are you sure you want to permanently delete your account? All your transaction history, custom categories, collections, and account data will be permanently wiped. This action cannot be undone.",
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
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
                        onClick = onDismiss,
                        enabled = !isDeleting,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = CircleShape
                    ) {
                        Text(
                            text = "Cancel",
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = onConfirm,
                        enabled = !isDeleting,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = CircleShape
                    ) {
                        if (isDeleting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onError,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Delete", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
