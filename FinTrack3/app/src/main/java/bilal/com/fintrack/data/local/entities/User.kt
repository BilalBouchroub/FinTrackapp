package bilal.com.fintrack.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: String, // Firebase UID
    val nom: String, // Récupéré de Firebase Auth
    val email: String, // Récupéré de Firebase Auth
    val devise: String = "MAD", // Récupéré des paramètres utilisateur
    val dateInscription: Long = System.currentTimeMillis()
)
