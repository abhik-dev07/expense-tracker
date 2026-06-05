package com.abhik.paisatrack.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import com.airbnb.lottie.compose.*
import com.abhik.paisatrack.R
import com.abhik.paisatrack.data.AuthManager
import kotlinx.coroutines.delay

@Composable
fun LoaderScreen(
    onNavigateToDashboard: () -> Unit,
    onNavigateToSignIn: () -> Unit,
    onNavigateToOnboarding: () -> Unit
) {
    val context = LocalContext.current
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.loader))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )

    LaunchedEffect(Unit) {
        // Wait for 2.5 seconds to show the loader animation, then transition
        delay(2500)
        if (AuthManager.isUserSignedIn(context)) {
            onNavigateToDashboard()
        } else {
            if (AuthManager.hasSeenOnboarding(context)) {
                onNavigateToSignIn()
            } else {
                onNavigateToOnboarding()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1FB47B)),
        contentAlignment = Alignment.Center
    ) {
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.size(200.dp)
        )
    }
}
