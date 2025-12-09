const jwt = require('jsonwebtoken');
const User = require('../models/User');

const auth = async (req, res, next) => {
    try {
        // Récupérer le token depuis le header Authorization
        const token = req.header('Authorization')?.replace('Bearer ', '');

        if (!token) {
            return res.status(401).json({
                success: false,
                message: 'Accès refusé. Aucun token fourni.'
            });
        }

        // Vérifier le token
        const decoded = jwt.verify(token, process.env.JWT_SECRET);

        // Trouver l'utilisateur
        const user = await User.findById(decoded.userId);

        if (!user) {
            return res.status(401).json({
                success: false,
                message: 'Token invalide. Utilisateur non trouvé.'
            });
        }

        // Ajouter l'utilisateur à la requête
        req.user = user;
        req.userId = user._id;

        next();
    } catch (error) {
        console.error('Erreur d\'authentification:', error);
        res.status(401).json({
            success: false,
            message: 'Token invalide ou expiré.'
        });
    }
};

module.exports = { protect: auth };
