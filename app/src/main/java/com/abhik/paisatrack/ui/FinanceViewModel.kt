package com.abhik.paisatrack.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.abhik.paisatrack.data.AuthManager
import com.abhik.paisatrack.data.database.AppDatabase
import com.abhik.paisatrack.data.model.CollectionEntity
import com.abhik.paisatrack.data.model.TransactionEntity
import com.abhik.paisatrack.data.repository.FinanceRepository
import com.abhik.paisatrack.data.repository.SyncResult
import com.abhik.paisatrack.data.network.GeminiInsightService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.channels.BufferOverflow
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
    val activeCollectionFilter: String? = null,
    val activeTimeFilter: String = "All",
    val activeTypeFilter: String = "All",
    val activeSortOrder: String = "Newest",
    
    // Separate filters for Collection Detail Screen
    val collActiveTimeFilter: String = "All",
    val collActiveTypeFilter: String = "All",
    val collActiveSortOrder: String = "Newest",
    val isLoading: Boolean = true,
    val isServerError: Boolean = false
)

class FinanceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FinanceRepository
    
    private val _selectedCollectionFilter = MutableStateFlow<String?>(null)
    val selectedCollectionFilter = _selectedCollectionFilter.asStateFlow()

    private val _selectedTimeFilter = MutableStateFlow("All")
    val selectedTimeFilter = _selectedTimeFilter.asStateFlow()

    private val _selectedTypeFilter = MutableStateFlow("All")
    val selectedTypeFilter = _selectedTypeFilter.asStateFlow()

    private val _selectedSortOrder = MutableStateFlow("Newest")
    val selectedSortOrder = _selectedSortOrder.asStateFlow()

    // Dedicated filter states for the Collection Detail screen
    private val _collTimeFilter = MutableStateFlow("All")
    val collTimeFilter = _collTimeFilter.asStateFlow()

    private val _collTypeFilter = MutableStateFlow("All")
    val collTypeFilter = _collTypeFilter.asStateFlow()

    private val _collSortOrder = MutableStateFlow("Newest")
    val collSortOrder = _collSortOrder.asStateFlow()

    // AI Insight state
    private val _aiInsights = MutableStateFlow("")
    val aiInsights: StateFlow<String> = _aiInsights.asStateFlow()

    private val _aiLoading = MutableStateFlow(false)
    val aiLoading: StateFlow<Boolean> = _aiLoading.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isServerError = MutableStateFlow(false)
    val isServerError: StateFlow<Boolean> = _isServerError.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    // Global active tab state to maintain consistent navigation stack transitions
    private val _activeTab = MutableStateFlow("Transactions")
    val activeTab: StateFlow<String> = _activeTab.asStateFlow()

    fun setActiveTab(tab: String) {
        _activeTab.value = tab
    }

    // One-shot event emitted when sync detects the user was deleted from the backend.
    // The UI collects this to force a logout.
    private val _userDeletedEvent = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val userDeletedEvent: SharedFlow<Unit> = _userDeletedEvent.asSharedFlow()

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
            val context = getApplication<Application>().applicationContext
            val userId = AuthManager.getUserId(context)
            if (userId != null) {
                val result = repository.syncFromBackend(userId)
                when (result) {
                    SyncResult.SUCCESS -> { /* data is fresh */ }
                    SyncResult.NETWORK_ERROR -> {
                        _isServerError.value = true
                    }
                    SyncResult.USER_DELETED -> {
                        _userDeletedEvent.tryEmit(Unit)
                        _isLoading.value = false
                        return@launch
                    }
                }
                syncFcmToken(userId)
            } else {
                repository.ensureDefaultCollectionsPreseeded()
            }
            _aiInsights.value = GeminiInsightService.getFallbackInsights(0.0, 0.0)
            _isLoading.value = false
        }
    }

    // Combine multiple flows into a clean UI State
    val uiState: StateFlow<FinanceUiState> = combine(
        repository.allCollections,
        repository.allTransactions,
        _selectedCollectionFilter,
        _selectedTimeFilter,
        _selectedTypeFilter,
        _selectedSortOrder,
        _collTimeFilter,
        _collTypeFilter,
        _collSortOrder,
        _isLoading,
        _isServerError
    ) { args ->
        @Suppress("UNCHECKED_CAST")
        val collections = args[0] as List<CollectionEntity>
        @Suppress("UNCHECKED_CAST")
        val transactions = args[1] as List<TransactionEntity>
        val collectionFilter = args[2] as String?
        val timeFilter = args[3] as String
        val typeFilter = args[4] as String
        val sortOrder = args[5] as String
        val cTime = args[6] as String
        val cType = args[7] as String
        val cSort = args[8] as String
        val isLoading = args[9] as Boolean
        val isServerError = args[10] as Boolean
        
        // 1. Filter transactions for Dashboard
        val now = System.currentTimeMillis()
        val filtered = transactions.filter { tx ->
            val matchesCollection = collectionFilter == null || tx.collectionId == collectionFilter
            val matchesType = typeFilter == "All" || tx.type.uppercase() == typeFilter.uppercase()
            
            val matchesTime = when (timeFilter) {
                "Today" -> isSameDay(tx.timestamp, now)
                "This Week" -> isSameWeek(tx.timestamp, now)
                "This Month" -> isSameMonth(tx.timestamp, now)
                else -> true
            }
            
            matchesCollection && matchesType && matchesTime
        }

        // Apply Dashboard Sorting
        val sorted = if (sortOrder == "Newest") {
            filtered.sortedByDescending { it.timestamp }
        } else {
            filtered.sortedBy { it.timestamp }
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
            filteredTransactions = sorted,
            totalIncome = totalInc,
            totalExpense = totalExp,
            balance = currentBalance,
            savingsRate = currentSavingsRate,
            collectionSummaries = summaries,
            dailyTransactionSums = dailySums,
            activeCollectionFilter = collectionFilter,
            activeTimeFilter = timeFilter,
            activeTypeFilter = typeFilter,
            activeSortOrder = sortOrder,
            collActiveTimeFilter = cTime,
            collActiveTypeFilter = cType,
            collActiveSortOrder = cSort,
            isLoading = isLoading,
            isServerError = isServerError
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FinanceUiState(isLoading = true)
    )

    fun onUserSignedIn(
        googleId: String,
        email: String,
        name: String,
        image: String,
        onComplete: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val response = repository.signupUser(googleId, email, name, image)
            val finalUserId = response?.userId ?: googleId

            AuthManager.setUserSignedIn(
                getApplication(),
                signedIn = true,
                name = name,
                email = email,
                profilePicUrl = image,
                userId = finalUserId
            )

            // Clear any stale local data left over from a different user
            // before attempting migration to prevent duplicate-key conflicts
            repository.clearAllLocalData()

            // Force clear and download database data for backward compatibility
            val result = repository.syncFromBackend(finalUserId)
            when (result) {
                SyncResult.NETWORK_ERROR -> {
                    _isServerError.value = true
                }
                SyncResult.USER_DELETED -> {
                    _userDeletedEvent.tryEmit(Unit)
                    return@launch
                }
                SyncResult.SUCCESS -> { /* data is fresh */ }
            }
            syncFcmToken(finalUserId)

            viewModelScope.launch(Dispatchers.Main) {
                onComplete(finalUserId)
            }
        }
    }

    fun syncFcmToken(userId: String) {
        try {
            com.google.firebase.messaging.FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful && task.result != null) {
                    val token = task.result
                    viewModelScope.launch(Dispatchers.IO) {
                        repository.updatePushTokenRemote(userId, token)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun addTransaction(description: String, amount: Double, type: String, collectionId: String, timestamp: Long = System.currentTimeMillis()) {
        viewModelScope.launch(Dispatchers.IO) {
            val transaction = TransactionEntity(
                description = description,
                amount = amount,
                type = type.uppercase(),
                collectionId = collectionId,
                timestamp = timestamp
            )
            repository.insertTransaction(transaction)

            val userId = AuthManager.getUserId(getApplication())
            if (userId != null) {
                repository.addTransactionRemote(userId, transaction)
            }
        }
    }

    fun updateTransaction(id: String, description: String, amount: Double, type: String, collectionId: String, timestamp: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val collections = repository.allCollections.first()
            val collectionName = collections.find { it.id == collectionId }?.name ?: "General"
            
            val updatedTransaction = TransactionEntity(
                id = id,
                description = description,
                amount = amount,
                type = type.uppercase(),
                collectionId = collectionId,
                notes = collectionName,
                timestamp = timestamp
            )
            repository.insertTransaction(updatedTransaction)

            val userId = AuthManager.getUserId(getApplication())
            if (userId != null) {
                repository.updateTransactionRemote(userId, updatedTransaction)
            }
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteTransaction(transaction)

            val userId = AuthManager.getUserId(getApplication())
            if (userId != null) {
                repository.deleteTransactionRemote(userId, transaction.id)
            }
        }
    }

    fun addCollection(name: String, hexColor: String, iconName: String, monthlyBudget: Double?) {
        viewModelScope.launch(Dispatchers.IO) {
            val collection = CollectionEntity(
                name = name,
                hexColor = hexColor,
                iconName = iconName,
                monthlyBudget = monthlyBudget
            )
            repository.insertCollection(collection)

            val userId = AuthManager.getUserId(getApplication())
            if (userId != null) {
                repository.addCollectionRemote(userId, collection)
            }
        }
    }

    fun updateCollection(collection: CollectionEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertCollection(collection)
            // Wait, does the backend support update? Yes, PUT /api/collections/{collectionId}
            val userId = AuthManager.getUserId(getApplication())
            if (userId != null) {
                try {
                    com.abhik.paisatrack.data.network.ApiClient.api.updateCollection(
                        collection.id,
                        com.abhik.paisatrack.data.network.UpdateCollectionRequest(
                            title = collection.name,
                            color = collection.hexColor,
                            icon = collection.iconName
                        )
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun deleteCollection(collection: CollectionEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteCollection(collection)

            val userId = AuthManager.getUserId(getApplication())
            if (userId != null) {
                repository.deleteCollectionRemote(userId, collection.id)
            }

            // Reset filter if we deleted the currently selected collection
            if (_selectedCollectionFilter.value == collection.id) {
                _selectedCollectionFilter.value = null
            }
        }
    }

    fun setCollectionFilter(collectionId: String?) {
        _selectedCollectionFilter.value = collectionId
    }

    fun setTimeFilter(timeFilter: String) {
        _selectedTimeFilter.value = timeFilter
    }

    fun setTypeFilter(typeFilter: String) {
        _selectedTypeFilter.value = typeFilter
    }

    fun setSortOrder(order: String) {
        _selectedSortOrder.value = order
    }

    // Collection Screen filter setters
    fun setCollectionTimeFilter(filter: String) {
        _collTimeFilter.value = filter
    }

    fun setCollectionTypeFilter(filter: String) {
        _collTypeFilter.value = filter
    }

    fun setCollectionSortOrder(order: String) {
        _collSortOrder.value = order
    }

    fun retrySync() {
        viewModelScope.launch(Dispatchers.IO) {
            _isServerError.value = false
            _isLoading.value = true
            val context = getApplication<Application>().applicationContext
            val userId = AuthManager.getUserId(context)
            if (userId != null) {
                val result = repository.syncFromBackend(userId)
                when (result) {
                    SyncResult.NETWORK_ERROR -> {
                        _isServerError.value = true
                    }
                    SyncResult.USER_DELETED -> {
                        _userDeletedEvent.tryEmit(Unit)
                        _isLoading.value = false
                        return@launch
                    }
                    SyncResult.SUCCESS -> { /* data is fresh */ }
                }
            }
            _isLoading.value = false
        }
    }

    /**
     * Re-sync data from backend. Call this when the app resumes to pick up
     * any changes made externally (e.g. data deleted from admin panel).
     * If the user was deleted from the backend, emits userDeletedEvent.
     */
    fun refreshFromBackend(noCache: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            _isRefreshing.value = true
            try {
                val context = getApplication<Application>().applicationContext
                val userId = AuthManager.getUserId(context) ?: return@launch
                val result = repository.syncFromBackend(userId, noCache)
                when (result) {
                    SyncResult.USER_DELETED -> {
                        _userDeletedEvent.tryEmit(Unit)
                    }
                    SyncResult.NETWORK_ERROR -> {
                        // Silently ignore on resume — don't show error screen
                    }
                    SyncResult.SUCCESS -> { /* data is fresh */ }
                }
            } finally {
                _isRefreshing.value = false
            }
        }
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

    fun signOut(context: Context, onCompleted: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearAllLocalData()
            AuthManager.signOut(context)
            viewModelScope.launch(Dispatchers.Main) {
                onCompleted()
            }
        }
    }

    fun deleteAccountData(onCompleted: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val userId = AuthManager.getUserId(getApplication())
            if (userId != null) {
                repository.deleteUserRemote(userId)
            }
            // Wipe all local Room tables
            val database = AppDatabase.getDatabase(getApplication())
            database.clearAllTables()
            // Also clear auth state so stale userId doesn't trigger re-sync
            AuthManager.signOut(getApplication())
            viewModelScope.launch(Dispatchers.Main) {
                onCompleted()
            }
        }
    }
}
