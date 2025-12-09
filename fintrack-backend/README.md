# FinTrack Backend API

Backend Node.js pour l'application FinTrack avec MongoDB Atlas.

## 🚀 Démarrage Rapide

### Installation
```bash
npm install
```

### Démarrage en développement
```bash
npm run dev
```

### Démarrage en production
```bash
npm start
```

## 📡 Endpoints API

### Authentification

#### POST /api/auth/register
Inscription d'un nouvel utilisateur
```json
{
  "firebaseUid": "string",
  "nom": "string",
  "email": "string",
  "devise": "MAD"
}
```

#### POST /api/auth/login
Connexion d'un utilisateur
```json
{
  "firebaseUid": "string"
}
```

#### GET /api/auth/me
Récupérer le profil utilisateur (nécessite authentification)
```
Headers: Authorization: Bearer <token>
```

### Transactions

Toutes les routes nécessitent l'authentification (Header: `Authorization: Bearer <token>`)

#### GET /api/transactions
Récupérer toutes les transactions de l'utilisateur

#### GET /api/transactions/:id
Récupérer une transaction spécifique

#### POST /api/transactions
Créer une nouvelle transaction
```json
{
  "amount": 100.0,
  "type": "EXPENSE",
  "categoryId": "1",
  "categoryName": "Alimentation",
  "paymentMethod": "Cash",
  "date": "2024-12-06T00:00:00.000Z",
  "notes": "Courses",
  "localId": "local_123"
}
```

#### PUT /api/transactions/:id
Mettre à jour une transaction

#### DELETE /api/transactions/:id
Supprimer une transaction

#### POST /api/transactions/sync
Synchroniser plusieurs transactions
```json
{
  "transactions": [...]
}
```

## 🔐 Variables d'Environnement

Créer un fichier `.env` avec:
```
PORT=5000
MONGODB_URI=mongodb+srv://...
JWT_SECRET=votre_secret
NODE_ENV=development
```

## 📊 Base de Données

MongoDB Atlas avec les collections:
- users
- transactions
- categories
- budgets

## 🛠️ Technologies

- Node.js
- Express.js
- MongoDB + Mongoose
- JWT pour l'authentification
- bcryptjs pour le hashage
- CORS pour les requêtes cross-origin
