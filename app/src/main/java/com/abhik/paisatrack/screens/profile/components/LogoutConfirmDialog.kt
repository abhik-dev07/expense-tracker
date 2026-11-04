package com.abhik.paisatrack.screens.profile.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.abhik.paisatrack.R
import com.abhik.paisatrack.utils.findActivity
import com.abhik.paisatrack.utils.getSafePresets
import com.swmansion.pulsar.Pulsar
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

private enum class LogoutSubmitState {
    Idle,
    Loading,
    Success
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LogoutConfirmDialog(
    onDismiss: () -> Unit,
    onConfirm: (onDone: () -> Unit) -> Unit,
    onCompleteLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val pulsar = remember(context) { Pulsar(context.findActivity() ?: context) }
    val presets = remember(pulsar) { pulsar.getSafePresets() }
    val scope = rememberCoroutineScope()
    var logoutSubmitState by remember { mutableStateOf(LogoutSubmitState.Idle) }
    val isSubmitting = logoutSubmitState == LogoutSubmitState.Loading || logoutSubmitState == LogoutSubmitState.Success

    // Continuous smooth rotation for the curvy wavy progress loader
    val infiniteTransition = rememberInfiniteTransition(label = "logoutRotation")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotationAngle"
    )

    Dialog(
        onDismissRequest = {
            if (!isSubmitting) {
                presets.boulder()
                onDismiss()
            }
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = modifier
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
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Are you sure you want to log out of your account?",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .height(52.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val totalWidth = maxWidth
                    val idleButtonWidth = (totalWidth - 12.dp) / 2
                    val actionButtonWidth by animateDpAsState(
                        targetValue = if (isSubmitting) 52.dp else idleButtonWidth,
                        animationSpec = spring(
                            dampingRatio = 0.8f,
                            stiffness = Spring.StiffnessMedium
                        ),
                        label = "logoutActionButtonWidth"
                    )
                    val rowSpacing by animateDpAsState(
                        targetValue = if (isSubmitting) 0.dp else 12.dp,
                        animationSpec = spring(
                            dampingRatio = 0.8f,
                            stiffness = Spring.StiffnessMedium
                        ),
                        label = "logoutRowSpacing"
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(rowSpacing, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AnimatedVisibility(
                            visible = !isSubmitting,
                            enter = fadeIn(tween(250)) + expandHorizontally(spring(stiffness = Spring.StiffnessMedium)),
                            exit = fadeOut(tween(200)) + shrinkHorizontally(spring(stiffness = Spring.StiffnessMedium))
                        ) {
                            OutlinedButton(
                                onClick = {
                                    presets.boulder()
                                    onDismiss()
                                },
                                modifier = Modifier
                                    .width(idleButtonWidth)
                                    .height(52.dp),
                                shape = CircleShape
                            ) {
                                Text(
                                    text = "Cancel",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        val logoutBgColor by animateColorAsState(
                            targetValue = MaterialTheme.colorScheme.error,
                            animationSpec = tween(300),
                            label = "logoutBgColor"
                        )

                        Box(
                            modifier = Modifier
                                .width(actionButtonWidth)
                                .height(52.dp)
                                .clip(CircleShape)
                                .background(logoutBgColor)
                                .clickable(enabled = logoutSubmitState == LogoutSubmitState.Idle) {
                                    presets.ping()
                                    scope.launch {
                                        logoutSubmitState = LogoutSubmitState.Loading

                                        val minDelayJob = async { delay(1000) }
                                        val signOutJob = async {
                                            suspendCancellableCoroutine<Unit> { continuation ->
                                                onConfirm {
                                                    if (continuation.isActive) {
                                                        continuation.resume(Unit)
                                                    }
                                                }
                                            }
                                        }

                                        // Wait for sign-out while showing rotating curvy loader (with 2.5s safety timeout)
                                        withTimeoutOrNull(2500) {
                                            minDelayJob.await()
                                            signOutJob.await()
                                        }
                                        minDelayJob.await()

                                        // Sign-out finished: show success tick with haptics
                                        logoutSubmitState = LogoutSubmitState.Success
                                        presets.systemNotificationSuccess()

                                        // Brief display of tick immediately before navigating to sign-in screen
                                        delay(650)
                                        onCompleteLogout()
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            AnimatedContent(
                                targetState = logoutSubmitState,
                                transitionSpec = {
                                    (fadeIn(tween(300)) + scaleIn(tween(300), initialScale = 0.8f))
                                        .togetherWith(fadeOut(tween(200)) + scaleOut(tween(200), targetScale = 0.6f))
                                },
                                label = "logoutButtonContent"
                            ) { state ->
                                when (state) {
                                    LogoutSubmitState.Idle -> {
                                        Text(
                                            text = "Log out",
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onError
                                        )
                                    }
                                    LogoutSubmitState.Loading -> {
                                        val density = LocalDensity.current
                                        val strokeWidthPx = with(density) { 2.5.dp.toPx() }
                                        val amplitudePx = with(density) { 3.dp.toPx() }
                                        CircularWavyProgressIndicator(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .rotate(rotationAngle),
                                            color = MaterialTheme.colorScheme.onError,
                                            trackColor = MaterialTheme.colorScheme.onError.copy(alpha = 0.25f),
                                            stroke = Stroke(width = strokeWidthPx),
                                            trackStroke = Stroke(width = strokeWidthPx),
                                            amplitude = amplitudePx,
                                            wavelength = 6.dp
                                        )
                                    }
                                    LogoutSubmitState.Success -> {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_tick),
                                            contentDescription = "Success",
                                            tint = MaterialTheme.colorScheme.onError,
                                            modifier = Modifier.size(26.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
