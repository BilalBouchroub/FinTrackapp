const mongoose = require('mongoose');

const budgetSchema = new mongoose.Schema({
    userId: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'User',
        required: true,
        index: true
    },
    categoryId: {
        type: String,
        required: true
    },
    categoryName: {
        type: String,
        required: true
    },
    amount: {
        type: Number,
        required: true
    },
    spent: {
        type: Number,
        default: 0
    },
    period: {
        type: String,
        enum: ['MONTHLY', 'YEARLY'],
        default: 'MONTHLY'
    },
    month: {
        type: Number,
        min: 1,
        max: 12
    },
    year: {
        type: Number
    }
}, {
    timestamps: true
});

module.exports = mongoose.model('Budget', budgetSchema);
