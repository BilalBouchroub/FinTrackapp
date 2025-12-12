package bilal.com.fintrack.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bilal.com.fintrack.data.remote.TokenManager
import bilal.com.fintrack.ui.theme.GradientEnd
import bilal.com.fintrack.ui.theme.GradientStart

import androidx.compose.ui.res.stringResource
import bilal.com.fintrack.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateUp: () -> Unit,
    onManageCategories: () -> Unit = {},
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    
    val userName = remember { tokenManager.getUserName() ?: context.getString(R.string.default_user) }
    val userEmail = remember { tokenManager.getUserEmail() ?: context.getString(R.string.default_email) }
    
    // Etats pour les dialogues
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    
    // Etats pour les valeurs sélectionnées
    var selectedCurrency by remember { mutableStateOf("MAD") }
    // On garde le nom de la langue en "dur" pour l'affichage de la sélection actuelle, 
    // ou on pourrait mapper si nécessaire, mais "Français" et "English" sont universels
    var selectedLanguage by remember { mutableStateOf("Français") }
    
    val currencies = listOf("MAD", "USD", "EUR", "GBP")
    val currencyDisplayMap = mapOf(
        "MAD" to "MAD",
        "USD" to "USD ($)",
        "EUR" to "EUR (€)",
        "GBP" to "GBP (£)"
    )
    
    val languages = listOf("Français", "English")
    val languageMap = mapOf(
        "Français" to "fr",
        "English" to "en"
    )

    // Charger les états initiaux
    LaunchedEffect(Unit) {
        selectedCurrency = tokenManager.getCurrency()
        val savedLang = tokenManager.getLanguage()
        selectedLanguage = languageMap.entries.find { it.value == savedLang }?.key ?: "Français"
    }

    if (showCurrencyDialog) {
        AlertDialog(
            onDismissRequest = { showCurrencyDialog = false },
            title = { Text(stringResource(R.string.dialog_currency_title)) },
            text = {
                Column {
                    currencies.forEach { currency ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedCurrency = currency
                                    tokenManager.saveCurrency(currency)
                                    showCurrencyDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (currency == selectedCurrency),
                                onClick = {
                                    selectedCurrency = currency
                                    tokenManager.saveCurrency(currency)
                                    showCurrencyDialog = false
                                }
                            )
                            Text(
                                text = currencyDisplayMap[currency] ?: currency,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCurrencyDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(stringResource(R.string.dialog_language_title)) },
            text = {
                Column {
                    languages.forEach { language ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    // 1. Sauvegarder
                                    val langCode = languageMap[language] ?: "fr"
                                    tokenManager.saveLanguage(langCode)
                                    selectedLanguage = language
                                    showLanguageDialog = false
                                    
                                    // 2. Restart App
                                    val intent = android.content.Intent(context, bilal.com.fintrack.MainActivity::class.java)
                                    intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                    context.startActivity(intent)
                                    (context as? android.app.Activity)?.finish()
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (language == selectedLanguage),
                                onClick = {
                                    val langCode = languageMap[language] ?: "fr"
                                    tokenManager.saveLanguage(langCode)
                                    selectedLanguage = language
                                    showLanguageDialog = false
                                    
                                    val intent = android.content.Intent(context, bilal.com.fintrack.MainActivity::class.java)
                                    intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                    context.startActivity(intent)
                                    (context as? android.app.Activity)?.finish()
                                }
                            )
                            Text(
                                text = language,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
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
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        stringResource(R.string.profile_title),
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Avatar et Infos Utilisateur
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userName.take(1).uppercase(),
                            style = MaterialTheme.typography.headlineLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = userName,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = userEmail,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
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
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
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
                        title = stringResource(R.string.settings_language),
                        value = selectedLanguage,
                        onClick = { showLanguageDialog = true }
                    )
                    
                    Divider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = Color(0xFFF0F0F0)
                    )
                    
                    // Currency
                    SettingsItem(
                        icon = Icons.Default.AttachMoney,
                        title = stringResource(R.string.settings_currency),
                        value = selectedCurrency,
                        onClick = { showCurrencyDialog = true }
                    )
                    
                    Divider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = Color(0xFFF0F0F0)
                    )
                    
                    // Categories Management
                    SettingsItem(
                        icon = Icons.Default.Category,
                        title = stringResource(R.string.settings_categories),
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
                        title = stringResource(R.string.settings_rate),
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
                        title = stringResource(R.string.settings_privacy),
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
                        title = stringResource(R.string.settings_share),
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
                        title = stringResource(R.string.settings_logout),
                        value = null,
                        onClick = onLogout,
                        textColor = Color.Red,
                        iconColor = Color.Red
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Version Info
            Text(
                "FinTrack v1.0",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 32.dp)
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
