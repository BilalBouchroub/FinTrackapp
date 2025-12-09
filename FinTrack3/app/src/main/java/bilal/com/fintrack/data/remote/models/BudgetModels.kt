package bilal.com.fintrack.data.remote.models

import bilal.com.fintrack.data.local.entities.Budget
import com.google.gson.annotations.SerializedName

/**
 * DTO pour Budget
 */
data class BudgetDto(
    @SerializedName("_id")
    val id: String?,
    
    @SerializedName("categoryId")
    val categoryId: String,
    
    @SerializedName("categoryName")
    val categoryName: String,
    
    @SerializedName("amount")
    val amount: Double,
    
    @SerializedName("spent")
    val spent: Double,
    
    @SerializedName("period")
    val period: String,
    
    @SerializedName("month")
    val month: Int?,
    
    @SerializedName("year")
    val year: Int?
)

/**
 * Extension: Budget -> BudgetDto
 */
fun Budget.toDto(categoryName: String): BudgetDto {
    val calendar = java.util.Calendar.getInstance()
    calendar.timeInMillis = startDate
    
    return BudgetDto(
        id = null,
        categoryId = categoryId?.toString() ?: "GLOBAL", // Handle global budget
        categoryName = categoryName,
        amount = amount,
        spent = 0.0, // Calculated field not in local entity
        period = period.name,
        month = calendar.get(java.util.Calendar.MONTH) + 1,
        year = calendar.get(java.util.Calendar.YEAR)
    )
}
