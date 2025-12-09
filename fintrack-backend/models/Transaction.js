const mongoose = require('mongoose');

const transactionSchema = new mongoose.Schema({
    userId: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'User',
        required: true,
        index: true
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
        type: String,
        required: true
    },
    categoryName: {
        type: String,
        required: false,
        default: 'Autre'
    },
    paymentMethod: {
        type: String,
        required: true
    },
    date: {
        type: Date,
        required: true
    },
    notes: {
        type: String,
        default: ''
    },
    localId: {
        type: String,
        index: true
    }
}, {
    timestamps: true
});

// Index pour recherche rapide par utilisateur et date
transactionSchema.index({ userId: 1, date: -1 });

module.exports = mongoose.model('Transaction', transactionSchema);
