package bilal.com.fintrack.ui.screens.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import bilal.com.fintrack.data.local.entities.Transaction
import bilal.com.fintrack.data.local.entities.TransactionType
import bilal.com.fintrack.ui.AppViewModelProvider
import bilal.com.fintrack.ui.theme.GradientStart
import java.text.SimpleDateFormat
import java.util.*

data class CategoryIcon(
    val name: String,
    val icon: ImageVector,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    viewModel: TransactionViewModel = viewModel(factory = AppViewModelProvider.Factory),
    onNavigateUp: () -> Unit,
    onManageCategories: () -> Unit = {}
) {
    var amount by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }
    var selectedBudgetId by remember { mutableStateOf<Long?>(null) }
    var showBudgetDropdown by remember { mutableStateOf(false) }
    var showBudgetWarning by remember { mutableStateOf(false) }
    var pendingTransaction by remember { mutableStateOf<Transaction?>(null) }
    
    val uiState by viewModel.uiState.collectAsState()
    
    // Initialize with first category when categories are loaded
    LaunchedEffect(uiState.categories) {
        if (selectedCategoryId == null && uiState.categories.isNotEmpty()) {
            selectedCategoryId = uiState.categories.first().id
        }
    }
    
    // Reset category when switching between Income/Expense
    LaunchedEffect(selectedType) {
        if (uiState.categories.isNotEmpty()) {
            val appropriateCategories = if (selectedType == TransactionType.INCOME) {
                uiState.categories.filter { it.name in listOf("Investir", "Affaires", "Intérêt", "Revenus") }
            } else {
                uiState.categories.filter { it.name !in listOf("Investir", "Affaires", "Intérêt", "Revenus") }
            }
            // Reset to first appropriate category
            selectedCategoryId = appropriateCategories.firstOrNull()?.id
        }
    }
    
    val currentDate = remember { SimpleDateFormat("dd MMMM yyyy", Locale.FRENCH).format(Date()) }
    val currentTime = remember { SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()) }

    // Category icons for INCOME
    val incomeCategories = listOf(
        CategoryIcon("Investir", Icons.Default.TrendingUp, Color(0xFF7ED957)),
        CategoryIcon("Affaires", Icons.Default.Business, Color(0xFF69B4FF)),
        CategoryIcon("Intérêt", Icons.Default.Percent, Color(0xFFB39DDB)),
        CategoryIcon("Revenus", Icons.Default.Payments, Color(0xFFFF9F9F))
    )
    
    // Category icons for EXPENSE
    val expenseCategories = listOf(
        CategoryIcon("Nourriture", Icons.Default.Restaurant, Color(0xFFFFC107)),
        CategoryIcon("Social", Icons.Default.People, Color(0xFFFFAB91)),
        CategoryIcon("Trafic", Icons.Default.DirectionsCar, Color(0xFF64B5F6)),
        CategoryIcon("Shopping", Icons.Default.ShoppingCart, Color(0xFFFF80AB)),
        CategoryIcon("Épicerie", Icons.Default.ShoppingBag, Color(0xFF81C784)),
        CategoryIcon("Education", Icons.Default.School, Color(0xFFB39DDB)),
        CategoryIcon("Factures", Icons.Default.Receipt, Color(0xFF80DEEA)),
        CategoryIcon("Locations", Icons.Default.Home, Color(0xFFFFD54F)),
        CategoryIcon("Médical", Icons.Default.MedicalServices, Color(0xFFEF9A9A)),
        CategoryIcon("Investissement", Icons.Default.ShowChart, Color(0xFF80CBC4)),
        CategoryIcon("Cadeau", Icons.Default.CardGiftcard, Color(0xFFCE93D8)),
        CategoryIcon("Autre", Icons.Default.MoreHoriz, Color(0xFF90CAF9))
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Ajouter",
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    TextButton(onClick = {
                        val amountVal = amount.toDoubleOrNull()
                        if (amountVal != null) {
                            // Check if transaction exceeds budget
                            if (selectedType == TransactionType.EXPENSE && selectedCategoryId != null) {
                                val budget = uiState.budgets.find { it.categoryId == selectedCategoryId }
                                if (budget != null) {
                                    val currentSpent = uiState.categoryExpenses[selectedCategoryId] ?: 0.0
                                    val newTotal = currentSpent + amountVal
                                    
                                    if (newTotal > budget.amount) {
                                        // Show warning dialog
                                        showBudgetWarning = true
                                        pendingTransaction = Transaction(
                                            amount = amountVal,
                                            type = selectedType,
                                            categoryId = selectedCategoryId ?: 1L,
                                            paymentMethod = "Cash",
                                            date = System.currentTimeMillis(),
                                            notes = notes.ifEmpty { null }
                                        )
                                        return@TextButton
                                    }
                                }
                            }
                            
                            // Save transaction directly if no budget issue
                            viewModel.addTransaction(
                                Transaction(
                                    amount = amountVal,
                                    type = selectedType,
                                    categoryId = selectedCategoryId ?: 1L,
                                    paymentMethod = "Cash",
                                    date = System.currentTimeMillis(),
                                    notes = notes.ifEmpty { null }
                                )
                            )
                            onNavigateUp()
                        }
                    }) {
                        Text(
                            "Sauvegarder",
                            color = GradientStart,
                            fontWeight = FontWeight.Bold
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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Type Selection (Dépenses / Revenu / Dette/Prêt)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TypeTab("Dépenses", selectedType == TransactionType.EXPENSE) { 
                    selectedType = TransactionType.EXPENSE 
                    selectedCategoryId = null
                }
                TypeTab("Revenu", selectedType == TransactionType.INCOME) { 
                    selectedType = TransactionType.INCOME
                    selectedCategoryId = null
                }
                TypeTab("Dette/Prêt", selectedType == TransactionType.DEBT) { 
                    selectedType = TransactionType.DEBT
                    selectedCategoryId = null
                }
            }

            // Montant
            Text("Montant", fontWeight = FontWeight.Bold, color = Color.Black)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    placeholder = { Text("10,000", color = GradientStart, style = MaterialTheme.typography.headlineMedium) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    textStyle = MaterialTheme.typography.headlineMedium.copy(
                        color = GradientStart,
                        fontWeight = FontWeight.Bold
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent
                    )
                )
                Text("MAD", style = MaterialTheme.typography.titleLarge, color = Color.Gray)
            }


            // Catégorie
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Catégorie", fontWeight = FontWeight.Bold, color = Color.Black)
                
                TextButton(
                    onClick = onManageCategories,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "Gérer",
                        tint = GradientStart,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Gérer",
                        color = GradientStart,
                        fontWeight = FontWeight.Medium,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            if (uiState.categories.isEmpty()) {
                Text(
                    "Chargement des catégories...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                // Filter categories based on transaction type
                val filteredCategories = if (selectedType == TransactionType.INCOME) {
                    // Show only income categories
                    uiState.categories.filter { 
                        it.name in listOf("Investir", "Affaires", "Intérêt", "Revenus")
                    }
                } else {
                    // Show only expense categories
                    uiState.categories.filter { 
                        it.name !in listOf("Investir", "Affaires", "Intérêt", "Revenus")
                    }
                }
                
                // Calculate dynamic height based on number of categories
                val rowCount = (filteredCategories.size + 3) / 4 // Round up division
                val gridHeight = (rowCount * 90).dp // 90dp per row (64dp icon + 4dp spacing + 22dp text)
                
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.height(gridHeight)
                ) {
                    items(filteredCategories) { category ->
                        val isSelected = selectedCategoryId == category.id
                        
                        // Map category to icon
                        val categoryIcon = when (category.name.lowercase()) {
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
                        
                        val categoryColor = try {
                            Color(android.graphics.Color.parseColor(category.color))
                        } catch (e: Exception) {
                            GradientStart
                        }
                        
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { 
                                selectedCategoryId = category.id
                            }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(categoryColor.copy(alpha = if (isSelected) 1f else 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    categoryIcon,
                                    contentDescription = category.name,
                                    tint = if (isSelected) Color.White else categoryColor,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                category.name,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) Color.Black else Color.Gray,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // Description
            Text("Description", fontWeight = FontWeight.Bold, color = Color.Black)
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                placeholder = { Text("Ajouter une description", color = Color.LightGray) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = GradientStart,
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                )
            )

            // Budget (only for expenses)
            if (selectedType == TransactionType.EXPENSE) {
                Text("Budget", fontWeight = FontWeight.Bold, color = Color.Black)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showBudgetDropdown = !showBudgetDropdown },
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Ueu", color = Color.Gray)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = GradientStart)
                    }
                }
            }

            // Date and Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null, tint = GradientStart)
                    Text(currentDate, color = Color.Gray)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.AccessTime, contentDescription = null, tint = GradientStart)
                    Text(currentTime, color = Color.Gray)
                }
            }
        }
    }
    
    // Budget Warning Dialog
    if (showBudgetWarning && pendingTransaction != null) {
        val budget = uiState.budgets.find { it.categoryId == selectedCategoryId }
        val category = uiState.categories.find { it.id == selectedCategoryId }
        val currentSpent = uiState.categoryExpenses[selectedCategoryId] ?: 0.0
        val newTotal = currentSpent + (pendingTransaction?.amount ?: 0.0)
        val exceededAmount = newTotal - (budget?.amount ?: 0.0)
        
        AlertDialog(
            onDismissRequest = { 
                showBudgetWarning = false
                pendingTransaction = null
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp),
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFFF9800),
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    "⚠️ Budget Dépassé !",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF9800)
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Vous êtes sur le point de dépasser votre budget pour ${category?.name ?: "cette catégorie"} !",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFF3E0)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Budget:", fontWeight = FontWeight.Medium)
                                Text("${budget?.amount?.toInt() ?: 0} MAD", fontWeight = FontWeight.Bold)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Déjà dépensé:", fontWeight = FontWeight.Medium)
                                Text("${currentSpent.toInt()} MAD", color = Color(0xFFFF9800))
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Cette transaction:", fontWeight = FontWeight.Medium)
                                Text("+${pendingTransaction?.amount?.toInt() ?: 0} MAD", color = Color.Red)
                            }
                            Divider(color = Color(0xFFFFCC80))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Nouveau total:", fontWeight = FontWeight.Bold)
                                Text(
                                    "${newTotal.toInt()} MAD",
                                    color = Color.Red,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Dépassement:", fontWeight = FontWeight.Bold)
                                Text(
                                    "+${exceededAmount.toInt()} MAD",
                                    color = Color.Red,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    
                    Text(
                        "Voulez-vous quand même enregistrer cette transaction ?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        pendingTransaction?.let { viewModel.addTransaction(it) }
                        showBudgetWarning = false
                        pendingTransaction = null
                        onNavigateUp()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF9800)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Continuer quand même", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showBudgetWarning = false
                        pendingTransaction = null
                    }
                ) {
                    Text("Annuler", color = Color.Gray, fontWeight = FontWeight.Medium)
                }
            }
        )
    }
}

@Composable
fun TypeTab(text: String, selected: Boolean, onClick: () -> Unit) {
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
