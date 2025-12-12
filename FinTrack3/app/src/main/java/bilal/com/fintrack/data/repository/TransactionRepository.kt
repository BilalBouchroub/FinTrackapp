package bilal.com.fintrack.data.repository

import bilal.com.fintrack.data.local.dao.TransactionDao
import bilal.com.fintrack.data.local.entities.Transaction
import bilal.com.fintrack.data.local.entities.TransactionType
import android.content.Context
import bilal.com.fintrack.data.remote.TokenManager
import kotlinx.coroutines.flow.Flow
import java.util.Date

class TransactionRepository(
    private val transactionDao: TransactionDao,
    context: Context
) {
    private val tokenManager = TokenManager(context)

    val allTransactions: Flow<List<Transaction>> 
        get() {
            val userId = tokenManager.getUserId() ?: ""
            return transactionDao.getAllTransactions(userId)
        }

    val totalExpenses: Flow<Double?> 
        get() {
            val userId = tokenManager.getUserId() ?: ""
            return transactionDao.getTotalExpenses(userId)
        }

    val totalIncome: Flow<Double?> 
        get() {
            val userId = tokenManager.getUserId() ?: ""
            return transactionDao.getTotalIncome(userId)
        }

    suspend fun insertTransaction(transaction: Transaction) {
        val userId = tokenManager.getUserId() ?: ""
        transactionDao.insertTransaction(transaction.copy(userId = userId))
    }

    suspend fun updateTransaction(transaction: Transaction) {
        // Ensure we don't accidentally decouple it from the user, but usually update comes from existing object
        val userId = tokenManager.getUserId() ?: ""
        transactionDao.updateTransaction(transaction.copy(userId = userId))
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.deleteTransaction(transaction)
    }
    
    suspend fun getTransactionById(id: Long): Transaction? {
        // Query by ID is generally safe, but we could enforce user check if DAO supports it.
        // For now, assuming ID is unique globally or sufficient.
        return transactionDao.getTransactionById(id)
    }
    
    fun getTransactionsByDateRange(startDate: Long, endDate: Long): Flow<List<Transaction>> {
        val userId = tokenManager.getUserId() ?: ""
        return transactionDao.getTransactionsByDateRange(userId, startDate, endDate)
    }
    
    fun getTransactionsByCategory(categoryId: Long): Flow<List<Transaction>> {
        val userId = tokenManager.getUserId() ?: ""
        return transactionDao.getTransactionsByCategory(userId, categoryId)
    }
}
