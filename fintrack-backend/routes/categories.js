const express = require('express');
const router = express.Router();
const { protect } = require('../middleware/auth');
const {
    getAllCategories,
    createCategory,
    updateCategory,
    deleteCategory
} = require('../controllers/categoryController');

router.use(protect); // Protéger toutes les routes

router.route('/')
    .get(getAllCategories)
    .post(createCategory);

router.route('/:id')
    .put(updateCategory)
    .delete(deleteCategory);

module.exports = router;
