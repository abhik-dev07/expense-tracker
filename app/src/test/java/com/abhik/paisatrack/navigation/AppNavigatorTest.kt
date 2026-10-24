package com.abhik.paisatrack.navigation

import androidx.compose.runtime.mutableStateListOf
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AppNavigatorTest {

    private lateinit var backStack: NavBackStack<NavKey>
    private lateinit var navigator: AppNavigator

    @Before
    fun setUp() {
        backStack = NavBackStack(mutableStateListOf<NavKey>(AppRoute.Loader))
        navigator = AppNavigator(backStack)
    }

    @Test
    fun `initial destination is Loader and cannot navigate back from root`() {
        assertEquals(AppRoute.Loader, navigator.currentRoute)
        assertEquals(1, backStack.size)
        assertFalse(navigator.canNavigateBack)

        // Attempting to navigate back from root should fail safely
        val result = navigator.navigateBack()
        assertFalse(result)
        assertEquals(1, backStack.size)
        assertEquals(AppRoute.Loader, navigator.currentRoute)
    }

    @Test
    fun `navigateTo pushes new destination and enables back navigation`() {
        navigator.navigateTo(AppRoute.Dashboard)

        assertEquals(2, backStack.size)
        assertEquals(AppRoute.Dashboard, navigator.currentRoute)
        assertTrue(navigator.canNavigateBack)
    }

    @Test
    fun `navigateTo avoids duplicate destinations when tapping same route repeatedly`() {
        navigator.navigateTo(AppRoute.Dashboard)
        assertEquals(2, backStack.size)

        // Repeated tap
        navigator.navigateTo(AppRoute.Dashboard)
        assertEquals(2, backStack.size)
        assertEquals(AppRoute.Dashboard, navigator.currentRoute)
    }

    @Test
    fun `navigateBack pops top destination until reaching root`() {
        navigator.navigateTo(AppRoute.Dashboard)
        navigator.navigateTo(AppRoute.Profile)
        assertEquals(3, backStack.size)
        assertEquals(AppRoute.Profile, navigator.currentRoute)

        val poppedFirst = navigator.navigateBack()
        assertTrue(poppedFirst)
        assertEquals(2, backStack.size)
        assertEquals(AppRoute.Dashboard, navigator.currentRoute)

        val poppedSecond = navigator.navigateBack()
        assertTrue(poppedSecond)
        assertEquals(1, backStack.size)
        assertEquals(AppRoute.Loader, navigator.currentRoute)

        val poppedRoot = navigator.navigateBack()
        assertFalse(poppedRoot)
        assertEquals(1, backStack.size)
    }

    @Test
    fun `replaceRoot clears back stack and sets new destination as root`() {
        navigator.navigateTo(AppRoute.Dashboard)
        navigator.navigateTo(AppRoute.Search)
        assertEquals(3, backStack.size)

        navigator.replaceRoot(AppRoute.SignIn)

        assertEquals(1, backStack.size)
        assertEquals(AppRoute.SignIn, navigator.currentRoute)
        assertFalse(navigator.canNavigateBack)
    }

    @Test
    fun `popTo pops until matching route is reached`() {
        navigator.navigateTo(AppRoute.Dashboard)
        navigator.navigateTo(AppRoute.Search)
        navigator.navigateTo(AppRoute.CollectionTransactions("col_123"))

        assertEquals(4, backStack.size)

        val popped = navigator.popTo(AppRoute.Dashboard, inclusive = false)
        assertTrue(popped)
        assertEquals(2, backStack.size)
        assertEquals(AppRoute.Dashboard, navigator.currentRoute)
    }

    @Test
    fun `typed destination keys with parameters hold correct arguments`() {
        val collectionRoute = AppRoute.CollectionTransactions(collectionId = "savings_01")
        assertEquals("savings_01", collectionRoute.collectionId)

        val addTxRoute = AppRoute.AddTransaction(collectionId = "food_02")
        assertEquals("food_02", addTxRoute.collectionId)

        val defaultAddTxRoute = AppRoute.AddTransaction()
        assertEquals("", defaultAddTxRoute.collectionId)
    }

    @Test
    fun `AppRoute serialization and deserialization preserves destination state`() {
        val json = Json {
            serializersModule = SerializersModule {
                polymorphic(AppRoute::class) {
                    subclass(AppRoute.Loader::class)
                    subclass(AppRoute.Onboarding::class)
                    subclass(AppRoute.SignIn::class)
                    subclass(AppRoute.Dashboard::class)
                    subclass(AppRoute.CollectionTransactions::class)
                    subclass(AppRoute.AddTransaction::class)
                    subclass(AppRoute.Profile::class)
                    subclass(AppRoute.Search::class)
                    subclass(AppRoute.Insights::class)
                    subclass(AppRoute.Collections::class)
                }
            }
        }

        val originalRoute: AppRoute = AppRoute.CollectionTransactions(collectionId = "test_col_99")
        val serialized = json.encodeToString(AppRoute.serializer(), originalRoute)
        val deserialized = json.decodeFromString(AppRoute.serializer(), serialized)

        assertEquals(originalRoute, deserialized)

        val insightsRoute: AppRoute = AppRoute.Insights
        val serializedInsights = json.encodeToString(AppRoute.serializer(), insightsRoute)
        val deserializedInsights = json.decodeFromString(AppRoute.serializer(), serializedInsights)
        assertEquals(insightsRoute, deserializedInsights)

        val collectionsRoute: AppRoute = AppRoute.Collections
        val serializedCollections = json.encodeToString(AppRoute.serializer(), collectionsRoute)
        val deserializedCollections = json.decodeFromString(AppRoute.serializer(), serializedCollections)
        assertEquals(collectionsRoute, deserializedCollections)
    }

    @Test
    fun `navigateToInsights and navigateToCollections push correct routes`() {
        navigator.navigateToInsights()
        assertEquals(AppRoute.Insights, navigator.currentRoute)
        assertEquals(2, backStack.size)

        navigator.navigateToCollections()
        assertEquals(AppRoute.Collections, navigator.currentRoute)
        assertEquals(3, backStack.size)

        assertTrue(navigator.navigateBack())
        assertEquals(AppRoute.Insights, navigator.currentRoute)
    }

    @Test
    fun `isBottomBarVisible returns true for Dashboard, Insights, and Collections, and false for others`() {
        // Visible destinations
        assertTrue(isBottomBarVisible(AppRoute.Dashboard))
        assertTrue(isBottomBarVisible(AppRoute.Insights))
        assertTrue(isBottomBarVisible(AppRoute.Collections))

        // Explicitly hidden destinations
        assertFalse(isBottomBarVisible(AppRoute.AddTransaction()))
        assertFalse(isBottomBarVisible(AppRoute.CollectionTransactions("col_1")))
        assertFalse(isBottomBarVisible(AppRoute.Profile))
        assertFalse(isBottomBarVisible(AppRoute.SignIn))

        // Default hidden destinations
        assertFalse(isBottomBarVisible(AppRoute.Loader))
        assertFalse(isBottomBarVisible(AppRoute.Onboarding))
        assertFalse(isBottomBarVisible(AppRoute.Search))
        assertFalse(isBottomBarVisible(null))
    }

    @Test
    fun `getTabForRoute returns correct tab names for top-level destinations`() {
        assertEquals("Home", getTabForRoute(AppRoute.Dashboard))
        assertEquals("Analysis", getTabForRoute(AppRoute.Insights))
        assertEquals("Collection", getTabForRoute(AppRoute.Collections))

        assertEquals(null, getTabForRoute(AppRoute.AddTransaction()))
        assertEquals(null, getTabForRoute(AppRoute.CollectionTransactions("col_1")))
        assertEquals(null, getTabForRoute(AppRoute.Profile))
        assertEquals(null, getTabForRoute(AppRoute.SignIn))
        assertEquals(null, getTabForRoute(AppRoute.Search))
        assertEquals(null, getTabForRoute(null))
    }

    @Test
    fun `navigateToTab switches tabs and avoids duplicate instances in backstack`() {
        navigator.replaceRoot(AppRoute.Dashboard)
        assertEquals(AppRoute.Dashboard, navigator.currentRoute)
        assertTrue(navigator.isBottomBarVisible)
        assertEquals(1, backStack.size)

        // Navigate to Insights
        navigator.navigateToTab(AppRoute.Insights)
        assertEquals(AppRoute.Insights, navigator.currentRoute)
        assertTrue(navigator.isBottomBarVisible)
        assertEquals(2, backStack.size)

        // Navigate to Collections
        navigator.navigateToTab(AppRoute.Collections)
        assertEquals(AppRoute.Collections, navigator.currentRoute)
        assertTrue(navigator.isBottomBarVisible)
        assertEquals(3, backStack.size)

        // Navigate to Insights again: should pop Collections and return to existing Insights
        navigator.navigateToTab(AppRoute.Insights)
        assertEquals(AppRoute.Insights, navigator.currentRoute)
        assertEquals(2, backStack.size)

        // Navigate to Transactions: should pop Insights and return to Dashboard
        navigator.navigateToTab(AppRoute.Dashboard)
        assertEquals(AppRoute.Dashboard, navigator.currentRoute)
        assertEquals(1, backStack.size)
    }

    @Test
    fun `isBottomBarVisible property on navigator updates with route changes`() {
        navigator.replaceRoot(AppRoute.Dashboard)
        assertTrue(navigator.isBottomBarVisible)

        navigator.navigateTo(AppRoute.AddTransaction())
        assertFalse(navigator.isBottomBarVisible)

        navigator.navigateBack()
        assertTrue(navigator.isBottomBarVisible)

        navigator.navigateTo(AppRoute.Profile)
        assertFalse(navigator.isBottomBarVisible)

        navigator.navigateBack()
        assertTrue(navigator.isBottomBarVisible)
    }

    @Test
    fun `isTopLevelTab correctly identifies peer tabs for default transition`() {
        assertTrue(NavigationTransitions.isTopLevelTab(AppRoute.Dashboard))
        assertTrue(NavigationTransitions.isTopLevelTab(AppRoute.Insights))
        assertTrue(NavigationTransitions.isTopLevelTab(AppRoute.Collections))

        assertFalse(NavigationTransitions.isTopLevelTab(AppRoute.AddTransaction()))
        assertFalse(NavigationTransitions.isTopLevelTab(AppRoute.Profile))
        assertFalse(NavigationTransitions.isTopLevelTab(AppRoute.CollectionTransactions("col_1")))
        assertFalse(NavigationTransitions.isTopLevelTab(AppRoute.SignIn))
        assertFalse(NavigationTransitions.isTopLevelTab(AppRoute.Search))
        assertFalse(NavigationTransitions.isTopLevelTab(AppRoute.Loader))
        assertFalse(NavigationTransitions.isTopLevelTab(null))
    }
}

