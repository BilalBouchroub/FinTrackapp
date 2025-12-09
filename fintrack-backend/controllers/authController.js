const jwt = require('jsonwebtoken');
const User = require('../models/User');

// Générer un token JWT
const generateToken = (userId) => {
    return jwt.sign(
        { userId },
        process.env.JWT_SECRET,
        { expiresIn: '30d' }
    );
};

// Inscription
exports.register = async (req, res) => {
    try {
        const { firebaseUid, nom, email, devise } = req.body;

        // Vérifier si l'utilisateur existe déjà
        let user = await User.findOne({ firebaseUid });

        if (user) {
            // Utilisateur existe déjà, retourner le token
            const token = generateToken(user._id);
            return res.status(200).json({
                success: true,
                message: 'Utilisateur déjà enregistré',
                token,
                user: {
                    id: user._id,
                    firebaseUid: user.firebaseUid,
                    nom: user.nom,
                    email: user.email,
                    devise: user.devise
                }
            });
        }

        // Créer un nouvel utilisateur
        user = new User({
            firebaseUid,
            nom,
            email,
            devise: devise || 'MAD'
        });

        await user.save();

        // Générer le token
        const token = generateToken(user._id);

        res.status(201).json({
            success: true,
            message: 'Utilisateur créé avec succès',
            token,
            user: {
                id: user._id,
                firebaseUid: user.firebaseUid,
                nom: user.nom,
                email: user.email,
                devise: user.devise
            }
        });
    } catch (error) {
        console.error('Erreur lors de l\'inscription:', error);
        res.status(500).json({
            success: false,
            message: 'Erreur serveur lors de l\'inscription',
            error: error.message
        });
    }
};

// Connexion
exports.login = async (req, res) => {
    try {
        const { firebaseUid } = req.body;

        // Trouver l'utilisateur
        const user = await User.findOne({ firebaseUid });

        if (!user) {
            return res.status(404).json({
                success: false,
                message: 'Utilisateur non trouvé'
            });
        }

        // Générer le token
        const token = generateToken(user._id);

        res.status(200).json({
            success: true,
            message: 'Connexion réussie',
            token,
            user: {
                id: user._id,
                firebaseUid: user.firebaseUid,
                nom: user.nom,
                email: user.email,
                devise: user.devise
            }
        });
    } catch (error) {
        console.error('Erreur lors de la connexion:', error);
        res.status(500).json({
            success: false,
            message: 'Erreur serveur lors de la connexion',
            error: error.message
        });
    }
};

// Obtenir le profil utilisateur
exports.getCurrentUser = async (req, res) => {
    try {
        const user = await User.findById(req.userId);

        if (!user) {
            return res.status(404).json({
                success: false,
                message: 'Utilisateur non trouvé'
            });
        }

        res.status(200).json({
            success: true,
            user: {
                id: user._id,
                firebaseUid: user.firebaseUid,
                nom: user.nom,
                email: user.email,
                devise: user.devise,
                dateInscription: user.dateInscription
            }
        });
    } catch (error) {
        console.error('Erreur lors de la récupération du profil:', error);
        res.status(500).json({
            success: false,
            message: 'Erreur serveur',
            error: error.message
        });
    }
};
