# 🚀 Prochaines Étapes - Connexion MongoDB Atlas

## 📋 Vue d'Ensemble

Ce document décrit les étapes à suivre pour connecter votre application FinTrack3 à MongoDB Atlas via un backend Node.js.

---

## ✅ Ce qui est déjà fait

1. ✅ **Classe User créée** - Prête à être synchronisée avec MongoDB
2. ✅ **UserRepository créé** - Gère les opérations sur les utilisateurs
3. ✅ **Base de données locale** - Room Database avec toutes les entités
4. ✅ **Exports PDF/CSV** - Fonctionnalités d'export complètes
5. ✅ **Notifications** - Système de notifications fonctionnel

---

## 🎯 Étapes à Suivre

### Phase 1 : Configuration MongoDB Atlas (Cloud)

#### Étape 1.1 : Créer un compte MongoDB Atlas
1. Aller sur https://www.mongodb.com/cloud/atlas
2. Cliquer sur "Try Free"
3. Créer un compte avec votre email
4. Vérifier votre email

#### Étape 1.2 : Créer un Cluster
1. Dans le dashboard, cliquer sur "Build a Database"
2. Choisir "M0 Free" (gratuit)
3. Sélectionner une région proche (Europe - Frankfurt ou Paris)
4. Nommer votre cluster : `fintrack-cluster`
5. Cliquer sur "Create Cluster"

#### Étape 1.3 : Configurer la Sécurité
1. **Créer un utilisateur de base de données :**
   - Username : `fintrack_admin`
   - Password : (générer un mot de passe fort et le noter)
   - Rôle : `Read and write to any database`

2. **Configurer l'accès réseau :**
   - Aller dans "Network Access"
   - Cliquer sur "Add IP Address"
   - Choisir "Allow Access from Anywhere" (0.0.0.0/0)
   - (Pour la production, limitez aux IPs spécifiques)

#### Étape 1.4 : Obtenir la Connection String
1. Cliquer sur "Connect" sur votre cluster
2. Choisir "Connect your application"
3. Sélectionner "Node.js" et version "4.1 or later"
4. Copier la connection string :
   ```
   mongodb+srv://fintrack_admin:<password>@fintrack-cluster.xxxxx.mongodb.net/?retryWrites=true&w=majority
   ```
5. Remplacer `<password>` par votre mot de passe

---

### Phase 2 : Créer le Backend Node.js

#### Étape 2.1 : Créer le Projet
```bash
# Créer un nouveau dossier
mkdir fintrack-backend
cd fintrack-backend

# Initialiser le projet Node.js
npm init -y
```

#### Étape 2.2 : Installer les Dépendances
```bash
npm install express mongoose cors dotenv bcryptjs jsonwebtoken
npm install --save-dev nodemon
```

**Dépendances installées :**
- `express` - Framework web
- `mongoose` - ODM pour MongoDB
- `cors` - Autoriser les requêtes cross-origin
- `dotenv` - Gérer les variables d'environnement
- `bcryptjs` - Hasher les mots de passe
- `jsonwebtoken` - Authentification JWT
- `nodemon` - Redémarrage automatique (dev)

#### Étape 2.3 : Structure du Projet
```
fintrack-backend/
├── .env                    # Variables d'environnement
├── .gitignore             # Fichiers à ignorer
├── package.json           # Dépendances
├── server.js              # Point d'entrée
├── config/
│   └── database.js        # Configuration MongoDB
├── models/
│   ├── User.js            # Modèle User
│   ├── Transaction.js     # Modèle Transaction
│   ├── Category.js        # Modèle Category
│   └── Budget.js          # Modèle Budget
├── routes/
│   ├── auth.js            # Routes d'authentification
│   ├── users.js           # Routes utilisateurs
│   ├── transactions.js    # Routes transactions
│   ├── categories.js      # Routes catégories
│   └── budgets.js         # Routes budgets
├── controllers/
│   ├── authController.js
│   ├── userController.js
│   ├── transactionController.js
│   ├── categoryController.js
│   └── budgetController.js
└── middleware/
    └── auth.js            # Middleware d'authentification
```

#### Étape 2.4 : Créer le fichier .env
```env
PORT=5000
MONGODB_URI=mongodb+srv://fintrack_admin:<password>@fintrack-cluster.xxxxx.mongodb.net/fintrack?retryWrites=true&w=majority
JWT_SECRET=votre_secret_jwt_tres_long_et_complexe
NODE_ENV=development
```

#### Étape 2.5 : Créer les Modèles MongoDB

