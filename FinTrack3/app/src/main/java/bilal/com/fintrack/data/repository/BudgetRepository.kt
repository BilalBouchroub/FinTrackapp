package bilal.com.fintrack.data.repository

import android.content.Context
import bilal.com.fintrack.data.local.dao.BudgetDao
import bilal.com.fintrack.data.local.entities.Budget
import bilal.com.fintrack.data.remote.RetrofitClient
import bilal.com.fintrack.data.remote.TokenManager
import bilal.com.fintrack.data.remote.models.toDto
import kotlinx.coroutines.flow.Flow

class BudgetRepository(
    private val budgetDao: BudgetDao,
    context: Context
) {
    private val tokenManager = TokenManager(context)
    private val api = RetrofitClient.budgetApi

    val allBudgets: Flow<List<Budget>> = budgetDao.getAllBudgets()

    suspend fun insertBudget(budget: Budget) {
        budgetDao.insertBudget(budget)
        
        try {
            val token = tokenManager.getBearerToken()
            if (token != null) {
                // On met un nom factice car on n'a pas accès au nom facilement ici sans requête DB supplémentaire
                val dto = budget.toDto(categoryName = "BudgetSync")
                api.createBudget(token, dto)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun updateBudget(budget: Budget) {
        budgetDao.updateBudget(budget)
    }

    suspend fun deleteBudget(budget: Budget) {
        budgetDao.deleteBudget(budget)
    }

    suspend fun getBudgetByCategory(categoryId: Long): Budget? {
        return budgetDao.getBudgetByCategory(categoryId)
    }

    suspend fun getGlobalBudget(): Budget? {
        return budgetDao.getGlobalBudget()
    }
}
