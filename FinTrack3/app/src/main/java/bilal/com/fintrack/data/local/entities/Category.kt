package bilal.com.fintrack.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val icon: String, // Store icon name or resource ID as string
    val color: String, // Hex color code
    val isCustom: Boolean = false,
    val keywords: List<String> = emptyList(),
    val userId: String = ""
)
