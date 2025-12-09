package bilal.com.fintrack.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import bilal.com.fintrack.ui.auth.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import bilal.com.fintrack.ui.screens.categories.CategoriesScreen
import bilal.com.fintrack.ui.screens.home.HomeScreen
import bilal.com.fintrack.ui.screens.reports.ReportsScreen
import bilal.com.fintrack.ui.screens.transactions.TransactionsScreen

enum class FinTrackScreen(val route: String) {
    Home("home"),
    Transactions("transactions"),
    Categories("categories"),
    Reports("reports"),
    Budget("budget"),
    AddTransaction("add_transaction"),
    AddCategory("add_category"),
    Settings("settings"),
    TransactionDetail("transaction_detail/{transactionId}"),
    ManageCategories("manage_categories")
}

@Composable
fun FinTrackNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = FinTrackScreen.Home.route,
        modifier = modifier
    ) {
        composable(FinTrackScreen.Home.route) {
            HomeScreen(
                onNavigateToTransactions = { navController.navigate(FinTrackScreen.Transactions.route) },
                onNavigateToReports = { navController.navigate(FinTrackScreen.Reports.route) },
                onAddTransaction = { navController.navigate(FinTrackScreen.AddTransaction.route) },
                onNavigateToSettings = { navController.navigate(FinTrackScreen.Settings.route) },
                onTransactionClick = { transactionId ->
                    navController.navigate("transaction_detail/$transactionId")
                }
            )
        }
        composable(FinTrackScreen.Transactions.route) {
            TransactionsScreen(
                onAddTransaction = { navController.navigate(FinTrackScreen.AddTransaction.route) },
                onTransactionClick = { transactionId ->
                    navController.navigate("transaction_detail/$transactionId")
                }
            )
        }
        composable(FinTrackScreen.Categories.route) {
            CategoriesScreen(
                onAddCategory = { navController.navigate(FinTrackScreen.AddCategory.route) }
            )
        }
        composable(FinTrackScreen.Reports.route) {
            bilal.com.fintrack.ui.screens.statistics.StatisticsScreen()
        }
        composable(FinTrackScreen.Budget.route) {
            bilal.com.fintrack.ui.screens.budget.BudgetScreen()
        }
        composable(FinTrackScreen.AddTransaction.route) {
            bilal.com.fintrack.ui.screens.transactions.AddTransactionScreen(
                onNavigateUp = { navController.navigateUp() },
                onManageCategories = { navController.navigate(FinTrackScreen.ManageCategories.route) }
            )
        }
        composable(FinTrackScreen.AddCategory.route) {
            bilal.com.fintrack.ui.screens.categories.AddCategoryScreen(onNavigateUp = { navController.navigateUp() })
        }
        composable(FinTrackScreen.Settings.route) {
            val context = LocalContext.current
            bilal.com.fintrack.ui.screens.settings.SettingsScreen(
                onNavigateUp = { navController.navigateUp() },
                onManageCategories = { navController.navigate(FinTrackScreen.ManageCategories.route) },
                onLogout = {
                    FirebaseAuth.getInstance().signOut()
                    val intent = Intent(context, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    context.startActivity(intent)
                }
            )
        }
        composable("transaction_detail/{transactionId}") { backStackEntry ->
            val transactionId = backStackEntry.arguments?.getString("transactionId")?.toLongOrNull() ?: 0L
            bilal.com.fintrack.ui.screens.transactions.TransactionDetailScreen(
                transactionId = transactionId,
                onNavigateUp = { navController.navigateUp() },
                onEdit = { id ->
                    // TODO: Navigate to edit transaction screen
                    navController.navigateUp()
                }
            )
        }
        composable(FinTrackScreen.ManageCategories.route) {
            bilal.com.fintrack.ui.screens.categories.ManageCategoriesScreen(
                onNavigateUp = { navController.navigateUp() },
                onAddCategory = { navController.navigate(FinTrackScreen.AddCategory.route) }
            )
        }
    }
}
