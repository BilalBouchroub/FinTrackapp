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
     * Obtenir toutes les transactions locales pour l'utilisateur connecté
     */
    val allTransactions: Flow<List<Transaction>>
        get() {
            val userId = tokenManager.getUserId() ?: ""
            return transactionDao.getAllTransactions(userId)
        }
    
    /**
     * Ajouter une transaction localement et la synchroniser avec le serveur
     */
    suspend fun insertTransaction(transaction: Transaction): Result<Transaction> {
        return try {
            // 1. Insérer localement d'abord
            val userId = tokenManager.getUserId() ?: ""
            val transactionWithUser = transaction.copy(userId = userId)
            val localId = transactionDao.insertTransaction(transactionWithUser)
            val insertedTransaction = transactionWithUser.copy(id = localId)
            
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
            if (token != null && transaction.serverId != null) {
                try {
                    val userId = tokenManager.getUserId() ?: ""
                    val transactionDto = transaction.toDto(userId, emptyMap())
                    // Utiliser serverId (MongoDB ObjectId) au lieu de l'ID local
                    val response = api.updateTransaction(token, transaction.serverId, transactionDto)
                    if (response.isSuccessful) {
                        android.util.Log.d("SyncRepo", "Transaction mise à jour sur serveur: ${transaction.serverId}")
                    } else {
                        android.util.Log.w("SyncRepo", "Échec mise à jour serveur: ${response.code()}")
                    }
                } catch (e: Exception) {
                    android.util.Log.e("SyncRepo", "Erreur mise à jour serveur", e)
                    // Ignorer les erreurs de sync
                }
            } else {
                android.util.Log.d("SyncRepo", "Transaction locale uniquement (pas de serverId), mise à jour locale seulement")
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
            if (token != null && transaction.serverId != null) {
                try {
                    // Utiliser serverId (MongoDB ObjectId) au lieu de l'ID local
                    val response = api.deleteTransaction(token, transaction.serverId)
                    if (response.isSuccessful) {
                        android.util.Log.d("SyncRepo", "Transaction supprimée du serveur: ${transaction.serverId}")
                    } else {
                        android.util.Log.w("SyncRepo", "Échec suppression serveur: ${response.code()}")
                    }
                } catch (e: Exception) {
                    android.util.Log.e("SyncRepo", "Erreur suppression serveur", e)
                    // Ignorer les erreurs de sync - la transaction est déjà supprimée localement
                }
            } else {
                android.util.Log.d("SyncRepo", "Transaction locale uniquement (pas de serverId), suppression locale seulement")
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
            val userId = tokenManager.getUserId() ?: ""
            
            android.util.Log.d("SyncRepo", "=== DÉBUT SYNCHRONISATION ===")
            android.util.Log.d("SyncRepo", "Token présent: ${token != null}")
            android.util.Log.d("SyncRepo", "UserId: $userId")
            
            if (token == null) {
                android.util.Log.e("SyncRepo", "ERREUR: Pas de token!")
                return Result.failure(Exception("Non authentifié"))
            }
            
            // 1. Récupérer les transactions du serveur
            android.util.Log.d("SyncRepo", "Appel API getAllTransactions...")
            val response = api.getAllTransactions(token)
            
            android.util.Log.d("SyncRepo", "Réponse HTTP: ${response.code()}")
            android.util.Log.d("SyncRepo", "Success: ${response.body()?.success}")
            android.util.Log.d("SyncRepo", "Nombre transactions: ${response.body()?.transactions?.size ?: 0}")
            
            if (response.isSuccessful && response.body()?.success == true) {
                val serverTransactions = response.body()?.transactions ?: emptyList()
                
                android.util.Log.d("SyncRepo", "Transactions reçues du serveur: ${serverTransactions.size}")
                serverTransactions.forEachIndexed { index, tx ->
                    android.util.Log.d("SyncRepo", "  [$index] amount=${tx.amount}, type=${tx.type}, id=${tx.id}")
                }
                
                // 2. Convertir en entités locales avec le userId
                val localTransactions = serverTransactions.map { it.toEntity(userId, emptyMap()) }
                
                // 3. Insérer/Mettre à jour dans la base locale avec déduplication par serverId
                var insertCount = 0
                var updateCount = 0
                var skipCount = 0
                
                localTransactions.forEach { transaction ->
                    val serverId = transaction.serverId
                    if (serverId != null) {
                        // Vérifier si la transaction existe déjà via serverId
                        val existing = transactionDao.getTransactionByServerId(serverId, userId)
                        if (existing != null) {
                            // La transaction existe déjà, mettre à jour avec l'ID local existant
                            transactionDao.updateTransaction(transaction.copy(id = existing.id))
                            updateCount++
                        } else {
                            // Nouvelle transaction du serveur, insérer
                            transactionDao.insertTransaction(transaction)
                            insertCount++
                        }
                    } else {
                        // Pas de serverId, skip pour éviter les doublons
                        skipCount++
                        android.util.Log.w("SyncRepo", "Transaction sans serverId ignorée")
                    }
                }
                
                android.util.Log.d("SyncRepo", "Insérées: $insertCount, Mises à jour: $updateCount, Ignorées: $skipCount")
                android.util.Log.d("SyncRepo", "=== FIN SYNCHRONISATION (SUCCÈS) ===")
                
                Result.success(Unit)
            } else {
                val errorMsg = response.body()?.message ?: "Erreur HTTP ${response.code()}"
                android.util.Log.e("SyncRepo", "Erreur API: $errorMsg")
                android.util.Log.e("SyncRepo", "Body brut: ${response.errorBody()?.string()}")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            android.util.Log.e("SyncRepo", "Exception: ${e.message}", e)
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
