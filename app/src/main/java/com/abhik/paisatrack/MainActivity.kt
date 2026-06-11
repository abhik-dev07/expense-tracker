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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
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
import com.abhik.paisatrack.ui.screens.NoInternetScreen
import com.abhik.paisatrack.ui.screens.ServerErrorScreen
import com.abhik.paisatrack.ui.theme.MyApplicationTheme
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import android.content.IntentSender
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability

class MainActivity : ComponentActivity() {
  private lateinit var appUpdateManager: AppUpdateManager

  private val updateLauncher = registerForActivityResult(
    ActivityResultContracts.StartIntentSenderForResult()
  ) { result ->
    if (result.resultCode != android.app.Activity.RESULT_OK) {
      android.util.Log.e("MainActivity", "Update flow failed or was cancelled by user.")
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    installSplashScreen()
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    
    appUpdateManager = AppUpdateManagerFactory.create(this)
    checkForUpdates()
    
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
          val context = LocalContext.current
          val isOnline by produceState(initialValue = true) {
            val connectivityManager =
              context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val callback = object : ConnectivityManager.NetworkCallback() {
              override fun onAvailable(network: Network) {
                value = true
              }

              override fun onLost(network: Network) {
                value = false
              }
            }
            connectivityManager.registerDefaultNetworkCallback(callback)

            val activeNetwork = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
            value = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true

            awaitDispose {
              connectivityManager.unregisterNetworkCallback(callback)
            }
          }

          if (!isOnline) {
            NoInternetScreen(onRetry = {
              val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
              val activeNetwork = connectivityManager.activeNetwork
              val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
              val connected =
                capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
              if (connected) {
                // The produceState value will update via the callback or manually if needed
              }
            })
          } else {
            val viewModel: FinanceViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsState()
            val navController = rememberNavController()

            // Sync data from backend when the app is resumed (comes to foreground)
            val lifecycleOwner = LocalLifecycleOwner.current
            DisposableEffect(lifecycleOwner) {
              val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                  viewModel.refreshFromBackend()
                }
              }
              lifecycleOwner.lifecycle.addObserver(observer)
              onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
              }
            }

            // Force logout when the backend reports this user was deleted
            LaunchedEffect(Unit) {
              viewModel.userDeletedEvent.collect {
                val ctx = context
                com.abhik.paisatrack.data.AuthManager.signOut(ctx)
                navController.navigate("sign_in") {
                  popUpTo(0) { inclusive = true }
                }
              }
            }

            if (uiState.isServerError) {
              ServerErrorScreen(onRetry = {
                viewModel.retrySync()
              })
            } else {
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
                    slideInHorizontally(animationSpec = tween(320), initialOffsetX = { -it })
                  },
                  exitTransition = {
                    slideOutHorizontally(animationSpec = tween(320), targetOffsetX = { -it })
                  },
                  popEnterTransition = {
                    slideInHorizontally(animationSpec = tween(320), initialOffsetX = { -it })
                  },
                  popExitTransition = {
                    slideOutHorizontally(animationSpec = tween(320), targetOffsetX = { it })
                  }
                ) { _ ->
                  DashboardScreen(
                    modifier = Modifier,
                    viewModel = viewModel,
                    onNavigateToAddTransaction = {
                      navController.navigate("add_transaction")
                    },
                    onNavigateToCollectionTransactions = { collectionId, rect ->
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
                    slideInHorizontally(animationSpec = tween(320), initialOffsetX = { it })
                  },
                  exitTransition = {
                    slideOutHorizontally(animationSpec = tween(320), targetOffsetX = { it })
                  },
                  popEnterTransition = {
                    slideInHorizontally(animationSpec = tween(320), initialOffsetX = { -it })
                  },
                  popExitTransition = {
                    slideOutHorizontally(animationSpec = tween(320), targetOffsetX = { it })
                  }
                ) { backStackEntry ->
                  val collectionId = backStackEntry.arguments?.getString("collectionId") ?: ""
                  var isBackNavigating by rememberSaveable(collectionId) { mutableStateOf(false) }

                  androidx.activity.compose.BackHandler(enabled = !isBackNavigating) {
                    isBackNavigating = true
                    navController.popBackStack()
                  }

                  CollectionTransactionsScreen(
                    modifier = Modifier,
                    viewModel = viewModel,
                    collectionId = collectionId,
                    onNavigateToAddTransaction = { colId ->
                      navController.navigate("add_transaction/$colId")
                    },
                    onBack = {
                      if (isBackNavigating) return@CollectionTransactionsScreen
                      isBackNavigating = true
                      navController.popBackStack()
                    }
                  )
                }
                composable(
                  "add_transaction",
                  enterTransition = {
                    slideInHorizontally(animationSpec = tween(320), initialOffsetX = { it })
                  },
                  exitTransition = {
                    slideOutHorizontally(animationSpec = tween(320), targetOffsetX = { -it })
                  },
                  popEnterTransition = {
                    slideInHorizontally(animationSpec = tween(320), initialOffsetX = { -it })
                  },
                  popExitTransition = {
                    slideOutHorizontally(animationSpec = tween(320), targetOffsetX = { it })
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
                    slideInHorizontally(animationSpec = tween(320), initialOffsetX = { it })
                  },
                  exitTransition = {
                    slideOutHorizontally(animationSpec = tween(320), targetOffsetX = { -it })
                  },
                  popEnterTransition = {
                    slideInHorizontally(animationSpec = tween(320), initialOffsetX = { -it })
                  },
                  popExitTransition = {
                    slideOutHorizontally(animationSpec = tween(320), targetOffsetX = { it })
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

  private fun checkForUpdates() {
    appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
      if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
        && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
      ) {
        try {
          appUpdateManager.startUpdateFlowForResult(
            appUpdateInfo,
            updateLauncher,
            AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
          )
        } catch (e: IntentSender.SendIntentException) {
          e.printStackTrace()
        }
      }
    }
  }

  override fun onResume() {
    super.onResume()
    if (::appUpdateManager.isInitialized) {
      appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
        if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
          try {
            appUpdateManager.startUpdateFlowForResult(
              appUpdateInfo,
              updateLauncher,
              AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
            )
          } catch (e: IntentSender.SendIntentException) {
            e.printStackTrace()
          }
        }
      }
    }
  }
}
