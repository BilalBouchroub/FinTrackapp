const Transaction = require('../models/Transaction');

// Récupérer toutes les transactions d'un utilisateur
exports.getAllTransactions = async (req, res) => {
    try {
        const transactions = await Transaction.find({ userId: req.userId })
            .sort({ date: -1 });

        res.status(200).json({
            success: true,
            count: transactions.length,
            transactions
        });
    } catch (error) {
        console.error('Erreur lors de la récupération des transactions:', error);
        res.status(500).json({
            success: false,
            message: 'Erreur serveur',
            error: error.message
        });
    }
};

// Récupérer une transaction spécifique
exports.getTransaction = async (req, res) => {
    try {
        const transaction = await Transaction.findOne({
            _id: req.params.id,
            userId: req.userId
        });

        if (!transaction) {
            return res.status(404).json({
                success: false,
                message: 'Transaction non trouvée'
            });
        }

        res.status(200).json({
            success: true,
            transaction
        });
    } catch (error) {
        console.error('Erreur lors de la récupération de la transaction:', error);
        res.status(500).json({
            success: false,
            message: 'Erreur serveur',
            error: error.message
        });
    }
};

// Créer une nouvelle transaction
exports.createTransaction = async (req, res) => {
    try {
        const { amount, type, categoryId, categoryName, paymentMethod, date, notes, localId } = req.body;

        const transaction = new Transaction({
            userId: req.userId,
            amount,
            type,
            categoryId,
            categoryName: categoryName || 'Autre',
            paymentMethod,
            date,
            notes,
            localId
        });

        await transaction.save();

        res.status(201).json({
            success: true,
            message: 'Transaction créée avec succès',
            transaction
        });
    } catch (error) {
        console.error('Erreur lors de la création de la transaction:', error);
        res.status(500).json({
            success: false,
            message: 'Erreur serveur',
            error: error.message
        });
    }
};

// Mettre à jour une transaction
exports.updateTransaction = async (req, res) => {
    try {
        const { amount, type, categoryId, categoryName, paymentMethod, date, notes } = req.body;

        const transaction = await Transaction.findOneAndUpdate(
            { _id: req.params.id, userId: req.userId },
            { amount, type, categoryId, categoryName, paymentMethod, date, notes },
            { new: true, runValidators: true }
        );

        if (!transaction) {
            return res.status(404).json({
                success: false,
                message: 'Transaction non trouvée'
            });
        }

        res.status(200).json({
            success: true,
            message: 'Transaction mise à jour avec succès',
            transaction
        });
    } catch (error) {
        console.error('Erreur lors de la mise à jour de la transaction:', error);
        res.status(500).json({
            success: false,
            message: 'Erreur serveur',
            error: error.message
        });
    }
};

// Supprimer une transaction
exports.deleteTransaction = async (req, res) => {
    try {
        const transaction = await Transaction.findOneAndDelete({
            _id: req.params.id,
            userId: req.userId
        });

        if (!transaction) {
            return res.status(404).json({
                success: false,
                message: 'Transaction non trouvée'
            });
        }

        res.status(200).json({
            success: true,
            message: 'Transaction supprimée avec succès'
        });
    } catch (error) {
        console.error('Erreur lors de la suppression de la transaction:', error);
        res.status(500).json({
            success: false,
            message: 'Erreur serveur',
            error: error.message
        });
    }
};

// Synchroniser plusieurs transactions
exports.syncTransactions = async (req, res) => {
    try {
        const { transactions } = req.body;

        if (!Array.isArray(transactions)) {
            return res.status(400).json({
                success: false,
                message: 'Le format des transactions est invalide'
            });
        }

        const syncedTransactions = [];

        for (const trans of transactions) {
            // Vérifier si la transaction existe déjà (par localId)
            if (trans.localId) {
                const existing = await Transaction.findOne({
                    userId: req.userId,
                    localId: trans.localId
                });

                if (existing) {
                    syncedTransactions.push(existing);
                    continue;
                }
            }

            // Créer la nouvelle transaction
            const newTransaction = new Transaction({
                userId: req.userId,
                ...trans
            });

            await newTransaction.save();
            syncedTransactions.push(newTransaction);
        }

        res.status(200).json({
            success: true,
            message: 'Synchronisation réussie',
            count: syncedTransactions.length,
            transactions: syncedTransactions
        });
    } catch (error) {
        console.error('Erreur lors de la synchronisation:', error);
        res.status(500).json({
            success: false,
            message: 'Erreur serveur',
            error: error.message
        });
    }
};
