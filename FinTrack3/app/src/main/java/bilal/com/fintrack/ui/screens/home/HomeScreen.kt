package bilal.com.fintrack.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import bilal.com.fintrack.data.local.entities.TransactionType
import bilal.com.fintrack.ui.AppViewModelProvider
import bilal.com.fintrack.ui.theme.GradientEnd
import bilal.com.fintrack.ui.theme.GradientStart
import java.text.SimpleDateFormat
import java.util.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory),
    onNavigateToTransactions: () -> Unit,
    onNavigateToReports: () -> Unit,
    onAddTransaction: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onTransactionClick: (Long) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    var balanceVisible by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    
    val context = androidx.compose.ui.platform.LocalContext.current
    val currency = remember(context) { bilal.com.fintrack.data.remote.TokenManager(context).getCurrency() }
    
    // Date picker state
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = uiState.selectedDate ?: System.currentTimeMillis()
    )
    
    // Format date display
    val dateFormatter = SimpleDateFormat("EEEE, dd MMMM", Locale.FRENCH)
    val monthFormatter = SimpleDateFormat("MMMM, yyyy", Locale.FRENCH)
    
    val displayDate = if (uiState.selectedDate != null) {
        dateFormatter.format(Date(uiState.selectedDate!!)).replaceFirstChar { it.uppercase() }
    } else {
        monthFormatter.format(Date()).replaceFirstChar { it.uppercase() }
    }
    
    // Show DatePicker Dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            viewModel.selectDate(millis)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Annuler")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        containerColor = Color(0xFFF5F6FA)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header with gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(GradientStart, GradientEnd)
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    // Title and date selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Solde total",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            modifier = Modifier.clickable { 
                                if (uiState.selectedDate != null) {
                                    viewModel.clearDateFilter()
                                } else {
                                    showDatePicker = true 
                                }
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    displayDate,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    if (uiState.selectedDate != null) Icons.Default.Close else Icons.Default.CalendarToday,
                                    contentDescription = if (uiState.selectedDate != null) "Effacer le filtre" else "Sélectionner une date",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Balance display
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            if (balanceVisible) "${uiState.totalBalance.toInt()} $currency" else "******",
                            style = MaterialTheme.typography.displayMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 36.sp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        IconButton(onClick = { balanceVisible = !balanceVisible }) {
                            Icon(
                                if (balanceVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle visibility",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Total card (Dépenses/Revenu)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                if (selectedTab == 0) "Dépenses totales" else "Revenu total",
                                style = MaterialTheme.typography.titleMedium,
                                color = GradientStart,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                if (selectedTab == 0) "${uiState.totalExpenses.toInt()} $currency" else "${uiState.totalIncome.toInt()} $currency",
                                style = MaterialTheme.typography.headlineSmall,
                                color = if (selectedTab == 0) Color(0xFFFF3D57) else Color(0xFF00C853),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            
            // Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TabButton("Dépenses", selectedTab == 0) { selectedTab = 0 }
                TabButton("Revenu", selectedTab == 1) { selectedTab = 1 }
                TabButton("Dette/Prêt", selectedTab == 2) { selectedTab = 2 }
            }
            
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Rechercher", color = Color.Gray) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = GradientStart,
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Transactions list - use filteredTransactions from ViewModel
            val displayTransactions = uiState.filteredTransactions.filter {
                when (selectedTab) {
                    0 -> it.type == TransactionType.EXPENSE
                    1 -> it.type == TransactionType.INCOME
                    2 -> it.type == TransactionType.DEBT
                    else -> false
                }
            }
            
            // Calculate totals based on whether a date is selected
            val displayExpenses = if (uiState.selectedDate != null) uiState.dailyExpenses else uiState.totalExpenses
            val displayIncome = if (uiState.selectedDate != null) uiState.dailyIncome else uiState.totalIncome
            
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Date header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            if (uiState.selectedDate != null) {
                                dateFormatter.format(Date(uiState.selectedDate!!)).replaceFirstChar { it.uppercase() }
                            } else {
                                SimpleDateFormat("EEE, dd MMMM", Locale.FRENCH).format(Date()).replaceFirstChar { it.uppercase() }
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            if (selectedTab == 0) "${displayExpenses.toInt()} $currency" else "${displayIncome.toInt()} $currency",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                items(displayTransactions) { transaction ->
                    val category = uiState.categories.find { it.id == transaction.categoryId }
                    // Use transaction.categoryName as fallback if category not found by ID
                    val displayCategoryName = category?.name ?: transaction.categoryName ?: "Transaction"
                    val categoryColor = try {
                        Color(android.graphics.Color.parseColor(category?.color ?: "#7D3FFF"))
                    } catch (e: Exception) {
                        GradientStart
                    }
                    
                    TransactionItem(
                        title = displayCategoryName,
                        description = transaction.notes ?: "Aucune description",
                        amount = transaction.amount,
                        time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(transaction.date)),
                        isExpense = transaction.type == TransactionType.EXPENSE,
                        iconColor = categoryColor,
                        categoryName = displayCategoryName,
                        currency = currency,
                        onClick = { onTransactionClick(transaction.id) }
                    )
                }
                
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
fun TabButton(text: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (selected) GradientStart else Color.White
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            color = if (selected) Color.White else Color.Gray,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun TransactionItem(
    title: String,
    description: String,
    amount: Double,
    time: String,
    isExpense: Boolean,
    iconColor: Color,
    categoryName: String = "",
    currency: String,
    onClick: () -> Unit = {}
) {
    // Map category names to icons
    val categoryIcon = when (categoryName.lowercase()) {
        "nourriture" -> Icons.Default.Restaurant
        "transport" -> Icons.Default.DirectionsCar
        "trafic" -> Icons.Default.DirectionsCar
        "logement" -> Icons.Default.Home
        "locations" -> Icons.Default.Home
        "médical" -> Icons.Default.MedicalServices
        "revenus" -> Icons.Default.Payments
        "investir" -> Icons.Default.TrendingUp
        "affaires" -> Icons.Default.Business
        "intérêt" -> Icons.Default.Percent
        "social" -> Icons.Default.People
        "shopping" -> Icons.Default.ShoppingCart
        "épicerie" -> Icons.Default.ShoppingBag
        "éducation" -> Icons.Default.School
        "education" -> Icons.Default.School
        "factures" -> Icons.Default.Receipt
        "investissement" -> Icons.Default.TrendingUp
        "cadeau" -> Icons.Default.CardGiftcard
        "transaction" -> Icons.Default.SwapHoriz
        "salaire" -> Icons.Default.AccountBalanceWallet
        "freelance" -> Icons.Default.Work
        else -> Icons.Default.Category
    }
    
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    categoryIcon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Title and description
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    categoryName.ifEmpty { title },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        description,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        maxLines = 1
                    )
                    Text(
                        time,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.LightGray
                    )
                }
            }
            
            // Amount
            Text(
                "${if (isExpense) "-" else "+"}${amount.toInt()} $currency",
                style = MaterialTheme.typography.titleMedium,
                color = if (isExpense) Color(0xFFFF3D57) else Color(0xFF00C853),
                fontWeight = FontWeight.Bold
            )
        }
    }
}
