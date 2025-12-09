package bilal.com.fintrack.data.repository

import android.content.Context
import bilal.com.fintrack.data.local.dao.CategoryDao
import bilal.com.fintrack.data.local.entities.Category
import bilal.com.fintrack.data.remote.RetrofitClient
import bilal.com.fintrack.data.remote.TokenManager
import bilal.com.fintrack.data.remote.models.toDto
import kotlinx.coroutines.flow.Flow

class CategoryRepository(
    private val categoryDao: CategoryDao,
    context: Context
) {
    private val tokenManager = TokenManager(context)
    private val api = RetrofitClient.categoryApi

    val allCategories: Flow<List<Category>> = categoryDao.getAllCategories()

    suspend fun insertCategory(category: Category) {
        val id = categoryDao.insertCategory(category)
        
        // Sync avec le serveur
        try {
            val token = tokenManager.getBearerToken()
            if (token != null) {
                // Créer le DTO
                val dto = category.toDto()
                api.createCategory(token, dto)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun updateCategory(category: Category) {
        categoryDao.updateCategory(category)
        // Note: Update sync non implémenté pour l'instant car l'API attend un ID MongoDB
    }

    suspend fun deleteCategory(category: Category) {
        categoryDao.deleteCategory(category)
        // Note: Delete sync non implémenté
    }

    suspend fun getCategoryById(id: Long): Category? {
        return categoryDao.getCategoryById(id)
    }
}
