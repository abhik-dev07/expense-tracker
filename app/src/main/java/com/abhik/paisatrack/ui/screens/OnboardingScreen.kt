package com.abhik.paisatrack.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.*
import com.abhik.paisatrack.R
import com.abhik.paisatrack.data.AuthManager
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalView
import android.view.HapticFeedbackConstants
import com.swmansion.pulsar.Pulsar
import com.abhik.paisatrack.ui.utils.findActivity
import com.abhik.paisatrack.ui.utils.getSafePresets

data class OnboardingStep(
    val title: String,
    val description: String,
    val rawResId: Int
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onNavigateToSignIn: () -> Unit
) {
    val context = LocalContext.current
    val pulsar = remember(context) { Pulsar(context.findActivity() ?: context) }
    val presets = remember(pulsar) { pulsar.getSafePresets() }
    val coroutineScope = rememberCoroutineScope()
    val view = LocalView.current

    val steps = remember {
        listOf(
            OnboardingStep(
                title = "Record Daily Spending",
                description = "Easily log your daily spending and organize your spending habits.",
                rawResId = R.raw.onboarding_1
            ),
            OnboardingStep(
                title = "Smart Categories",
                description = "Sort your spending into categories for easy viewing.",
                rawResId = R.raw.onboarding_2
            ),
            OnboardingStep(
                title = "Spending Insights",
                description = "Get a clear view of your spending habits with intuitive charts.",
                rawResId = R.raw.onboarding_3
            ),
            OnboardingStep(
                title = "You're All Set!",
                description = "Let's create your first collection and start tracking your spending.",
                rawResId = R.raw.onboarding_4
            )
        )
    }

    val pagerState = rememberPagerState(pageCount = { steps.size })

    fun handleComplete() {
        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        AuthManager.setSeenOnboarding(context, true)
        onNavigateToSignIn()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Horizontal Pager
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { pageIndex ->
            val step = steps[pageIndex]
            val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(step.rawResId))
            val progress by animateLottieCompositionAsState(
                composition = composition,
                iterations = LottieConstants.IterateForever
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 32.dp, end = 32.dp, top = 24.dp, bottom = 160.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Lottie Animation Container
                Box(
                    modifier = Modifier
                        .size(400.dp)
                        .padding(bottom = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    LottieAnimation(
                        composition = composition,
                        progress = { progress },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Title
                Text(
                    text = step.title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Description
                Text(
                    text = step.description,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
            }
        }

        // Skip button (Top Right) - placed after pager so it draws on top and is clickable
        if (pagerState.currentPage < steps.size - 1) {
            val skipInteractionSource = remember { MutableInteractionSource() }
            val skipPressed by skipInteractionSource.collectIsPressedAsState()
            val skipAlpha by animateFloatAsState(
                targetValue = if (skipPressed) 0.72f else 1f,
                animationSpec = tween(durationMillis = 120),
                label = "SkipButtonAlpha"
            )

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .width(72.dp)
                    .height(36.dp)
                    .alpha(skipAlpha)
                    .clip(RoundedCornerShape(999.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                    .clickable(
                        interactionSource = skipInteractionSource,
                        indication = LocalIndication.current
                    ) {
                        presets.plunk()
                        handleComplete()
                    }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Skip",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Bottom Actions (Indicator + Buttons)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(start = 32.dp, end = 32.dp, bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Page Indicators (Dots)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(steps.size) { i ->
                    val active = pagerState.currentPage == i
                    Box(
                        modifier = Modifier
                            .size(if (active) 10.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (active) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                            )
                    )
                }
            }

            // Buttons row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val isFirstPage = pagerState.currentPage == 0
                val isLastPage = pagerState.currentPage == steps.size - 1

                // Back Button (only visible after page 0)
                if (!isFirstPage) {
                    OutlinedButton(
                        onClick = {
                            presets.boulder()
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        },
                        shape = CircleShape,
                        modifier = Modifier
                            .weight(1.2f)
                            .padding(end = 8.dp)
                    ) {
                        Text(
                            text = "Back",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                // Next / Get Started Button
                Button(
                    onClick = {
                        presets.ping()
                        if (isLastPage) {
                            handleComplete()
                        } else {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    },
                    shape = CircleShape,
                    modifier = if (isFirstPage) {
                        Modifier.weight(1f) // Takes full width of Row since it's the only child
                    } else {
                        Modifier.weight(2.8f).padding(start = 8.dp)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = if (isLastPage) "Get Started" else "Next",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
