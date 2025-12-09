package bilal.com.fintrack.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import bilal.com.fintrack.MainActivity
import bilal.com.fintrack.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    
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
                            if (token != null) {
                                val tokenManager = bilal.com.fintrack.data.remote.TokenManager(this@LoginActivity)
                                tokenManager.saveToken(token)
                                response.body()?.user?.let { u ->
                                    tokenManager.saveUserInfo(u.id, u.email, u.nom)
                                }
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
                        // En cas d'erreur réseau backend, on laisse entrer quand même (mode offline possible)
                        // mais sans token de sync
                        Toast.makeText(this@LoginActivity, "Mode hors ligne: Impossible de contacter le serveur", Toast.LENGTH_SHORT).show()
                        navigateToMain()
                    }
                }
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
                if (token != null) {
                    val tokenManager = bilal.com.fintrack.data.remote.TokenManager(this@LoginActivity)
                    tokenManager.saveToken(token)
                    response.body()?.user?.let { u ->
                        tokenManager.saveUserInfo(u.id, u.email, u.nom)
                    }
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
