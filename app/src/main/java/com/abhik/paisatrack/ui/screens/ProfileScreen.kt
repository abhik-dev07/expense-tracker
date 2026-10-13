package com.abhik.paisatrack.ui.screens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.zIndex
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.alpha
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.SettingsSystemDaydream
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Share
import com.abhik.paisatrack.R
import com.abhik.paisatrack.data.AuthManager
import com.abhik.paisatrack.ui.theme.ThemeManager
import com.abhik.paisatrack.ui.theme.ThemeMode
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.UpdateAvailability
import com.swmansion.pulsar.Pulsar
import com.abhik.paisatrack.ui.utils.findActivity
import com.abhik.paisatrack.ui.utils.getSafePresets
import kotlinx.coroutines.launch

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
                                androidx.compose.ui.graphics.Color.Transparent
                            )
                        )
                    } else {
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                androidx.compose.ui.graphics.Color.Transparent,
                                androidx.compose.ui.graphics.Color.Transparent
                            )
                        )
                    }
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular back button
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (isDark) MaterialTheme.colorScheme.surfaceVariant else androidx.compose.ui.graphics.Color.White)
                        .border(
                            BorderStroke(1.dp, if (isDark) androidx.compose.ui.graphics.Color.White.copy(alpha = 0.15f) else androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.1f)),
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
                    .padding(horizontal = 24.dp, vertical = 12.dp)
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
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .clickable {
                            presets.plunk()
                            try {
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    data = Uri.parse("market://details?id=${context.packageName}")
                                    setPackage("com.android.vending")
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    data = Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}")
                                }
                                context.startActivity(intent)
                            }
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) MaterialTheme.colorScheme.surfaceVariant else Color.White
                    ),
                    border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.05f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_date), 
                            contentDescription = "Update",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "App Update Available",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.weight(1f)
                        )
                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "v$availableVersionCode",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Go",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // APPEARANCE
            var appearanceExpanded by rememberSaveable { mutableStateOf(false) }
            val currentThemeMode by ThemeManager.themeMode.collectAsState()
            
            val getThemeIcon: (ThemeMode) -> Int = { mode ->
                when (mode) {
                    ThemeMode.Light -> R.drawable.ic_sun_custom
                    ThemeMode.Dark -> R.drawable.ic_moon_custom
                    ThemeMode.System -> R.drawable.ic_system_theme_custom
                }
            }
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
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
                                        painter = painterResource(id = getThemeIcon(currentThemeMode)),
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
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // INFORMATION ROWS
            InfoRow(
                title = "Share the app",
                icon = { Icon(painterResource(id = R.drawable.ic_share_custom), contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp)) },
                onClick = {
                    presets.plunk()
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, "Download Paisa Track app at https://play.google.com/store/apps/details?id=${context.packageName}")
                    }
                    context.startActivity(Intent.createChooser(shareIntent, null))
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
            
            val versionName = try {
                context.packageManager.getPackageInfo(context.packageName, 0).versionName
            } catch (e: Exception) {
                "1.0"
            }
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
        Dialog(
            onDismissRequest = { 
                showLogoutConfirmDialog = false
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
                        painter = painterResource(id = R.drawable.ic_signout),
                        contentDescription = "Logout",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(48.dp)
                    )

                    Text(
                        text = "Log out?",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Text(
                        text = "Are you sure you want to log out of your account?",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
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
                            },
                            modifier = Modifier.weight(1f).height(52.dp),
                            shape = CircleShape
                        ) {
                            Text("Cancel", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Button(
                            onClick = {
                                presets.ping()
                                showLogoutConfirmDialog = false
                                onLogout()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            ),
                            modifier = Modifier.weight(1f).height(52.dp),
                            shape = CircleShape
                        ) {
                            Text("Log out", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionCard(
    modifier: Modifier = Modifier,
    title: String,
    icon: @Composable () -> Unit,
    isDark: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(88.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) MaterialTheme.colorScheme.surfaceVariant else Color.White
        ),
        border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.05f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            icon()
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ThemeOptionRow(
    title: String,
    icon: @Composable () -> Unit,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp)
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon()
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f)
        )
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(14.dp)
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .border(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), CircleShape)
            )
        }
    }
}

@Composable
private fun InfoRow(
    title: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    val isDark = com.abhik.paisatrack.ui.theme.isAppInDarkTheme()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    if (isDark) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f)
        )
    }
}
