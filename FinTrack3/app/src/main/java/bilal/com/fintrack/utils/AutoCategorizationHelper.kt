package bilal.com.fintrack.utils

import bilal.com.fintrack.data.local.entities.Category
import bilal.com.fintrack.data.local.entities.Transaction

object AutoCategorizationHelper {

    fun categorizeTransaction(transaction: Transaction, categories: List<Category>): Long {
        // 1. Check for Keyword Matches in Notes
        if (!transaction.notes.isNullOrEmpty()) {
            val matchedCategory = categories.find { category ->
                category.keywords.any { keyword ->
                    transaction.notes.contains(keyword, ignoreCase = true)
                }
            }
            if (matchedCategory != null) {
                return matchedCategory.id
            }
        }

        // 2. Amount-based Rules (User Request: 1-10 -> Transport)
        // Interpreting "vic vers ca" as: Small amounts (1-10) -> Transport, Larger -> Alimentation (as a fallback guess)
        if (transaction.amount >= 1.0 && transaction.amount <= 10.0) {
            val transportCategory = categories.find { it.name.equals("Transport", ignoreCase = true) }
            if (transportCategory != null) return transportCategory.id
        } else if (transaction.amount > 10.0) {
             // Fallback for larger amounts if no keyword matched - let's try Alimentation as a common expense
             // or just leave it if we want to be safe. But user asked for "vic vers ca".
             val foodCategory = categories.find { it.name.equals("Alimentation", ignoreCase = true) }
             if (foodCategory != null) return foodCategory.id
        }

        // 3. Default to "Autre" or keep original if 1L is default
        return transaction.categoryId
    }
}
