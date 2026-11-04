package com.abhik.paisatrack.screens.privacypolicy

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
 * High-craft Privacy Policy screen styled in exact harmony with the Blinkit-inspired About Us screen:
 * - Circular back button header with centered screen title
 * - Centered "Last Updated" pill badge
 * - Rich, readable typography and comfortable paragraph spacing
 * - Styled highlight cards for critical statements (Financial data purpose, Zero selling of data)
 * - Interactive contact badge with email support
 */
@Composable
fun PrivacyPolicyScreen(
    onBack: () -> Unit,
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
    val subTextColor = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280)
    val dividerColor = if (isDark) Color.White.copy(alpha = 0.10f) else Color.Black.copy(alpha = 0.08f)

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
                // Circular back button
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

                // Centered Screen Title
                Text(
                    text = "Privacy Policy",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 44.dp),
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

            // Centered Last Updated Pill Badge
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(100.dp),
                    color = if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f) else Color(0xFFF3F4F6),
                    border = BorderStroke(1.dp, dividerColor)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Last Updated:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "13th April 2026",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            color = subTextColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Introduction Paragraphs
            Text(
                text = "Paisa Track (“we”, “our”, or “us”) operates the Paisa Track mobile application. This Privacy Policy explains how we collect, use, and protect your information when you use our app.",
                fontSize = 15.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Normal,
                color = bodyTextColor
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "By using Paisa Track, you agree to the collection and use of information in accordance with this policy.",
                fontSize = 15.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Normal,
                color = bodyTextColor
            )

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = dividerColor, thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(20.dp))

            // 1. Information We Collect
            PolicySectionHeader(title = "1. Information We Collect")
            Spacer(modifier = Modifier.height(12.dp))

            // a. Personal Information
            PolicySubheader(title = "a. Personal Information")
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "We may collect:",
                fontSize = 15.sp,
                lineHeight = 22.sp,
                color = bodyTextColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            PolicyBulletItem(text = "Name", textColor = bodyTextColor)
            PolicyBulletItem(text = "Email address", textColor = bodyTextColor)
            PolicyBulletItem(text = "Login credentials (if signup/login is available)", textColor = bodyTextColor)

            Spacer(modifier = Modifier.height(16.dp))

            // b. Financial Data
            PolicySubheader(title = "b. Financial Data")
            Spacer(modifier = Modifier.height(6.dp))
            PolicyBulletItem(text = "Income and expense details entered by you", textColor = bodyTextColor)
            PolicyBulletItem(text = "Categories, notes, and transaction history", textColor = bodyTextColor)

            Spacer(modifier = Modifier.height(10.dp))

            // Highlight Callout Note for Financial Data
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isDark) Color(0xFF2E2413) else Color(0xFFFEF3C7),
                border = BorderStroke(1.dp, if (isDark) Color(0xFF854D0E) else Color(0xFFFDE68A)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_warn),
                        contentDescription = "Warning",
                        tint = if (isDark) Color(0xFFFDE68A) else Color(0xFF92400E),
                        modifier = Modifier
                            .padding(end = 10.dp)
                            .size(18.dp)
                    )
                    Text(
                        text = "This data is stored only to provide app functionality.",
                        fontSize = 13.5.sp,
                        lineHeight = 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isDark) Color(0xFFFDE68A) else Color(0xFF92400E)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // c. Device & Usage Information
            PolicySubheader(title = "c. Device & Usage Information")
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "We may automatically collect:",
                fontSize = 15.sp,
                lineHeight = 22.sp,
                color = bodyTextColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            PolicyBulletItem(text = "Device type and OS version", textColor = bodyTextColor)
            PolicyBulletItem(text = "App usage data", textColor = bodyTextColor)
            PolicyBulletItem(text = "IP address", textColor = bodyTextColor)
            PolicyBulletItem(text = "Crash logs and performance data", textColor = bodyTextColor)

            Spacer(modifier = Modifier.height(16.dp))

            // d. Cookies & Tracking Technologies
            PolicySubheader(title = "d. Cookies & Tracking Technologies")
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "If applicable, we may use cookies or similar technologies to:",
                fontSize = 15.sp,
                lineHeight = 22.sp,
                color = bodyTextColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            PolicyBulletItem(text = "Improve user experience", textColor = bodyTextColor)
            PolicyBulletItem(text = "Analyze usage patterns", textColor = bodyTextColor)

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = dividerColor, thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(20.dp))

            // 2. How We Use Your Information
            PolicySectionHeader(title = "2. How We Use Your Information")
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "We use your data to:",
                fontSize = 15.sp,
                lineHeight = 22.sp,
                color = bodyTextColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            PolicyBulletItem(text = "Provide and improve app functionality", textColor = bodyTextColor)
            PolicyBulletItem(text = "Save and sync your financial records", textColor = bodyTextColor)
            PolicyBulletItem(text = "Analyze usage to enhance features", textColor = bodyTextColor)
            PolicyBulletItem(text = "Provide customer support", textColor = bodyTextColor)
            PolicyBulletItem(text = "Ensure app security", textColor = bodyTextColor)

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = dividerColor, thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(20.dp))

            // 3. Data Storage & Security
            PolicySectionHeader(title = "3. Data Storage & Security")
            Spacer(modifier = Modifier.height(8.dp))
            PolicyBulletItem(text = "Your data is stored securely using industry-standard practices", textColor = bodyTextColor)
            PolicyBulletItem(text = "We take reasonable steps to protect your data from unauthorized access", textColor = bodyTextColor)
            PolicyBulletItem(text = "However, no method of transmission over the internet is 100% secure", textColor = bodyTextColor)

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = dividerColor, thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(20.dp))

            // 4. Data Sharing
            PolicySectionHeader(title = "4. Data Sharing")
            Spacer(modifier = Modifier.height(10.dp))

            // Highlight Badge: Zero Selling of Data
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isDark) Color(0xFF132A1C) else Color(0xFFECFDF5),
                border = BorderStroke(1.dp, if (isDark) Color(0xFF166534) else Color(0xFFA7F3D0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_shield),
                        contentDescription = "Security",
                        tint = if (isDark) Color(0xFF4ADE80) else Color(0xFF059669),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "We do not sell your personal or financial data.",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDark) Color(0xFF86EFAC) else Color(0xFF065F46)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "We may share data only in the following cases:",
                fontSize = 15.sp,
                lineHeight = 22.sp,
                color = bodyTextColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            PolicyBulletItem(text = "With service providers (e.g., hosting, analytics tools)", textColor = bodyTextColor)
            PolicyBulletItem(text = "To comply with legal obligations", textColor = bodyTextColor)
            PolicyBulletItem(text = "To protect our rights and prevent fraud", textColor = bodyTextColor)

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = dividerColor, thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(20.dp))

            // 5. Third-Party Services
            PolicySectionHeader(title = "5. Third-Party Services")
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Paisa Track may use third-party services such as:",
                fontSize = 15.sp,
                lineHeight = 22.sp,
                color = bodyTextColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            PolicyBulletItem(text = "Analytics tools (e.g., Firebase, Google Analytics)", textColor = bodyTextColor)
            PolicyBulletItem(text = "Advertising networks (if ads are shown)", textColor = bodyTextColor)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "These third parties may collect data according to their own privacy policies.",
                fontSize = 14.sp,
                lineHeight = 22.sp,
                color = subTextColor
            )

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = dividerColor, thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(20.dp))

            // 6. Data Retention
            PolicySectionHeader(title = "6. Data Retention")
            Spacer(modifier = Modifier.height(8.dp))
            PolicyBulletItem(text = "We retain your data as long as you use the app", textColor = bodyTextColor)
            PolicyBulletItem(text = "You can request deletion of your data at any time", textColor = bodyTextColor)

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = dividerColor, thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(20.dp))

            // 7. Your Rights
            PolicySectionHeader(title = "7. Your Rights")
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "You have the right to:",
                fontSize = 15.sp,
                lineHeight = 22.sp,
                color = bodyTextColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            PolicyBulletItem(text = "Access your data", textColor = bodyTextColor)
            PolicyBulletItem(text = "Update or correct your data", textColor = bodyTextColor)
            PolicyBulletItem(text = "Request deletion of your data", textColor = bodyTextColor)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "To exercise these rights, contact us at the email below.",
                fontSize = 14.5.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = dividerColor, thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(20.dp))

            // 8. Children’s Privacy
            PolicySectionHeader(title = "8. Children’s Privacy")
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Paisa Track is not intended for users under the age of 13.",
                fontSize = 15.sp,
                lineHeight = 24.sp,
                color = bodyTextColor
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "We do not knowingly collect data from children.",
                fontSize = 15.sp,
                lineHeight = 24.sp,
                color = bodyTextColor
            )

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = dividerColor, thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(20.dp))

            // 9. Changes to This Privacy Policy
            PolicySectionHeader(title = "9. Changes to This Privacy Policy")
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "We may update this Privacy Policy from time to time.",
                fontSize = 15.sp,
                lineHeight = 24.sp,
                color = bodyTextColor
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Changes will be posted within the app or website.",
                fontSize = 15.sp,
                lineHeight = 24.sp,
                color = bodyTextColor
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Contact / Privacy Inquiries Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else Color(0xFFF9FAFB),
                border = BorderStroke(1.dp, dividerColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Privacy Inquiries & Data Rights",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "To access, update, or request deletion of your information, reach out to us:",
                        fontSize = 12.5.sp,
                        lineHeight = 18.sp,
                        color = subTextColor
                    )

                    Spacer(modifier = Modifier.height(14.dp))

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
                                            putExtra(Intent.EXTRA_SUBJECT, "Paisa Track Privacy Request")
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
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = SUPPORT_EMAIL,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Copy email button
                        IconButton(
                            onClick = {
                                presets.plunk()
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Paisa Track Support Email", SUPPORT_EMAIL)
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
            HorizontalDivider(color = dividerColor, thickness = 0.8.dp)

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
private fun PolicySectionHeader(
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
private fun PolicySubheader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        fontSize = 15.5.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = modifier
    )
}

@Composable
private fun PolicyBulletItem(
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
