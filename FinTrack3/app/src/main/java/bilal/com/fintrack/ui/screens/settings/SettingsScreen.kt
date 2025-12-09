package bilal.com.fintrack.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import bilal.com.fintrack.ui.theme.GradientEnd
import bilal.com.fintrack.ui.theme.GradientStart

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateUp: () -> Unit,
    onManageCategories: () -> Unit = {},
    onLogout: () -> Unit
) {
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
                        .padding(20.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "Paramètres",
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        containerColor = Color(0xFFF5F6FA)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            
            // Settings Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    // Language
                    SettingsItem(
                        icon = Icons.Default.Language,
                        title = "Langue",
                        value = "Français",
                        onClick = { /* TODO */ }
                    )
                    
                    Divider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = Color(0xFFF0F0F0)
                    )
                    
                    // Currency
                    SettingsItem(
                        icon = Icons.Default.AttachMoney,
                        title = "Monnaie",
                        value = "MAD",
                        onClick = { /* TODO */ }
                    )
                    
                    Divider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = Color(0xFFF0F0F0)
                    )
                    
                    // Categories Management
                    SettingsItem(
                        icon = Icons.Default.Category,
                        title = "Gérer les catégories",
                        value = null,
                        onClick = onManageCategories
                    )
                    
                    Divider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = Color(0xFFF0F0F0)
                    )
                    
                    // Rate App
                    SettingsItem(
                        icon = Icons.Default.Star,
                        title = "Évaluer",
                        value = null,
                        onClick = { /* TODO */ }
                    )
                    
                    Divider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = Color(0xFFF0F0F0)
                    )
                    
                    // Privacy Policy
                    SettingsItem(
                        icon = Icons.Default.Security,
                        title = "Politique de confidentialité",
                        value = null,
                        onClick = { /* TODO */ }
                    )
                    
                    Divider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = Color(0xFFF0F0F0)
                    )
                    
                    // Share
                    SettingsItem(
                        icon = Icons.Default.Share,
                        title = "Partager",
                        value = null,
                        onClick = { /* TODO */ }
                    )

                    Divider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = Color(0xFFF0F0F0)
                    )

                    // Logout
                    SettingsItem(
                        icon = Icons.Default.ExitToApp,
                        title = "Déconnexion",
                        value = null,
                        onClick = onLogout,
                        textColor = Color.Red,
                        iconColor = Color.Red
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Version Info
            Text(
                "FinTrack v1.0",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 100.dp)
            )
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    value: String?,
    onClick: () -> Unit,
    textColor: Color = Color.Black,
    iconColor: Color = GradientStart
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge,
                color = textColor,
                fontWeight = FontWeight.Medium
            )
        }
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (value != null) {
                Text(
                    value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = GradientStart,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
