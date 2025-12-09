package bilal.com.fintrack.data.repository

import android.content.Context
import bilal.com.fintrack.data.local.dao.TransactionDao
import bilal.com.fintrack.data.local.entities.Transaction
import bilal.com.fintrack.data.remote.RetrofitClient
import bilal.com.fintrack.data.remote.TokenManager
import bilal.com.fintrack.data.remote.models.toDto
import bilal.com.fintrack.data.remote.models.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

/**
 * Repository pour gérer les transactions avec synchronisation cloud
 * Combine Room (local) et Retrofit (API)
 */
class SyncTransactionRepository(
    private val transactionDao: TransactionDao,
    private val context: Context
) {
    
    private val tokenManager = TokenManager(context)
    private val api = RetrofitClient.transactionApi
    
    /**
     * Flow de toutes les transactions locales
     */
    val allTransactions: Flow<List<Transaction>> = transactionDao.getAllTransactions()
    
    /**
     * Ajouter une transaction localement et la synchroniser avec le serveur
     */
    suspend fun insertTransaction(transaction: Transaction): Result<Transaction> {
        return try {
            // 1. Insérer localement d'abord
            val localId = transactionDao.insertTransaction(transaction)
            val insertedTransaction = transaction.copy(id = localId)
            
            // 2. Essayer de synchroniser avec le serveur
            val token = tokenManager.getBearerToken()
            if (token != null) {
                try {
                    val userId = tokenManager.getUserId() ?: ""
                    val transactionDto = insertedTransaction.toDto(userId, emptyMap())
                    
                    val response = api.createTransaction(token, transactionDto)
                    if (response.isSuccessful && response.body()?.success == true) {
                        // Succès de la synchronisation
                        Result.success(insertedTransaction)
                    } else {
                        // Échec de la synchronisation, mais données sauvegardées localement
                        Result.success(insertedTransaction)
                    }
                } catch (e: Exception) {
                    // Erreur réseau, mais données sauvegardées localement
                    Result.success(insertedTransaction)
                }
            } else {
                // Pas de token, mode offline
                Result.success(insertedTransaction)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Mettre à jour une transaction
     */
    suspend fun updateTransaction(transaction: Transaction): Result<Unit> {
        return try {
            transactionDao.updateTransaction(transaction)
            
            // Synchroniser avec le serveur
            val token = tokenManager.getBearerToken()
            if (token != null) {
                try {
                    val userId = tokenManager.getUserId() ?: ""
                    val transactionDto = transaction.toDto(userId, emptyMap())
                    api.updateTransaction(token, transaction.id.toString(), transactionDto)
                } catch (e: Exception) {
                    // Ignorer les erreurs de sync
                }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Supprimer une transaction
     */
    suspend fun deleteTransaction(transaction: Transaction): Result<Unit> {
        return try {
            transactionDao.deleteTransaction(transaction)
            
            // Synchroniser avec le serveur
            val token = tokenManager.getBearerToken()
            if (token != null) {
                try {
                    api.deleteTransaction(token, transaction.id.toString())
                } catch (e: Exception) {
                    // Ignorer les erreurs de sync
                }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Synchroniser toutes les transactions avec le serveur
     * Télécharge les transactions du serveur et les fusionne avec les données locales
     */
    suspend fun syncWithServer(): Result<Unit> {
        return try {
            val token = tokenManager.getBearerToken()
            if (token == null) {
                return Result.failure(Exception("Non authentifié"))
            }
            
            // 1. Récupérer les transactions du serveur
            val response = api.getAllTransactions(token)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val serverTransactions = response.body()?.data ?: emptyList()
                
                // 2. Convertir en entités locales
                val localTransactions = serverTransactions.map { it.toEntity(emptyMap()) }
                
                // 3. Insérer/Mettre à jour dans la base locale
                localTransactions.forEach { transaction ->
                    try {
                        transactionDao.insertTransaction(transaction)
                    } catch (e: Exception) {
                        // Transaction existe déjà, la mettre à jour
                        transactionDao.updateTransaction(transaction)
                    }
                }
                
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Erreur de synchronisation"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Envoyer toutes les transactions locales au serveur
     */
    suspend fun pushLocalTransactionsToServer(): Result<Unit> {
        return try {
            val token = tokenManager.getBearerToken()
            if (token == null) {
                return Result.failure(Exception("Non authentifié"))
            }
            
            val userId = tokenManager.getUserId() ?: ""
            val localTransactions = allTransactions.first()
            
            // Convertir en DTOs
            val transactionDtos = localTransactions.map { it.toDto(userId, emptyMap()) }
            
            // Envoyer au serveur
            val response = api.syncTransactions(token, transactionDtos)
            
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Erreur d'envoi"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
