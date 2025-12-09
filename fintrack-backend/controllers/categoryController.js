const Category = require('../models/Category');

// Récupérer toutes les catégories
exports.getAllCategories = async (req, res) => {
    try {
        // Récupérer les catégories de l'utilisateur ET les catégories par défaut (si on gère ça ainsi)
        // Ici on récupère celles liées à l'utilisateur
        const categories = await Category.find({ userId: req.userId });

        res.status(200).json({
            success: true,
            count: categories.length,
            data: categories
        });
    } catch (error) {
        res.status(500).json({
            success: false,
            message: 'Erreur serveur',
            error: error.message
        });
    }
};

// Créer une catégorie
exports.createCategory = async (req, res) => {
    try {
        const { name, type, icon, isDefault, color } = req.body;

        const category = new Category({
            userId: req.userId,
            name,
            type,
            icon,
            isDefault: isDefault || false,
            color
        });

        await category.save();

        res.status(201).json({
            success: true,
            data: category
        });
    } catch (error) {
        res.status(500).json({
            success: false,
            message: 'Erreur serveur',
            error: error.message
        });
    }
};

// Mettre à jour une catégorie
exports.updateCategory = async (req, res) => {
    try {
        const category = await Category.findOneAndUpdate(
            { _id: req.params.id, userId: req.userId },
            req.body,
            { new: true, runValidators: true }
        );

        if (!category) {
            return res.status(404).json({ success: false, message: 'Catégorie non trouvée' });
        }

        res.status(200).json({
            success: true,
            data: category
        });
    } catch (error) {
        res.status(500).json({ success: false, error: error.message });
    }
};

// Supprimer une catégorie
exports.deleteCategory = async (req, res) => {
    try {
        const category = await Category.findOneAndDelete({
            _id: req.params.id,
            userId: req.userId
        });

        if (!category) {
            return res.status(404).json({ success: false, message: 'Catégorie non trouvée' });
        }

        res.status(200).json({
            success: true,
            message: 'Catégorie supprimée'
        });
    } catch (error) {
        res.status(500).json({ success: false, error: error.message });
    }
};
