const express = require('express');
const router = express.Router();
const transactionController = require('../controllers/transactionController');
const { protect } = require('../middleware/auth');

// Toutes les routes nécessitent l'authentification
router.use(protect);

// Routes CRUD
router.get('/', transactionController.getAllTransactions);
router.get('/:id', transactionController.getTransaction);
router.post('/', transactionController.createTransaction);
router.put('/:id', transactionController.updateTransaction);
router.delete('/:id', transactionController.deleteTransaction);

// Route de synchronisation
router.post('/sync', transactionController.syncTransactions);

module.exports = router;
