package com.abhik.paisatrack.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * Type-safe navigation destination keys for Paisa Track.
 * All destinations implement [NavKey] and are [Serializable]
 * to support state restoration across process recreation.
 */
@Serializable
sealed interface AppRoute : NavKey {

    /** Initial splash / loader destination that determines whether user is logged in */
    @Serializable
    data object Loader : AppRoute

    /** Onboarding destination for first-time launch */
    @Serializable
    data object Onboarding : AppRoute

    /** Authentication / Sign-In destination */
    @Serializable
    data object SignIn : AppRoute

    /** Main dashboard destination */
    @Serializable
    data object Dashboard : AppRoute

    /**
     * Destination displaying transactions belonging to a specific collection.
     * @param collectionId ID of the collection to display.
     */
    @Serializable
    data class CollectionTransactions(val collectionId: String) : AppRoute

    /**
     * Destination for adding a new transaction.
     * @param collectionId Optional initial collection ID pre-selected in the flow.
     */
    @Serializable
    data class AddTransaction(val collectionId: String = "") : AppRoute

    /** User profile and application settings destination */
    @Serializable
    data object Profile : AppRoute

    /** Financial search destination */
    @Serializable
    data object Search : AppRoute

    /** Financial insights and analytics destination */
    @Serializable
    data object Insights : AppRoute

    /** Collections and vaults management destination */
    @Serializable
    data object Collections : AppRoute
}

/**
 * Centralized rule determining whether the bottom navigation bar is visible for a given [route].
 *
 * Visible on exactly:
 * 1. [AppRoute.Dashboard] (Transactions)
 * 2. [AppRoute.Insights]
 * 3. [AppRoute.Collections]
 *
 * Hidden on:
 * 1. [AppRoute.AddTransaction]
 * 2. [AppRoute.CollectionTransactions]
 * 3. [AppRoute.Profile]
 * 4. [AppRoute.SignIn]
 *
 * Also hidden on full-screen / onboarding / search flows by default:
 * - [AppRoute.Loader]
 * - [AppRoute.Onboarding]
 * - [AppRoute.Search]
 */
fun isBottomBarVisible(route: AppRoute?): Boolean = when (route) {
    is AppRoute.Dashboard,
    is AppRoute.Insights,
    is AppRoute.Collections -> true
    else -> false
}

/**
 * Maps a destination [route] to its corresponding bottom bar tab label,
 * or null if the route does not belong to a top-level bottom navigation tab.
 *
 * - Home -> Dashboard screen
 * - Analysis -> Insights screen
 * - Collection -> Collections screen
 */
fun getTabForRoute(route: AppRoute?): String? = when (route) {
    is AppRoute.Dashboard -> "Home"
    is AppRoute.Insights -> "Analysis"
    is AppRoute.Collections -> "Collection"
    else -> null
}


