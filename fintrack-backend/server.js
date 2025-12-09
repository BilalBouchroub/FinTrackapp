const express = require('express');
const mongoose = require('mongoose');
const cors = require('cors');
require('dotenv').config();

const app = express();

// Middleware
app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Logging middleware
app.use((req, res, next) => {
    console.log(`${new Date().toISOString()} - ${req.method} ${req.path}`);
    next();
});

// Routes
app.use('/api/auth', require('./routes/auth'));
app.use('/api/transactions', require('./routes/transactions'));
app.use('/api/categories', require('./routes/categories'));
app.use('/api/budgets', require('./routes/budgets'));

// Route de test
app.get('/', (req, res) => {
    res.json({
        success: true,
        message: 'FinTrack API est en ligne! 🚀',
        version: '1.0.0',
        endpoints: {
            auth: '/api/auth',
            transactions: '/api/transactions',
            categories: '/api/categories',
            budgets: '/api/budgets'
        }
    });
});

// Route 404
app.use((req, res) => {
    res.status(404).json({
        success: false,
        message: 'Route non trouvée'
    });
});

// Gestion des erreurs globales
app.use((err, req, res, next) => {
    console.error('Erreur serveur:', err);
    res.status(500).json({
        success: false,
        message: 'Erreur serveur interne',
        error: process.env.NODE_ENV === 'development' ? err.message : undefined
    });
});

// Connexion à MongoDB
mongoose.connect(process.env.MONGODB_URI)
    .then(() => {
        console.log('✅ Connecté à MongoDB Atlas avec succès!');
        console.log(`📊 Base de données: ${mongoose.connection.name}`);
    })
    .catch(err => {
        console.error('❌ Erreur de connexion à MongoDB:', err.message);
        process.exit(1);
    });

// Gestion des événements MongoDB
mongoose.connection.on('error', err => {
    console.error('❌ Erreur MongoDB:', err);
});

mongoose.connection.on('disconnected', () => {
    console.log('⚠️ Déconnecté de MongoDB');
});

// Démarrer le serveur
const PORT = process.env.PORT || 5000;
app.listen(PORT, () => {
    console.log(`🚀 Serveur démarré sur le port ${PORT}`);
    console.log(`🌍 Environnement: ${process.env.NODE_ENV}`);
    console.log(`📡 API disponible sur: http://localhost:${PORT}`);
});

// Gestion de l'arrêt gracieux
process.on('SIGINT', async () => {
    console.log('\n⏹️ Arrêt du serveur...');
    await mongoose.connection.close();
    console.log('✅ Connexion MongoDB fermée');
    process.exit(0);
});
