package bilal.com.fintrack.ui.screens.statistics

import android.graphics.Color as AndroidColor
import android.view.ViewGroup
import androidx.compose.foundation.background
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import bilal.com.fintrack.data.local.entities.TransactionType
import bilal.com.fintrack.ui.AppViewModelProvider
import bilal.com.fintrack.ui.theme.GradientEnd
import bilal.com.fintrack.ui.theme.GradientStart
import com.github.mikephil.charting.charts.BarChart as MPBarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.text.SimpleDateFormat
import java.util.*

enum class ChartType {
    BAR, DONUT
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedType by remember { mutableStateOf(TransactionType.INCOME) }
    var chartType by remember { mutableStateOf(ChartType.BAR) }
    
    val currentMonth = remember { SimpleDateFormat("MMMM, yyyy", Locale.FRENCH).format(Date()) }
    
    // Context pour les exports
    val context = androidx.compose.ui.platform.LocalContext.current
    
    // État pour les toasts
    var showToast by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf("") }
    
    // Afficher le toast
    if (showToast) {
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(3000)
            showToast = false
        }
    }
    
    // Vérifier le solde au chargement
    LaunchedEffect(uiState.totalBalance) {
        viewModel.checkBalanceAndNotify(context)
    }

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
                            "Statistiques",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        
                        // Export buttons
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // PDF Export button
                            IconButton(
                                onClick = {
                                    viewModel.exportToPdf(context) { file ->
                                        if (file != null) {
                                            toastMessage = "PDF exporté avec succès!"
                                            showToast = true
                                            bilal.com.fintrack.utils.ReportGenerator.shareFile(context, file)
                                        } else {
                                            toastMessage = "Erreur lors de l'export PDF"
                                            showToast = true
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f))
                            ) {
                                Icon(
                                    Icons.Default.PictureAsPdf,
                                    contentDescription = "Exporter en PDF",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            
                            // CSV Export button
                            IconButton(
                                onClick = {
                                    viewModel.exportToCsv(context) { file ->
                                        if (file != null) {
                                            toastMessage = "CSV exporté avec succès!"
                                            showToast = true
                                            bilal.com.fintrack.utils.ReportGenerator.shareFile(context, file)
                                        } else {
                                            toastMessage = "Erreur lors de l'export CSV"
                                            showToast = true
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f))
                            ) {
                                Icon(
                                    Icons.Default.TableChart,
                                    contentDescription = "Exporter en CSV",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Month selector and Chart type button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Month dropdown
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White.copy(alpha = 0.2f)
                            ),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    currentMonth,
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Icon(
                                    Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                            }
                        }
                        
                        // Chart type toggle button
                        Button(
                            onClick = { 
                                chartType = if (chartType == ChartType.BAR) ChartType.DONUT else ChartType.BAR
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White
                            ),
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                if (chartType == ChartType.BAR) Icons.Default.BarChart else Icons.Default.PieChart,
                                contentDescription = "Graphique",
                                tint = GradientStart
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Graphique",
                                color = GradientStart,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Type selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TypeChip(
                            text = "Dépenses",
                            selected = selectedType == TransactionType.EXPENSE,
                            onClick = { selectedType = TransactionType.EXPENSE }
                        )
                        TypeChip(
                            text = "Revenu",
                            selected = selectedType == TransactionType.INCOME,
                            onClick = { selectedType = TransactionType.INCOME }
                        )
                        TypeChip(
                            text = "Dette/Prêt",
                            selected = selectedType == TransactionType.DEBT,
                            onClick = { selectedType = TransactionType.DEBT }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }
            
            // Total balance card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Solde total",
                                style = MaterialTheme.typography.titleMedium,
                                color = GradientStart,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "${uiState.totalBalance.toInt()} MAD",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                        Icon(
                            Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = GradientStart
                        )
                    }
                }
            }
            
            // Chart
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (chartType == ChartType.BAR) {
                            BarChart(
                                data = uiState.categoryStats.filter { 
                                    it.type == selectedType 
                                }.take(4)
                            )
                        } else {
                            DonutChart(
                                data = uiState.categoryStats.filter { 
                                    it.type == selectedType 
                                }.take(3)
                            )
                        }
                    }
                }
            }
            
            // Category list
            items(uiState.categoryStats.filter { it.type == selectedType }) { stat ->
                CategoryStatItem(stat)
            }
            
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
        
        // Snackbar pour les messages
        if (showToast) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (toastMessage.contains("succès")) 
                            Color(0xFF4CAF50) 
                        else 
                            Color(0xFFF44336)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = toastMessage,
                        modifier = Modifier.padding(16.dp),
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun TypeChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) Color.White else Color.White.copy(alpha = 0.2f)
        ),
        shape = RoundedCornerShape(20.dp),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Text(
            text,
            color = if (selected) GradientStart else Color.White,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun BarChart(data: List<CategoryStat>) {
    if (data.isEmpty()) {
        Text("Aucune donnée", color = Color.Gray)
        return
    }
    
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            MPBarChart(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                
                description.isEnabled = false
                setTouchEnabled(true)
                isDragEnabled = false
                setScaleEnabled(false)
                setDrawGridBackground(false)
                setDrawBarShadow(false)
                
                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(false)
                    textColor = AndroidColor.GRAY
                }
                
                axisLeft.apply {
                    setDrawGridLines(true)
                    gridColor = AndroidColor.LTGRAY
                    textColor = AndroidColor.GRAY
                    axisMinimum = 0f
                }
                
                axisRight.isEnabled = false
                legend.isEnabled = false
            }
        },
        update = { barChart ->
            val entries = data.mapIndexed { index, stat ->
                BarEntry(index.toFloat(), stat.amount.toFloat())
            }
            
            val dataSet = BarDataSet(entries, "Catégories").apply {
                setDrawValues(false)
                colors = data.map { stat ->
                    try {
                        AndroidColor.parseColor(stat.categoryColor)
                    } catch (e: Exception) {
                        AndroidColor.parseColor("#6C63FF")
                    }
                }
            }
            
            barChart.data = BarData(dataSet)
            barChart.xAxis.valueFormatter = IndexAxisValueFormatter(data.map { it.categoryName })
            barChart.animateY(1000)
            barChart.invalidate()
        }
    )
}

