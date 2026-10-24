package com.abhik.paisatrack.screens.dashboard.components

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhik.paisatrack.R

/**
 * Animated back-to-top button displayed when user scrolls up on the Dashboard.
 */
@Composable
fun DashboardBackToTopButton(
    visible: Boolean,
    isDark: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(250)) + slideInVertically(
            initialOffsetY = { 40 },
            animationSpec = tween(250, easing = FastOutSlowInEasing)
        ),
        exit = fadeOut(tween(200)) + slideOutVertically(
            targetOffsetY = { 40 },
            animationSpec = tween(200, easing = FastOutSlowInEasing)
        ),
        modifier = modifier
    ) {
        FilledTonalButton(
            onClick = onClick,
            elevation = ButtonDefaults.filledTonalButtonElevation(defaultElevation = 6.dp),
            shape = CircleShape,
            modifier = Modifier.height(40.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_up_arrow),
                contentDescription = "Back to Top",
                modifier = Modifier.size(16.dp),
                tint = if (isDark) Color.White else Color.Black
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                "Back to Top",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color.White else Color.Black
            )
        }
    }
}
