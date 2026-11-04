package com.abhik.paisatrack.screens.licenses

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

private data class OpenSourceLibrary(
    val name: String,
    val author: String,
    val license: String,
    val description: String
)

private val OPEN_SOURCE_LIBRARIES = listOf(
    OpenSourceLibrary(
        name = "Jetpack Compose",
        author = "Google LLC",
        license = "Apache License 2.0",
        description = "Modern toolkit for building native Android UI."
    ),
    OpenSourceLibrary(
        name = "Kotlin & Coroutines",
        author = "JetBrains s.r.o.",
        license = "Apache License 2.0",
        description = "Language runtime and asynchronous concurrency framework."
    ),
    OpenSourceLibrary(
        name = "AndroidX Room",
        author = "Google LLC",
        license = "Apache License 2.0",
        description = "Robust SQLite object mapping and persistence layer."
    ),
    OpenSourceLibrary(
        name = "Retrofit & OkHttp",
        author = "Square, Inc.",
        license = "Apache License 2.0",
        description = "Type-safe HTTP client for Android and Java."
    ),
    OpenSourceLibrary(
        name = "Coil",
        author = "Coil Contributors",
        license = "Apache License 2.0",
        description = "Fast, lightweight image loading library for Android Compose."
    ),
    OpenSourceLibrary(
        name = "Lottie Compose",
        author = "Airbnb, Inc.",
        license = "Apache License 2.0",
        description = "Native rendering of Adobe After Effects animations."
    ),
    OpenSourceLibrary(
        name = "Pulsar Haptics",
        author = "Software Mansion",
        license = "MIT License",
        description = "High-precision haptic feedback library for modern Android."
    ),
    OpenSourceLibrary(
        name = "Firebase Android SDK",
        author = "Google LLC",
        license = "Apache License 2.0",
        description = "Analytics, Cloud Messaging, and backend services."
    ),
    OpenSourceLibrary(
        name = "KMPAuth",
        author = "Mirzemehdi Karimov",
        license = "Apache License 2.0",
        description = "Google authentication for Kotlin multiplatform & Android."
    )
)

/**
 * High-craft Licenses screen styled in exact consistency with PrivacyPolicyScreen:
 * - Circular back button header with centered screen title
 * - Centered pill badge
 * - Rich, readable typography and comfortable card layouts
 * - List of open source software and libraries with license tags
 * - Interactive disclaimer and email contact support
 */
@Composable
fun LicensesScreen(
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
                    text = "Licenses",
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

            // Welcome Introduction Paragraph
            Text(
                text = "Paisa Track is built with the help of these incredible open-source projects. We are grateful to the open-source community for making this application possible.",
                fontSize = 15.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Normal,
                color = bodyTextColor
            )

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = dividerColor, thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(20.dp))

            // Section Header: Open Source Libraries
            Text(
                text = "Open Source Libraries",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(14.dp))

            // List of Libraries
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OPEN_SOURCE_LIBRARIES.forEach { lib ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f) else Color(0xFFF9FAFB),
                        border = BorderStroke(1.dp, dividerColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = lib.name,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Box(
                                    modifier = Modifier
                                        .background(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                            RoundedCornerShape(6.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                    Text(
                                        text = lib.license,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "By ${lib.author}",
                                fontSize = 12.sp,
                                color = subTextColor
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = lib.description,
                                fontSize = 12.5.sp,
                                lineHeight = 17.sp,
                                color = bodyTextColor
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = dividerColor, thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(28.dp))


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
                        text = "Questions Regarding Licenses?",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "For inquiries regarding third-party software and licenses, reach out to us:",
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
                                            putExtra(Intent.EXTRA_SUBJECT, "Paisa Track Licenses Inquiry")
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
