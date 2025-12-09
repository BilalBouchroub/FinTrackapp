package bilal.com.fintrack.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bilal.com.fintrack.data.local.entities.Transaction
import bilal.com.fintrack.data.repository.BudgetRepository
import bilal.com.fintrack.data.repository.TransactionRepository
import kotlinx.coroutines.flow.*
import java.util.*

data class HomeUiState(
    val recentTransactions: List<Transaction> = emptyList(),
    val totalBalance: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val totalIncome: Double = 0.0,
    val expenseByCategories: Map<Long, Double> = emptyMap(),
    val categories: List<bilal.com.fintrack.data.local.entities.Category> = emptyList(),
    val selectedDate: Long? = null,
    val filteredTransactions: List<Transaction> = emptyList(),
    val dailyExpenses: Double = 0.0,
    val dailyIncome: Double = 0.0
)

class HomeViewModel(
    private val transactionRepository: TransactionRepository,
    private val budgetRepository: BudgetRepository,
    private val categoryRepository: bilal.com.fintrack.data.repository.CategoryRepository
) : ViewModel() {

    private val _selectedDate = MutableStateFlow<Long?>(null)
    
    val uiState: StateFlow<HomeUiState> = combine(
        transactionRepository.allTransactions,
        transactionRepository.totalIncome,
        transactionRepository.totalExpenses,
        categoryRepository.allCategories,
        _selectedDate
    ) { transactions, income, expenses, categories, selectedDate ->
        val recent = transactions.sortedByDescending { it.date }
        val balance = (income ?: 0.0) - (expenses ?: 0.0)
        
        // Calculate expense by category for pie chart
        val expenseMap = transactions
            .filter { it.type == bilal.com.fintrack.data.local.entities.TransactionType.EXPENSE }
            .groupBy { it.categoryId }
            .mapValues { entry -> entry.value.sumOf { it.amount } }

        // Filter transactions by selected date
        val filtered = if (selectedDate != null) {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = selectedDate
            val selectedDay = calendar.get(Calendar.DAY_OF_MONTH)
            val selectedMonth = calendar.get(Calendar.MONTH)
            val selectedYear = calendar.get(Calendar.YEAR)
            
            transactions.filter { transaction ->
                calendar.timeInMillis = transaction.date
                calendar.get(Calendar.DAY_OF_MONTH) == selectedDay &&
                calendar.get(Calendar.MONTH) == selectedMonth &&
                calendar.get(Calendar.YEAR) == selectedYear
            }.sortedByDescending { it.date }
        } else {
            recent
        }
        
        // Calculate daily totals for selected date
        val dailyExpenses = filtered
            .filter { it.type == bilal.com.fintrack.data.local.entities.TransactionType.EXPENSE }
            .sumOf { it.amount }
        
        val dailyIncome = filtered
            .filter { it.type == bilal.com.fintrack.data.local.entities.TransactionType.INCOME }
            .sumOf { it.amount }

        HomeUiState(
            recentTransactions = recent,
            totalBalance = balance,
            totalExpenses = expenses ?: 0.0,
            totalIncome = income ?: 0.0,
            expenseByCategories = expenseMap,
            categories = categories,
            selectedDate = selectedDate,
            filteredTransactions = filtered,
            dailyExpenses = dailyExpenses,
            dailyIncome = dailyIncome
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )
    
    fun selectDate(dateMillis: Long?) {
        _selectedDate.value = dateMillis
    }
    
    fun clearDateFilter() {
        _selectedDate.value = null
    }
}
