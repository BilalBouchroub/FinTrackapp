const Budget = require('../models/Budget');

exports.getAllBudgets = async (req, res) => {
    try {
        const budgets = await Budget.find({ userId: req.userId });
        res.status(200).json({ success: true, count: budgets.length, data: budgets });
    } catch (error) {
        res.status(500).json({ success: false, error: error.message });
    }
};

exports.createBudget = async (req, res) => {
    try {
        // Fallback pour categoryName si manquant
        const { categoryId, categoryName, amount, period, month, year } = req.body;

        const budget = new Budget({
            userId: req.userId,
            categoryId,
            categoryName: categoryName || 'Catégorie Inconnue',
            amount,
            period,
            month,
            year
        });

        await budget.save();
        res.status(201).json({ success: true, data: budget });
    } catch (error) {
        res.status(500).json({ success: false, error: error.message });
    }
};

exports.updateBudget = async (req, res) => {
    try {
        const budget = await Budget.findOneAndUpdate(
            { _id: req.params.id, userId: req.userId },
            req.body,
            { new: true, runValidators: true }
        );

        if (!budget) {
            return res.status(404).json({ success: false, message: 'Budget non trouvé' });
        }
        res.status(200).json({ success: true, data: budget });
    } catch (error) {
        res.status(500).json({ success: false, error: error.message });
    }
};

exports.deleteBudget = async (req, res) => {
    try {
        const budget = await Budget.findOneAndDelete({
            _id: req.params.id,
            userId: req.userId
        });

        if (!budget) {
            return res.status(404).json({ success: false, message: 'Budget non trouvé' });
        }
        res.status(200).json({ success: true, message: 'Budget supprimé' });
    } catch (error) {
        res.status(500).json({ success: false, error: error.message });
    }
};
