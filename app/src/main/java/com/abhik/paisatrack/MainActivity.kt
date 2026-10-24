package com.abhik.paisatrack

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.*
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import com.abhik.paisatrack.navigation.AppNavigation
import com.abhik.paisatrack.ui.FinanceViewModel
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

class MainActivity : AppCompatActivity() {
  private lateinit var appUpdateManager: AppUpdateManager

  private val updateLauncher = registerForActivityResult(
    ActivityResultContracts.StartIntentSenderForResult()
  ) { result ->
    if (result.resultCode != android.app.Activity.RESULT_OK) {
      android.util.Log.e("MainActivity", "Update flow failed or was cancelled by user.")
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    android.util.Log.d("ThemeSwitch", "MainActivity.onCreate called (recreated: ${savedInstanceState != null})")
    installSplashScreen()
    com.abhik.paisatrack.ui.theme.ThemeManager.init(this)
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    if (savedInstanceState == null) {   // fresh launch only, see below
      WindowCompat.getInsetsController(window, window.decorView).apply {
        systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        hide(WindowInsetsCompat.Type.navigationBars())
      }
    }
    
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
      val isDark = com.abhik.paisatrack.ui.theme.isAppInDarkTheme()
      DisposableEffect(isDark) {
        enableEdgeToEdge(
          statusBarStyle = androidx.activity.SystemBarStyle.auto(
            android.graphics.Color.TRANSPARENT,
            android.graphics.Color.TRANSPARENT
          ) { isDark },
          navigationBarStyle = androidx.activity.SystemBarStyle.auto(
            android.graphics.Color.TRANSPARENT,
            android.graphics.Color.TRANSPARENT
          ) { isDark }
        )
        onDispose {}
      }
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

          val viewModel: FinanceViewModel = viewModel()
          val uiState by viewModel.uiState.collectAsState()

          // Trigger sync automatically when network transitions back to online
          LaunchedEffect(isOnline) {
            if (isOnline) {
              com.abhik.paisatrack.data.sync.SyncManager.triggerSyncNow(context)
            }
          }

          // Sync data from backend when the app is resumed (comes to foreground)
          val lifecycleOwner = LocalLifecycleOwner.current
          DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
              if (event == Lifecycle.Event.ON_RESUME && isOnline) {
                viewModel.refreshFromBackend()
              }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
              lifecycleOwner.lifecycle.removeObserver(observer)
            }
          }

          androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize()) {
            androidx.compose.foundation.layout.Column(
              modifier = Modifier.fillMaxSize()
            ) {
              AppNavigation(
                viewModel = viewModel,
                isOnline = isOnline,
                modifier = Modifier.fillMaxSize()
              )
            }
            com.abhik.paisatrack.ui.theme.ThemeTransitionOverlay()
          } // Close Box
        } // Close Surface
      } // Close MyApplicationTheme
    } // Close setContent
  } // Close onCreate

  private fun createNotificationChannel() {
    val channelId = "paisa_track_alerts_v3"
    val soundUri = Uri.parse("android.resource://" + packageName + "/raw/notification")
    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      // Delete old channels
      notificationManager.deleteNotificationChannel("paisa_track_notifications")
      notificationManager.deleteNotificationChannel("paisa_track_alerts_v2")

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
        enableVibration(true)
        vibrationPattern = longArrayOf(0, 500, 200, 500)
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
  override fun onDestroy() {
    android.util.Log.d("ThemeSwitch", "MainActivity.onDestroy called (isFinishing: $isFinishing)")
    super.onDestroy()
  }
}
