package bilal.com.fintrack.data.remote

import bilal.com.fintrack.data.remote.models.ApiResponse
import bilal.com.fintrack.data.remote.models.BudgetDto
import retrofit2.Response
import retrofit2.http.*

interface BudgetApiService {
    @GET("budgets")
    suspend fun getAllBudgets(
        @Header("Authorization") token: String
    ): Response<ApiResponse<List<BudgetDto>>>
    
    @POST("budgets")
    suspend fun createBudget(
        @Header("Authorization") token: String,
        @Body budget: BudgetDto
    ): Response<ApiResponse<BudgetDto>>
    
    @PUT("budgets/{id}")
    suspend fun updateBudget(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body budget: BudgetDto
    ): Response<ApiResponse<BudgetDto>>
    
    @DELETE("budgets/{id}")
    suspend fun deleteBudget(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<ApiResponse<Unit>>
}
