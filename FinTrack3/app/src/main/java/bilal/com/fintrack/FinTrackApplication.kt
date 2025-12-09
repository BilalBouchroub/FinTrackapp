package bilal.com.fintrack

import android.app.Application
import android.content.Context
import bilal.com.fintrack.data.local.database.FinTrackDatabase
import bilal.com.fintrack.data.repository.BudgetRepository
import bilal.com.fintrack.data.repository.CategoryRepository
import bilal.com.fintrack.data.repository.TransactionRepository
import bilal.com.fintrack.data.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class AppContainer(private val context: Context) {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    val database by lazy {
        FinTrackDatabase.getDatabase(context, applicationScope)
    }

    val transactionRepository by lazy {
        TransactionRepository(database.transactionDao())
    }

    val categoryRepository by lazy {
        CategoryRepository(database.categoryDao(), context)
    }

    val budgetRepository by lazy {
        BudgetRepository(database.budgetDao(), context)
    }
    
    val userRepository by lazy {
        UserRepository(database.userDao())
    }

    val syncTransactionRepository by lazy {
        bilal.com.fintrack.data.repository.SyncTransactionRepository(
            transactionDao = database.transactionDao(),
            context = context
        )
    }
}

class FinTrackApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
