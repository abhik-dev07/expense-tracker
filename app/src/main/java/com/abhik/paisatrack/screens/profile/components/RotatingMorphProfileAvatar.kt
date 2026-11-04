package com.abhik.paisatrack.screens.profile.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.abhik.paisatrack.R

/**
 * Profile avatar with crisp circular framing and a hairline border.
 */
@Composable
fun RotatingMorphProfileAvatar(
    profilePicUrl: String?,
    isDark: Boolean,
    modifier: Modifier = Modifier,
    isActive: Boolean = true,
    avatarSize: Dp = 88.dp,
    iconSize: Dp = 46.dp
) {
    Box(
        modifier = modifier
            .size(avatarSize)
            .clip(CircleShape)
            .background(if (isDark) MaterialTheme.colorScheme.surfaceVariant else Color.White)
            .border(
                BorderStroke(
                    1.dp,
                    if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.10f)
                ),
                CircleShape
            ),
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
            Icon(
                painter = painterResource(id = R.drawable.ic_user),
                contentDescription = "Profile Picture",
                tint = if (isDark) Color.White else Color.Black,
                modifier = Modifier.size(iconSize)
            )
        }
    }
}
