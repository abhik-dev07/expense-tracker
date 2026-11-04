package com.abhik.paisatrack.screens.termsconditions

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
 * High-craft Terms & Conditions screen styled in exact consistency with PrivacyPolicyScreen:
 * - Circular back button header with centered screen title
 * - Centered "Last Updated" pill badge
 * - Rich, readable typography and comfortable paragraph spacing
 * - Styled highlight cards for critical statements (Advisory disclaimer, Privacy commitment, Governing law)
 * - Interactive contact badge with email support
 */
@Composable
fun TermsConditionsScreen(
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
                    text = "Terms & Conditions",
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

            // Welcome Introduction Paragraph
            Text(
                text = "Welcome to Paisa Track. These Terms & Conditions govern your use of our mobile application and services. By using Paisa Track, you agree to these terms.",
                fontSize = 15.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Normal,
                color = bodyTextColor
            )

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = dividerColor, thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(20.dp))

            // 1. Acceptance of Terms
            TermsSectionHeader(title = "1. Acceptance of Terms")
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "By accessing or using Paisa Track, you agree to be bound by these Terms & Conditions. If you do not agree, please do not use the app.",
                fontSize = 15.sp,
                lineHeight = 24.sp,
                color = bodyTextColor
            )

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = dividerColor, thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(20.dp))

            // 2. Description of Service
            TermsSectionHeader(title = "2. Description of Service")
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Paisa Track is an expense tracking application that allows users to:",
                fontSize = 15.sp,
                lineHeight = 22.sp,
                color = bodyTextColor
            )
            Spacer(modifier = Modifier.height(6.dp))
            TermsBulletItem(text = "Record daily income and expenses", textColor = bodyTextColor)
            TermsBulletItem(text = "Monitor financial habits", textColor = bodyTextColor)
            TermsBulletItem(text = "Generate reports and insights", textColor = bodyTextColor)

            Spacer(modifier = Modifier.height(12.dp))

            // Highlight Callout Note for No Financial Advice
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
                        painter = painterResource(id = R.drawable.ic_notice),
                        contentDescription = "Notice",
                        tint = if (isDark) Color(0xFFFDE68A) else Color(0xFF92400E),
                        modifier = Modifier
                            .padding(end = 10.dp)
                            .size(18.dp)
                    )
                    Text(
                        text = "We do not provide financial, investment, or tax advice.",
                        fontSize = 13.5.sp,
                        lineHeight = 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isDark) Color(0xFFFDE68A) else Color(0xFF92400E)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = dividerColor, thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(20.dp))

            // 3. User Responsibilities
            TermsSectionHeader(title = "3. User Responsibilities")
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "By using Paisa Track, you agree:",
                fontSize = 15.sp,
                lineHeight = 22.sp,
                color = bodyTextColor
            )
            Spacer(modifier = Modifier.height(6.dp))
            TermsBulletItem(text = "To provide accurate and complete information", textColor = bodyTextColor)
            TermsBulletItem(text = "Not to use the app for illegal or fraudulent activities", textColor = bodyTextColor)
            TermsBulletItem(text = "Not to misuse, hack, or disrupt the app", textColor = bodyTextColor)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "You are solely responsible for the data you enter.",
                fontSize = 14.5.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = dividerColor, thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(20.dp))

            // 4. Data & Privacy
            TermsSectionHeader(title = "4. Data & Privacy")
            Spacer(modifier = Modifier.height(8.dp))
            TermsBulletItem(
                text = "We may collect and store user data such as expenses, usage data, and device information",
                textColor = bodyTextColor
            )
            TermsBulletItem(
                text = "Your data is handled according to our Privacy Policy",
                textColor = bodyTextColor
            )

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
                        text = "We do not sell your personal financial data to third parties.",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDark) Color(0xFF86EFAC) else Color(0xFF065F46)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = dividerColor, thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(20.dp))

            // 5. Account Security
            TermsSectionHeader(title = "5. Account Security")
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "If your app includes login/signup:",
                fontSize = 15.sp,
                lineHeight = 22.sp,
                color = bodyTextColor
            )
            Spacer(modifier = Modifier.height(6.dp))
            TermsBulletItem(
                text = "You are responsible for maintaining the confidentiality of your account",
                textColor = bodyTextColor
            )
            TermsBulletItem(
                text = "Notify us immediately of unauthorized access",
                textColor = bodyTextColor
            )
            TermsBulletItem(
                text = "We are not liable for losses due to account misuse",
                textColor = bodyTextColor
            )

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = dividerColor, thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(20.dp))

            // 6. Limitation of Liability
            TermsSectionHeader(title = "6. Limitation of Liability")
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Paisa Track is provided “as is”:",
                fontSize = 15.sp,
                lineHeight = 22.sp,
                color = bodyTextColor
            )
            Spacer(modifier = Modifier.height(6.dp))
            TermsBulletItem(
                text = "We do not guarantee accuracy of financial calculations",
                textColor = bodyTextColor
            )
            TermsBulletItem(
                text = "We are not responsible for financial losses or decisions made using the app",
                textColor = bodyTextColor
            )
            TermsBulletItem(
                text = "Use the app at your own risk",
                textColor = bodyTextColor
            )

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = dividerColor, thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(20.dp))

            // 7. Intellectual Property
            TermsSectionHeader(title = "7. Intellectual Property")
            Spacer(modifier = Modifier.height(8.dp))
            TermsBulletItem(
                text = "All content, branding, logo, and design of Paisa Track are owned by us",
                textColor = bodyTextColor
            )
            TermsBulletItem(
                text = "You may not copy, distribute, or reproduce any part without permission",
                textColor = bodyTextColor
            )

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = dividerColor, thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(20.dp))

            // 8. Third-Party Services
            TermsSectionHeader(title = "8. Third-Party Services")
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Our app may include links or integrations with third-party services:",
                fontSize = 15.sp,
                lineHeight = 22.sp,
                color = bodyTextColor
            )
            Spacer(modifier = Modifier.height(6.dp))
            TermsBulletItem(
                text = "We are not responsible for their content or policies",
                textColor = bodyTextColor
            )
            TermsBulletItem(
                text = "Use them at your own discretion",
                textColor = bodyTextColor
            )

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = dividerColor, thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(20.dp))

            // 9. Termination
            TermsSectionHeader(title = "9. Termination")
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "We reserve the right to:",
                fontSize = 15.sp,
                lineHeight = 22.sp,
                color = bodyTextColor
            )
            Spacer(modifier = Modifier.height(6.dp))
            TermsBulletItem(
                text = "Suspend or terminate access if terms are violated",
                textColor = bodyTextColor
            )
            TermsBulletItem(
                text = "Modify or discontinue the app at any time",
                textColor = bodyTextColor
            )

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = dividerColor, thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(20.dp))

            // 10. Updates to Terms
            TermsSectionHeader(title = "10. Updates to Terms")
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "We may update these Terms & Conditions:",
                fontSize = 15.sp,
                lineHeight = 22.sp,
                color = bodyTextColor
            )
            Spacer(modifier = Modifier.height(6.dp))
            TermsBulletItem(
                text = "Changes will be posted within the app",
                textColor = bodyTextColor
            )
            TermsBulletItem(
                text = "Continued use means you accept the updated terms",
                textColor = bodyTextColor
            )

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = dividerColor, thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(20.dp))

            // 11. Governing Law
            TermsSectionHeader(title = "11. Governing Law")
            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else Color(0xFFF9FAFB),
                border = BorderStroke(1.dp, dividerColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id  = R.drawable.ic_court),
                        contentDescription = "Law",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "These Terms shall be governed by the laws of India.",
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Contact / Inquiries Card
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
                        text = "Questions Regarding Terms?",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "For inquiries regarding these Terms & Conditions, please contact us:",
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
                                            putExtra(Intent.EXTRA_SUBJECT, "Paisa Track Terms Inquiry")
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
private fun TermsSectionHeader(
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
private fun TermsBulletItem(
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
