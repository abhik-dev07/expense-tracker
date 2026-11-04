package com.abhik.paisatrack.screens.aboutus

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.abhik.paisatrack.R
import com.abhik.paisatrack.screens.profile.utils.ProfileUtils
import com.abhik.paisatrack.ui.theme.isAppInDarkTheme
import com.abhik.paisatrack.utils.findActivity
import com.abhik.paisatrack.utils.getSafePresets
import com.swmansion.pulsar.Pulsar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val SUPPORT_EMAIL = "paisatrack18@gmail.com"

/**
 * Clean, modern About Us destination screen adhering to the Blinkit-inspired layout:
 * - Circular back button header with centered screen title
 * - Rich, readable typography and comfortable paragraph spacing
 * - Interactive contact badge with email and app details
 * - Legal navigation rows (Privacy Policy, Terms & Conditions, Licenses) with chevrons and dividers
 */
@Composable
fun AboutUsScreen(
    onBack: () -> Unit,
    onNavigateToPrivacyPolicy: () -> Unit = {},
    onNavigateToTermsConditions: () -> Unit = {},
    onNavigateToDisclaimer: () -> Unit = {},
    onNavigateToLicenses: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val appName = stringResource(id = R.string.app_name)
    val versionName = remember(context) { ProfileUtils.getAppVersionName(context) }
    val isDark = isAppInDarkTheme()
    val pulsar = remember(context) { Pulsar(context.findActivity() ?: context) }
    val presets = remember(pulsar) { pulsar.getSafePresets() }
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    var emailCopied by remember { mutableStateOf(false) }

    val isHeaderScrolled by remember { derivedStateOf { scrollState.value > 10 } }

    val bodyTextColor = if (isDark) Color(0xFFD1D5DB) else Color(0xFF374151)
    val dividerColor = if (isDark) Color.White.copy(alpha = 0.10f) else Color.Black.copy(alpha = 0.08f)
    val chevronColor = if (isDark) Color(0xFF9CA3AF) else Color(0xFF9E9E9E)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Sticky / Blur Top App Bar
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
                // Circular back button matching Profile Screen
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

                // Centered "About Us" Title
                Text(
                    text = "About Us",
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

        // Scrollable Body Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.statusBarsPadding().height(76.dp))

            // Welcome Introduction Paragraph
            Text(
                text = "Welcome to Paisa Track, your simple and smart solution for managing daily expenses and tracking your financial life with ease.",
                fontSize = 15.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Normal,
                color = bodyTextColor
            )

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = dividerColor, thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(20.dp))

            // Who We Are Section
            AboutSectionHeader(title = "Who We Are")
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Paisa Track is a user-friendly expense tracking app designed to help individuals take control of their money. We believe that managing finances shouldn’t be complicated — it should be simple, clear, and accessible to everyone.",
                fontSize = 15.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Normal,
                color = bodyTextColor
            )

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = dividerColor, thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(20.dp))

            // Our Mission Section
            AboutSectionHeader(title = "Our Mission")
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Our mission is to:",
                fontSize = 15.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Normal,
                color = bodyTextColor
            )
            Spacer(modifier = Modifier.height(6.dp))
            BulletItem(text = "Help people understand their spending habits", textColor = bodyTextColor)
            BulletItem(text = "Encourage better financial decisions", textColor = bodyTextColor)
            BulletItem(text = "Make expense tracking effortless and efficient", textColor = bodyTextColor)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "We aim to empower users to stay organized and financially aware in their daily lives.",
                fontSize = 15.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Normal,
                color = bodyTextColor
            )

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = dividerColor, thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(20.dp))

            // What We Offer Section
            AboutSectionHeader(title = "What We Offer")
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "With Paisa Track, you can:",
                fontSize = 15.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Normal,
                color = bodyTextColor
            )
            Spacer(modifier = Modifier.height(6.dp))
            BulletItem(text = "Easily record your daily income and expenses", textColor = bodyTextColor)
            BulletItem(text = "Categorize transactions for better insights", textColor = bodyTextColor)
            BulletItem(text = "Track spending patterns over time", textColor = bodyTextColor)
            BulletItem(text = "Get a clear overview of your financial activity", textColor = bodyTextColor)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Our goal is to give you clarity and control over your money.",
                fontSize = 15.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Normal,
                color = bodyTextColor
            )

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = dividerColor, thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(20.dp))

            // Why Choose Paisa Track Section
            AboutSectionHeader(title = "Why Choose Paisa Track")
            Spacer(modifier = Modifier.height(8.dp))
            BulletItem(text = "Simple and easy-to-use interface", textColor = bodyTextColor)
            BulletItem(text = "Fast and lightweight app experience", textColor = bodyTextColor)
            BulletItem(text = "Secure handling of your data", textColor = bodyTextColor)
            BulletItem(text = "Designed for everyday users", textColor = bodyTextColor)

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = dividerColor, thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(20.dp))

            // Our Vision Section
            AboutSectionHeader(title = "Our Vision")
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "We envision a future where everyone can manage their finances confidently without needing complex tools. Paisa Track is built to make that vision a reality.",
                fontSize = 15.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Normal,
                color = bodyTextColor
            )

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = dividerColor, thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(20.dp))

            // Contact Us Section
            AboutSectionHeader(title = "Contact Us")
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "We’re always open to feedback and suggestions to improve your experience.",
                fontSize = 15.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Normal,
                color = bodyTextColor
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Interactive Contact Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else Color(0xFFF9FAFB),
                border = BorderStroke(1.dp, dividerColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_mail_custom),
                                contentDescription = "Email",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    presets.plunk()
                                    try {
                                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                                            data = Uri.parse("mailto:$SUPPORT_EMAIL")
                                            putExtra(Intent.EXTRA_SUBJECT, "Paisa Track Feedback")
                                        }
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, SUPPORT_EMAIL, Toast.LENGTH_SHORT).show()
                                    }
                                }
                        ) {
                            Text(
                                text = "Email Support",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = SUPPORT_EMAIL,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Copy button
                        IconButton(
                            onClick = {
                                presets.plunk()
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Paisa Track Email", SUPPORT_EMAIL)
                                clipboard.setPrimaryClip(clip)
                                emailCopied = true
                                Toast.makeText(context, "Email copied to clipboard", Toast.LENGTH_SHORT).show()
                                scope.launch {
                                    delay(2000)
                                    emailCopied = false
                                }
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            if (emailCopied) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_tick),
                                    contentDescription = "Copied",
                                    tint = if (isDark) Color.White else Color(0xFF1FB47B),
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_copy),
                                    contentDescription = "Copy email",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Blinkit-style Legal Navigation List Section
            HorizontalDivider(color = dividerColor, thickness = 0.8.dp)

            LegalNavigationRow(
                title = "Privacy policy",
                chevronColor = chevronColor,
                onClick = {
                    presets.plunk()
                    onNavigateToPrivacyPolicy()
                }
            )

            HorizontalDivider(color = dividerColor, thickness = 0.8.dp)

            LegalNavigationRow(
                title = "Terms & Conditions",
                chevronColor = chevronColor,
                onClick = {
                    presets.plunk()
                    onNavigateToTermsConditions()
                }
            )

            HorizontalDivider(color = dividerColor, thickness = 0.8.dp)

            LegalNavigationRow(
                title = "Disclaimer",
                chevronColor = chevronColor,
                onClick = {
                    presets.plunk()
                    onNavigateToDisclaimer()
                }
            )

            HorizontalDivider(color = dividerColor, thickness = 0.8.dp)

            LegalNavigationRow(
                title = "Licenses",
                chevronColor = chevronColor,
                onClick = {
                    presets.plunk()
                    onNavigateToLicenses()
                }
            )

            HorizontalDivider(color = dividerColor, thickness = 0.8.dp)

            Spacer(modifier = Modifier.height(16.dp))

            // Footer note
            Text(
                text = "$appName • Version $versionName",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.navigationBarsPadding().height(24.dp))
        }
    }
}

@Composable
private fun AboutSectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        fontSize = 17.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = modifier
    )
}

@Composable
private fun BulletItem(
    text: String,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "•",
            fontSize = 15.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            modifier = Modifier.padding(end = 8.dp, start = 4.dp)
        )
        Text(
            text = text,
            fontSize = 15.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.Normal,
            color = textColor,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun LegalNavigationRow(
    title: String,
    chevronColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple()
            ) { onClick() }
            .padding(vertical = 18.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Icon(
            painter = painterResource(id = R.drawable.ic_chevron_right),
            contentDescription = null,
            tint = chevronColor,
            modifier = Modifier.size(20.dp)
        )
    }
}
