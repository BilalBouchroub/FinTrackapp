package bilal.com.fintrack.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import bilal.com.fintrack.FinTrackApplication
import bilal.com.fintrack.ui.screens.categories.CategoryViewModel
import bilal.com.fintrack.ui.screens.home.HomeViewModel
import bilal.com.fintrack.ui.screens.reports.ReportsViewModel
import bilal.com.fintrack.ui.screens.transactions.TransactionViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            HomeViewModel(
                fintrackApplication().container.transactionRepository,
                fintrackApplication().container.budgetRepository,
                fintrackApplication().container.categoryRepository
            )
        }
        initializer {
            TransactionViewModel(
                fintrackApplication().container.syncTransactionRepository,
                fintrackApplication().container.categoryRepository,
                fintrackApplication().container.budgetRepository
            )
        }
        initializer {
            CategoryViewModel(
                fintrackApplication().container.categoryRepository
            )
        }
        initializer {
            ReportsViewModel(
                fintrackApplication().container.transactionRepository,
                fintrackApplication().container.budgetRepository
            )
        }
        initializer {
            bilal.com.fintrack.ui.screens.budget.BudgetViewModel(
                fintrackApplication().container.budgetRepository,
                fintrackApplication().container.categoryRepository,
                fintrackApplication().container.transactionRepository
            )
        }
        initializer {
            bilal.com.fintrack.ui.screens.statistics.StatisticsViewModel(
                fintrackApplication().container.transactionRepository,
                fintrackApplication().container.categoryRepository,
                fintrackApplication().container.budgetRepository
            )
        }
    }
}

fun CreationExtras.fintrackApplication(): FinTrackApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as FinTrackApplication)
