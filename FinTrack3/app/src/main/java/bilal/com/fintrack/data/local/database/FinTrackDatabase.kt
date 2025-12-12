package bilal.com.fintrack.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import bilal.com.fintrack.data.local.dao.BudgetDao
import bilal.com.fintrack.data.local.dao.CategoryDao
import bilal.com.fintrack.data.local.dao.TransactionDao
import bilal.com.fintrack.data.local.entities.Budget
import bilal.com.fintrack.data.local.entities.Category
import bilal.com.fintrack.data.local.entities.Transaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import androidx.room.TypeConverters

import bilal.com.fintrack.data.local.dao.UserDao
import bilal.com.fintrack.data.local.entities.User

@Database(entities = [Transaction::class, Category::class, Budget::class, User::class], version = 8, exportSchema = false)
@TypeConverters(Converters::class)
abstract class FinTrackDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun budgetDao(): BudgetDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: FinTrackDatabase? = null

        fun getDatabase(context: Context): FinTrackDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FinTrackDatabase::class.java,
                    "fintrack_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(FinTrackDatabaseCallback(context))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class FinTrackDatabaseCallback(
            private val context: Context
        ) : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateDatabase(database.categoryDao())
                    }
                }
            }
            
            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                // Ne pas appeler populateDatabase ici pour éviter les doublons
            }

            suspend fun populateDatabase(categoryDao: CategoryDao) {
                // Vérifier si les catégories SYSTEM existent déjà
                val systemCount = categoryDao.getCountByUserId(userId = "SYSTEM")
                
                android.util.Log.d("FinTrackDB", "Catégories SYSTEM existantes: $systemCount")
                
                // Ne créer les catégories que si aucune catégorie SYSTEM n'existe
                if (systemCount == 0) {
                    android.util.Log.d("FinTrackDB", "Création des catégories par défaut...")
                    
                    val defaultCategories = listOf(
                        // Catégories Dépenses
                        Category(name = "Nourriture", icon = "restaurant", color = "#FF5722", isCustom = false, userId = "SYSTEM"),
                        Category(name = "Transport", icon = "traffic", color = "#FFC107", isCustom = false, userId = "SYSTEM"),
                        Category(name = "Logement", icon = "home", color = "#03A9F4", isCustom = false, userId = "SYSTEM"),
                        Category(name = "Médical", icon = "medical_services", color = "#E91E63", isCustom = false, userId = "SYSTEM"),
                        Category(name = "Shopping", icon = "shopping_cart", color = "#9C27B0", isCustom = false, userId = "SYSTEM"),
                        Category(name = "Social", icon = "people", color = "#3F51B5", isCustom = false, userId = "SYSTEM"),
                        Category(name = "Épicerie", icon = "shopping_basket", color = "#4CAF50", isCustom = false, userId = "SYSTEM"),
                        Category(name = "Éducation", icon = "school", color = "#009688", isCustom = false, userId = "SYSTEM"),
                        Category(name = "Factures", icon = "receipt", color = "#795548", isCustom = false, userId = "SYSTEM"),
                        Category(name = "Investissement", icon = "trending_up", color = "#607D8B", isCustom = false, userId = "SYSTEM"),
                        Category(name = "Affaires", icon = "business", color = "#00BCD4", isCustom = false, userId = "SYSTEM"),
                        Category(name = "Cadeau", icon = "card_giftcard", color = "#F44336", isCustom = false, userId = "SYSTEM"),

                        // Catégories Revenus
                        Category(name = "Revenus", icon = "payments", color = "#8BC34A", isCustom = false, userId = "SYSTEM"),
                        Category(name = "Salaire", icon = "account_balance_wallet", color = "#4CAF50", isCustom = false, userId = "SYSTEM"),
                        Category(name = "Freelance", icon = "work", color = "#2196F3", isCustom = false, userId = "SYSTEM")
                    )
                    
                    defaultCategories.forEach { categoryDao.insertCategory(it) }
                    android.util.Log.d("FinTrackDB", "Catégories créées: ${defaultCategories.size}")
                }
            }
        }
    }
}
