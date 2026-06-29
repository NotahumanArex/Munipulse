package com.munipulse.app.repository

import User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

import kotlinx.coroutines.tasks.await
import my.n.munipulse.auth.AuthRepository

class FirebaseAuthRepositoryImpl(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : AuthRepository {

    override val currentUser get() = auth.currentUser

    override suspend fun loginWithEmail(email: String, password: String): Result<Unit> {
        return try {
            // Standard email login
            auth.signInWithEmailAndPassword(email, password).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun registerWithEmail(
        email: String,
        password: String,
        firstName: String,
        lastName: String
    ): Result<Unit> {
        return try {
            // 1. Create the user in Firebase Auth
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = authResult.user?.uid ?: throw Exception("Failed to get UID")

            // 2. Create the user document in Firestore
            val newUser = User(
                uid = uid,
                firstName = firstName,
                lastName = lastName,
                email = email,
                role = "citizen" // Everyone starts as a citizen
            )

            db.collection("users").document(uid).set(newUser).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun loginWithGoogle(idToken: String): Result<Unit> {
        return try {
            // 1. Authenticate with the Google ID Token
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            val user = authResult.user ?: throw Exception("Google sign-in failed")

            // 2. If this is a brand new user, create their Firestore document
            if (authResult.additionalUserInfo?.isNewUser == true) {
                // Attempt to split their Google display name into first/last
                val names = user.displayName?.split(" ") ?: listOf("", "")
                val firstName = names.firstOrNull() ?: ""
                val lastName = if (names.size > 1) names.drop(1).joinToString(" ") else ""

                val newUser = User(
                    uid = user.uid,
                    firstName = firstName,
                    lastName = lastName,
                    email = user.email ?: "",
                    role = "citizen"
                )
                db.collection("users").document(user.uid).set(newUser).await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun logout() {
        auth.signOut()
    }
}