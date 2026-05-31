package com.abhik.paisatrack

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
import com.abhik.paisatrack.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    installSplashScreen()
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          val viewModel: FinanceViewModel = viewModel()
          val navController = rememberNavController()
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
                slideInHorizontally(animationSpec = tween(400), initialOffsetX = { -it }) + fadeIn(animationSpec = tween(400))
              },
              exitTransition = {
                slideOutHorizontally(animationSpec = tween(400), targetOffsetX = { -it }) + fadeOut(animationSpec = tween(400))
              },
              popEnterTransition = {
                slideInHorizontally(animationSpec = tween(400), initialOffsetX = { -it }) + fadeIn(animationSpec = tween(400))
              }
            ) {
              DashboardScreen(
                viewModel = viewModel,
                onNavigateToAddTransaction = {
                  navController.navigate("add_transaction")
                },
                onNavigateToCollectionTransactions = { collectionId ->
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
              val collectionId = backStackEntry.arguments?.getString("collectionId")?.toLongOrNull() ?: 0L
              CollectionTransactionsScreen(
                viewModel = viewModel,
                collectionId = collectionId,
                onNavigateToAddTransaction = { colId ->
                  navController.navigate("add_transaction/$colId")
                },
                onBack = {
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
              val colId = backStackEntry.arguments?.getString("collectionId")?.toLongOrNull() ?: 0L
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
