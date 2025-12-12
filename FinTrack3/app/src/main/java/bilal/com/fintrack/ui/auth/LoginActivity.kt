package bilal.com.fintrack.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import bilal.com.fintrack.MainActivity
import bilal.com.fintrack.R
import bilal.com.fintrack.ui.base.BaseActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class LoginActivity : BaseActivity() {
    
    private lateinit var auth: FirebaseAuth
    private lateinit var emailEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var loginButton: MaterialButton
    private lateinit var registerTextView: TextView
    private lateinit var progressBar: ProgressBar
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        
        // Initialiser Firebase Auth
        auth = Firebase.auth
        
        // Vérifier si l'utilisateur est déjà connecté
        if (auth.currentUser != null) {
            navigateToMain()
            return
        }
        
        // Initialiser les vues
        initializeViews()
        
        // Configurer les listeners
        setupListeners()
    }
    
    private fun initializeViews() {
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)
        registerTextView = findViewById(R.id.registerTextView)
        progressBar = findViewById(R.id.progressBar)
    }
    
    private fun setupListeners() {
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            
            if (validateInputs(email, password)) {
                loginUser(email, password)
            }
        }
        
        registerTextView.setOnClickListener {
            navigateToRegister()
        }
    }
    
    private fun validateInputs(email: String, password: String): Boolean {
        // Vérifier l'email
        if (email.isEmpty()) {
            emailEditText.error = "L'email est requis"
            emailEditText.requestFocus()
            return false
        }
        
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.error = "Email invalide"
            emailEditText.requestFocus()
            return false
        }
        
        // Vérifier le mot de passe
        if (password.isEmpty()) {
            passwordEditText.error = "Le mot de passe est requis"
            passwordEditText.requestFocus()
            return false
        }
        
        if (password.length < 6) {
            passwordEditText.error = "Le mot de passe doit contenir au moins 6 caractères"
            passwordEditText.requestFocus()
            return false
        }
        
        return true
    }
    
    private fun loginUser(email: String, password: String) {
        showLoading(true)
        
        // Connexion avec Firebase Auth
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (!task.isSuccessful) {
                    showLoading(false)
                    val errorMessage = when {
                        task.exception?.message?.contains("password") == true -> "Mot de passe incorrect"
                        task.exception?.message?.contains("user") == true -> "Utilisateur introuvable"
                        task.exception?.message?.contains("network") == true -> "Erreur de connexion Internet"
                        else -> "Erreur: ${task.exception?.message}"
                    }
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                    return@addOnCompleteListener
                }

                // Connexion Firebase réussie, maintenant connexion Backend
                val user = auth.currentUser
                val uid = user?.uid ?: ""
                
                // Utiliser une coroutine pour l'appel réseau
                lifecycleScope.launch {
                    try {
                        val request = bilal.com.fintrack.data.remote.models.LoginRequest(firebaseUid = uid)
                        val response = bilal.com.fintrack.data.remote.RetrofitClient.authApi.login(request)
                        
                        if (response.isSuccessful && response.body()?.success == true) {
                            // Backend Login Succès
                            val token = response.body()?.token
                            val user = response.body()?.user
                            if (token != null && user != null) {
                                saveUserAndNavigate(token, user)
                                return@launch
                            }
                            navigateToMain()
                        } else if (response.code() == 404) {
                            // Utilisateur existe dans Firebase mais pas dans MongoDB -> Inscription silencieuse
                            tryRegisterOnBackend(user)
                        } else {
                            showLoading(false)
                            Toast.makeText(this@LoginActivity, "Erreur Backend: ${response.message()}", Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        showLoading(false)
                        // Afficher l'erreur exacte pour le débogage
                        val errorMsg = e.message ?: "Erreur inconnue"
                        android.util.Log.e("LoginActivity", "Login Error", e)
                        
                        if (errorMsg.contains("Failed to connect") || errorMsg.contains("timeout")) {
                             Toast.makeText(this@LoginActivity, "Erreur connexion: Vérifiez votre internet ou l'adresse IP ($errorMsg)", Toast.LENGTH_LONG).show()
                        } else if (errorMsg.contains("JSON") || errorMsg.contains("GSON")) {
                             Toast.makeText(this@LoginActivity, "Erreur format données: $errorMsg", Toast.LENGTH_LONG).show()
                        } else {
                             Toast.makeText(this@LoginActivity, "Erreur: $errorMsg", Toast.LENGTH_LONG).show()
                        }
                        
                        // On ne navigue PAS vers main si le login backend échoue explicitement, 
                        // sauf si on veut forcer le mode hors ligne. 
                        // Pour l'instant, on laisse l'utilisateur réessayer ou voir l'erreur.
                        // navigateToMain() // Commenté pour permettre de voir l'erreur
                    }
                }
            }
    }
    
    private fun saveUserAndNavigate(token: String, user: bilal.com.fintrack.data.remote.models.UserDto) {
        val tokenManager = bilal.com.fintrack.data.remote.TokenManager(this)
        tokenManager.saveToken(token)
        // Gestion sécurisée des nulls
        tokenManager.saveUserInfo(
            user.id ?: "",
            user.email ?: "",
            user.nom ?: "Utilisateur"
        )
        
        // Synchroniser les données depuis le serveur MongoDB
        lifecycleScope.launch {
            try {
                val app = application as bilal.com.fintrack.FinTrackApplication
                
                // 1. Synchroniser les transactions
                val syncTransResult = app.container.syncTransactionRepository.syncWithServer()
                if (syncTransResult.isSuccess) {
                    android.util.Log.d("LoginActivity", "Transactions synchronisées avec succès")
                } else {
                    android.util.Log.w("LoginActivity", "Échec sync transactions: ${syncTransResult.exceptionOrNull()?.message}")
                }
                
                // 2. Synchroniser les budgets
                val syncBudgetResult = app.container.budgetRepository.syncWithServer()
                if (syncBudgetResult.isSuccess) {
                    android.util.Log.d("LoginActivity", "Budgets synchronisés avec succès")
                } else {
                    android.util.Log.w("LoginActivity", "Échec sync budgets: ${syncBudgetResult.exceptionOrNull()?.message}")
                }
                
            } catch (e: Exception) {
                android.util.Log.e("LoginActivity", "Erreur lors de la synchronisation", e)
            }
            showLoading(false)
            navigateToMain()
        }
    }


    private suspend fun tryRegisterOnBackend(firebaseUser: com.google.firebase.auth.FirebaseUser?) {
        try {
            if (firebaseUser == null) {
                showLoading(false)
                return
            }
            
            val request = bilal.com.fintrack.data.remote.models.RegisterRequest(
                firebaseUid = firebaseUser.uid,
                nom = firebaseUser.displayName ?: "Utilisateur",
                email = firebaseUser.email ?: "",
                devise = "MAD"
            )
            
            val response = bilal.com.fintrack.data.remote.RetrofitClient.authApi.register(request)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val token = response.body()?.token
                val user = response.body()?.user
                if (token != null && user != null) {
                    saveUserAndNavigate(token, user)
                    return
                }
                navigateToMain()
            } else {
                showLoading(false)
                Toast.makeText(this@LoginActivity, "Erreur migration compte: ${response.message()}", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            showLoading(false)
            Toast.makeText(this@LoginActivity, "Erreur connexion: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            progressBar.visibility = View.VISIBLE
            loginButton.isEnabled = false
            loginButton.alpha = 0.5f
        } else {
            progressBar.visibility = View.GONE
            loginButton.isEnabled = true
            loginButton.alpha = 1f
        }
    }
    
    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
    
    private fun navigateToRegister() {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
    }
}
