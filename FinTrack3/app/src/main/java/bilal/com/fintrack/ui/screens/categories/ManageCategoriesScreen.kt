package bilal.com.fintrack.ui.screens.categories

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import bilal.com.fintrack.data.local.entities.Category
import bilal.com.fintrack.ui.AppViewModelProvider
import bilal.com.fintrack.ui.theme.GradientStart

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageCategoriesScreen(
    viewModel: CategoryViewModel = viewModel(factory = AppViewModelProvider.Factory),
    onNavigateUp: () -> Unit,
    onAddCategory: () -> Unit
) {
    val categories by viewModel.allCategories.collectAsState(initial = emptyList())
    var showDeleteDialog by remember { mutableStateOf(false) }
    var categoryToDelete by remember { mutableStateOf<Category?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Gérer les catégories",
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddCategory,
                containerColor = GradientStart,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Ajouter catégorie")
            }
        },
        containerColor = Color(0xFFF5F6FA)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }
            
            // Income Categories Section
            item {
                Text(
                    "Catégories de Revenu",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = GradientStart,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            val incomeCategories = categories.filter { 
                it.name in listOf("Investir", "Affaires", "Intérêt", "Revenus") 
            }
            
            items(incomeCategories) { category ->
                CategoryListItem(
                    category = category,
                    onDelete = {
                        categoryToDelete = category
                        showDeleteDialog = true
                    }
                )
            }
            
            item { Spacer(modifier = Modifier.height(16.dp)) }
            
            // Expense Categories Section
            item {
                Text(
                    "Catégories de Dépenses",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = GradientStart,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            val expenseCategories = categories.filter { 
                it.name !in listOf("Investir", "Affaires", "Intérêt", "Revenus") 
            }
            
            items(expenseCategories) { category ->
                CategoryListItem(
                    category = category,
                    onDelete = {
                        categoryToDelete = category
                        showDeleteDialog = true
                    }
                )
            }
            
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
    
    // Delete Confirmation Dialog
    if (showDeleteDialog && categoryToDelete != null) {
        AlertDialog(
            onDismissRequest = { 
                showDeleteDialog = false
                categoryToDelete = null
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
                    "Supprimer la catégorie ?",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Voulez-vous vraiment supprimer la catégorie \"${categoryToDelete?.name}\" ?",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    if (categoryToDelete?.isCustom == false) {
                        Text(
                            "⚠️ Cette catégorie est une catégorie par défaut. Les transactions associées seront affectées.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFFF9800)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        categoryToDelete?.let { viewModel.deleteCategory(it) }
                        showDeleteDialog = false
                        categoryToDelete = null
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
                    onClick = { 
                        showDeleteDialog = false
                        categoryToDelete = null
                    }
                ) {
                    Text("Annuler", color = Color.Gray, fontWeight = FontWeight.Medium)
                }
            }
        )
    }
}

@Composable
fun CategoryListItem(
    category: Category,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Category Icon
                val categoryColor = try {
                    Color(android.graphics.Color.parseColor(category.color))
                } catch (e: Exception) {
                    GradientStart
                }
                
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
                
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(categoryColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        categoryIcon,
                        contentDescription = null,
                        tint = categoryColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Column {
                    Text(
                        category.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    if (category.isCustom) {
                        Text(
                            "Personnalisée",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
            }
            
            // Delete button (only for custom categories)
            if (category.isCustom) {
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Supprimer",
                        tint = Color(0xFFFF5252)
                    )
                }
            }
        }
    }
}
