package com.abhik.paisatrack.screens.signin

import android.view.HapticFeedbackConstants
import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhik.paisatrack.BuildConfig
import com.abhik.paisatrack.R
import com.abhik.paisatrack.data.AuthManager
import com.abhik.paisatrack.utils.FinanceViewModel
import com.abhik.paisatrack.ui.components.commonUi.OfflineBanner
import com.abhik.paisatrack.screens.signin.components.LegalFooterLinks
import com.abhik.paisatrack.screens.signin.components.SignInButtonState
import com.abhik.paisatrack.screens.signin.components.TermsAgreementRow
import com.abhik.paisatrack.screens.signin.utils.SignInAuthUtils
import com.abhik.paisatrack.ui.theme.isAppInDarkTheme
import com.abhik.paisatrack.utils.findActivity
import com.abhik.paisatrack.utils.getSafePresets
import com.airbnb.lottie.compose.*
import com.mmk.kmpauth.google.GoogleAuthCredentials
import com.mmk.kmpauth.google.GoogleAuthProvider
import com.mmk.kmpauth.google.GoogleButtonUiContainer
import com.swmansion.pulsar.Pulsar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SignInScreen(
    viewModel: FinanceViewModel,
    isOnline: Boolean = true,
    onNavigateToDashboard: () -> Unit
) {
    val context = LocalContext.current
    val pulsar = remember(context) { Pulsar(context.findActivity() ?: context) }
    val presets = remember(pulsar) { pulsar.getSafePresets() }
    val coroutineScope = rememberCoroutineScope()
    val view = LocalView.current

    var authReady by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        val provider = GoogleAuthProvider.create(
            credentials = GoogleAuthCredentials(
                serverId = BuildConfig.WEB_CLIENT_ID
            )
        )
        AuthManager.setGoogleAuthProvider(provider)
        authReady = true
    }

    var isAgreed by remember { mutableStateOf(AuthManager.hasAgreedToTerms(context)) }
    var isLoading by remember { mutableStateOf(false) }
    var buttonState by remember { mutableStateOf(SignInButtonState.Idle) }

    val fadeAlpha = remember { Animatable(1f) }
    val slideOffset = remember { Animatable(0f) }
    val density = context.resources.displayMetrics.density

    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.revenue))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )

    val isDark = isAppInDarkTheme()
    val buttonBg = if (isDark) Color(0xFF1C1C1E) else Color(0xFFFFFFFF)
    val buttonBorderColor = if (isDark) Color(0xFF38383A) else Color(0x14000000)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Offline banner pill when internet is unavailable
            OfflineBanner(
                isVisible = !isOnline,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            // Lottie illustration
            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(310.dp)
                    .padding(bottom = 32.dp)
            )

            // Title
            Text(
                text = "Paisa Track",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Subtitle
            Text(
                text = "Manage your spending efficiently with just a single tap.",
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 40.dp, start = 16.dp, end = 16.dp)
            )

            // Google sign in button
            val buttonOpacity = if (!isAgreed || !isOnline) 0.5f else if (isLoading) 0.6f else 1f
            if (authReady) {
                GoogleButtonUiContainer(
                    onGoogleSignInResult = { googleUser ->
                        if (googleUser != null) {
                            coroutineScope.launch {
                                // Let the spinner show for a tiny bit then proceed
                                delay(400)

                                val email = SignInAuthUtils.getEmailFromIdToken(googleUser.idToken) ?: "no-email@google.com"
                                val googleId = SignInAuthUtils.getGoogleIdFromIdToken(googleUser.idToken) ?: email
                                viewModel.onUserSignedIn(
                                    googleId = googleId,
                                    email = email,
                                    name = googleUser.displayName ?: "",
                                    image = googleUser.profilePicUrl ?: "",
                                    onComplete = {
                                        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                                        Toast.makeText(context, "Signed in successfully!", Toast.LENGTH_SHORT).show()
                                        isLoading = false
                                        onNavigateToDashboard()
                                    }
                                )
                            }
                        } else {
                            isLoading = false
                            buttonState = SignInButtonState.Idle
                            coroutineScope.launch {
                                fadeAlpha.snapTo(1f)
                                slideOffset.snapTo(0f)
                            }
                            presets.catPaw()
                            Toast.makeText(context, "Sign in failed or was cancelled", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    OutlinedButton(
                        onClick = {
                            if (!isOnline) {
                                presets.catPaw()
                                Toast.makeText(context, "You are currently offline", Toast.LENGTH_SHORT).show()
                            } else if (!isAgreed) {
                                presets.catPaw()
                                Toast.makeText(
                                    context,
                                    "Please agree to our Terms & Conditions and Privacy Policy to continue",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else if (!isLoading && authReady && isOnline) {
                                presets.ping()
                                isLoading = true
                                val triggerSignIn = { this.onClick() }
                                coroutineScope.launch {
                                    // Phase 1: Fade out the Google content
                                    fadeAlpha.animateTo(0f, animationSpec = tween(durationMillis = 200))

                                    // Phase 2: Show "Made with ❤️ by Abhik" and slide in
                                    buttonState = SignInButtonState.MadeWith
                                    slideOffset.snapTo(10f)

                                    launch {
                                        fadeAlpha.animateTo(1f, animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing))
                                    }
                                    launch {
                                        slideOffset.animateTo(0f, animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing))
                                    }

                                    // Wait for 1 second
                                    delay(1000)

                                    // Phase 3: Fade out "Made with ❤️ by Abhik" and slide up/out
                                    launch {
                                        fadeAlpha.animateTo(0f, animationSpec = tween(durationMillis = 350, easing = FastOutLinearInEasing))
                                    }
                                    launch {
                                        slideOffset.animateTo(-10f, animationSpec = tween(durationMillis = 350, easing = FastOutLinearInEasing))
                                    }

                                    delay(350)

                                    // Phase 4: Show "Signing in" (ActivityIndicator)
                                    buttonState = SignInButtonState.SigningIn
                                    slideOffset.snapTo(10f)

                                    launch {
                                        fadeAlpha.animateTo(1f, animationSpec = tween(durationMillis = 300, easing = LinearOutSlowInEasing))
                                    }
                                    launch {
                                        slideOffset.animateTo(0f, animationSpec = tween(durationMillis = 300, easing = LinearOutSlowInEasing))
                                    }

                                    delay(300)
                                    triggerSignIn()
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .alpha(buttonOpacity),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = buttonBg,
                            contentColor = MaterialTheme.colorScheme.onBackground,
                            disabledContainerColor = buttonBg.copy(alpha = 0.5f),
                            disabledContentColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        ),
                        border = BorderStroke(1.dp, buttonBorderColor),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                        enabled = !isLoading && authReady && isOnline
                    ) {
                        Box(
                            modifier = Modifier
                                .graphicsLayer {
                                    alpha = fadeAlpha.value
                                    translationY = slideOffset.value * density
                                }
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            when (buttonState) {
                                SignInButtonState.Idle -> {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Image(
                                            painter = painterResource(id = R.drawable.img_google_logo),
                                            contentDescription = "Google Logo",
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = "Continue with Google",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                    }
                                }
                                SignInButtonState.MadeWith -> {
                                    Text(
                                        text = "Made with ❤️ by Abhik",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                                SignInButtonState.SigningIn -> {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        LoadingIndicator(
                                            modifier = Modifier.size(24.dp),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = "Signing in",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                OutlinedButton(
                    onClick = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .alpha(0.5f),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = buttonBg,
                        contentColor = MaterialTheme.colorScheme.onBackground,
                        disabledContainerColor = buttonBg,
                        disabledContentColor = MaterialTheme.colorScheme.onBackground
                    ),
                    border = BorderStroke(1.dp, buttonBorderColor),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                    enabled = false
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        LoadingIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Loading Google services...",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Checkbox and Agreement Text Row
            TermsAgreementRow(
                isAgreed = isAgreed,
                onAgreementChanged = { newVal ->
                    isAgreed = newVal
                    AuthManager.setAgreedToTerms(context, newVal)
                },
                onPlunkHaptic = { presets.plunk() }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Legal documents links (About Us & Disclaimer)
            LegalFooterLinks(
                onPlunkHaptic = { presets.plunk() }
            )
        }
    }
}
