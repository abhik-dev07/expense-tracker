package com.abhik.paisatrack.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.zIndex
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.abhik.paisatrack.utils.FinanceViewModel
import com.abhik.paisatrack.ui.components.commonUi.AddCollectionBottomSheet
import com.abhik.paisatrack.ui.components.commonUi.PlusMenuDialog
import com.abhik.paisatrack.screens.addtransaction.AddTransactionScreen
import com.abhik.paisatrack.screens.collectiontransaction.CollectionTransactionScreen
import com.abhik.paisatrack.screens.dashboard.DashboardScreen
import com.abhik.paisatrack.screens.insights.InsightsScreen
import com.abhik.paisatrack.screens.collections.CollectionsScreen
import com.abhik.paisatrack.screens.LoaderScreen
import com.abhik.paisatrack.screens.OnboardingScreen
import com.abhik.paisatrack.screens.profile.ProfileScreen
import com.abhik.paisatrack.screens.signin.SignInScreen
import com.abhik.paisatrack.screens.search.SearchScreen
import com.abhik.paisatrack.ui.theme.isAppInDarkTheme
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

private const val TAB_FADE_MS = 180

private val TabRoutes: List<AppRoute> =
    listOf(AppRoute.Dashboard, AppRoute.Insights, AppRoute.Collections)

/**
 * One persistent tab screen. Stays composed once visited; the active tab sits on top and fades in
 * while the previous tab stays opaque underneath, then is hidden (alpha 0) without being disposed.
 */
