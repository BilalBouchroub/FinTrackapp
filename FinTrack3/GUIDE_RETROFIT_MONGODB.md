# 🚀 Guide d'Utilisation - Retrofit avec MongoDB Atlas

## ✅ Ce qui a été créé

Voici tous les fichiers créés pour la communication avec votre backend Node.js :

### 📁 Structure des Fichiers

```
app/src/main/java/bilal/com/fintrack/
├── data/
│   ├── remote/
│   │   ├── api/
│   │   │   ├── AuthApiService.kt          ✅ API d'authentification
│   │   │   └── TransactionApiService.kt   ✅ API des transactions
│   │   ├── models/
│   │   │   ├── AuthModels.kt              ✅ Modèles d'authentification
│   │   │   └── TransactionModels.kt       ✅ Modèles de transactions
│   │   ├── RetrofitClient.kt              ✅ Client Retrofit principal
│   │   └── TokenManager.kt                ✅ Gestionnaire de tokens JWT
│   └── repository/
│       └── SyncTransactionRepository.kt   ✅ Repository avec sync cloud
```

---

## 🔧 Configuration Requise

### 1. **Modifier l'URL du Backend**

Ouvrez `RetrofitClient.kt` et changez `BASE_URL` selon votre environnement :

```kotlin
// Pour émulateur Android (backend local)
private const val BASE_URL = "http://10.0.2.2:5000/api/"

// Pour appareil réel (backend local)
private const val BASE_URL = "http://192.168.1.100:5000/api/" // Remplacez par votre IP

// Pour production (Render/Heroku)
private const val BASE_URL = "https://fintrack-backend.onrender.com/api/"
```

### 2. **Ajouter la Permission Internet**

Dans `AndroidManifest.xml`, ajoutez :

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

### 3. **Autoriser le HTTP (pour développement local)**

Dans `AndroidManifest.xml`, dans la balise `<application>` :

```xml
<application
    ...
    android:usesCleartextTraffic="true">
```

---

## 📖 Comment Utiliser

### Exemple 1 : Authentification avec Firebase et Backend

```kotlin
// Dans votre LoginActivity ou ViewModel

import bilal.com.fintrack.data.remote.RetrofitClient
import bilal.com.fintrack.data.remote.TokenManager
import bilal.com.fintrack.data.remote.models.RegisterRequest

class AuthViewModel(context: Context) {
    private val tokenManager = TokenManager(context)
    private val authApi = RetrofitClient.authApi
    
    suspend fun registerUser(firebaseUid: String, name: String, email: String) {
        try {
            val request = RegisterRequest(
                firebaseUid = firebaseUid,
                nom = name,
                email = email,
                devise = "MAD"
            )
            
            val response = authApi.register(request)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val token = response.body()?.token
                val user = response.body()?.user
                
                // Sauvegarder le token
                token?.let { tokenManager.saveToken(it) }
                
                // Sauvegarder les infos utilisateur
                user?.let {
                    tokenManager.saveUserInfo(it.id, it.email, it.nom)
                }
                
                println("✅ Inscription réussie!")
            } else {
                println("❌ Erreur: ${response.body()?.message}")
            }
        } catch (e: Exception) {
            println("❌ Erreur réseau: ${e.message}")
        }
    }
}
```

### Exemple 2 : Ajouter une Transaction avec Sync

```kotlin
// Dans votre TransactionViewModel

import bilal.com.fintrack.data.repository.SyncTransactionRepository

class TransactionViewModel(
    private val syncRepository: SyncTransactionRepository
) : ViewModel() {
    
    fun addTransaction(transaction: Transaction) {
        viewModelScope.launch {
            val result = syncRepository.insertTransaction(transaction)
            
            result.onSuccess {
                println("✅ Transaction ajoutée et synchronisée!")
            }.onFailure { error ->
                println("❌ Erreur: ${error.message}")
            }
        }
    }
}
```

### Exemple 3 : Synchroniser avec le Serveur

```kotlin
// Dans votre HomeViewModel ou SettingsScreen

fun syncWithCloud() {
    viewModelScope.launch {
        val result = syncRepository.syncWithServer()
        
        result.onSuccess {
            println("✅ Synchronisation réussie!")
        }.onFailure { error ->
            println("❌ Erreur de sync: ${error.message}")
        }
    }
}
```

---

## 🔄 Flux de Synchronisation

### Mode Automatique (Recommandé)

```
1. Utilisateur ajoute une transaction
   ↓
2. Transaction sauvegardée dans Room (local)
   ↓
3. Tentative d'envoi au serveur
   ↓
4. Si succès: Transaction synchronisée ✅
   Si échec: Transaction reste locale (sera synchro plus tard)
```

### Mode Manuel

```
1. Utilisateur clique sur "Synchroniser"
   ↓
2. Téléchargement des transactions du serveur
   ↓
3. Fusion avec les données locales
   ↓
4. Envoi des transactions locales non synchronisées
```

