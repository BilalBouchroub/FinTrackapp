const express = require('express');
const router = express.Router();
const { protect } = require('../middleware/auth');
const {
    getAllBudgets,
    createBudget,
    updateBudget,
    deleteBudget
} = require('../controllers/budgetController');

router.use(protect);

router.route('/')
    .get(getAllBudgets)
    .post(createBudget);

router.route('/:id')
    .put(updateBudget)
    .delete(deleteBudget);

module.exports = router;
