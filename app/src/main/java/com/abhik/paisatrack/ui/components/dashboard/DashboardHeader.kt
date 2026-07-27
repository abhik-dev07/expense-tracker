package com.abhik.paisatrack.ui.components.dashboard

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.swmansion.pulsar.Pulsar

@Composable
fun DashboardHeader(
    activeTab: String,
    userName: String,
    firstName: String,
    profilePicUrl: String?,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val pulsar = remember { Pulsar(context) }
    val presets = remember { com.abhik.paisatrack.ui.utils.SafePresets(pulsar.getPresets()) }

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
        label = "DashboardHeaderVerticalTransition"
    ) { currentTab ->
        if (currentTab == "Transactions") {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, top = 20.dp, end = 24.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // High-contrast profile picture from Google or letter badge
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
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
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Column {
                        Text(
                            text = "Hello,",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color(0xFF9CA3AF)
                        )
                        Text(
                            text = firstName,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                // Settings button
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable {
                            presets.plunk()
                            onSettingsClick()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        } else {
            // Header for secondary screens (Collections, Insights)
            val headerText = when (currentTab) {
                "Collections" -> "Collections & Vaults"
                "Insights" -> "Insights & Analytics"
                else -> ""
            }
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, top = 20.dp, end = 24.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = headerText,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}
