package bilal.com.fintrack.ui.screens.reports

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import bilal.com.fintrack.ui.AppViewModelProvider
import bilal.com.fintrack.ui.components.ExpenseLineChart
import bilal.com.fintrack.utils.ExportHelper

@Composable
fun ReportsScreen(
    viewModel: ReportsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text("Rapports Financiers", style = MaterialTheme.typography.headlineMedium)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Charts
            Text("Évolution des dépenses", style = MaterialTheme.typography.titleMedium)
            
            // Prepare data points from transactions
            // Group expenses by day of month
            val expensesByDay = uiState.transactions
                .filter { it.type == bilal.com.fintrack.data.local.entities.TransactionType.EXPENSE }
                .groupBy { 
                    val cal = java.util.Calendar.getInstance()
                    cal.timeInMillis = it.date
                    cal.get(java.util.Calendar.DAY_OF_MONTH)
                }
                .mapValues { it.value.sumOf { t -> t.amount }.toFloat() }
            
            // Create a list of points for the chart (e.g., 1..31)
            val chartData = (1..31).map { day ->
                expensesByDay[day] ?: 0f
            }

            if (chartData.isNotEmpty() && chartData.any { it > 0 }) {
                ExpenseLineChart(
                    dataPoints = chartData,
                    modifier = Modifier.fillMaxWidth().height(200.dp)
                )
            } else {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Text("Aucune donnée pour ce mois", color = MaterialTheme.colorScheme.outline)
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = {
                    val file = ExportHelper.exportToPdf(context, uiState.transactions)
                    file?.let { ExportHelper.shareFile(context, it) }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Share, contentDescription = null)
                Text("Exporter et Partager PDF")
            }
        }
    }
}
