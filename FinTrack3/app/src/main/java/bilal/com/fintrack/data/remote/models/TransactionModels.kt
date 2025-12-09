package bilal.com.fintrack.data.remote.models

import bilal.com.fintrack.data.local.entities.Transaction
import bilal.com.fintrack.data.local.entities.TransactionType
import com.google.gson.annotations.SerializedName

/**
 * Modèle DTO pour Transaction (API)
 */
data class TransactionDto(
    @SerializedName("_id")
    val id: String?,
    
    @SerializedName("userId")
    val userId: String,
    
    @SerializedName("amount")
    val amount: Double,
    
    @SerializedName("type")
    val type: String, // "INCOME", "EXPENSE", "DEBT"
    
    @SerializedName("categoryId")
    val categoryId: String,
    
    @SerializedName("paymentMethod")
    val paymentMethod: String,
    
    @SerializedName("date")
    val date: String, // ISO 8601 format
    
    @SerializedName("notes")
    val notes: String?,
    
    @SerializedName("createdAt")
    val createdAt: String?,
    
    @SerializedName("updatedAt")
    val updatedAt: String?
)

/**
 * Convertir Transaction local en TransactionDto pour l'API
 */
fun Transaction.toDto(userId: String, categoryIdMapping: Map<Long, String>): TransactionDto {
    return TransactionDto(
        id = if (id == 0L) null else id.toString(),
        userId = userId,
        amount = amount,
        type = type.name,
        categoryId = categoryIdMapping[categoryId] ?: categoryId.toString(),
        paymentMethod = paymentMethod,
        date = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US).apply {
            timeZone = java.util.TimeZone.getTimeZone("UTC")
        }.format(java.util.Date(date)),
        notes = notes,
        createdAt = null,
        updatedAt = null
    )
}

/**
 * Convertir TransactionDto de l'API en Transaction local
 */
fun TransactionDto.toEntity(categoryIdMapping: Map<String, Long>): Transaction {
    return Transaction(
        id = id?.toLongOrNull() ?: 0L,
        amount = amount,
        type = TransactionType.valueOf(type),
        categoryId = categoryIdMapping[categoryId] ?: categoryId.toLongOrNull() ?: 1L,
        paymentMethod = paymentMethod,
        date = try {
            java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US).apply {
                timeZone = java.util.TimeZone.getTimeZone("UTC")
            }.parse(date)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        },
        notes = notes
    )
}

/**
 * Réponse générique de l'API
 */
data class ApiResponse<T>(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("data")
    val data: T?,
    
    @SerializedName("message")
    val message: String?
)
