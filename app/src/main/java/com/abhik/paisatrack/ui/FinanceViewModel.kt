package com.abhik.paisatrack.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.abhik.paisatrack.data.database.AppDatabase
import com.abhik.paisatrack.data.model.CollectionEntity
import com.abhik.paisatrack.data.model.TransactionEntity
import com.abhik.paisatrack.data.repository.FinanceRepository
import com.abhik.paisatrack.data.network.GeminiInsightService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class CollectionSummary(
    val collection: CollectionEntity,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val budgetSpentPercent: Float = 0f,
    val transactionCount: Int = 0
)

data class DailySum(
    val dateString: String,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val timestamp: Long
)

data class FinanceUiState(
    val collections: List<CollectionEntity> = emptyList(),
    val rawTransactions: List<TransactionEntity> = emptyList(),
    val filteredTransactions: List<TransactionEntity> = emptyList(),
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val balance: Double = 0.0,
    val savingsRate: Double = 0.0,
    val collectionSummaries: List<CollectionSummary> = emptyList(),
    val dailyTransactionSums: List<DailySum> = emptyList(),
    val activeCollectionFilter: Long? = null,
    val activeTimeFilter: String = "All", // "All", "Today", "This Week", "This Month"
    val activeTypeFilter: String = "All" // "All", "INCOME", "EXPENSE"
)

class FinanceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FinanceRepository
    
    private val _selectedCollectionFilter = MutableStateFlow<Long?>(null)
    val selectedCollectionFilter = _selectedCollectionFilter.asStateFlow()

    private val _selectedTimeFilter = MutableStateFlow("All")
    val selectedTimeFilter = _selectedTimeFilter.asStateFlow()

    private val _selectedTypeFilter = MutableStateFlow("All")
    val selectedTypeFilter = _selectedTypeFilter.asStateFlow()

    // AI Insight state
    private val _aiInsights = MutableStateFlow("")
    val aiInsights: StateFlow<String> = _aiInsights.asStateFlow()

    private val _aiLoading = MutableStateFlow(false)
    val aiLoading: StateFlow<Boolean> = _aiLoading.asStateFlow()

    // Global active tab state to maintain consistent navigation stack transitions
    private val _activeTab = MutableStateFlow("Transactions")
    val activeTab: StateFlow<String> = _activeTab.asStateFlow()

    fun setActiveTab(tab: String) {
        _activeTab.value = tab
    }

    private val _activeCollectionTab = MutableStateFlow("All")
    val activeCollectionTab: StateFlow<String> = _activeCollectionTab.asStateFlow()

    fun setActiveCollectionTab(tab: String) {
        _activeCollectionTab.value = tab
    }

    init {
        val database = AppDatabase.getDatabase(application)
        repository = FinanceRepository(database.collectionDao(), database.transactionDao())

        // Ensure seeding is completed in a safe thread
        viewModelScope.launch(Dispatchers.IO) {
            repository.ensureDefaultCollectionsPreseeded()
            _aiInsights.value = GeminiInsightService.getFallbackInsights(0.0, 0.0)
        }
    }

    // Combine multiple flows into a clean UI State
    val uiState: StateFlow<FinanceUiState> = combine(
        repository.allCollections,
        repository.allTransactions,
        _selectedCollectionFilter,
        _selectedTimeFilter,
        _selectedTypeFilter
    ) { collections, transactions, collectionFilter, timeFilter, typeFilter ->
        
        // 1. Filter transactions
        val now = System.currentTimeMillis()
        val filtered = transactions.filter { tx ->
            val matchesCollection = collectionFilter == null || tx.collectionId == collectionFilter
            val matchesType = typeFilter == "All" || tx.type.uppercase() == typeFilter.uppercase()
            
            val matchesTime = when (timeFilter) {
                "Today" -> {
                    isSameDay(tx.timestamp, now)
                }
                "This Week" -> {
                    isSameWeek(tx.timestamp, now)
                }
                "This Month" -> {
                    isSameMonth(tx.timestamp, now)
                }
                else -> true // "All"
            }
            
            matchesCollection && matchesType && matchesTime
        }

        // 2. Compute totals for active transactions (all transactions, unfiltered)
        var totalInc = 0.0
        var totalExp = 0.0
        transactions.forEach { tx ->
            if (tx.type.uppercase() == "INCOME") {
                totalInc += tx.amount
            } else {
                totalExp += tx.amount
            }
        }
        val currentBalance = totalInc - totalExp
        val currentSavingsRate = if (totalInc > 0.0) (currentBalance / totalInc) else 0.0

        // 3. Compute summaries per collection
        val summaries = collections.map { col ->
            val colTx = transactions.filter { it.collectionId == col.id }
            var incSum = 0.0
            var expSum = 0.0
            colTx.forEach {
                if (it.type.uppercase() == "INCOME") incSum += it.amount else expSum += it.amount
            }
            val percent = if (col.monthlyBudget != null && col.monthlyBudget > 0.0) {
                (expSum / col.monthlyBudget).toFloat()
            } else {
                0f
            }
            CollectionSummary(
                collection = col,
                totalIncome = incSum,
                totalExpense = expSum,
                budgetSpentPercent = percent,
                transactionCount = colTx.size
            )
        }

        // 4. Compute daily sums for graphical charts (last 7 days of transactions)
        val dailySums = computeDailySums(transactions)

        FinanceUiState(
            collections = collections,
            rawTransactions = transactions,
            filteredTransactions = filtered,
            totalIncome = totalInc,
            totalExpense = totalExp,
            balance = currentBalance,
            savingsRate = currentSavingsRate,
            collectionSummaries = summaries,
            dailyTransactionSums = dailySums,
            activeCollectionFilter = collectionFilter,
            activeTimeFilter = timeFilter,
            activeTypeFilter = typeFilter
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FinanceUiState()
    )

    fun addTransaction(description: String, amount: Double, type: String, collectionId: Long, timestamp: Long = System.currentTimeMillis()) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertTransaction(
                TransactionEntity(
                    description = description,
                    amount = amount,
                    type = type.uppercase(),
                    collectionId = collectionId,
                    timestamp = timestamp
                )
            )
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteTransaction(transaction)
        }
    }

    fun addCollection(name: String, hexColor: String, iconName: String, monthlyBudget: Double?) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertCollection(
                CollectionEntity(
                    name = name,
                    hexColor = hexColor,
                    iconName = iconName,
                    monthlyBudget = monthlyBudget
                )
            )
        }
    }

    fun updateCollection(collection: CollectionEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertCollection(collection)
        }
    }

    fun deleteCollection(collection: CollectionEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteCollection(collection)
            // Reset filter if we deleted the currently selected collection
            if (_selectedCollectionFilter.value == collection.id) {
                _selectedCollectionFilter.value = null
            }
        }
    }

    fun setCollectionFilter(collectionId: Long?) {
        _selectedCollectionFilter.value = collectionId
    }

    fun setTimeFilter(timeFilter: String) {
        _selectedTimeFilter.value = timeFilter
    }

    fun setTypeFilter(typeFilter: String) {
        _selectedTypeFilter.value = typeFilter
    }

    // Triggers local or remote Gemini insights based on current states
    fun fetchAiInsights() {
        val state = uiState.value
        if (_aiLoading.value) return
        
        _aiLoading.value = true
        _aiInsights.value = "Analyzing your spending patterns..."

        viewModelScope.launch(Dispatchers.IO) {
            val breakdown = state.collectionSummaries.joinToString("\n") { sum ->
                "- ${sum.collection.name}: Spent $${sum.totalExpense} (Budget: ${sum.collection.monthlyBudget?.let { "$$it" } ?: "No Budget"})"
            }
            val result = GeminiInsightService.getFinancialInsights(
                totalIncome = state.totalIncome,
                totalExpense = state.totalExpense,
                balance = state.balance,
                breakdownText = breakdown
            )
            _aiInsights.value = result
            _aiLoading.value = false
        }
    }

    // Helper Date utilities
    private fun isSameDay(t1: Long, t2: Long): Boolean {
        val cal1 = Calendar.getInstance().apply { timeInMillis = t1 }
        val cal2 = Calendar.getInstance().apply { timeInMillis = t2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun isSameWeek(t1: Long, t2: Long): Boolean {
        val cal1 = Calendar.getInstance().apply { timeInMillis = t1 }
        val cal2 = Calendar.getInstance().apply { timeInMillis = t2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR)
    }

    private fun isSameMonth(t1: Long, t2: Long): Boolean {
        val cal1 = Calendar.getInstance().apply { timeInMillis = t1 }
        val cal2 = Calendar.getInstance().apply { timeInMillis = t2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH)
    }

    private fun computeDailySums(transactions: List<TransactionEntity>): List<DailySum> {
        val cal = Calendar.getInstance()
        val dailyMap = mutableMapOf<String, Pair<Double, Double>>() // Key: DateString, Value: Pair(IncomeSum, ExpenseSum)
        val format = SimpleDateFormat("EEE", Locale.getDefault()) // "Mon", "Tue"
        
        // Pre-populate past 7 days to ensure a complete, ordered list in charts
        val dayList = mutableListOf<DailySum>()
        for (i in 6 downTo 0) {
            val dateCal = Calendar.getInstance()
            dateCal.add(Calendar.DAY_OF_YEAR, -i)
            val dateStr = format.format(dateCal.time)
            val dayKey = getDayKeyId(dateCal.timeInMillis)
            dailyMap[dayKey] = Pair(0.0, 0.0)
            dayList.add(DailySum(dateString = dateStr, totalIncome = 0.0, totalExpense = 0.0, timestamp = dateCal.timeInMillis))
        }

        // Aggregate actual transaction data
        transactions.forEach { tx ->
            val dayKey = getDayKeyId(tx.timestamp)
            if (dailyMap.containsKey(dayKey)) {
                val current = dailyMap[dayKey] ?: Pair(0.0, 0.0)
                val updated = if (tx.type.uppercase() == "INCOME") {
                    Pair(current.first + tx.amount, current.second)
                } else {
                    Pair(current.first, current.second + tx.amount)
                }
                dailyMap[dayKey] = updated
            }
        }

        // Map computed sums back into chronological order
        return dayList.map { item ->
            val dayKey = getDayKeyId(item.timestamp)
            val sums = dailyMap[dayKey] ?: Pair(0.0, 0.0)
            item.copy(totalIncome = sums.first, totalExpense = sums.second)
        }
    }

    private fun getDayKeyId(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun deleteAccountData(onCompleted: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val database = AppDatabase.getDatabase(getApplication())
            database.clearAllTables()
            viewModelScope.launch(Dispatchers.Main) {
                onCompleted()
            }
        }
    }
}
