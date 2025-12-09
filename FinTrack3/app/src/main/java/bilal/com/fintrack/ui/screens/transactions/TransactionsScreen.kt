package bilal.com.fintrack.ui.screens.transactions

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import bilal.com.fintrack.ui.AppViewModelProvider
import bilal.com.fintrack.ui.components.TransactionCard

@Composable
fun TransactionsScreen(
    viewModel: TransactionViewModel = viewModel(factory = AppViewModelProvider.Factory),
    onAddTransaction: () -> Unit,
    onTransactionClick: (Long) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAddTransaction) {
                Icon(Icons.Default.Add, contentDescription = "Add Transaction")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Search and Filters here (TODO)

            LazyColumn {
                items(uiState.transactions) { transaction ->
                    val category = uiState.categories.find { it.id == transaction.categoryId }
                    TransactionCard(
                        transaction = transaction,
                        categoryName = category?.name ?: "Unknown",
                        categoryColor = category?.color ?: "#CCCCCC",
                        onDelete = { viewModel.deleteTransaction(transaction) },
                        onClick = { onTransactionClick(transaction.id) }
                    )
                }
            }
        }
    }
}
