package bilal.com.fintrack.ui.screens.statistics

import android.content.Context
import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bilal.com.fintrack.data.local.entities.TransactionType
import bilal.com.fintrack.data.repository.BudgetRepository
import bilal.com.fintrack.data.repository.CategoryRepository
import bilal.com.fintrack.data.repository.TransactionRepository
import bilal.com.fintrack.utils.NotificationService
import bilal.com.fintrack.utils.ReportGenerator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

data class CategoryStat(
    val categoryId: Long,
    val categoryName: String,
    val categoryColor: String,
    val amount: Double,
    val percentage: Float,
    val type: TransactionType
)

data class StatisticsUiState(
    val totalBalance: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val categoryStats: List<CategoryStat> = emptyList(),
    val isExporting: Boolean = false,
    val exportSuccess: Boolean = false,
    val exportError: String? = null
)

class StatisticsViewModel(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val budgetRepository: BudgetRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsUiState())
    
    val uiState: StateFlow<StatisticsUiState> = combine(
        transactionRepository.allTransactions,
        categoryRepository.allCategories,
        _uiState
    ) { transactions, categories, currentState ->
        val income = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val expense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        val balance = income - expense
        
        // Calculate stats per category
        val stats = mutableListOf<CategoryStat>()
        
        // Group transactions by category and type
        val groupedTransactions = transactions.groupBy { it.categoryId to it.type }
        
        groupedTransactions.forEach { (key, txList) ->
            val (categoryId, type) = key
            val category = categories.find { it.id == categoryId }
            val total = txList.sumOf { it.amount }
            
            // Calculate percentage based on type
            val typeTotal = transactions.filter { it.type == type }.sumOf { it.amount }
            val percentage = if (typeTotal > 0) (total / typeTotal * 100).toFloat() else 0f
            
            if (category != null) {
                stats.add(
                    CategoryStat(
                        categoryId = categoryId,
                        categoryName = category.name,
                        categoryColor = category.color,
                        amount = total,
                        percentage = percentage,
                        type = type
                    )
                )
            }
        }
        
        // Sort by amount descending
        val sortedStats = stats.sortedByDescending { it.amount }
        
        currentState.copy(
            totalBalance = balance,
            totalIncome = income,
            totalExpense = expense,
            categoryStats = sortedStats
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StatisticsUiState()
    )
    
    /**
     * Exporter les statistiques en PDF
     */
    fun exportToPdf(context: Context, chartView: View? = null, onComplete: (File?) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExporting = true, exportError = null)
            
            try {
                val transactions = transactionRepository.allTransactions.first()
                val categories = categoryRepository.allCategories.first()
                val currentState = uiState.value
                
                val file = ReportGenerator.generateStatisticsPdf(
                    context = context,
                    transactions = transactions,
                    categories = categories,
                    categoryStats = currentState.categoryStats,
                    totalBalance = currentState.totalBalance,
                    totalIncome = currentState.totalIncome,
                    totalExpense = currentState.totalExpense,
                    chartView = chartView
                )
                
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    exportSuccess = file != null,
                    exportError = if (file == null) "Erreur lors de l'export PDF" else null
                )
                
                onComplete(file)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    exportSuccess = false,
                    exportError = e.message
                )
                onComplete(null)
            }
        }
    }
    
    /**
     * Exporter les statistiques en CSV
     */
    fun exportToCsv(context: Context, onComplete: (File?) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExporting = true, exportError = null)
            
            try {
                val transactions = transactionRepository.allTransactions.first()
                val categories = categoryRepository.allCategories.first()
                val budgets = budgetRepository.allBudgets.first()
                val currentState = uiState.value
                
                val file = ReportGenerator.generateStatisticsCsv(
                    context = context,
                    transactions = transactions,
                    categories = categories,
                    budgets = budgets,
                    totalBalance = currentState.totalBalance
                )
                
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    exportSuccess = file != null,
                    exportError = if (file == null) "Erreur lors de l'export CSV" else null
                )
                
                onComplete(file)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    exportSuccess = false,
                    exportError = e.message
                )
                onComplete(null)
            }
        }
    }
    
    /**
     * Vérifier le solde et envoyer une notification si négatif
     */
    fun checkBalanceAndNotify(context: Context) {
        viewModelScope.launch {
            val currentState = uiState.value
            if (currentState.totalBalance < 0) {
                NotificationService.showNegativeBalanceNotification(context, currentState.totalBalance)
            }
        }
    }
    
    /**
     * Réinitialiser l'état d'export
     */
    fun resetExportState() {
        _uiState.value = _uiState.value.copy(
            exportSuccess = false,
            exportError = null
        )
    }
}
