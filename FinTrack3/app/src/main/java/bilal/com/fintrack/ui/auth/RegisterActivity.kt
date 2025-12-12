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
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class RegisterActivity : BaseActivity() {
    
    private lateinit var auth: FirebaseAuth
    private lateinit var nameEditText: TextInputEditText
    private lateinit var emailEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var confirmPasswordEditText: TextInputEditText
    private lateinit var registerButton: MaterialButton
    private lateinit var loginTextView: TextView
    private lateinit var progressBar: ProgressBar
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        
        // Initialiser Firebase Auth
        auth = Firebase.auth
        
        // Initialiser les vues
        initializeViews()
        
        // Configurer les listeners
        setupListeners()
    }
    
    private fun initializeViews() {
        nameEditText = findViewById(R.id.nameEditText)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText)
        registerButton = findViewById(R.id.registerButton)
        loginTextView = findViewById(R.id.loginTextView)
        progressBar = findViewById(R.id.progressBar)
    }
    
    private fun setupListeners() {
        registerButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val confirmPassword = confirmPasswordEditText.text.toString().trim()
            
            if (validateInputs(name, email, password, confirmPassword)) {
                registerUser(name, email, password)
            }
        }
        
        loginTextView.setOnClickListener {
            finish() // Retour à LoginActivity
        }
    }
    
    private fun validateInputs(
        name: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        // Vérifier le nom
        if (name.isEmpty()) {
            nameEditText.error = "Le nom est requis"
            nameEditText.requestFocus()
            return false
        }
        
        if (name.length < 3) {
            nameEditText.error = "Le nom doit contenir au moins 3 caractères"
            nameEditText.requestFocus()
            return false
        }
        
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
        
        // Vérifier la confirmation du mot de passe
        if (confirmPassword.isEmpty()) {
            confirmPasswordEditText.error = "Confirmation requise"
            confirmPasswordEditText.requestFocus()
            return false
        }
        
        if (password != confirmPassword) {
            confirmPasswordEditText.error = "Les mots de passe ne correspondent pas"
            confirmPasswordEditText.requestFocus()
            return false
        }
        
        return true
    }
    
    private fun registerUser(name: String, email: String, password: String) {
        showLoading(true)
        
        // Créer un compte avec Firebase Auth
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (!task.isSuccessful) {
                    showLoading(false)
                    val errorMessage = when {
                        task.exception?.message?.contains("already in use") == true -> "Cet email est déjà utilisé"
                        task.exception?.message?.contains("weak password") == true -> "Mot de passe trop faible"
                        task.exception?.message?.contains("network") == true -> "Erreur de connexion Internet"
                        else -> "Erreur: ${task.exception?.message}"
                    }
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                    return@addOnCompleteListener
                }

                // Inscription Firebase réussie - mettre à jour le profil
                val user = auth.currentUser
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build()
                
                user?.updateProfile(profileUpdates)
                    ?.addOnCompleteListener { profileTask ->
                        if (!profileTask.isSuccessful) {
                            // On continue quand même vers le backend, le nom sera passé manuellement
                        }
                        
                        // Inscrire l'utilisateur sur le Backend
                        lifecycleScope.launch {
                            try {
                                val request = bilal.com.fintrack.data.remote.models.RegisterRequest(
                                    firebaseUid = user?.uid ?: "",
                                    nom = name,
                                    email = email,
                                    devise = "MAD"
                                )
                                
                                val response = bilal.com.fintrack.data.remote.RetrofitClient.authApi.register(request)
                                
                                if (response.isSuccessful && response.body()?.success == true) {
                                    val token = response.body()?.token
                                    if (token != null) {
                                        val tokenManager = bilal.com.fintrack.data.remote.TokenManager(this@RegisterActivity)
                                        tokenManager.saveToken(token)
                                        response.body()?.user?.let { u ->
                                            tokenManager.saveUserInfo(
                                                u.id ?: "",
                                                u.email ?: "",
                                                u.nom ?: "Utilisateur"
                                            )
                                        }
                                    }
                                    showLoading(false)
                                    Toast.makeText(this@RegisterActivity, "Compte créé avec succès!", Toast.LENGTH_SHORT).show()
                                    navigateToMain()
                                } else {
                                    showLoading(false)
                                    Toast.makeText(this@RegisterActivity, "Compte Firebase créé mais erreur Backend: ${response.message()}", Toast.LENGTH_LONG).show()
                                    // On force la navigation même si erreur backend, l'utilisateur pourra réessayer en se connectant
                                    navigateToMain() 
                                }
                            } catch (e: Exception) {
                                showLoading(false)
                                Toast.makeText(this@RegisterActivity, "Compte créé (Mode hors ligne): ${e.message}", Toast.LENGTH_SHORT).show()
                                navigateToMain()
                            }
                        }
                    }
            }
    }
    
    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            progressBar.visibility = View.VISIBLE
            registerButton.isEnabled = false
            registerButton.alpha = 0.5f
        } else {
            progressBar.visibility = View.GONE
            registerButton.isEnabled = true
            registerButton.alpha = 1f
        }
    }
    
    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
