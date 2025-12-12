package bilal.com.fintrack.ui.screens.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bilal.com.fintrack.data.local.entities.Budget
import bilal.com.fintrack.data.local.entities.Category
import bilal.com.fintrack.data.local.entities.Transaction
import bilal.com.fintrack.data.local.entities.TransactionType
import bilal.com.fintrack.data.repository.CategoryRepository
import bilal.com.fintrack.data.repository.SyncTransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TransactionsUiState(
    val transactions: List<Transaction> = emptyList(),
    val categories: List<Category> = emptyList(),
    val budgets: List<Budget> = emptyList(),
    val categoryExpenses: Map<Long, Double> = emptyMap(),
    val isLoading: Boolean = false
)

class TransactionViewModel(
    private val transactionRepository: SyncTransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val budgetRepository: bilal.com.fintrack.data.repository.BudgetRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _filterType = MutableStateFlow<TransactionType?>(null)
    val filterType = _filterType.asStateFlow()

    val uiState: StateFlow<TransactionsUiState> = combine(
        transactionRepository.allTransactions,
        categoryRepository.allCategories,
        budgetRepository.allBudgets,
        _searchQuery,
        _filterType
    ) { transactions, categories, budgets, query, type ->
        var filtered = transactions
        if (query.isNotEmpty()) {
            filtered = filtered.filter { 
                it.notes?.contains(query, ignoreCase = true) == true || 
                categories.find { c -> c.id == it.categoryId }?.name?.contains(query, ignoreCase = true) == true
            }
        }
        if (type != null) {
            filtered = filtered.filter { it.type == type }
        }
        
        // Calculate expenses per category
        val expenseMap = transactions
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.categoryId }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
        
        TransactionsUiState(
            transactions = filtered,
            categories = categories,
            budgets = budgets,
            categoryExpenses = expenseMap
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TransactionsUiState()
    )

    fun addTransaction(transaction: Transaction) {
        viewModelScope.launch {
            transactionRepository.insertTransaction(transaction)
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            transactionRepository.deleteTransaction(transaction)
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateFilterType(type: TransactionType?) {
        _filterType.value = type
    }
}