**Exemple : models/User.js**
```javascript
const mongoose = require('mongoose');

const userSchema = new mongoose.Schema({
  firebaseUid: {
    type: String,
    required: true,
    unique: true
  },
  nom: {
    type: String,
    required: true
  },
  email: {
    type: String,
    required: true,
    unique: true
  },
  devise: {
    type: String,
    default: 'MAD'
  },
  dateInscription: {
    type: Date,
    default: Date.now
  }
}, {
  timestamps: true
});

module.exports = mongoose.model('User', userSchema);
```

**Exemple : models/Transaction.js**
```javascript
const mongoose = require('mongoose');

const transactionSchema = new mongoose.Schema({
  userId: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User',
    required: true
  },
  amount: {
    type: Number,
    required: true
  },
  type: {
    type: String,
    enum: ['INCOME', 'EXPENSE', 'DEBT'],
    required: true
  },
  categoryId: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'Category',
    required: true
  },
  paymentMethod: {
    type: String,
    required: true
  },
  date: {
    type: Date,
    required: true
  },
  notes: String
}, {
  timestamps: true
});

module.exports = mongoose.model('Transaction', transactionSchema);
```

#### Étape 2.6 : Créer les Routes API

**Exemple : routes/transactions.js**
```javascript
const express = require('express');
const router = express.Router();
const auth = require('../middleware/auth');
const transactionController = require('../controllers/transactionController');

// Toutes les routes nécessitent l'authentification
router.use(auth);

// GET /api/transactions - Récupérer toutes les transactions
router.get('/', transactionController.getAllTransactions);

// GET /api/transactions/:id - Récupérer une transaction
router.get('/:id', transactionController.getTransaction);

// POST /api/transactions - Créer une transaction
router.post('/', transactionController.createTransaction);

// PUT /api/transactions/:id - Mettre à jour une transaction
router.put('/:id', transactionController.updateTransaction);

// DELETE /api/transactions/:id - Supprimer une transaction
router.delete('/:id', transactionController.deleteTransaction);

module.exports = router;
```

#### Étape 2.7 : Créer le serveur principal

**server.js**
```javascript
const express = require('express');
const mongoose = require('mongoose');
const cors = require('cors');
require('dotenv').config();

const app = express();

// Middleware
app.use(cors());
app.use(express.json());

// Routes
app.use('/api/auth', require('./routes/auth'));
app.use('/api/users', require('./routes/users'));
app.use('/api/transactions', require('./routes/transactions'));
app.use('/api/categories', require('./routes/categories'));
app.use('/api/budgets', require('./routes/budgets'));

// Connexion à MongoDB
mongoose.connect(process.env.MONGODB_URI)
  .then(() => console.log('✅ Connecté à MongoDB Atlas'))
  .catch(err => console.error('❌ Erreur MongoDB:', err));

// Démarrer le serveur
const PORT = process.env.PORT || 5000;
app.listen(PORT, () => {
  console.log(`🚀 Serveur démarré sur le port ${PORT}`);
});
```

#### Étape 2.8 : Tester le Backend
```bash
# Démarrer le serveur
npm run dev

# Vous devriez voir :
# ✅ Connecté à MongoDB Atlas
# 🚀 Serveur démarré sur le port 5000
```

---

### Phase 3 : Déployer le Backend

#### Option 1 : Render (Recommandé - Gratuit)

1. **Créer un compte sur Render.com**
2. **Pousser le code sur GitHub**
   ```bash
   git init
   git add .
   git commit -m "Initial commit"
   git remote add origin https://github.com/votre-username/fintrack-backend.git
   git push -u origin main
   ```

3. **Créer un Web Service sur Render**
   - Connecter votre repository GitHub
   - Build Command : `npm install`
   - Start Command : `node server.js`
   - Ajouter les variables d'environnement (.env)

4. **Obtenir l'URL de déploiement**
   - Exemple : `https://fintrack-backend.onrender.com`

#### Option 2 : Heroku

1. **Créer un compte Heroku**
2. **Installer Heroku CLI**
3. **Déployer :**
   ```bash
   heroku login
   heroku create fintrack-backend
   git push heroku main
   ```

---

### Phase 4 : Modifier l'Application Android

#### Étape 4.1 : Ajouter Retrofit

**Dans `build.gradle.kts` (app):**
```kotlin
dependencies {
    // Retrofit pour les appels API
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
    
    // Existant...
}
```

#### Étape 4.2 : Créer les Services API

