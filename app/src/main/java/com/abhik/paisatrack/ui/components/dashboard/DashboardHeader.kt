package com.abhik.paisatrack.ui.components.dashboard

import androidx.compose.animation.*
import com.abhik.paisatrack.R
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.Icon
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Brush
import com.abhik.paisatrack.ui.theme.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import coil.compose.AsyncImage
import com.swmansion.pulsar.Pulsar
import com.abhik.paisatrack.ui.utils.findActivity
import com.abhik.paisatrack.ui.utils.getSafePresets
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ripple
import kotlinx.coroutines.delay
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.toShape
import androidx.compose.material3.MaterialShapes

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DashboardHeader(
    activeTab: String,
    userName: String,
    firstName: String,
    profilePicUrl: String?,
    onSettingsClick: () -> Unit,
    isSettingsOpen: Boolean = false,
    scrollOffset: Float = 0f,
    onSearchClick: () -> Unit = {},
    onFilterClick: () -> Unit = {},
    showFilters: Boolean = false,
    isAnyFilterActive: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val pulsar = remember(context) { Pulsar(context.findActivity() ?: context) }
    val presets = remember(pulsar) { pulsar.getSafePresets() }

    val targetRotation = if (isSettingsOpen) 180f else 0f
    val animatedRotation by animateFloatAsState(
        targetValue = targetRotation,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "SettingsRotationAnimation"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "ShapeRotation")
    val shapeRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RotationAngle"
    )

    val calendar = java.util.Calendar.getInstance()
    val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
    val greeting = when (hour) {
        in 5..11 -> "Good morning"
        12 -> "Good noon"
        in 13..16 -> "Good afternoon"
        in 17..20 -> "Good evening"
        else -> "Good night"
    }
    
    val fullGreeting = "$greeting $firstName"
    val searchKeywords = remember { listOf("records...", "groceries...", "rent...", "food...", "gas...", "Netflix...", "salary...") }
    var keywordIndex by remember { androidx.compose.runtime.mutableIntStateOf(0) }
    var displayedKeyword by remember { androidx.compose.runtime.mutableStateOf("") }
    
    LaunchedEffect(keywordIndex) {
        val target = searchKeywords[keywordIndex]
        for (i in 0..target.length) {
            displayedKeyword = target.substring(0, i)
            delay(70)
        }
        delay(2000)
        for (i in target.length downTo 0) {
            displayedKeyword = target.substring(0, i)
            delay(30)
        }
        delay(500)
        keywordIndex = (keywordIndex + 1) % searchKeywords.size
    }

    AnimatedContent(
        targetState = activeTab,
        transitionSpec = {
            val fromIndex = when (initialState) {
                "Transactions" -> 0
                "Insights" -> 1
                "Collections" -> 2
                else -> 0
            }
            val toIndex = when (targetState) {
                "Transactions" -> 0
                "Insights" -> 1
                "Collections" -> 2
                else -> 0
            }
            if (toIndex > fromIndex) {
                (slideInVertically(animationSpec = tween(320, easing = FastOutSlowInEasing), initialOffsetY = { -it }) + fadeIn(animationSpec = tween(320))) togetherWith
                    (slideOutVertically(animationSpec = tween(320, easing = FastOutSlowInEasing), targetOffsetY = { it }) + fadeOut(animationSpec = tween(320)))
            } else {
                (slideInVertically(animationSpec = tween(320, easing = FastOutSlowInEasing), initialOffsetY = { it }) + fadeIn(animationSpec = tween(320))) togetherWith
                    (slideOutVertically(animationSpec = tween(320, easing = FastOutSlowInEasing), targetOffsetY = { -it }) + fadeOut(animationSpec = tween(320)))
            }
        },
        label = "DashboardHeaderTransition"
    ) { currentTab ->
        val density = androidx.compose.ui.platform.LocalDensity.current
        val collapseRange = with(density) { 100.dp.toPx() } // Start collapsing at 100dp range
        val collapseProgress = (scrollOffset / collapseRange).coerceIn(0f, 1f)
        
        val isDark = com.abhik.paisatrack.ui.theme.isAppInDarkTheme()
        val pageBgColor = if (isDark) androidx.compose.ui.graphics.Color(0xFF202027) else androidx.compose.ui.graphics.Color(0xFFFFFFFF)
        
        val headerGradient = Brush.verticalGradient(
            colorStops = if (isDark) arrayOf(
                0.0f to androidx.compose.ui.graphics.lerp(HeaderGradient0Dark, pageBgColor, collapseProgress),
                0.18f to androidx.compose.ui.graphics.lerp(HeaderGradient18Dark, pageBgColor, collapseProgress),
                0.38f to androidx.compose.ui.graphics.lerp(HeaderGradient38Dark, pageBgColor, collapseProgress),
                0.58f to androidx.compose.ui.graphics.lerp(HeaderGradient58Dark, pageBgColor, collapseProgress),
                0.75f to androidx.compose.ui.graphics.lerp(HeaderGradient75Dark, pageBgColor, collapseProgress),
                0.90f to androidx.compose.ui.graphics.lerp(HeaderGradient90Dark, pageBgColor, collapseProgress),
                1.0f to androidx.compose.ui.graphics.lerp(HeaderGradient100Dark, pageBgColor, collapseProgress)
            ) else arrayOf(
                0.0f to androidx.compose.ui.graphics.lerp(HeaderGradient0Light, pageBgColor, collapseProgress),
                0.18f to androidx.compose.ui.graphics.lerp(HeaderGradient18Light, pageBgColor, collapseProgress),
                0.38f to androidx.compose.ui.graphics.lerp(HeaderGradient38Light, pageBgColor, collapseProgress),
                0.58f to androidx.compose.ui.graphics.lerp(HeaderGradient58Light, pageBgColor, collapseProgress),
                0.75f to androidx.compose.ui.graphics.lerp(HeaderGradient75Light, pageBgColor, collapseProgress),
                0.90f to androidx.compose.ui.graphics.lerp(HeaderGradient90Light, pageBgColor, collapseProgress),
                1.0f to androidx.compose.ui.graphics.lerp(HeaderGradient100Light, pageBgColor, collapseProgress)
            )
        )
        val headerTextPrimary = if (isDark) HeaderTextPrimaryDark else HeaderTextPrimaryLight
        val headerTextSecondary = if (isDark) HeaderTextSecondaryDark else HeaderTextSecondaryLight
        val searchSurfaceColor = if (isDark) HeaderSurfaceDark else MaterialTheme.colorScheme.surface
        
        val searchBg = if (isDark) SearchBackgroundDark else SearchBackgroundLight
        val searchBorder = if (isDark) SearchBorderDark else SearchBorderLight
        val searchPlaceholder = if (isDark) SearchPlaceholderDark else SearchPlaceholderLight
        val searchDivider = if (isDark) SearchDividerDark else SearchDividerLight
        val searchIconTint = if (isDark) SearchIconDark else SearchIconLight
        
        val profileSurface = if (isDark) ProfileSurfaceDark else ProfileSurfaceLight
        val profileBorder = if (isDark) ProfileBorderDark else ProfileBorderLight
        
        Column(
            modifier = modifier
                .fillMaxWidth()
                .background(headerGradient)
                .statusBarsPadding()
                .padding(bottom = 12.dp)
        ) {
            if (currentTab == "Transactions") {
                // Top Row: Greeting & Name (Left) + Logo (Right)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 8.dp)
                        .height(50.dp * (1f - collapseProgress))
                        .alpha(1f - collapseProgress)
                        .graphicsLayer {
                            translationY = -20f * collapseProgress
                        },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Greeting and First Name
                    Column {
                        Text(
                            text = "$greeting,",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = headerTextSecondary
                        )
                        Text(
                            text = firstName,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = headerTextPrimary
                        )
                    }
                    
                    // Right: Logo (acts as Settings button now)
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .scale(1f - (0.15f * collapseProgress)) // Scale when collapsed
                            .clip(MaterialShapes.Clover8Leaf.toShape())
                            .clickable {
                                onSettingsClick()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (!profilePicUrl.isNullOrEmpty()) {
                            AsyncImage(
                                model = profilePicUrl,
                                contentDescription = "Profile Picture",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                text = userName.take(1).uppercase(),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) HeaderTextPrimaryDark else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(14.dp * (1f - collapseProgress)))
                
                val searchInteractionSource = remember { MutableInteractionSource() }
                val isSearchPressed by searchInteractionSource.collectIsPressedAsState()
                val searchPressAlpha by animateFloatAsState(
                    targetValue = if (isSearchPressed) 0.72f else 1f,
                    animationSpec = tween(durationMillis = 120),
                    label = "SearchPressAlpha"
                )
                val searchPillLeftShape = RoundedCornerShape(
                    topStart = 26.dp,
                    bottomStart = 26.dp,
                    topEnd = 0.dp,
                    bottomEnd = 0.dp
                )

                // Bottom Row: Search Bar
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(52.dp),
                    shape = RoundedCornerShape(26.dp), // pill shaped
                    color = searchBg,
                    border = BorderStroke(1.dp, searchBorder)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Clickable Search Area (before the filter divider) with pill shape and press touch opacity
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(searchPillLeftShape)
                                .clickable(
                                    interactionSource = searchInteractionSource,
                                    indication = ripple()
                                ) {
                                    presets.plunk()
                                    onSearchClick()
                                }
                                .alpha(searchPressAlpha)
                                .padding(start = 16.dp, end = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_search),
                                contentDescription = "Search",
                                tint = searchIconTint,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Search \"$displayedKeyword\"",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = searchPlaceholder,
                                maxLines = 1,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        
                        // Vertical Divider
                        Box(
                            modifier = Modifier
                                .height(24.dp)
                                .width(1.dp)
                                .background(searchDivider)
                        )
                        
                        // Filter
                        val isFilterActive = isAnyFilterActive || showFilters
                        val filterTint = if (isFilterActive) {
                            if (isDark) Color(0xFFFFFFFF) else Color(0xFF1FB47B)
                        } else {
                            searchIconTint
                        }

                        Box(
                            modifier = Modifier
                                .padding(start = 12.dp, end = 16.dp)
                                .size(32.dp)
                                .clip(CircleShape)
                                .clickable { onFilterClick() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = if (isFilterActive) R.drawable.ic_filter_active else R.drawable.ic_filter),
                                contentDescription = "Filter",
                                tint = filterTint,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            } else {
                // Headers for secondary screens
                val headerText = when (currentTab) {
                    "Collections" -> "Collections & Vaults"
                    "Insights" -> "Insights & Analytics"
                    else -> ""
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = headerText,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = headerTextPrimary
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(if (isDark) Color.Black.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.8f))
                                .border(
                                    width = 1.dp,
                                    color = if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.1f),
                                    shape = CircleShape
                                )
                                .clickable {
                                    presets.plunk()
                                    onSearchClick()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_search),
                                contentDescription = "Search",
                                tint = if (isDark) Color.White else Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Profile section
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(MaterialShapes.Clover8Leaf.toShape())
                                .clickable {
                                    onSettingsClick()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (!profilePicUrl.isNullOrEmpty()) {
                                AsyncImage(
                                    model = profilePicUrl,
                                    contentDescription = "Profile Picture",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text(
                                    text = userName.take(1).uppercase(),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) HeaderTextPrimaryDark else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


