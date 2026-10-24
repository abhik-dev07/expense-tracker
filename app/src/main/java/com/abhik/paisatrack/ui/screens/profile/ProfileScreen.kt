package com.abhik.paisatrack.ui.screens.profile

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.abhik.paisatrack.R
import com.abhik.paisatrack.data.AuthManager
import com.abhik.paisatrack.ui.screens.profile.components.AppUpdateCard
import com.abhik.paisatrack.ui.screens.profile.components.InfoRow
import com.abhik.paisatrack.ui.screens.profile.components.LogoutConfirmDialog
import com.abhik.paisatrack.ui.screens.profile.components.ThemeOptionRow
import com.abhik.paisatrack.ui.screens.profile.utils.ProfileUtils
import com.abhik.paisatrack.ui.theme.ThemeManager
import com.abhik.paisatrack.ui.theme.ThemeMode
import com.abhik.paisatrack.ui.utils.findActivity
import com.abhik.paisatrack.ui.utils.getSafePresets
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.UpdateAvailability
import com.swmansion.pulsar.Pulsar

@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val pulsar = remember(context) { Pulsar(context.findActivity() ?: context) }
    val presets = remember(pulsar) { pulsar.getSafePresets() }
    val scrollState = rememberScrollState()

    val userName = remember { AuthManager.getUserName(context) }
    val userEmail = remember { AuthManager.getUserEmail(context) }

    val isDark = com.abhik.paisatrack.ui.theme.isAppInDarkTheme()

    var showLogoutConfirmDialog by remember { mutableStateOf(false) }

    // App Update
    var updateAvailable by remember { mutableStateOf(false) }
    var availableVersionCode by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val appUpdateManager = AppUpdateManagerFactory.create(context)
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                updateAvailable = true
                availableVersionCode = appUpdateInfo.availableVersionCode().toString()
            }
        }.addOnFailureListener {
            // Ignore
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        val isHeaderScrolled by remember { derivedStateOf { scrollState.value > 10 } }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .zIndex(1f)
                .background(
                    if (isHeaderScrolled) {
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.background,
                                MaterialTheme.colorScheme.background.copy(alpha = 0.9f),
                                MaterialTheme.colorScheme.background.copy(alpha = 0.5f),
                                Color.Transparent
                            )
                        )
                    } else {
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Transparent
                            )
                        )
                    }
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular back button
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (isDark) MaterialTheme.colorScheme.surfaceVariant else Color.White)
                        .border(
                            BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.1f)),
                            CircleShape
                        )
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_chevron_back),
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Text(
                    text = "Profile",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 44.dp), // Balance the back button
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 32.dp)
        ) {
            Spacer(modifier = Modifier.statusBarsPadding().height(84.dp))

            // USER INFORMATION
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = userName,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_mail_custom),
                        contentDescription = "Email",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = userEmail,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // APP UPDATE
            if (updateAvailable) {
                AppUpdateCard(
                    availableVersionCode = availableVersionCode,
                    isDark = isDark,
                    onClick = {
                        presets.plunk()
                        ProfileUtils.openPlayStore(context)
                    },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // APPEARANCE
            var appearanceExpanded by rememberSaveable { mutableStateOf(false) }
            val currentThemeMode by ThemeManager.themeMode.collectAsState()

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) MaterialTheme.colorScheme.surfaceVariant else Color.White
                ),
                border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.05f))
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                presets.plunk()
                                appearanceExpanded = !appearanceExpanded
                            }
                            .padding(horizontal = 16.dp)
                            .height(56.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AnimatedVisibility(
                            visible = !appearanceExpanded,
                            enter = expandHorizontally(animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)),
                            exit = shrinkHorizontally(animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(
                                            if (isDark) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(id = ProfileUtils.getThemeIcon(currentThemeMode)),
                                        contentDescription = "Appearance",
                                        tint = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                            }
                        }
                        Text(
                            text = "Appearance",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.weight(1f)
                        )

                        if (!appearanceExpanded) {
                            AnimatedVisibility(
                                visible = !appearanceExpanded,
                                enter = fadeIn(animationSpec = tween(300)),
                                exit = fadeOut(animationSpec = tween(300))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (currentThemeMode == ThemeMode.System) "Automatic" else currentThemeMode.name,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        val rotation by animateFloatAsState(
                            targetValue = if (appearanceExpanded) 180f else 0f,
                            animationSpec = tween(300),
                            label = "chevron_rotation"
                        )

                        Icon(
                            painter = painterResource(id = R.drawable.ic_chevron_down_custom),
                            contentDescription = "Toggle",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp).rotate(rotation)
                        )
                    }

                    AnimatedVisibility(
                        visible = appearanceExpanded,
                        enter = expandVertically(animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)),
                        exit = shrinkVertically(animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
                    ) {
                        Column {
                            HorizontalDivider(color = if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f))

                            ThemeOptionRow(
                                title = "Light theme",
                                icon = { Icon(painterResource(id = R.drawable.ic_sun_custom), contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp)) },
                                isSelected = currentThemeMode == ThemeMode.Light,
                                onClick = { ThemeManager.requestTheme(context, ThemeMode.Light) }
                            )
                            HorizontalDivider(color = if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f))
                            ThemeOptionRow(
                                title = "Dark theme",
                                icon = { Icon(painterResource(id = R.drawable.ic_moon_custom), contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp)) },
                                isSelected = currentThemeMode == ThemeMode.Dark,
                                onClick = { ThemeManager.requestTheme(context, ThemeMode.Dark) }
                            )
                            HorizontalDivider(color = if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f))
                            ThemeOptionRow(
                                title = "System default",
                                icon = { Icon(painterResource(id = R.drawable.ic_system_theme_custom), contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp)) },
                                isSelected = currentThemeMode == ThemeMode.System,
                                onClick = { ThemeManager.requestTheme(context, ThemeMode.System) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "INFORMATION",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // INFORMATION ROWS
            InfoRow(
                title = "Share the app",
                icon = { Icon(painterResource(id = R.drawable.ic_share_custom), contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp)) },
                onClick = {
                    presets.plunk()
                    ProfileUtils.shareApp(context)
                }
            )

            InfoRow(
                title = "About us",
                icon = { Icon(painterResource(id = R.drawable.ic_info_custom), contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp)) },
                onClick = {
                    presets.plunk()
                    Toast.makeText(context, "About us coming soon", Toast.LENGTH_SHORT).show()
                }
            )

            InfoRow(
                title = "Account privacy",
                icon = { Icon(painterResource(id = R.drawable.ic_lock_custom), contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp)) },
                onClick = {
                    presets.plunk()
                    Toast.makeText(context, "Privacy settings coming soon", Toast.LENGTH_SHORT).show()
                }
            )

            InfoRow(
                title = "Log out",
                icon = { Icon(painterResource(id = R.drawable.ic_signout), contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp)) },
                onClick = {
                    presets.plunk()
                    showLogoutConfirmDialog = true
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            val versionName = ProfileUtils.getAppVersionName(context)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = context.getString(R.string.app_name),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                Text(
                    text = "v$versionName",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
    }

    if (showLogoutConfirmDialog) {
        LogoutConfirmDialog(
            onDismiss = { showLogoutConfirmDialog = false },
            onConfirm = {
                showLogoutConfirmDialog = false
                onLogout()
            }
        )
    }
}