@Composable
private fun TabLayer(active: Boolean, content: @Composable () -> Unit) {
    val alpha = remember { Animatable(0f) }
    LaunchedEffect(active) {
        if (active) {
            alpha.animateTo(1f, tween(TAB_FADE_MS, easing = LinearOutSlowInEasing))
        } else {
            delay(TAB_FADE_MS.toLong().milliseconds)
            alpha.snapTo(0f)
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(if (active) 1f else 0f)
            .graphicsLayer { this.alpha = alpha.value }
    ) {
        content()
    }
}

/**
 * Centralized Jetpack Navigation 3 root composable for Paisa Track.
 *
 * Top-level tabs (Dashboard, Insights, Collections) are hosted in a persistent layer beneath
 * [NavDisplay] so switching tabs never rebuilds a screen. Their [NavDisplay] entries are empty
 * placeholders. All other destinations are regular [NavDisplay] entries drawn above the tab layer.
 */
@Composable
fun AppNavigation(
    viewModel: FinanceViewModel,
    isOnline: Boolean,
    modifier: Modifier = Modifier,
    navigator: AppNavigator = rememberAppNavigator()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isDark = isAppInDarkTheme()

    val currentRoute = navigator.currentRoute
    val isBottomBarVisible = navigator.isBottomBarVisible

    // Collected as State instead of reading StateFlow.value during composition.
    val viewModelActiveTab by viewModel.activeTab.collectAsState()

    // Most recent tab in the back stack: the tab shown beneath any pushed subscreen.
    val activeTabRoute: AppRoute? =
        navigator.backStack.lastOrNull { NavigationTransitions.isTopLevelTab(it) } as? AppRoute
    val activeTab = getTabForRoute(activeTabRoute) ?: viewModelActiveTab

    // Tabs that have been visited stay composed. Cleared when no tab is in the stack (logout etc.).
    var visitedTabs by remember { mutableStateOf(emptySet<AppRoute>()) }
    SideEffect {
        visitedTabs = if (activeTabRoute == null) emptySet() else visitedTabs + activeTabRoute
    }
    val shownTabs: Set<AppRoute> =
        if (activeTabRoute == null) emptySet() else visitedTabs + activeTabRoute

    var showPlusMenu by remember { mutableStateOf(false) }
    var showAddCollectionDialog by remember { mutableStateOf(false) }

    // Scroll state: hides BottomNavBar when scrolling down, reveals when scrolling up
    var isNavBarScrolledVisible by rememberSaveable { mutableStateOf(true) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            private var accumulatedDelta = 0f

            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val deltaY = available.y
                if (deltaY < -1f) {
                    if (accumulatedDelta > 0f) accumulatedDelta = 0f
                    accumulatedDelta += deltaY
                    if (accumulatedDelta < -25f) {
                        isNavBarScrolledVisible = false
                    }
                } else if (deltaY > 1f) {
                    if (accumulatedDelta < 0f) accumulatedDelta = 0f
                    accumulatedDelta += deltaY
                    if (accumulatedDelta > 20f) {
                        isNavBarScrolledVisible = true
                    }
                }
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (available.y > 0f) {
                    isNavBarScrolledVisible = true
                    accumulatedDelta = 0f
                }
                return Offset.Zero
            }
        }
    }

    // Synchronize activeTab in ViewModel and reset scroll visibility when destination changes
    LaunchedEffect(currentRoute) {
        isNavBarScrolledVisible = true
        getTabForRoute(currentRoute)?.let { tab ->
            viewModel.setActiveTab(tab)
        }
    }

    // Dismiss plus menu when navigating away from tabs where bottom bar is visible
    LaunchedEffect(isBottomBarVisible) {
        if (!isBottomBarVisible) {
            showPlusMenu = false
        }
    }

    // Listen for forced logout event (e.g., when the account is deleted on the server)
    LaunchedEffect(viewModel, navigator) {
        viewModel.userDeletedEvent.collect {
            navigator.logout(context)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection)
    ) {
        // Persistent tab layer (below NavDisplay). Wrapped so its zIndex values stay scoped inside it.
        Box(modifier = Modifier.fillMaxSize()) {
            TabRoutes.forEach { route ->
                if (route in shownTabs) {
                    key(route) {
                        TabLayer(active = route == activeTabRoute) {
                            when (route) {
                                AppRoute.Dashboard -> DashboardScreen(
                                    viewModel = viewModel,
                                    isOnline = isOnline,
                                    onNavigateToAddTransaction = { navigator.navigateToAddTransaction() },
                                    onNavigateToCollectionTransactions = { collectionId, _ ->
                                        navigator.navigateToCollectionTransactions(collectionId)
                                    },
                                    onLogout = { navigator.logout(coroutineScope, context) },
                                    onProfileClick = { navigator.navigateToProfile() },
                                    onNavigateToSearch = { navigator.navigateToSearch() },
                                    onNavigateToInsights = { navigator.navigateToInsights() },
                                    onNavigateToCollections = { navigator.navigateToCollections() }
                                )

                                AppRoute.Insights -> InsightsScreen(
                                    viewModel = viewModel,
                                    isOnline = isOnline,
                                    onBack = { navigator.navigateBack() },
                                    onNavigateToSearch = { navigator.navigateToSearch() },
                                    onProfileClick = { navigator.navigateToProfile() }
                                )

                                AppRoute.Collections -> CollectionsScreen(
                                    viewModel = viewModel,
                                    isOnline = isOnline,
                                    onNavigateToCollectionTransactions = { collectionId, _ ->
                                        navigator.navigateToCollectionTransactions(collectionId)
                                    },
                                    onBack = { navigator.navigateBack() },
                                    onNavigateToSearch = { navigator.navigateToSearch() },
                                    onProfileClick = { navigator.navigateToProfile() },
                                    onNavigateToAddTransaction = { navigator.navigateToAddTransaction() }
                                )

                                else -> Unit
                            }
                        }
                    }
                }
            }
        }

        NavDisplay(
            backStack = navigator.backStack,
            modifier = Modifier.fillMaxSize(),
            onBack = { navigator.navigateBack() },
            transitionSpec = NavigationTransitions.transitionSpec,
            popTransitionSpec = NavigationTransitions.popTransitionSpec,
            predictivePopTransitionSpec = NavigationTransitions.predictivePopTransitionSpec,
            entryProvider = entryProvider {
                entry<AppRoute.Loader> {
                    LoaderScreen(
                        onNavigateToDashboard = { navigator.navigateToDashboard() },
                        onNavigateToSignIn = { navigator.navigateToSignIn() },
                        onNavigateToOnboarding = { navigator.navigateToOnboarding() }
                    )
                }

                entry<AppRoute.Onboarding> {
                    OnboardingScreen(
                        onNavigateToSignIn = { navigator.navigateToSignIn() }
                    )
                }

                entry<AppRoute.SignIn> {
                    SignInScreen(
                        viewModel = viewModel,
                        isOnline = isOnline,
                        onNavigateToDashboard = { navigator.navigateToDashboard() }
                    )
                }

                // Tabs are rendered by the persistent tab layer; these are empty placeholders.
                entry<AppRoute.Dashboard> { }
                entry<AppRoute.Insights> { }
                entry<AppRoute.Collections> { }

                entry<AppRoute.CollectionTransactions> { route ->
                    CollectionTransactionScreen(
                        viewModel = viewModel,
                        collectionId = route.collectionId,
                        isOnline = isOnline,
                        onNavigateToAddTransaction = { colId ->
                            navigator.navigateToAddTransaction(colId)
                        },
                        onBack = { navigator.navigateBack() }
                    )
                }

                entry<AppRoute.AddTransaction> { route ->
                    AddTransactionScreen(
                        viewModel = viewModel,
                        initialCollectionId = route.collectionId,
                        onBack = { navigator.navigateBack() }
                    )
                }

                entry<AppRoute.Profile> {
                    ProfileScreen(
                        isActive = navigator.currentRoute == AppRoute.Profile,
                        onBack = { navigator.navigateBack() },
                        onLogout = { navigator.logout(coroutineScope, context) }
                    )
                }

                entry<AppRoute.Search> {
                    SearchScreen(
                        viewModel = viewModel,
                        onBack = { navigator.navigateBack() },
                        onNavigateToCollectionTransactions = { collectionId ->
                            navigator.navigateToCollectionTransactions(collectionId)
                        }
                    )
                }
            }
        )

        // Centralized bottom navigation bar with destination-based visibility and scroll-hide/show behavior
        AnimatedVisibility(
            visible = isBottomBarVisible && (isNavBarScrolledVisible || showPlusMenu),
            enter = slideInVertically(
                initialOffsetY = { it * 2 },
                animationSpec = tween(280, easing = CubicBezierEasing(0.32f, 0.72f, 0f, 1f))
            ) + fadeIn(tween(160)),
            exit = slideOutVertically(
                targetOffsetY = { it * 2 },
                animationSpec = tween(220, easing = FastOutLinearInEasing)
            ) + fadeOut(tween(120)),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .zIndex(2f)
                .navigationBarsPadding()
        ) {
            BottomNavBar(
                activeTab = activeTab,
                onTabSelected = { tab ->
                    when (tab) {
                        "Home", "Dashboard", "Transactions" -> navigator.navigateToTab(AppRoute.Dashboard)
                        "Analysis", "Insights" -> navigator.navigateToTab(AppRoute.Insights)
                        "Collection", "Collections" -> navigator.navigateToTab(AppRoute.Collections)
                    }
                },
                showPlusMenu = showPlusMenu,
                onPlusClick = { showPlusMenu = !showPlusMenu },
                isDark = isDark
            )
        }
    }

    if (showPlusMenu) {
        PlusMenuDialog(
            onDismiss = { showPlusMenu = false },
            onAddTransaction = {
                showPlusMenu = false
                navigator.navigateToAddTransaction()
            },
            onAddCollection = {
                showPlusMenu = false
                showAddCollectionDialog = true
            }
        )
    }

    if (showAddCollectionDialog) {
        AddCollectionBottomSheet(
            onDismiss = { showAddCollectionDialog = false },
            onConfirm = { name, color, icon, budget ->
                viewModel.addCollection(name, color, icon, budget)
                showAddCollectionDialog = false
            }
        )
    }
}