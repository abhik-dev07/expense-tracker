package com.abhik.paisatrack.ui.screens.addtransaction.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhik.paisatrack.R

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SubmitActionButton(
    submitState: SubmitState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String = "Add",
    enabled: Boolean = true
) {
    val isSubmitting = submitState == SubmitState.Loading || submitState == SubmitState.Success

    val buttonBgColor by animateColorAsState(
        targetValue = when {
            isSubmitting -> MaterialTheme.colorScheme.primary
            enabled -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        },
        animationSpec = tween(350),
        label = "buttonBg"
    )
    val buttonContentColor by animateColorAsState(
        targetValue = when {
            isSubmitting -> MaterialTheme.colorScheme.onPrimary
            enabled -> MaterialTheme.colorScheme.onPrimary
            else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        },
        animationSpec = tween(350),
        label = "buttonContent"
    )
    val buttonBorderWidth by animateDpAsState(
        targetValue = if (isSubmitting || enabled) 0.dp else 1.dp,
        animationSpec = tween(350),
        label = "buttonBorder"
    )
    val buttonBorderColor by animateColorAsState(
        targetValue = if (isSubmitting || enabled) Color.Transparent else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
        animationSpec = tween(350),
        label = "buttonBorderColor"
    )

    BoxWithConstraints(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        val buttonWidth by animateDpAsState(
            targetValue = if (isSubmitting) 52.dp else maxWidth,
            animationSpec = spring(
                dampingRatio = 0.8f,
                stiffness = Spring.StiffnessMedium
            ),
            label = "buttonWidth"
        )

        Box(
            modifier = Modifier
                .width(buttonWidth)
                .height(52.dp)
                .clip(CircleShape)
                .border(buttonBorderWidth, buttonBorderColor, CircleShape)
                .background(buttonBgColor)
                .clickable(enabled = submitState == SubmitState.Idle && enabled) {
                    onClick()
                },
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = submitState,
                transitionSpec = {
                    (fadeIn(tween(300)) + scaleIn(tween(300), initialScale = 0.8f))
                        .togetherWith(fadeOut(tween(200)) + scaleOut(tween(200), targetScale = 0.6f))
                },
                label = "submitContent"
            ) { state ->
                when (state) {
                    SubmitState.Idle -> {
                        Text(
                            text = text,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = buttonContentColor
                        )
                    }
                    SubmitState.Loading -> {
                        val density = LocalDensity.current
                        val strokeWidthPx = with(density) { 2.5.dp.toPx() }
                        val amplitudePx = with(density) { 3.dp.toPx() }
                        CircularWavyProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            trackColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f),
                            stroke = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidthPx),
                            trackStroke = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidthPx),
                            amplitude = amplitudePx,
                            wavelength = 6.dp
                        )
                    }
                    SubmitState.Success -> {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_tick),
                            contentDescription = "Check",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }
        }
    }
}
