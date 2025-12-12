package bilal.com.fintrack.data.remote.models

import com.google.gson.annotations.SerializedName

/**
 * Modèle de réponse pour l'authentification
 */
data class AuthResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("token")
    val token: String?,
    
    @SerializedName("user")
    val user: UserDto?,
    
    @SerializedName("message")
    val message: String?
)

/**
 * Modèle de requête pour le login
 */
data class LoginRequest(
    @SerializedName("firebaseUid")
    val firebaseUid: String
)

/**
 * Modèle de requête pour l'inscription
 */
data class RegisterRequest(
    @SerializedName("firebaseUid")
    val firebaseUid: String,
    
    @SerializedName("nom")
    val nom: String,
    
    @SerializedName("email")
    val email: String,
    
    @SerializedName("devise")
    val devise: String = "MAD"
)

/**
 * Modèle DTO pour User (Data Transfer Object)
 */
data class UserDto(
    @SerializedName("id")
    val id: String?,
    
    @SerializedName("firebaseUid")
    val firebaseUid: String?,
    
    @SerializedName("nom")
    val nom: String?,
    
    @SerializedName("email")
    val email: String?,
    
    @SerializedName("devise")
    val devise: String?,
    
    @SerializedName("dateInscription")
    val dateInscription: String?,
    
    @SerializedName("createdAt")
    val createdAt: String?,
    
    @SerializedName("updatedAt")
    val updatedAt: String?
)
