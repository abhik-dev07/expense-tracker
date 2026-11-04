package com.abhik.paisatrack.screens.accountprivacy

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import com.abhik.paisatrack.R
import com.abhik.paisatrack.ui.theme.isAppInDarkTheme

private const val SUPPORT_EMAIL = "paisatrack18@gmail.com"

@Composable
fun AccountPrivacyScreen(
    onBack: () -> Unit,
    onNavigateToDeleteAccount: () -> Unit = {},
    onDeleteAccount: (() -> Unit)? = null,
    onNavigateToPrivacyPolicy: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDark = isAppInDarkTheme()
    val scrollState = rememberScrollState()

    var isExpanded by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }

    val isHeaderScrolled by remember { derivedStateOf { scrollState.value > 10 } }

    val bodyTextColor = if (isDark) Color(0xFFD1D5DB) else Color(0xFF4B5563)
    val accentGreen = Color(0xFF1FB47B)

    val collapsedIntroText = "We, i.e., \"Paisa Track\" (“Company”), are committed to protecting the privacy and security of your personal information. Your privacy is important to us, and maintaining your trust is paramount"

    val fullParagraph1 = "We, i.e., \"Paisa Track\" (“Company”), are committed to protecting the privacy and security of your personal information. Your privacy is important to us, and maintaining your trust is paramount."

    val paragraph2 = "This privacy policy explains how we collect, use, process and disclose information about you. By using our website/ app/ platform and affiliated services, you consent to the terms of our privacy policy (“Privacy Policy”) in addition to our ‘Terms of Use.’ We encourage you to read this privacy policy to understand the collection, use, and disclosure of your information from time to time, to keep yourself updated with the changes and updates that we make to this policy."

    val paragraph3 = "This privacy policy describes our privacy practices for all websites, products and services that are linked to it. However this policy does not apply to those affiliates and partners that have their own privacy policy. In such situations, we recommend that you read the privacy policy on the applicable site."

    val paragraph4 = "Should you have any clarifications regarding this privacy policy, please write to us at $SUPPORT_EMAIL"

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
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
                // Circular back button matching Blinkit reference (no haptic on back press)
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

                // Centered "Account privacy" Title
                Text(
                    text = "Account privacy",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 44.dp), // Balance circular back button to keep title centered
                    textAlign = TextAlign.Center
                )
            }
        }

        // Scrollable Body Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.statusBarsPadding().height(86.dp))

            // Section Title: "Account privacy and policy"
            Text(
                text = "Account privacy and policy",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Expandable Privacy Description
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
            ) {
                if (!isExpanded) {
                    Text(
                        text = collapsedIntroText,
                        fontSize = 14.5.sp,
                        lineHeight = 22.sp,
                        color = bodyTextColor
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { isExpanded = true }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Read More ",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = accentGreen
                        )
                        Icon(
                            painter = painterResource(id = R.drawable.ic_chevron_down_custom),
                            contentDescription = "Read More",
                            tint = accentGreen,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                } else {
                    Text(
                        text = fullParagraph1,
                        fontSize = 14.5.sp,
                        lineHeight = 22.sp,
                        color = bodyTextColor
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = paragraph2,
                        fontSize = 14.5.sp,
                        lineHeight = 22.sp,
                        color = bodyTextColor
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = paragraph3,
                        fontSize = 14.5.sp,
                        lineHeight = 22.sp,
                        color = bodyTextColor
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = paragraph4,
                        fontSize = 14.5.sp,
                        lineHeight = 22.sp,
                        color = bodyTextColor,
                        modifier = Modifier.clickable {
                            try {
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:$SUPPORT_EMAIL")
                                }
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                Toast.makeText(context, SUPPORT_EMAIL, Toast.LENGTH_SHORT).show()
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { isExpanded = false }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Read Less ",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = accentGreen
                        )
                        Icon(
                            painter = painterResource(id = R.drawable.ic_chevron_down_custom),
                            contentDescription = "Read Less",
                            tint = accentGreen,
                            modifier = Modifier
                                .size(15.dp)
                                .graphicsLayer { rotationZ = 180f }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Card: "Request to delete account"
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable {
                        onNavigateToDeleteAccount()
                    },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) MaterialTheme.colorScheme.surfaceVariant else Color.White
                ),
                border = BorderStroke(
                    1.dp,
                    if (isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.06f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_delete),
                        contentDescription = "Delete Account",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(22.dp)
                    )

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Request to delete account",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(1.dp))
                        Text(
                            text = "Request for closure of your account",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Normal,
                            color = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280)
                        )
                    }

                    Icon(
                        painter = painterResource(id = R.drawable.ic_chevron_right),
                        contentDescription = null,
                        tint = if (isDark) Color(0xFF9CA3AF) else Color(0xFF9CA3AF),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.navigationBarsPadding().height(100.dp))
        }
    }

    if (showDeleteConfirmDialog) {
        DeleteAccountDialog(
            isDeleting = isDeleting,
            onDismiss = {
                if (!isDeleting) {
                    showDeleteConfirmDialog = false
                }
            },
            onConfirm = {
                isDeleting = true
                onDeleteAccount?.invoke()
            }
        )
    }
}

/**
 * Confirmation dialog for account deletion and data wipe.
 */
@Composable
private fun DeleteAccountDialog(
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
                    text = "Are you sure you want to request closure and deletion of your account? All your transaction history, custom categories, collections, and account data will be permanently wiped. This action cannot be undone.",
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
