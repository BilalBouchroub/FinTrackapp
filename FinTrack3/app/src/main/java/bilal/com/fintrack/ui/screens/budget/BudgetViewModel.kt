package bilal.com.fintrack.ui.screens.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bilal.com.fintrack.data.local.entities.Budget
import bilal.com.fintrack.data.local.entities.BudgetPeriod
import bilal.com.fintrack.data.local.entities.Category
import bilal.com.fintrack.data.repository.BudgetRepository
import bilal.com.fintrack.data.repository.CategoryRepository
import bilal.com.fintrack.data.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class BudgetUiState(
    val budgets: List<Budget> = emptyList(),
    val categories: List<Category> = emptyList(),
    val categoryExpenses: Map<Long, Double> = emptyMap()
)

class BudgetViewModel(
    private val budgetRepository: BudgetRepository,
    private val categoryRepository: CategoryRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    val uiState: StateFlow<BudgetUiState> = combine(
        budgetRepository.allBudgets,
        categoryRepository.allCategories,
        transactionRepository.allTransactions
    ) { budgets, categories, transactions ->
        // Calculate expenses per category
        val expenseMap = transactions
            .filter { it.type == bilal.com.fintrack.data.local.entities.TransactionType.EXPENSE }
            .groupBy { it.categoryId }
            .mapValues { entry -> entry.value.sumOf { it.amount } }

        BudgetUiState(
            budgets = budgets,
            categories = categories,
            categoryExpenses = expenseMap
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BudgetUiState()
    )

    fun addBudget(budget: Budget) {
        viewModelScope.launch {
            budgetRepository.insertBudget(budget)
        }
    }

    fun updateBudget(budget: Budget) {
        viewModelScope.launch {
            budgetRepository.updateBudget(budget)
        }
    }

    fun deleteBudget(budget: Budget) {
        viewModelScope.launch {
            budgetRepository.deleteBudget(budget)
        }
    }
}