---

## 🛠️ Intégration dans l'Application

### Étape 1 : Modifier AppContainer

Ajoutez le `SyncTransactionRepository` dans `FinTrackApplication.kt` :

```kotlin
class AppContainer(private val context: Context) {
    // ... existant
    
    val syncTransactionRepository by lazy {
        SyncTransactionRepository(
            transactionDao = database.transactionDao(),
            context = context
        )
    }
}
```

### Étape 2 : Utiliser dans les ViewModels

Modifiez `TransactionViewModel` pour utiliser `SyncTransactionRepository` :

```kotlin
class TransactionViewModel(
    private val syncRepository: SyncTransactionRepository,
    // ... autres repositories
) : ViewModel() {
    
    // Utiliser syncRepository au lieu de transactionRepository
    val allTransactions = syncRepository.allTransactions
    
    fun addTransaction(transaction: Transaction) {
        viewModelScope.launch {
            syncRepository.insertTransaction(transaction)
        }
    }
}
```

### Étape 3 : Ajouter un Bouton de Synchronisation

Dans `SettingsScreen.kt` :

```kotlin
Button(
    onClick = {
        viewModel.syncWithCloud()
    }
) {
    Icon(Icons.Default.CloudSync, contentDescription = null)
    Text("Synchroniser avec le Cloud")
}
```

---

## 🔐 Gestion de l'Authentification

### Vérifier si l'utilisateur est connecté

```kotlin
val tokenManager = TokenManager(context)

if (tokenManager.isLoggedIn()) {
    // Utilisateur connecté
    val userName = tokenManager.getUserName()
    println("Bonjour $userName!")
} else {
    // Rediriger vers login
    navController.navigate("login")
}
```

### Déconnexion

```kotlin
fun logout() {
    tokenManager.clearAll()
    // Rediriger vers login
    navController.navigate("login")
}
```

---

## 📊 Endpoints API Disponibles

### Authentification

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| POST | `/auth/register` | Inscription |
| POST | `/auth/login` | Connexion |
| GET | `/auth/me` | Profil utilisateur |

### Transactions

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/transactions` | Liste des transactions |
| GET | `/transactions/:id` | Une transaction |
| POST | `/transactions` | Créer une transaction |
| PUT | `/transactions/:id` | Modifier une transaction |
| DELETE | `/transactions/:id` | Supprimer une transaction |
| POST | `/transactions/sync` | Synchroniser plusieurs |

---

## 🐛 Debug et Tests

### Voir les Logs Retrofit

Les logs HTTP sont activés automatiquement. Dans Logcat, filtrez par "OkHttp" :

```
D/OkHttp: --> POST http://10.0.2.2:5000/api/transactions
D/OkHttp: Content-Type: application/json
D/OkHttp: {"amount":100.0,"type":"EXPENSE",...}
D/OkHttp: --> END POST
D/OkHttp: <-- 200 OK
```

### Tester avec Postman

Avant d'utiliser dans l'app, testez vos endpoints :

1. Ouvrir Postman
2. POST `http://localhost:5000/api/auth/register`
3. Body (JSON) :
   ```json
   {
     "firebaseUid": "test123",
     "nom": "Test User",
     "email": "test@example.com",
     "devise": "MAD"
   }
   ```

---

## ⚠️ Gestion des Erreurs

### Erreur de Connexion

```kotlin
try {
    val response = api.getAllTransactions(token)
    // ...
} catch (e: java.net.UnknownHostException) {
    println("❌ Pas de connexion internet")
} catch (e: java.net.SocketTimeoutException) {
    println("❌ Timeout - Serveur trop lent")
} catch (e: Exception) {
    println("❌ Erreur: ${e.message}")
}
```

### Erreur 401 (Non autorisé)

```kotlin
if (response.code() == 401) {
    // Token expiré, déconnecter l'utilisateur
    tokenManager.clearAll()
    navController.navigate("login")
}
```

---

## 🚀 Prochaines Étapes

1. ✅ **Tester l'authentification**
   - Créer un compte via Firebase
   - Enregistrer dans MongoDB via l'API

2. ✅ **Tester la synchronisation**
   - Ajouter une transaction
   - Vérifier qu'elle apparaît dans MongoDB Atlas

3. ✅ **Implémenter la synchronisation automatique**
   - WorkManager pour sync périodique
   - Sync au démarrage de l'app

4. ✅ **Gérer le mode offline**
   - Queue de synchronisation
   - Indicateur de statut de sync

---

## 📞 Support

Si vous rencontrez des problèmes :

1. Vérifiez que le backend Node.js est démarré
2. Vérifiez l'URL dans `RetrofitClient.kt`
3. Vérifiez les logs dans Logcat
4. Testez les endpoints avec Postman

---

**Votre application est maintenant prête à communiquer avec MongoDB Atlas via Node.js ! 🎉**
