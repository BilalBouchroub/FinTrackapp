package bilal.com.fintrack.ui.screens.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bilal.com.fintrack.data.local.entities.Category
import bilal.com.fintrack.data.repository.CategoryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
// Extension function for map since it's not standard on Flow without import
import kotlinx.coroutines.flow.map
data class CategoriesUiState(
    val categories: List<Category> = emptyList()
)

class CategoryViewModel(
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    val uiState: StateFlow<CategoriesUiState> = categoryRepository.allCategories
        .map { CategoriesUiState(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = CategoriesUiState()
        )
    
    val allCategories = categoryRepository.allCategories

    fun addCategory(category: Category) {
        viewModelScope.launch {
            categoryRepository.insertCategory(category)
        }
    }

    fun updateCategory(category: Category) {
        viewModelScope.launch {
            categoryRepository.updateCategory(category)
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            categoryRepository.deleteCategory(category)
        }
    }
}

