package bilal.com.fintrack.data.remote.api

import bilal.com.fintrack.data.remote.models.ApiResponse
import bilal.com.fintrack.data.remote.models.TransactionDto
import bilal.com.fintrack.data.remote.models.TransactionListResponse
import retrofit2.Response
import retrofit2.http.*

/**
 * Service API pour les transactions
 */
interface TransactionApiService {
    
    /**
     * Récupérer toutes les transactions de l'utilisateur
     */
    @GET("transactions")
    suspend fun getAllTransactions(
        @Header("Authorization") token: String
    ): Response<TransactionListResponse>
    
    /**
     * Récupérer une transaction spécifique
     */
    @GET("transactions/{id}")
    suspend fun getTransaction(
        @Header("Authorization") token: String,
        @Path("id") transactionId: String
    ): Response<ApiResponse<TransactionDto>>
    
    /**
     * Créer une nouvelle transaction
     */
    @POST("transactions")
    suspend fun createTransaction(
        @Header("Authorization") token: String,
        @Body transaction: TransactionDto
    ): Response<ApiResponse<TransactionDto>>
    
    /**
     * Mettre à jour une transaction
     */
    @PUT("transactions/{id}")
    suspend fun updateTransaction(
        @Header("Authorization") token: String,
        @Path("id") transactionId: String,
        @Body transaction: TransactionDto
    ): Response<ApiResponse<TransactionDto>>
    
    /**
     * Supprimer une transaction
     */
    @DELETE("transactions/{id}")
    suspend fun deleteTransaction(
        @Header("Authorization") token: String,
        @Path("id") transactionId: String
    ): Response<ApiResponse<Unit>>
    
    /**
     * Synchroniser les transactions (envoyer plusieurs à la fois)
     */
    @POST("transactions/sync")
    suspend fun syncTransactions(
        @Header("Authorization") token: String,
        @Body transactions: List<TransactionDto>
    ): Response<ApiResponse<List<TransactionDto>>>
}
