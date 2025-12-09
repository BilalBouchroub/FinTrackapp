package bilal.com.fintrack.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType {
    EXPENSE, INCOME, DEBT
}

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val type: TransactionType,
    val categoryId: Long,
    val paymentMethod: String,
    val date: Long,
    val notes: String?,
    val createdAt: Long = System.currentTimeMillis()
)
