package bilal.com.fintrack.ui.screens.budget

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
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
import bilal.com.fintrack.data.local.entities.Budget
import bilal.com.fintrack.data.local.entities.BudgetPeriod
import bilal.com.fintrack.ui.AppViewModelProvider
import bilal.com.fintrack.ui.theme.GradientEnd
import bilal.com.fintrack.ui.theme.GradientStart

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    viewModel: BudgetViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedBudget by remember { mutableStateOf<Budget?>(null) }

    val context = androidx.compose.ui.platform.LocalContext.current
    val currency = remember(context) { bilal.com.fintrack.data.remote.TokenManager(context).getCurrency() }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Budget",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { showAddDialog = true }) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Ajouter budget",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Total Budget Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.2f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                "Budget Total",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            Text(
                                "${uiState.budgets.sumOf { it.amount }} $currency",
                                style = MaterialTheme.typography.headlineLarge,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = GradientStart,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Ajouter")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }
            
            items(uiState.budgets) { budget ->
                val category = uiState.categories.find { it.id == budget.categoryId }
                val spent = uiState.categoryExpenses[budget.categoryId] ?: 0.0
                val percentage = if (budget.amount > 0) (spent / budget.amount * 100).toFloat() else 0f
                
                BudgetCard(
                    budget = budget,
                    categoryName = category?.name ?: "Global",
                    categoryColor = category?.color ?: "#7D3FFF",
                    spent = spent,
                    percentage = percentage,
                    onEdit = {
                        selectedBudget = budget
                        showAddDialog = true
                    },
                    onDelete = { viewModel.deleteBudget(budget) }
                )
            }
            
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    if (showAddDialog) {
        AddBudgetDialog(
            budget = selectedBudget,
            categories = uiState.categories,
            onDismiss = {
                showAddDialog = false
                selectedBudget = null
            },
            onSave = { budget ->
                if (selectedBudget != null) {
                    viewModel.updateBudget(budget)
                } else {
                    viewModel.addBudget(budget)
                }
                showAddDialog = false
                selectedBudget = null
            }
        )
    }
}

@Composable
fun BudgetCard(
    budget: Budget,
    categoryName: String,
    categoryColor: String,
    spent: Double,
    percentage: Float,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val currency = remember(context) { bilal.com.fintrack.data.remote.TokenManager(context).getCurrency() }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                try { Color(android.graphics.Color.parseColor(categoryColor)) }
                                catch (e: Exception) { GradientStart }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Category,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            categoryName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            budget.period.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Modifier") },
                            onClick = {
                                showMenu = false
                                onEdit()
                            },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Supprimer") },
                            onClick = {
                                showMenu = false
                                onDelete()
                            },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Progress bar
            LinearProgressIndicator(
                progress = (percentage / 100f).coerceIn(0f, 1f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (percentage > 100) MaterialTheme.colorScheme.error else GradientStart,
                trackColor = Color(0xFFE0E0E0)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "${spent.toInt()} $currency dépensé",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "${budget.amount.toInt()} $currency",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBudgetDialog(
    budget: Budget?,
    categories: List<bilal.com.fintrack.data.local.entities.Category>,
    onDismiss: () -> Unit,
    onSave: (Budget) -> Unit
) {
    var amount by remember { mutableStateOf(budget?.amount?.toString() ?: "") }
    var selectedCategoryId by remember { mutableStateOf(budget?.categoryId) }
    var selectedPeriod by remember { mutableStateOf(budget?.period ?: BudgetPeriod.MONTHLY) }
    var expandedPeriod by remember { mutableStateOf(false) }
    
    val context = androidx.compose.ui.platform.LocalContext.current
    val currency = remember(context) { bilal.com.fintrack.data.remote.TokenManager(context).getCurrency() }
    
    // Initialize with first category if none selected
    LaunchedEffect(categories) {
        if (selectedCategoryId == null && categories.isNotEmpty()) {
            selectedCategoryId = categories.first().id
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(20.dp),
        title = { 
            Text(
                if (budget != null) "Modifier Budget" else "Ajouter Budget",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Montant
                Text(
                    "Montant du budget",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    placeholder = { Text("0", color = GradientStart, style = MaterialTheme.typography.headlineMedium) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.headlineMedium.copy(
                        color = GradientStart,
                        fontWeight = FontWeight.Bold
                    ),
                    trailingIcon = {
                        Text(currency, style = MaterialTheme.typography.titleLarge, color = Color.Gray)
                    },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    ),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.LightGray,
                        focusedBorderColor = GradientStart
                    )
                )
                
                // Catégorie
                Text(
                    "Catégorie",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.height(200.dp)
                ) {
                    items(categories) { category ->
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
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(categoryColor.copy(alpha = if (isSelected) 1f else 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    categoryIcon,
                                    contentDescription = category.name,
                                    tint = if (isSelected) Color.White else categoryColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                category.name,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) Color.Black else Color.Gray,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1
                            )
                        }
                    }
                }
                
                // Période
                Text(
                    "Période",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                ExposedDropdownMenuBox(
                    expanded = expandedPeriod,
                    onExpandedChange = { expandedPeriod = it }
                ) {
                    OutlinedTextField(
                        value = when(selectedPeriod) {
                            BudgetPeriod.WEEKLY -> "Hebdomadaire"
                            BudgetPeriod.MONTHLY -> "Mensuel"
                            BudgetPeriod.YEARLY -> "Annuel"
                        },
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPeriod) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.LightGray,
                            focusedBorderColor = GradientStart
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expandedPeriod,
                        onDismissRequest = { expandedPeriod = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Hebdomadaire") },
                            onClick = {
                                selectedPeriod = BudgetPeriod.WEEKLY
                                expandedPeriod = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Mensuel") },
                            onClick = {
                                selectedPeriod = BudgetPeriod.MONTHLY
                                expandedPeriod = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Annuel") },
                            onClick = {
                                selectedPeriod = BudgetPeriod.YEARLY
                                expandedPeriod = false
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amountVal = amount.toDoubleOrNull()
                    if (amountVal != null) {
                        onSave(
                            Budget(
                                id = budget?.id ?: 0,
                                categoryId = selectedCategoryId ?: 1L,
                                amount = amountVal,
                                period = selectedPeriod,
                                startDate = budget?.startDate ?: System.currentTimeMillis()
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = GradientStart
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(48.dp)
            ) {
                Text(
                    "Enregistrer",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.height(48.dp)
            ) {
                Text(
                    "Annuler",
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    )
}
