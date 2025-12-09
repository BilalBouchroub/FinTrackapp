package bilal.com.fintrack.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import bilal.com.fintrack.data.local.dao.BudgetDao
import bilal.com.fintrack.data.local.dao.CategoryDao
import bilal.com.fintrack.data.local.dao.TransactionDao
import bilal.com.fintrack.data.local.dao.UserDao
import bilal.com.fintrack.data.local.entities.Budget
import bilal.com.fintrack.data.local.entities.Category
import bilal.com.fintrack.data.local.entities.Transaction
import bilal.com.fintrack.data.local.entities.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Transaction::class, Category::class, Budget::class, User::class],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class FinTrackDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun budgetDao(): BudgetDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: FinTrackDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): FinTrackDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FinTrackDatabase::class.java,
                    "fintrack_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(FinTrackDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class FinTrackDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database.categoryDao())
                }
            }
        }

        suspend fun populateDatabase(categoryDao: CategoryDao) {
            // Pre-populate categories
            val categories = listOf(
                // Expense categories (Dépenses)
                Category(name = "Nourriture", icon = "restaurant", color = "#FFC107", keywords = listOf("restaurant", "mcdo", "burger", "pizza", "supermarché", "courses")),
                Category(name = "Social", icon = "people", color = "#FFAB91", keywords = listOf("social", "amis", "sortie")),
                Category(name = "Trafic", icon = "directions_car", color = "#64B5F6", keywords = listOf("essence", "uber", "taxi", "train", "bus", "péage", "transport")),
                Category(name = "Shopping", icon = "shopping_cart", color = "#FF80AB", keywords = listOf("shopping", "vêtements", "magasin")),
                Category(name = "Épicerie", icon = "shopping_bag", color = "#A5D6A7", keywords = listOf("épicerie", "courses")),
                Category(name = "Education", icon = "school", color = "#B39DDB", keywords = listOf("école", "université", "formation", "cours")),
                Category(name = "Factures", icon = "receipt", color = "#80DEEA", keywords = listOf("facture", "abonnement")),
                Category(name = "Locations", icon = "home", color = "#FFD54F", keywords = listOf("loyer", "électricité", "eau", "internet", "assurance", "logement")),
                Category(name = "Médical", icon = "medical_services", color = "#EF9A9A", keywords = listOf("médecin", "pharmacie", "dentiste", "hôpital", "santé")),
                Category(name = "Investissement", icon = "trending_up", color = "#CE93D8", keywords = listOf("investissement", "bourse", "crypto")),
                Category(name = "Cadeau", icon = "card_giftcard", color = "#FFCCBC", keywords = listOf("cadeau", "anniversaire")),
                
                // Income categories (Revenu)
                Category(name = "Investir", icon = "payments", color = "#7ED957", keywords = listOf("investissement", "dividende")),
                Category(name = "Affaires", icon = "business", color = "#69B4FF", keywords = listOf("business", "entreprise", "freelance")),
                Category(name = "Intérêt", icon = "percent", color = "#CE93D8", keywords = listOf("intérêt", "intérêts", "banque")),
                Category(name = "Revenus", icon = "payments", color = "#FFAB91", keywords = listOf("salaire", "virement", "prime", "paie")),
                
                // Other
                Category(name = "Autre", icon = "category", color = "#90CAF9", keywords = listOf())
            )
            categoryDao.insertAll(categories)
        }
    }
}
