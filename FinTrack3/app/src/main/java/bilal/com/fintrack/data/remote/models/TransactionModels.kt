package bilal.com.fintrack.data.remote.models

import bilal.com.fintrack.data.local.entities.Transaction
import bilal.com.fintrack.data.local.entities.TransactionType
import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

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
        date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(Date(date)),
        notes = notes,
        createdAt = null,
        updatedAt = null
    )
}

/**
 * Convertir TransactionDto de l'API en Transaction local
 * Note: Les IDs MongoDB sont des chaînes hexadécimales (ObjectId) qui ne peuvent pas
 * être converties en Long. On laisse Room générer ses propres IDs (id = 0).
 */
fun TransactionDto.toEntity(userId: String, categoryIdMapping: Map<String, Long>): Transaction {
    // L'ID MongoDB est une chaîne hex comme "675a1234...", pas un nombre
    // On le stocke dans serverId pour la déduplication lors de la sync
    return Transaction(
        id = 0L, // Room va générer un nouvel ID automatiquement
        amount = amount,
        type = try { 
            TransactionType.valueOf(type) 
        } catch (e: Exception) { 
            TransactionType.EXPENSE 
        },
        categoryId = categoryIdMapping[categoryId] ?: categoryId.toLongOrNull() ?: 1L,
        paymentMethod = paymentMethod,
        date = try {
            // Essayer plusieurs formats de date
            val formats = listOf(
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
                "yyyy-MM-dd'T'HH:mm:ssZ",
                "yyyy-MM-dd'T'HH:mm:ss"
            )
            var parsed: Long? = null
            for (format in formats) {
                try {
                    parsed = SimpleDateFormat(format, Locale.US).apply {
                        timeZone = TimeZone.getTimeZone("UTC")
                    }.parse(date)?.time
                    if (parsed != null) break
                } catch (e: Exception) { /* try next format */ }
            }
            parsed ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        },
        notes = notes,
        userId = userId,
        createdAt = System.currentTimeMillis(),
        serverId = id  // Stocker l'ObjectId MongoDB pour la déduplication
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

/**
 * Réponse spécifique pour la liste des transactions
 * Le backend retourne 'transactions' au lieu de 'data'
 */
data class TransactionListResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("count")
    val count: Int?,
    
    @SerializedName("transactions")
    val transactions: List<TransactionDto>?,
    
    @SerializedName("message")
    val message: String?
)
