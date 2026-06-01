package com.abhik.paisatrack

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.*
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalConfiguration
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.abhik.paisatrack.ui.FinanceViewModel
import com.abhik.paisatrack.ui.screens.DashboardScreen
import com.abhik.paisatrack.ui.screens.AddTransactionScreen
import com.abhik.paisatrack.ui.screens.CollectionTransactionsScreen
import com.abhik.paisatrack.ui.screens.LoaderScreen
import com.abhik.paisatrack.ui.screens.SignInScreen
import com.abhik.paisatrack.ui.screens.OnboardingScreen
import com.abhik.paisatrack.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    installSplashScreen()
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    
    // Initialize Google Auth Provider to ensure sign-out works even when bypassing SignInScreen
    try {
      val provider = com.mmk.kmpauth.google.GoogleAuthProvider.create(
        credentials = com.mmk.kmpauth.google.GoogleAuthCredentials(
          serverId = BuildConfig.WEB_CLIENT_ID
        )
      )
      com.abhik.paisatrack.data.AuthManager.setGoogleAuthProvider(provider)
    } catch (e: Exception) {
      e.printStackTrace()
    }
    
    // Create the notification channel with custom sound
    createNotificationChannel()

    // Request notification permission for Android 13+ (API 33+)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      if (androidx.core.content.ContextCompat.checkSelfPermission(
          this,
          android.Manifest.permission.POST_NOTIFICATIONS
        ) != android.content.pm.PackageManager.PERMISSION_GRANTED
      ) {
        androidx.core.app.ActivityCompat.requestPermissions(
          this,
          arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
          101
        )
      }
    }

    setContent {
      MyApplicationTheme {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          val viewModel: FinanceViewModel = viewModel()
          val navController = rememberNavController()
          var clickedCardBounds by remember { mutableStateOf<androidx.compose.ui.geometry.Rect?>(null) }
          var isFlippingTransition by remember { mutableStateOf(false) }

          NavHost(navController = navController, startDestination = "loader") {
            composable("loader") {
              LoaderScreen(
                onNavigateToDashboard = {
                  navController.navigate("dashboard") {
                    popUpTo("loader") { inclusive = true }
                  }
                },
                onNavigateToSignIn = {
                  navController.navigate("sign_in") {
                    popUpTo("loader") { inclusive = true }
                  }
                },
                onNavigateToOnboarding = {
                  navController.navigate("onboarding") {
                    popUpTo("loader") { inclusive = true }
                  }
                }
              )
            }
            composable("onboarding") {
              OnboardingScreen(
                onNavigateToSignIn = {
                  navController.navigate("sign_in") {
                    popUpTo("onboarding") { inclusive = true }
                  }
                }
              )
            }
            composable("sign_in") {
              SignInScreen(
                viewModel = viewModel,
                onNavigateToDashboard = {
                  navController.navigate("dashboard") {
                    popUpTo("sign_in") { inclusive = true }
                  }
                }
              )
            }
            composable(
              "dashboard",
              enterTransition = {
                if (initialState.destination.route == "collection_transactions/{collectionId}") {
                  EnterTransition.None
                } else {
                  slideInHorizontally(animationSpec = tween(400), initialOffsetX = { -it }) + fadeIn(animationSpec = tween(400))
                }
              },
              exitTransition = {
                if (targetState.destination.route == "collection_transactions/{collectionId}") {
                  ExitTransition.None
                } else {
                  slideOutHorizontally(animationSpec = tween(400), targetOffsetX = { -it }) + fadeOut(animationSpec = tween(400))
                }
              },
              popEnterTransition = {
                if (initialState.destination.route == "collection_transactions/{collectionId}") {
                  EnterTransition.None
                } else {
                  slideInHorizontally(animationSpec = tween(400), initialOffsetX = { -it }) + fadeIn(animationSpec = tween(400))
                }
              }
            ) { backStackEntry ->
              val transition = this@composable.transition
              val isFlipping = isFlippingTransition
              
              val isTransitionRunning = transition.currentState != transition.targetState
              LaunchedEffect(isTransitionRunning) {
                if (!isTransitionRunning) {
                  isFlippingTransition = false
                }
              }
              
              val progress by transition.animateFloat(
                transitionSpec = {
                  if (targetState == EnterExitState.Visible) {
                    tween(durationMillis = 600, easing = LinearOutSlowInEasing)
                  } else {
                    tween(durationMillis = 600, easing = FastOutLinearInEasing)
                  }
                },
                label = "FlipProgress"
              ) { enterExitState ->
                if (enterExitState == EnterExitState.Visible) 1f else 0f
              }

              val isPop = navController.currentBackStackEntry?.destination?.route == "dashboard"
              val rotationY: Float
              val alpha: Float
              val isEntering = transition.targetState == EnterExitState.Visible
              if (isEntering) {
                if (progress <= 0.5f) {
                  rotationY = if (isPop) -90f else 90f
                  alpha = 0f
                } else {
                  val t = (progress - 0.5f) * 2f
                  rotationY = (if (isPop) -90f else 90f) * (1f - t)
                  alpha = 1f
                }
              } else {
                if (progress >= 0.5f) {
                  val t = (1f - progress) * 2f
                  rotationY = (if (isPop) 90f else -90f) * t
                  alpha = 1f
                } else {
                  rotationY = if (isPop) 90f else -90f
                  alpha = 0f
                }
              }

              val density = LocalDensity.current.density
              val modifier = if (isFlipping) {
                val scale = 0.96f + 0.04f * progress
                Modifier.graphicsLayer {
                  this.rotationY = rotationY
                  this.alpha = alpha
                  this.scaleX = scale
                  this.scaleY = scale
                  this.cameraDistance = 18f * density
                }
              } else {
                Modifier
              }

              DashboardScreen(
                modifier = modifier,
                viewModel = viewModel,
                onNavigateToAddTransaction = {
                  navController.navigate("add_transaction")
                },
                onNavigateToCollectionTransactions = { collectionId, rect ->
                  clickedCardBounds = rect
                  isFlippingTransition = true
                  navController.navigate("collection_transactions/$collectionId")
                },
                onLogout = {
                  navController.navigate("sign_in") {
                    popUpTo(0) { inclusive = true }
                  }
                }
              )
            }
            composable(
              "collection_transactions/{collectionId}",
              enterTransition = {
                if (initialState.destination.route == "dashboard") {
                  EnterTransition.None
                } else {
                  slideInHorizontally(animationSpec = tween(400), initialOffsetX = { it }) + fadeIn(animationSpec = tween(400))
                }
              },
              exitTransition = {
                if (targetState.destination.route == "dashboard") {
                  ExitTransition.None
                } else {
                  slideOutHorizontally(animationSpec = tween(400), targetOffsetX = { -it }) + fadeOut(animationSpec = tween(400))
                }
              },
              popEnterTransition = {
                if (initialState.destination.route == "dashboard") {
                  EnterTransition.None
                } else {
                  slideInHorizontally(animationSpec = tween(400), initialOffsetX = { -it }) + fadeIn(animationSpec = tween(400))
                }
              },
              popExitTransition = {
                if (targetState.destination.route == "dashboard") {
                  ExitTransition.None
                } else {
                  slideOutHorizontally(animationSpec = tween(400), targetOffsetX = { it }) + fadeOut(animationSpec = tween(400))
                }
              }
            ) { backStackEntry ->
              val collectionId = backStackEntry.arguments?.getString("collectionId") ?: ""
              val transition = this@composable.transition
              val isFlipping = isFlippingTransition
              
              val isTransitionRunning = transition.currentState != transition.targetState
              LaunchedEffect(isTransitionRunning) {
                if (!isTransitionRunning) {
                  isFlippingTransition = false
                }
              }

              androidx.activity.compose.BackHandler {
                isFlippingTransition = true
                navController.popBackStack()
              }
              
              val progress by transition.animateFloat(
                transitionSpec = {
                  if (targetState == EnterExitState.Visible) {
                    tween(durationMillis = 600, easing = LinearOutSlowInEasing)
                  } else {
                    tween(durationMillis = 600, easing = FastOutLinearInEasing)
                  }
                },
                label = "FlipProgress"
              ) { enterExitState ->
                if (enterExitState == EnterExitState.Visible) 1f else 0f
              }

              val isPop = navController.currentBackStackEntry?.destination?.route == "dashboard"
              val rotationY: Float
              val alpha: Float
              val isEntering = transition.targetState == EnterExitState.Visible
              if (isEntering) {
                if (progress <= 0.5f) {
                  rotationY = if (isPop) -90f else 90f
                  alpha = 0f
                } else {
                  val t = (progress - 0.5f) * 2f
                  rotationY = (if (isPop) -90f else 90f) * (1f - t)
                  alpha = 1f
                }
              } else {
                if (progress >= 0.5f) {
                  val t = (1f - progress) * 2f
                  rotationY = (if (isPop) 90f else -90f) * t
                  alpha = 1f
                } else {
                  rotationY = if (isPop) 90f else -90f
                  alpha = 0f
                }
              }

              val config = LocalConfiguration.current
              val density = LocalDensity.current.density
              val screenWidthPx = config.screenWidthDp * density
              val screenHeightPx = config.screenHeightDp * density

              val targetScaleX = if (clickedCardBounds != null && screenWidthPx > 0f) clickedCardBounds!!.width / screenWidthPx else 1f
              val targetScaleY = if (clickedCardBounds != null && screenHeightPx > 0f) clickedCardBounds!!.height / screenHeightPx else 1f
              val targetTranslationX = if (clickedCardBounds != null) {
                val cardCenterX = clickedCardBounds!!.left + clickedCardBounds!!.width / 2f
                cardCenterX - (screenWidthPx / 2f)
              } else 0f
              val targetTranslationY = if (clickedCardBounds != null) {
                val cardCenterY = clickedCardBounds!!.top + clickedCardBounds!!.height / 2f
                cardCenterY - (screenHeightPx / 2f)
              } else 0f

              val currentScaleX = targetScaleX + (1f - targetScaleX) * progress
              val currentScaleY = targetScaleY + (1f - targetScaleY) * progress
              val currentTranslationX = targetTranslationX * (1f - progress)
              val currentTranslationY = targetTranslationY * (1f - progress)

              val modifier = if (isFlipping) {
                Modifier.graphicsLayer {
                  this.rotationY = rotationY
                  this.alpha = alpha
                  this.scaleX = currentScaleX
                  this.scaleY = currentScaleY
                  this.translationX = currentTranslationX
                  this.translationY = currentTranslationY
                  this.cameraDistance = 18f * density
                }
              } else {
                Modifier
              }

              CollectionTransactionsScreen(
                modifier = modifier,
                viewModel = viewModel,
                collectionId = collectionId,
                onNavigateToAddTransaction = { colId ->
                  navController.navigate("add_transaction/$colId")
                },
                onBack = {
                  isFlippingTransition = true
                  navController.popBackStack()
                }
              )
            }
            composable(
              "add_transaction",
              enterTransition = {
                slideInHorizontally(animationSpec = tween(400), initialOffsetX = { it }) + fadeIn(animationSpec = tween(400))
              },
              exitTransition = {
                slideOutHorizontally(animationSpec = tween(400), targetOffsetX = { -it }) + fadeOut(animationSpec = tween(400))
              },
              popEnterTransition = {
                slideInHorizontally(animationSpec = tween(400), initialOffsetX = { -it }) + fadeIn(animationSpec = tween(400))
              },
              popExitTransition = {
                slideOutHorizontally(animationSpec = tween(400), targetOffsetX = { it }) + fadeOut(animationSpec = tween(400))
              }
            ) {
              AddTransactionScreen(
                viewModel = viewModel,
                onBack = {
                  navController.popBackStack()
                }
              )
            }
            composable(
              "add_transaction/{collectionId}",
              enterTransition = {
                slideInHorizontally(animationSpec = tween(400), initialOffsetX = { it }) + fadeIn(animationSpec = tween(400))
              },
              exitTransition = {
                slideOutHorizontally(animationSpec = tween(400), targetOffsetX = { -it }) + fadeOut(animationSpec = tween(400))
              },
              popEnterTransition = {
                slideInHorizontally(animationSpec = tween(400), initialOffsetX = { -it }) + fadeIn(animationSpec = tween(400))
              },
              popExitTransition = {
                slideOutHorizontally(animationSpec = tween(400), targetOffsetX = { it }) + fadeOut(animationSpec = tween(400))
              }
            ) { backStackEntry ->
              val colId = backStackEntry.arguments?.getString("collectionId") ?: ""
              AddTransactionScreen(
                viewModel = viewModel,
                initialCollectionId = colId,
                onBack = {
                  navController.popBackStack()
                }
              )
            }
          }
        }
      }
    }
  }

  private fun createNotificationChannel() {
    val channelId = "paisa_track_notifications"
    val soundUri = Uri.parse("android.resource://" + packageName + "/" + R.raw.notification)
    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      // Delete old channel first to force sound update
      notificationManager.deleteNotificationChannel(channelId)

      val channel = NotificationChannel(
        channelId,
        "Paisa Track Alerts",
        NotificationManager.IMPORTANCE_HIGH
      ).apply {
        description = "Channel for budget and expense push notifications"
        val audioAttributes = AudioAttributes.Builder()
          .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
          .setUsage(AudioAttributes.USAGE_NOTIFICATION)
          .build()
        setSound(soundUri, audioAttributes)
      }
      notificationManager.createNotificationChannel(channel)
    }
  }
}
