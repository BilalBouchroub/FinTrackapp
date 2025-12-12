package bilal.com.fintrack.data.remote.models

import bilal.com.fintrack.data.local.entities.Category
import bilal.com.fintrack.data.local.entities.CategoryType
import com.google.gson.annotations.SerializedName

/**
 * DTO pour Category
 */
data class CategoryDto(
    @SerializedName("_id")
    val id: String?,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("type")
    val type: String,
    
    @SerializedName("icon")
    val icon: String,
    
    @SerializedName("color")
    val color: String?,
    
    @SerializedName("isDefault")
    val isDefault: Boolean
)

/**
 * Extension: Category -> CategoryDto
 */
fun Category.toDto(): CategoryDto {
    return CategoryDto(
        id = null,
        name = name,
        type = "EXPENSE", // Default since local entity doesn't have type
        icon = icon,
        color = color,
        isDefault = !isCustom
    )
}

/**
 * Extension: CategoryDto -> Category
 */
fun CategoryDto.toEntity(userId: String): Category {
    return Category(
        name = name,
        // type ignored
        icon = icon,
        color = color ?: "#000000",
        isCustom = !isDefault,
        userId = userId
    )
}
