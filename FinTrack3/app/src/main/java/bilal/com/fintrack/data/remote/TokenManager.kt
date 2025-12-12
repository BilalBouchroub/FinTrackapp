package bilal.com.fintrack.data.remote

import android.content.Context
import android.content.SharedPreferences

/**
 * Gestionnaire de tokens JWT pour l'authentification
 * Stocke et récupère le token dans SharedPreferences
 */
class TokenManager(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )
    
    /**
     * Sauvegarder le token JWT
     */
    fun saveToken(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }
    
    /**
     * Récupérer le token JWT
     */
    fun getToken(): String? {
        return prefs.getString(KEY_TOKEN, null)
    }
    
    /**
     * Récupérer le token avec le préfixe "Bearer "
     */
    fun getBearerToken(): String? {
        val token = getToken()
        return if (token != null) "Bearer $token" else null
    }
    
    /**
     * Sauvegarder les informations utilisateur
     */
    fun saveUserInfo(userId: String, email: String, name: String) {
        prefs.edit().apply {
            putString(KEY_USER_ID, userId)
            putString(KEY_USER_EMAIL, email)
            putString(KEY_USER_NAME, name)
            apply()
        }
    }
    
    /**
     * Récupérer l'ID utilisateur
     */
    fun getUserId(): String? {
        return prefs.getString(KEY_USER_ID, null)
    }
    
    /**
     * Récupérer l'email utilisateur
     */
    fun getUserEmail(): String? {
        return prefs.getString(KEY_USER_EMAIL, null)
    }
    
    /**
     * Récupérer le nom utilisateur
     */
    fun getUserName(): String? {
        return prefs.getString(KEY_USER_NAME, null)
    }
    
    /**
     * Vérifier si l'utilisateur est connecté
     */
    fun isLoggedIn(): Boolean {
        return getToken() != null
    }
    
    /**
     * Déconnexion - Supprimer toutes les données
     */
    fun clearAll() {
        prefs.edit().clear().apply()
    }

    /**
     * Sauvegarder la langue préférée
     */
    fun saveLanguage(language: String) {
        prefs.edit().putString(KEY_LANGUAGE, language).apply()
    }

    /**
     * Récupérer la langue préférée (défaut: "fr")
     */
    fun getLanguage(): String {
        return prefs.getString(KEY_LANGUAGE, "fr") ?: "fr"
    }

    /**
     * Sauvegarder la devise préférée
     */
    fun saveCurrency(currency: String) {
        prefs.edit().putString(KEY_CURRENCY, currency).apply()
    }

    /**
     * Récupérer la devise préférée (défaut: "MAD")
     */
    fun getCurrency(): String {
        return prefs.getString(KEY_CURRENCY, "MAD") ?: "MAD"
    }
    
    companion object {
        private const val PREFS_NAME = "fintrack_auth"
        private const val KEY_TOKEN = "jwt_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_LANGUAGE = "app_language"
        private const val KEY_CURRENCY = "app_currency"
    }
}
