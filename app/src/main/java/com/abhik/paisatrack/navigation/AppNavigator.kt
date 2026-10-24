package com.abhik.paisatrack.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import com.abhik.paisatrack.data.AuthManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Centralized navigation manager that wraps the Jetpack Navigation 3 [NavBackStack].
 * Manages all back-stack operations, avoiding duplicate destinations and ensuring
 * proper lifecycle and state transitions throughout Paisa Track.
 */
@Stable
class AppNavigator(
    val backStack: NavBackStack<NavKey>
) {
    /** True if there is a previous screen to pop to */
    val canNavigateBack: Boolean
        get() = backStack.size > 1

    /** The currently active destination route, if any */
    val currentRoute: AppRoute?
        get() = backStack.lastOrNull() as? AppRoute

    /** Whether the bottom navigation bar should be displayed on the active destination */
    val isBottomBarVisible: Boolean
        get() = com.abhik.paisatrack.navigation.isBottomBarVisible(currentRoute)

    /**
     * Navigates to a new destination.
     * Prevents duplicate entries if the top destination is already equal to [route].
     */
    fun navigateTo(route: AppRoute) {
        if (backStack.lastOrNull() == route) return
        backStack.add(route)
    }

    /**
     * Attempts to navigate back to the previous destination.
     * @return true if an entry was popped, false if already at the root of the back stack.
     */
    fun navigateBack(): Boolean {
        return if (canNavigateBack) {
            backStack.removeAt(backStack.lastIndex)
            true
        } else {
            false
        }
    }

    /**
     * Replaces the entire back stack with a single root destination.
     * Useful for login, logout, onboarding completion, or splash transitions.
     */
    fun replaceRoot(route: AppRoute) {
        backStack.clear()
        backStack.add(route)
    }

    /**
     * Pops destinations until finding an instance of [route].
     * @param inclusive Whether to also pop [route] itself.
     * @return true if destination was found in stack and popped to, false otherwise.
     */
    fun popTo(route: AppRoute, inclusive: Boolean = false): Boolean {
        val index = backStack.indexOfLast { it == route }
        if (index < 0) return false

        val targetIndex = if (inclusive) index else index + 1
        while (backStack.size > targetIndex) {
            backStack.removeAt(backStack.lastIndex)
        }
        return true
    }

    // --- Domain Navigation Actions ---

    fun navigateToDashboard() {
        replaceRoot(AppRoute.Dashboard)
    }

    fun navigateToCollectionTransactions(collectionId: String) {
        navigateTo(AppRoute.CollectionTransactions(collectionId = collectionId))
    }

    fun navigateToAddTransaction(collectionId: String = "") {
        navigateTo(AppRoute.AddTransaction(collectionId = collectionId))
    }

    fun navigateToProfile() {
        navigateTo(AppRoute.Profile)
    }

    fun navigateToSearch() {
        navigateTo(AppRoute.Search)
    }

    fun navigateToInsights() {
        navigateTo(AppRoute.Insights)
    }

    fun navigateToCollections() {
        navigateTo(AppRoute.Collections)
    }

    /**
     * Navigates to a top-level tab destination (Transactions/Dashboard, Insights, Collections).
     * If the destination is already present in the back stack, pops back to it to prevent
     * redundant stack accumulation and preserve single-instance tab state.
     */
    fun navigateToTab(route: AppRoute) {
        if (currentRoute == route) return
        val popped = popTo(route)
        if (!popped) {
            navigateTo(route)
        }
    }

    fun navigateToSignIn() {
        replaceRoot(AppRoute.SignIn)
    }

    fun navigateToOnboarding() {
        replaceRoot(AppRoute.Onboarding)
    }

    /**
     * Handles complete user sign-out by clearing session credentials
     * and resetting the navigation back stack to [AppRoute.SignIn].
     */
    suspend fun logout(context: Context) {
        AuthManager.signOut(context)
        com.abhik.paisatrack.ui.components.dashboard.VisualSummaryTracker.reset()
        com.abhik.paisatrack.ui.components.dashboard.TransactionsPanelTracker.reset()
        replaceRoot(AppRoute.SignIn)
    }

    /**
     * Convenience method to perform logout from non-coroutine scopes.
     */
    fun logout(coroutineScope: CoroutineScope, context: Context) {
        coroutineScope.launch {
            logout(context)
        }
    }
}

/**
 * Creates and remembers an [AppNavigator] using a saveable Navigation 3 back stack.
 *
 * @param startDestination The initial destination for the back stack.
 */
@Composable
fun rememberAppNavigator(
    startDestination: AppRoute = AppRoute.Loader,
    backStack: NavBackStack<NavKey> = rememberNavBackStack(startDestination)
): AppNavigator {
    return remember(backStack) {
        AppNavigator(backStack)
    }
}
