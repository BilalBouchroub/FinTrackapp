package bilal.com.fintrack.data.repository

import android.content.Context
import bilal.com.fintrack.data.local.dao.BudgetDao
import bilal.com.fintrack.data.local.entities.Budget
import bilal.com.fintrack.data.remote.RetrofitClient
import bilal.com.fintrack.data.remote.TokenManager
import bilal.com.fintrack.data.remote.models.toDto
import bilal.com.fintrack.data.remote.models.toEntity
import kotlinx.coroutines.flow.Flow

class BudgetRepository(
    private val budgetDao: BudgetDao,
    context: Context
) {
    private val tokenManager = TokenManager(context)
    private val api = RetrofitClient.budgetApi

    val allBudgets: Flow<List<Budget>>
        get() {
            val userId = tokenManager.getUserId() ?: ""
            return budgetDao.getAllBudgets(userId)
        }

    suspend fun insertBudget(budget: Budget) {
        val userId = tokenManager.getUserId() ?: ""
        val budgetWithUser = budget.copy(userId = userId)
        budgetDao.insertBudget(budgetWithUser)
        
        // Sync avec le serveur
        try {
            val token = tokenManager.getBearerToken()
            if (token != null) {
                val dto = budget.toDto("Unknown") 
                api.createBudget(token, dto)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun updateBudget(budget: Budget) {
        budgetDao.updateBudget(budget)
        
        // Sync avec le serveur
        val token = tokenManager.getBearerToken()
        if (token != null && budget.serverId != null) {
            try {
                val dto = budget.toDto("Unknown")
                val response = api.updateBudget(token, budget.serverId, dto)
                if (response.isSuccessful) {
                    android.util.Log.d("BudgetRepo", "Budget mis à jour sur serveur: ${budget.serverId}")
                } else {
                    android.util.Log.w("BudgetRepo", "Échec mise à jour serveur: ${response.code()}")
                }
            } catch (e: Exception) {
                android.util.Log.e("BudgetRepo", "Erreur mise à jour serveur", e)
            }
        } else {
            android.util.Log.d("BudgetRepo", "Budget local uniquement (pas de serverId)")
        }
    }

    suspend fun deleteBudget(budget: Budget) {
        budgetDao.deleteBudget(budget)
        
        // Sync avec le serveur
        val token = tokenManager.getBearerToken()
        if (token != null && budget.serverId != null) {
            try {
                val response = api.deleteBudget(token, budget.serverId)
                if (response.isSuccessful) {
                    android.util.Log.d("BudgetRepo", "Budget supprimé du serveur: ${budget.serverId}")
                } else {
                    android.util.Log.w("BudgetRepo", "Échec suppression serveur: ${response.code()}")
                }
            } catch (e: Exception) {
                android.util.Log.e("BudgetRepo", "Erreur suppression serveur", e)
            }
        } else {
            android.util.Log.d("BudgetRepo", "Budget local uniquement (pas de serverId)")
        }
    }

    suspend fun getBudgetByCategory(categoryId: Long): Budget? {
        val userId = tokenManager.getUserId() ?: ""
        return budgetDao.getBudgetByCategory(userId, categoryId)
    }
    
    suspend fun getGlobalBudget(): Budget? {
        val userId = tokenManager.getUserId() ?: ""
        return budgetDao.getGlobalBudget(userId)
    }
    
    /**
     * Synchroniser les budgets depuis le serveur MongoDB
     */
    suspend fun syncWithServer(): Result<Unit> {
        return try {
            val token = tokenManager.getBearerToken()
            val userId = tokenManager.getUserId() ?: ""
            
            android.util.Log.d("BudgetRepo", "=== SYNC BUDGETS ===")
            android.util.Log.d("BudgetRepo", "Token: ${token != null}, UserId: $userId")
            
            if (token == null) {
                return Result.failure(Exception("Non authentifié"))
            }
            
            val response = api.getAllBudgets(token)
            
            android.util.Log.d("BudgetRepo", "HTTP: ${response.code()}, Success: ${response.body()?.success}")
            android.util.Log.d("BudgetRepo", "Budgets reçus: ${response.body()?.data?.size ?: 0}")
            
            if (response.isSuccessful && response.body()?.success == true) {
                val serverBudgets = response.body()?.data ?: emptyList()
                
                serverBudgets.forEachIndexed { index, b ->
                    android.util.Log.d("BudgetRepo", "  [$index] amount=${b.amount}, categoryId=${b.categoryId}")
                }
                
                // Convertir et insérer
                val localBudgets = serverBudgets.map { it.toEntity(userId) }
                localBudgets.forEach { budget ->
                    try {
                        budgetDao.insertBudget(budget)
                    } catch (e: Exception) {
                        budgetDao.updateBudget(budget)
                    }
                }
                
                android.util.Log.d("BudgetRepo", "Budgets synchronisés: ${localBudgets.size}")
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.body()?.toString() ?: "Erreur sync budgets"))
            }
        } catch (e: Exception) {
            android.util.Log.e("BudgetRepo", "Exception sync: ${e.message}", e)
            Result.failure(e)
        }
    }
}
