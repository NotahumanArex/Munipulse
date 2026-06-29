package my.n.munipulse.auth

import com.google.firebase.auth.FirebaseUser

interface AuthRepository {
    val currentUser: FirebaseUser?

    suspend fun loginWithEmail(email: String, password: String): Result<Unit>

    suspend fun registerWithEmail(
        email: String,
        password: String,
        firstName: String,
        lastName: String
    ): Result<Unit>

    suspend fun loginWithGoogle(idToken: String): Result<Unit>

    fun logout()
}