package bilal.com.fintrack.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import bilal.com.fintrack.data.local.entities.Budget
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets WHERE userId = :userId")
    fun getAllBudgets(userId: String): Flow<List<Budget>>

    @Query("SELECT * FROM budgets WHERE userId = :userId AND categoryId = :categoryId")
    suspend fun getBudgetByCategory(userId: String, categoryId: Long): Budget?
    
    @Query("SELECT * FROM budgets WHERE userId = :userId AND categoryId IS NULL")
    suspend fun getGlobalBudget(userId: String): Budget?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: Budget)

    @Update
    suspend fun updateBudget(budget: Budget)

    @Delete
    suspend fun deleteBudget(budget: Budget)
}