@Composable
fun DonutChart(data: List<CategoryStat>) {
    if (data.isEmpty()) {
        Text("Aucune donnée", color = Color.Gray)
        return
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            PieChart(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                
                description.isEnabled = false
                setTouchEnabled(true)
                setDrawEntryLabels(false)
                
                // Configuration du trou central (donut)
                isDrawHoleEnabled = true
                holeRadius = 58f
                transparentCircleRadius = 61f
                setHoleColor(AndroidColor.TRANSPARENT)
                
                // Texte au centre
                setDrawCenterText(true)
                centerText = "Total\n${data.sumOf { it.amount }.toInt()} MAD"
                setCenterTextColor(AndroidColor.BLACK)
                setCenterTextSize(16f)
                
                legend.apply {
                    isEnabled = true
                    textColor = AndroidColor.GRAY
                    verticalAlignment = com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.BOTTOM
                    horizontalAlignment = com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.CENTER
                    orientation = com.github.mikephil.charting.components.Legend.LegendOrientation.HORIZONTAL
                    setDrawInside(false)
                }
            }
        },
        update = { pieChart ->
            val entries = data.map { stat ->
                PieEntry(stat.amount.toFloat(), stat.categoryName)
            }
            
            val dataSet = PieDataSet(entries, "").apply {
                sliceSpace = 3f
                selectionShift = 5f
                setDrawValues(true)
                valueTextSize = 12f
                valueTextColor = AndroidColor.WHITE
                
                colors = data.map { stat ->
                    try {
                        AndroidColor.parseColor(stat.categoryColor)
                    } catch (e: Exception) {
                        AndroidColor.parseColor("#6C63FF")
                    }
                }
            }
            
            pieChart.data = PieData(dataSet)
            pieChart.animateY(1000)
            pieChart.invalidate()
        }
    )
}

@Composable
fun CategoryStatItem(stat: CategoryStat) {
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
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        try { Color(AndroidColor.parseColor(stat.categoryColor)).copy(alpha = 0.2f) }
                        catch (e: Exception) { GradientStart.copy(alpha = 0.2f) }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    getCategoryIcon(stat.categoryName),
                    contentDescription = null,
                    tint = try { Color(AndroidColor.parseColor(stat.categoryColor)) }
                    catch (e: Exception) { GradientStart },
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Category info
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        stat.categoryName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "(${stat.amount.toInt()} MAD)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Progress bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFF5F6FA))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(stat.percentage / 100f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                try { Color(AndroidColor.parseColor(stat.categoryColor)) }
                                catch (e: Exception) { GradientStart }
                            )
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Percentage
            Text(
                "${stat.percentage.toInt()}%",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

fun getCategoryIcon(categoryName: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (categoryName.lowercase()) {
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
}