**Créer : `data/remote/ApiService.kt`**
```kotlin
interface ApiService {
    @GET("transactions")
    suspend fun getAllTransactions(@Header("Authorization") token: String): List<Transaction>
    
    @POST("transactions")
    suspend fun createTransaction(
        @Header("Authorization") token: String,
        @Body transaction: Transaction
    ): Transaction
    
    @PUT("transactions/{id}")
    suspend fun updateTransaction(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body transaction: Transaction
    ): Transaction
    
    @DELETE("transactions/{id}")
    suspend fun deleteTransaction(
        @Header("Authorization") token: String,
        @Path("id") id: String
    )
}
```

#### Étape 4.3 : Créer le Client Retrofit

**Créer : `data/remote/RetrofitClient.kt`**
```kotlin
object RetrofitClient {
    private const val BASE_URL = "https://fintrack-backend.onrender.com/api/"
    
    val instance: ApiService by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
        
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
```

#### Étape 4.4 : Modifier les Repositories

**Exemple : TransactionRepository avec sync**
```kotlin
class TransactionRepository(
    private val transactionDao: TransactionDao,
    private val apiService: ApiService
) {
    val allTransactions: Flow<List<Transaction>> = transactionDao.getAllTransactions()
    
    // Synchroniser avec le serveur
    suspend fun syncTransactions(token: String) {
        try {
            // Récupérer du serveur
            val serverTransactions = apiService.getAllTransactions("Bearer $token")
            
            // Mettre à jour la base locale
            serverTransactions.forEach { transaction ->
                transactionDao.insertTransaction(transaction)
            }
        } catch (e: Exception) {
            // Gérer l'erreur
        }
    }
    
    suspend fun insertTransaction(transaction: Transaction, token: String) {
        // Insérer localement
        transactionDao.insertTransaction(transaction)
        
        // Envoyer au serveur
        try {
            apiService.createTransaction("Bearer $token", transaction)
        } catch (e: Exception) {
            // Marquer pour sync ultérieure
        }
    }
}
```

---

## 📊 Architecture Finale

```
┌─────────────────┐
│  Android App    │
│   (FinTrack3)   │
└────────┬────────┘
         │ HTTP/REST
         │ (Retrofit)
         ▼
┌─────────────────┐
│  Backend API    │
│   (Node.js +    │
│    Express)     │
└────────┬────────┘
         │ Mongoose
         ▼
┌─────────────────┐
│  MongoDB Atlas  │
│    (Cloud DB)   │
└─────────────────┘
```

---

## ✅ Checklist Complète

### MongoDB Atlas
- [ ] Compte créé
- [ ] Cluster créé
- [ ] Utilisateur de base de données créé
- [ ] Accès réseau configuré
- [ ] Connection string obtenue

### Backend Node.js
- [ ] Projet initialisé
- [ ] Dépendances installées
- [ ] Structure de dossiers créée
- [ ] Fichier .env configuré
- [ ] Modèles MongoDB créés
- [ ] Routes API créées
- [ ] Controllers créés
- [ ] Middleware d'authentification créé
- [ ] Serveur testé localement

### Déploiement
- [ ] Code poussé sur GitHub
- [ ] Service créé sur Render/Heroku
- [ ] Variables d'environnement configurées
- [ ] URL de déploiement obtenue
- [ ] API testée en production

### Application Android
- [ ] Retrofit ajouté
- [ ] ApiService créé
- [ ] RetrofitClient créé
- [ ] Repositories modifiés
- [ ] Synchronisation implémentée
- [ ] Gestion des erreurs ajoutée
- [ ] Tests effectués

---

## 🎯 Prochaines Fonctionnalités Recommandées

1. **Synchronisation automatique**
   - Sync en arrière-plan avec WorkManager
   - Détection de connexion internet
   - Queue de synchronisation pour les opérations offline

2. **Authentification Firebase + JWT**
   - Utiliser Firebase Auth pour l'authentification
   - Générer un JWT côté serveur
   - Stocker le token dans SharedPreferences

3. **Mode Offline**
   - Garder Room comme cache local
   - Marquer les transactions non synchronisées
   - Sync automatique quand internet revient

4. **Gestion des conflits**
   - Timestamps pour détecter les conflits
   - Stratégie de résolution (dernier gagne, ou manuel)

5. **Optimisations**
   - Pagination pour les grandes listes
   - Compression des données
   - Cache des requêtes

---

## 📞 Ressources Utiles

- **MongoDB Atlas Documentation** : https://docs.atlas.mongodb.com/
- **Express.js Guide** : https://expressjs.com/
- **Mongoose Documentation** : https://mongoosejs.com/
- **Retrofit Documentation** : https://square.github.io/retrofit/
- **Firebase Auth** : https://firebase.google.com/docs/auth

---

**Bon courage pour la suite ! 🚀**

*Si vous avez besoin d'aide pour implémenter ces étapes, n'hésitez pas à demander.*
