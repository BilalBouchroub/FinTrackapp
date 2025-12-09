package bilal.com.fintrack.data.remote

import bilal.com.fintrack.data.remote.models.ApiResponse
import bilal.com.fintrack.data.remote.models.CategoryDto
import retrofit2.Response
import retrofit2.http.*

interface CategoryApiService {
    @GET("categories")
    suspend fun getAllCategories(
        @Header("Authorization") token: String
    ): Response<ApiResponse<List<CategoryDto>>>
    
    @POST("categories")
    suspend fun createCategory(
        @Header("Authorization") token: String,
        @Body category: CategoryDto
    ): Response<ApiResponse<CategoryDto>>
    
    @DELETE("categories/{id}")
    suspend fun deleteCategory(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<ApiResponse<Unit>>
}
