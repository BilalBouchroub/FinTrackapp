package bilal.com.fintrack.data.remote

import bilal.com.fintrack.data.remote.api.AuthApiService
import bilal.com.fintrack.data.remote.api.TransactionApiService
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Client Retrofit pour communiquer avec le backend Node.js
 * 
 * IMPORTANT: Remplacez BASE_URL par l'URL de votre backend déployé
 * - Développement local (émulateur): http://10.0.2.2:5000/api/
 * - Développement local (appareil réel): http://VOTRE_IP_LOCAL:5000/api/
 * - Production (Render/Heroku): https://votre-app.onrender.com/api/
 */
object RetrofitClient {
    
    // ⚠️ IMPORTANT: Changez cette URL selon votre environnement
    private const val BASE_URL = "http://10.0.2.2:5000/api/" // Pour émulateur Android
    // private const val BASE_URL = "http://192.168.1.100:5000/api/" // Pour appareil réel (remplacez par votre IP)
    // private const val BASE_URL = "https://fintrack-backend.onrender.com/api/" // Pour production
    
    /**
     * Intercepteur pour logger les requêtes HTTP (utile pour le debug)
     */
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    /**
     * Client OkHttp avec configuration
     */
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    /**
     * Configuration Gson pour la sérialisation/désérialisation
     */
    private val gson = GsonBuilder()
        .setLenient()
        .create()
    
    /**
     * Instance Retrofit
     */
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
    
    /**
     * Service API pour l'authentification
     */
    val authApi: AuthApiService by lazy {
        retrofit.create(AuthApiService::class.java)
    }
    
    /**
     * Service API pour les transactions
     */
    val transactionApi: TransactionApiService by lazy {
        retrofit.create(TransactionApiService::class.java)
    }
    
    /**
     * Changer l'URL de base (utile pour passer de dev à prod)
     */
    fun updateBaseUrl(newBaseUrl: String): RetrofitClient {
        // Note: Dans une vraie app, il faudrait recréer l'instance Retrofit
        // Pour simplifier, on garde cette méthode pour référence
        return this
    }
    val categoryApi: CategoryApiService by lazy {
        retrofit.create(CategoryApiService::class.java)
    }
    
    val budgetApi: BudgetApiService by lazy {
        retrofit.create(BudgetApiService::class.java)
    }
}
