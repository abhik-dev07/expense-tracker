package com.abhik.paisatrack.screens.disclaimer

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
 * High-craft Disclaimer screen styled in exact consistency with PrivacyPolicyScreen:
 * - Circular back button header with centered screen title
 * - Centered "Last Updated" pill badge
 * - Rich, readable typography and comfortable paragraph spacing
 * - Styled highlight cards for critical statements (No financial advice, Use at own risk)
 * - Interactive contact badge with email support
 */
@Composable
fun DisclaimerScreen(
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
                    text = "Disclaimer",
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

            // Welcome / Overview Paragraph
            Text(
                text = "The information provided by Paisa Track (“we”, “our”, or “us”) on our mobile application is for general informational and personal financial tracking purposes only.",
                fontSize = 15.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Normal,
                color = bodyTextColor
            )

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = dividerColor, thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(20.dp))

            // 1. No Financial Advice
            DisclaimerSectionHeader(title = "1. No Financial Advice")
            Spacer(modifier = Modifier.height(10.dp))

            // Highlight Callout Card
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
                        text = "Paisa Track is not a financial advisor.",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color(0xFFFDE68A) else Color(0xFF92400E)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            DisclaimerBulletItem(
                text = "We do not provide financial, investment, tax, or legal advice",
                textColor = bodyTextColor
            )
            DisclaimerBulletItem(
                text = "All data and insights shown in the app are for informational purposes only",
                textColor = bodyTextColor
            )
            DisclaimerBulletItem(
                text = "You should consult a qualified professional before making financial decisions",
                textColor = bodyTextColor
            )

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = dividerColor, thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(20.dp))

            // 2. Accuracy of Information
            DisclaimerSectionHeader(title = "2. Accuracy of Information")
            Spacer(modifier = Modifier.height(8.dp))
            DisclaimerBulletItem(
                text = "We strive to provide accurate calculations and reports",
                textColor = bodyTextColor
            )
            DisclaimerBulletItem(
                text = "However, we do not guarantee the completeness, reliability, or accuracy of any data",
                textColor = bodyTextColor
            )
            DisclaimerBulletItem(
                text = "Errors may occur due to incorrect user input or technical issues",
                textColor = bodyTextColor
            )

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = dividerColor, thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(20.dp))

            // 3. User Responsibility
            DisclaimerSectionHeader(title = "3. User Responsibility")
            Spacer(modifier = Modifier.height(8.dp))
            DisclaimerBulletItem(
                text = "You are solely responsible for the financial data you enter",
                textColor = bodyTextColor
            )
            DisclaimerBulletItem(
                text = "Any decisions you make based on app data are at your own risk",
                textColor = bodyTextColor
            )
            DisclaimerBulletItem(
                text = "We are not liable for any financial loss or damages",
                textColor = bodyTextColor
            )

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = dividerColor, thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(20.dp))

            // 4. No Guarantees
            DisclaimerSectionHeader(title = "4. No Guarantees")
            Spacer(modifier = Modifier.height(8.dp))
            DisclaimerBulletItem(
                text = "Paisa Track does not guarantee savings, financial improvement, or outcomes",
                textColor = bodyTextColor
            )
            DisclaimerBulletItem(
                text = "The app is only a tool to help track expenses",
                textColor = bodyTextColor
            )

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = dividerColor, thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(20.dp))

            // 5. Third-Party Services
            DisclaimerSectionHeader(title = "5. Third-Party Services")
            Spacer(modifier = Modifier.height(8.dp))
            DisclaimerBulletItem(
                text = "Our app may include links or integrations with third-party services",
                textColor = bodyTextColor
            )
            DisclaimerBulletItem(
                text = "We do not control or take responsibility for their content, policies, or practices",
                textColor = bodyTextColor
            )

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = dividerColor, thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(20.dp))

            // 6. App Availability
            DisclaimerSectionHeader(title = "6. App Availability")
            Spacer(modifier = Modifier.height(8.dp))
            DisclaimerBulletItem(
                text = "We do not guarantee uninterrupted access to the app",
                textColor = bodyTextColor
            )
            DisclaimerBulletItem(
                text = "The app may be updated, modified, or discontinued at any time without notice",
                textColor = bodyTextColor
            )

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = dividerColor, thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(20.dp))

            // 7. Limitation of Liability
            DisclaimerSectionHeader(title = "7. Limitation of Liability")
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "To the fullest extent permitted by law:",
                fontSize = 15.sp,
                lineHeight = 22.sp,
                color = bodyTextColor
            )
            Spacer(modifier = Modifier.height(6.dp))
            DisclaimerBulletItem(
                text = "Paisa Track shall not be held liable for any direct, indirect, or incidental damages",
                textColor = bodyTextColor
            )
            DisclaimerBulletItem(
                text = "This includes loss of data, financial loss, or business interruption",
                textColor = bodyTextColor
            )

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = dividerColor, thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(20.dp))

            // 8. Use at Your Own Risk
            DisclaimerSectionHeader(title = "8. Use at Your Own Risk")
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Your use of Paisa Track is entirely at your own risk.",
                fontSize = 15.sp,
                lineHeight = 24.sp,
                color = bodyTextColor
            )

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
                        painter = painterResource(id = R.drawable.ic_notice),
                        contentDescription = "Notice",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "The app is provided on an “as-is” and “as-available” basis.",
                        fontSize = 14.sp,
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
                        text = "Questions Regarding Disclaimer?",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "For inquiries regarding this disclaimer, reach out to us:",
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
                                            putExtra(Intent.EXTRA_SUBJECT, "Paisa Track Disclaimer Inquiry")
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
private fun DisclaimerSectionHeader(
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
private fun DisclaimerBulletItem(
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
