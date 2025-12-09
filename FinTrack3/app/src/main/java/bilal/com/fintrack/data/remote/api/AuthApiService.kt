package bilal.com.fintrack.data.remote.api

import bilal.com.fintrack.data.remote.models.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Service API pour l'authentification
 */
interface AuthApiService {
    
    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<AuthResponse>
    
    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<AuthResponse>
    
    @GET("auth/me")
    suspend fun getCurrentUser(
        @Header("Authorization") token: String
    ): Response<ApiResponse<UserDto>>
}
