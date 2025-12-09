package bilal.com.fintrack.ui.screens.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bilal.com.fintrack.data.local.entities.Transaction
import bilal.com.fintrack.data.repository.BudgetRepository
import bilal.com.fintrack.data.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

data class ReportsUiState(
    val transactions: List<Transaction> = emptyList(),
    val totalIncome: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val selectedMonth: Int = Calendar.getInstance().get(Calendar.MONTH),
    val selectedYear: Int = Calendar.getInstance().get(Calendar.YEAR)
)

class ReportsViewModel(
    private val transactionRepository: TransactionRepository,
    private val budgetRepository: BudgetRepository
) : ViewModel() {

    private val _selectedMonth = MutableStateFlow(Calendar.getInstance().get(Calendar.MONTH))
    private val _selectedYear = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))

    val uiState: StateFlow<ReportsUiState> = combine(
        transactionRepository.allTransactions,
        _selectedMonth,
        _selectedYear
    ) { transactions, month, year ->
        val calendar = Calendar.getInstance()
        val filtered = transactions.filter {
            calendar.timeInMillis = it.date
            calendar.get(Calendar.MONTH) == month && calendar.get(Calendar.YEAR) == year
        }
        
        val income = filtered.filter { it.type == bilal.com.fintrack.data.local.entities.TransactionType.INCOME }.sumOf { it.amount }
        val expenses = filtered.filter { it.type == bilal.com.fintrack.data.local.entities.TransactionType.EXPENSE }.sumOf { it.amount }

        ReportsUiState(
            transactions = filtered,
            totalIncome = income,
            totalExpenses = expenses,
            selectedMonth = month,
            selectedYear = year
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ReportsUiState()
    )

    fun updateMonth(month: Int) {
        _selectedMonth.value = month
    }

    fun updateYear(year: Int) {
        _selectedYear.value = year
    }
}
