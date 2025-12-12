package bilal.com.fintrack.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import bilal.com.fintrack.data.local.entities.Transaction
import bilal.com.fintrack.data.local.entities.TransactionType
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY date DESC")
    fun getAllTransactions(userId: String): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Long): Transaction?

    @Query("SELECT * FROM transactions WHERE userId = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getTransactionsByDateRange(userId: String, startDate: Long, endDate: Long): Flow<List<Transaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction): Long

    @Update
    suspend fun updateTransaction(transaction: Transaction)

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)

    @Query("SELECT SUM(amount) FROM transactions WHERE userId = :userId AND type = 'EXPENSE'")
    fun getTotalExpenses(userId: String): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE userId = :userId AND type = 'INCOME'")
    fun getTotalIncome(userId: String): Flow<Double?>
    
    @Query("SELECT * FROM transactions WHERE userId = :userId AND categoryId = :categoryId")
    fun getTransactionsByCategory(userId: String, categoryId: Long): Flow<List<Transaction>>
    
    @Query("SELECT * FROM transactions WHERE serverId = :serverId AND userId = :userId LIMIT 1")
    suspend fun getTransactionByServerId(serverId: String, userId: String): Transaction?
}
