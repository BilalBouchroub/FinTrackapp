package bilal.com.fintrack.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.size
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import bilal.com.fintrack.ui.navigation.FinTrackScreen

data class BottomNavItem(
    val name: String,
    val route: String,
    val icon: ImageVector
)

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        BottomNavItem("Accueil", FinTrackScreen.Home.route, Icons.Default.Home),
        BottomNavItem("Statistiques", FinTrackScreen.Reports.route, Icons.Default.PieChart),
        BottomNavItem("", "", Icons.Default.Add), // Placeholder for FAB
        BottomNavItem("Budget", FinTrackScreen.Budget.route, Icons.Default.AccountBalance),
        BottomNavItem("Paramètres", FinTrackScreen.Settings.route, Icons.Default.Settings)
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Only show bottom bar on main screens
    if (currentRoute in items.map { it.route }) {
        Box(modifier = Modifier.fillMaxWidth()) {
            androidx.compose.material3.NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                items.forEachIndexed { index, item ->
                    if (index == 2) {
                        // Empty space for FAB
                        Spacer(modifier = Modifier.weight(1f))
                    } else {
                        val selected = currentRoute == item.route
                        NavigationBarItem(
                            icon = { 
                                Icon(
                                    item.icon, 
                                    contentDescription = item.name,
                                    modifier = Modifier.size(24.dp)
                                ) 
                            },
                            label = { 
                                Text(
                                    item.name,
                                    style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            selected = selected,
                            colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                                selectedIconColor = bilal.com.fintrack.ui.theme.GradientStart,
                                selectedTextColor = bilal.com.fintrack.ui.theme.GradientStart,
                                indicatorColor = bilal.com.fintrack.ui.theme.GradientStart.copy(alpha = 0.1f),
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray
                            ),
                            onClick = {
                                if (item.route.isNotEmpty()) {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.startDestinationId) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
            
            // Floating Action Button
            androidx.compose.material3.FloatingActionButton(
                onClick = { navController.navigate(FinTrackScreen.AddTransaction.route) },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-28).dp),
                containerColor = bilal.com.fintrack.ui.theme.GradientStart,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Ajouter",
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}
