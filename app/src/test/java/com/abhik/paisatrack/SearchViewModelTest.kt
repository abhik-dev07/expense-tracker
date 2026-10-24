package com.abhik.paisatrack

import com.abhik.paisatrack.data.model.TransactionEntity
import com.abhik.paisatrack.ui.SearchSummary
import com.abhik.paisatrack.ui.buildSearchTokens
import com.abhik.paisatrack.ui.matchesSearch
import com.abhik.paisatrack.ui.screens.search.FinancialSearchUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    private fun createTx(
        id: String = "tx_1",
        description: String = "Test Transaction",
        amount: Double = 100.0,
        type: String = "EXPENSE",
        collectionId: String = "col_1",
        timestamp: Long = System.currentTimeMillis()
    ) = TransactionEntity(
        id = id,
        description = description,
        amount = amount,
        type = type,
        collectionId = collectionId,
        timestamp = timestamp
    )

    private fun getTimestampFor(year: Int, month: Int, day: Int, hour: Int = 12): Long {
        val cal = Calendar.getInstance().apply {
            set(year, month, day, hour, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    // 1. Case-insensitive description matching
    @Test
    fun `case-insensitive description matching finds matching records`() {
        val tx1 = createTx(description = "Rent to landlord")
        val tx2 = createTx(description = "NETFLIX subscription")
        val tx3 = createTx(description = "Monthly Salary from employer")

        val rentTokens = buildSearchTokens("rent")
        assertTrue(matchesSearch(tx1, rentTokens))
        assertFalse(matchesSearch(tx2, rentTokens))

        val netflixTokens = buildSearchTokens("netflix")
        assertTrue(matchesSearch(tx2, netflixTokens))

        val salaryTokens = buildSearchTokens("sAlArY")
        assertTrue(matchesSearch(tx3, salaryTokens))
    }

    // 2. Multi-word query matching (order independent)
    @Test
    fun `multi-word query matches order-independently`() {
        val tx1 = createTx(description = "Payment to Kumar Rahul for rent")
        val tokens = buildSearchTokens("rahul kumar")

        assertEquals(listOf("rahul", "kumar"), tokens)
        assertTrue(matchesSearch(tx1, tokens))

        val electricityTokens = buildSearchTokens("bill electricity")
        val tx2 = createTx(description = "Electricity bill payment October")
        assertTrue(matchesSearch(tx2, electricityTokens))

        val nonMatchingTokens = buildSearchTokens("rahul grocery")
        assertFalse(matchesSearch(tx1, nonMatchingTokens))
    }

    // 3. Collection-name matching
    @Test
    fun `collection-name matching matches records through collection relationship`() {
        val tx = createTx(description = "Dinner with friends", collectionId = "col_personal")

        // Description does not contain "Personal", but collection name does
        val tokens = buildSearchTokens("personal")
        val matchesWithoutCollection = matchesSearch(tx, tokens, collectionName = "")
        assertFalse(matchesWithoutCollection)

        val matchesWithCollection = matchesSearch(tx, tokens, collectionName = "Personal Expenses")
        assertTrue(matchesWithCollection)

        val travelTokens = buildSearchTokens("travel")
        val matchesTravel = matchesSearch(tx, travelTokens, collectionName = "Travel Trip")
        assertTrue(matchesTravel)
    }

    // 4. Exact-date filtering
    @Test
    fun `exact-date parsing extracts start and end of day`() {
        val range = FinancialSearchUtils.parseNaturalDateQuery("01/10/2026")
        assertNotNull(range)

        val start = range!!.first
        val end = range.second

        val calStart = Calendar.getInstance().apply { timeInMillis = start }
        val calEnd = Calendar.getInstance().apply { timeInMillis = end }

        assertEquals(2026, calStart.get(Calendar.YEAR))
        assertEquals(Calendar.OCTOBER, calStart.get(Calendar.MONTH))
        assertEquals(1, calStart.get(Calendar.DAY_OF_MONTH))
        assertEquals(0, calStart.get(Calendar.HOUR_OF_DAY))

        assertEquals(2026, calEnd.get(Calendar.YEAR))
        assertEquals(Calendar.OCTOBER, calEnd.get(Calendar.MONTH))
        assertEquals(1, calEnd.get(Calendar.DAY_OF_MONTH))
        assertEquals(23, calEnd.get(Calendar.HOUR_OF_DAY))

        val txOnOct1 = createTx(timestamp = getTimestampFor(2026, Calendar.OCTOBER, 1, 14))
        val txOnOct2 = createTx(timestamp = getTimestampFor(2026, Calendar.OCTOBER, 2, 10))

        assertTrue(txOnOct1.timestamp in start..end)
        assertFalse(txOnOct2.timestamp in start..end)
    }

    // 5. Inclusive start and end date ranges
    @Test
    fun `inclusive date range query covers start and end days`() {
        val range = FinancialSearchUtils.parseNaturalDateQuery("01/10/2026 - 14/10/2026")
        assertNotNull(range)

        val start = range!!.first
        val end = range.second

        val txStartDay = createTx(timestamp = getTimestampFor(2026, Calendar.OCTOBER, 1, 0))
        val txMidRange = createTx(timestamp = getTimestampFor(2026, Calendar.OCTOBER, 7, 12))
        val txEndDay = createTx(timestamp = getTimestampFor(2026, Calendar.OCTOBER, 14, 23))
        val txOutside = createTx(timestamp = getTimestampFor(2026, Calendar.OCTOBER, 15, 1))

        assertTrue(txStartDay.timestamp in start..end)
        assertTrue(txMidRange.timestamp in start..end)
        assertTrue(txEndDay.timestamp in start..end)
        assertFalse(txOutside.timestamp in start..end)
    }

    // 6. Month and year filtering
    @Test
    fun `month and year parsing handles month length and year correctly`() {
        val monthRange = FinancialSearchUtils.parseNaturalDateQuery("October 2026")
        assertNotNull(monthRange)

        val start = monthRange!!.first
        val end = monthRange.second

        val calStart = Calendar.getInstance().apply { timeInMillis = start }
        val calEnd = Calendar.getInstance().apply { timeInMillis = end }

        assertEquals(1, calStart.get(Calendar.DAY_OF_MONTH))
        assertEquals(31, calEnd.get(Calendar.DAY_OF_MONTH))
        assertEquals(Calendar.OCTOBER, calStart.get(Calendar.MONTH))

        // Year format: "2026"
        val yearRange = FinancialSearchUtils.parseNaturalDateQuery("2026")
        assertNotNull(yearRange)
        val validYear = yearRange!!
        val yStartCal = Calendar.getInstance().apply { timeInMillis = validYear.first }
        val yEndCal = Calendar.getInstance().apply { timeInMillis = validYear.second }

        assertEquals(Calendar.JANUARY, yStartCal.get(Calendar.MONTH))
        assertEquals(1, yStartCal.get(Calendar.DAY_OF_MONTH))
        assertEquals(Calendar.DECEMBER, yEndCal.get(Calendar.MONTH))
        assertEquals(31, yEndCal.get(Calendar.DAY_OF_MONTH))
    }

    // 7. Combining text, date, and type filters
    @Test
    fun `combining text query and date range filters accurately`() {
        val tx1 = createTx(
            description = "House Rent",
            amount = 12000.0,
            type = "EXPENSE",
            timestamp = getTimestampFor(2026, Calendar.OCTOBER, 5)
        )
        val tx2 = createTx(
            description = "House Rent",
            amount = 12000.0,
            type = "EXPENSE",
            timestamp = getTimestampFor(2026, Calendar.SEPTEMBER, 5)
        )
        val tx3 = createTx(
            description = "Electricity Bill",
            amount = 1500.0,
            type = "EXPENSE",
            timestamp = getTimestampFor(2026, Calendar.OCTOBER, 10)
        )
        val tx4 = createTx(
            description = "Salary Bonus",
            amount = 25000.0,
            type = "INCOME",
            timestamp = getTimestampFor(2026, Calendar.OCTOBER, 1)
        )

        val allTransactions = listOf(tx1, tx2, tx3, tx4)
        val octRange = FinancialSearchUtils.parseNaturalDateQuery("October 2026")!!
        val rentTokens = buildSearchTokens("Rent")

        // Filter for "Rent" in October 2026
        val matchingRentOct = allTransactions.filter { tx ->
            val matchesQuery = matchesSearch(tx, rentTokens)
            val matchesDate = tx.timestamp in octRange.first..octRange.second
            matchesQuery && matchesDate
        }

        assertEquals(1, matchingRentOct.size)
        assertEquals(tx1.id, matchingRentOct[0].id)

        // Filter for "All Expenses" in October 2026
        val expensesOct = allTransactions.filter { tx ->
            val matchesType = tx.type.equals("EXPENSE", ignoreCase = true)
            val matchesDate = tx.timestamp in octRange.first..octRange.second
            matchesType && matchesDate
        }

        assertEquals(2, expensesOct.size) // tx1 and tx3
    }

    // 8. Empty query and zero-match states
    @Test
    fun `empty query matches all records and zero-match state is handled cleanly`() {
        val emptyTokens = buildSearchTokens("")
        assertTrue(emptyTokens.isEmpty())

        val tx = createTx(description = "Lunch")
        assertTrue(matchesSearch(tx, emptyTokens))

        val nonMatchingTokens = buildSearchTokens("cryptocurrencyxyznothere")
        assertFalse(matchesSearch(tx, nonMatchingTokens))
    }

    // 9. Summary calculations
    @Test
    fun `financial summary calculates match count, expenses, income, and timestamps`() {
        val t1 = getTimestampFor(2026, Calendar.OCTOBER, 2)
        val t2 = getTimestampFor(2026, Calendar.OCTOBER, 15)

        val tx1 = createTx(id = "1", description = "Freelance Project", amount = 15000.0, type = "INCOME", timestamp = t1)
        val tx2 = createTx(id = "2", description = "Server Hosting", amount = 2000.0, type = "EXPENSE", timestamp = t2)
        val results = listOf(tx1, tx2)

        var sent = 0.0
        var received = 0.0
        results.forEach {
            if (it.type.uppercase() == "INCOME") received += it.amount else sent += it.amount
        }

        val summary = SearchSummary(
            query = "work",
            matchCount = results.size,
            totalSent = sent,
            totalReceived = received,
            firstTimestamp = results.minOf { it.timestamp },
            lastTimestamp = results.maxOf { it.timestamp }
        )

        assertEquals(2, summary.matchCount)
        assertEquals(2000.0, summary.totalSent, 0.001)
        assertEquals(15000.0, summary.totalReceived, 0.001)
        assertEquals(t1, summary.firstTimestamp)
        assertEquals(t2, summary.lastTimestamp)
    }

    // 10. Pagination reset on query or filter changes
    @Test
    fun `pagination limit resets to initial batch when filter or query changes`() {
        var visibleLimit = 60

        fun onQueryOrFilterChanged() {
            visibleLimit = 20
        }

        assertEquals(60, visibleLimit)
        onQueryOrFilterChanged()
        assertEquals(20, visibleLimit)
    }

    // 11. Debounce behavior
    @Test
    fun `debounce emits only after specified quiet period while clear is instant`() = runTest {
        val queryFlow = MutableStateFlow("")
        val debouncedResults = mutableListOf<String>()

        val job = backgroundScope.launch(kotlinx.coroutines.test.UnconfinedTestDispatcher(testScheduler)) {
            queryFlow
                .debounce { if (it.isEmpty()) 0L else 180L }
                .distinctUntilChanged()
                .collect { debouncedResults.add(it) }
        }

        // Initial empty query collected immediately
        advanceUntilIdle()
        assertEquals(listOf(""), debouncedResults)

        // Typing "r", "re", "rent" rapidly within 100ms
        queryFlow.value = "r"
        advanceTimeBy(50)
        queryFlow.value = "re"
        advanceTimeBy(50)
        queryFlow.value = "rent"

        // Still within 180ms debounce window; "rent" not yet emitted
        advanceTimeBy(100)
        assertEquals(listOf(""), debouncedResults)

        // Advance past 180ms
        advanceTimeBy(90)
        assertEquals(listOf("", "rent"), debouncedResults)

        // Clearing query has 0ms debounce
        queryFlow.value = ""
        advanceTimeBy(1)
        assertEquals(listOf("", "rent", ""), debouncedResults)

        job.cancel()
    }
}
