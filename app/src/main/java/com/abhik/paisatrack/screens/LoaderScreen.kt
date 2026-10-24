package com.abhik.paisatrack.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.airbnb.lottie.compose.*
import com.abhik.paisatrack.R
import com.abhik.paisatrack.data.AuthManager
import com.abhik.paisatrack.utils.findActivity
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

    // Hide the system navigation bar only while the loader is on screen.
    val activity = remember(context) { context.findActivity() }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(activity, lifecycleOwner) {
        val window = activity?.window
        val controller = window?.let { WindowCompat.getInsetsController(it, it.decorView) }

        fun hideNav() {
            // A swipe from the edge shows the bar briefly; it auto-hides again.
            controller?.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller?.hide(WindowInsetsCompat.Type.navigationBars())
        }

        hideNav()
        // Re-hide if the bar came back (e.g. after the notification permission dialog).
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) hideNav()
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            controller?.show(WindowInsetsCompat.Type.navigationBars()) // app screens get the bar back
        }
    }

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

    val isDark = com.abhik.paisatrack.ui.theme.isAppInDarkTheme()
    val bgColor = if (isDark) Color(0xFF1D3A30) else Color(0xFF1FB47B)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.size(200.dp)
        )
    }
}
