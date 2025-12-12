package bilal.com.fintrack.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class BudgetPeriod {
    MONTHLY, YEARLY, WEEKLY
}

@Entity(tableName = "budgets")
data class Budget(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val categoryId: Long?, // Null for global budget
    val amount: Double,
    val userId: String = "",
    val period: BudgetPeriod,
    val startDate: Long,
    val serverId: String? = null  // MongoDB ObjectId for sync
)
