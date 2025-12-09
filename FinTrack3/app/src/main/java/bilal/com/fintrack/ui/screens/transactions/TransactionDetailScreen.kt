package bilal.com.fintrack.ui.screens.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import bilal.com.fintrack.data.local.entities.TransactionType
import bilal.com.fintrack.ui.AppViewModelProvider
import bilal.com.fintrack.ui.theme.GradientEnd
import bilal.com.fintrack.ui.theme.GradientStart
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailScreen(
    transactionId: Long,
    viewModel: TransactionViewModel = viewModel(factory = AppViewModelProvider.Factory),
    onNavigateUp: () -> Unit,
    onEdit: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val transaction = uiState.transactions.find { it.id == transactionId }
    val category = transaction?.let { tx ->
        uiState.categories.find { it.id == tx.categoryId }
    }
    
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    if (transaction == null) {
        // Transaction not found
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Transaction non trouvée")
        }
        return
    }
    
    val dateFormat = SimpleDateFormat("EEE., dd MMMM", Locale.FRENCH)
    val formattedDate = dateFormat.format(Date(transaction.date))
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Transactions",
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            Icons.Default.ArrowBack, 
                            contentDescription = "Retour",
                            tint = Color.Black
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showDeleteDialog = true }
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Supprimer",
                            tint = Color.White,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFFF5252))
                                .padding(8.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF5F6FA)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Date
            Text(
                formattedDate,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            
            // Amount
            Text(
                when (transaction.type) {
                    TransactionType.EXPENSE -> "-${transaction.amount.toInt()} MAD"
                    TransactionType.INCOME -> "+${transaction.amount.toInt()} MAD"
                    TransactionType.DEBT -> "${transaction.amount.toInt()} MAD"
                },
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = when (transaction.type) {
                    TransactionType.EXPENSE -> Color(0xFFFF5252)
                    TransactionType.INCOME -> Color(0xFF4CAF50)
                    TransactionType.DEBT -> Color(0xFFFF9800)
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            
            // Category Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Catégorie",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Category Icon
                        val categoryColor = try {
                            Color(android.graphics.Color.parseColor(category?.color ?: "#6C63FF"))
                        } catch (e: Exception) {
                            GradientStart
                        }
                        
                        val categoryIcon = when (category?.name?.lowercase()) {
                            "nourriture" -> Icons.Default.Restaurant
                            "trafic" -> Icons.Default.DirectionsCar
                            "locations" -> Icons.Default.Home
                            "médical" -> Icons.Default.MedicalServices
                            "shopping" -> Icons.Default.ShoppingCart
                            "social" -> Icons.Default.People
                            "épicerie" -> Icons.Default.ShoppingBag
                            "education" -> Icons.Default.School
                            "factures" -> Icons.Default.Receipt
                            "investir" -> Icons.Default.TrendingUp
                            "affaires" -> Icons.Default.Business
                            "intérêt" -> Icons.Default.Percent
                            "revenus" -> Icons.Default.Payments
                            "investissement" -> Icons.Default.TrendingUp
                            "cadeau" -> Icons.Default.CardGiftcard
                            else -> Icons.Default.Category
                        }
                        
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(categoryColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                categoryIcon,
                                contentDescription = null,
                                tint = categoryColor,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        
                        Text(
                            category?.name ?: "Inconnu",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )
                    }
                }
            }
            
            // Note Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Note",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    
                    Text(
                        transaction.notes ?: "Aucune note",
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (transaction.notes != null) Color.Black else Color.Gray
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Edit Button
            Button(
                onClick = { onEdit(transactionId) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GradientStart
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    "Éditer",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
    
    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp),
            icon = {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null,
                    tint = Color(0xFFFF5252),
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    "Supprimer la transaction ?",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "Cette action est irréversible. Voulez-vous vraiment supprimer cette transaction ?",
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteTransaction(transaction)
                        showDeleteDialog = false
                        onNavigateUp()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF5252)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Supprimer", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text("Annuler", color = Color.Gray, fontWeight = FontWeight.Medium)
                }
            }
        )
    }
}
