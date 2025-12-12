package bilal.com.fintrack.data.remote.models

import bilal.com.fintrack.data.local.entities.Budget
import bilal.com.fintrack.data.local.entities.BudgetPeriod
import com.google.gson.annotations.SerializedName

/**
 * DTO pour Budget
 */
data class BudgetDto(
    @SerializedName("_id")
    val id: String?,
    
    @SerializedName("categoryId")
    val categoryId: String?,
    
    @SerializedName("categoryName")
    val categoryName: String?,
    
    @SerializedName("amount")
    val amount: Double,
    
    @SerializedName("spent")
    val spent: Double?,
    
    @SerializedName("period")
    val period: String?,
    
    @SerializedName("month")
    val month: Int?,
    
    @SerializedName("year")
    val year: Int?
)

/**
 * Réponse spécifique pour la liste des budgets
 */
data class BudgetListResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("count")
    val count: Int?,
    @SerializedName("data")
    val data: List<BudgetDto>?
)

/**
 * Extension: Budget -> BudgetDto
 */
fun Budget.toDto(categoryName: String): BudgetDto {
    val calendar = java.util.Calendar.getInstance()
    calendar.timeInMillis = startDate
    
    return BudgetDto(
        id = null,
        categoryId = categoryId?.toString() ?: "GLOBAL",
        categoryName = categoryName,
        amount = amount,
        spent = 0.0,
        period = period.name,
        month = calendar.get(java.util.Calendar.MONTH) + 1,
        year = calendar.get(java.util.Calendar.YEAR)
    )
}

/**
 * Extension: BudgetDto -> Budget
 */
fun BudgetDto.toEntity(userId: String): Budget {
    val calendar = java.util.Calendar.getInstance()
    calendar.set(java.util.Calendar.MONTH, (month ?: 1) - 1)
    calendar.set(java.util.Calendar.YEAR, year ?: calendar.get(java.util.Calendar.YEAR))
    calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
    
    return Budget(
        id = 0L, // Room génère l'ID
        categoryId = categoryId?.toLongOrNull(),
        amount = amount,
        userId = userId,
        period = try { 
            BudgetPeriod.valueOf(period ?: "MONTHLY") 
        } catch (e: Exception) { 
            BudgetPeriod.MONTHLY 
        },
        startDate = calendar.timeInMillis,
        serverId = id  // Stocker l'ObjectId MongoDB
    )
}
