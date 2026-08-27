package com.spbalkan.simplychat.auth

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

interface AuthRepository {
    fun isUserSignedIn(): Boolean
    suspend fun isUsernameAvailable(rawUsername: String): Boolean
    suspend fun addUsername(rawUsername: String, uid: String): AuthResult
    suspend fun onRegister(email: String, password: String): AuthResult
    suspend fun onLogin(email: String, password: String): AuthResult
}

class AuthRepositoryImp(
    private val auth: FirebaseAuth,
    private val firebaseDb: FirebaseFirestore
) : AuthRepository {

    override fun isUserSignedIn(): Boolean {
        val currentUser = auth.currentUser
        return currentUser != null
    }

    override suspend fun isUsernameAvailable(rawUsername: String): Boolean {
        val cleanUsername = rawUsername.trim().lowercase()

        return try {
            val docSnapshot = firebaseDb.collection("usernames")
                .document(cleanUsername)
                .get()
                .await()

            // Return TRUE if available (document does NOT exist)
            !docSnapshot.exists()
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun addUsername(rawUsername: String, uid: String): AuthResult {
        val cleanUsername = rawUsername.trim().lowercase()
        val data = hashMapOf(
            "uid" to uid
        )
        try {
            firebaseDb.collection("usernames").document(cleanUsername).set(data).await()
            return AuthResult.AddUsernameSuccess
        } catch (e: Exception) {
            Log.e(TAG, "checkUsername: error: ${e.localizedMessage}")
            val errorMessage = e.localizedMessage ?: "Authentication failed."
            return AuthResult.Error(errorMessage)
        }
    }

    override suspend fun onRegister(email: String, password: String): AuthResult {

        try {
            val result = Firebase.auth
                .createUserWithEmailAndPassword(email, password)
                .await()
            result.user?.uid?.let { uid ->
                return AuthResult.RegisterSuccess(uid)
            } ?: run {
                Log.e(TAG, "onRegister: error: user null")
                return AuthResult.Error("error: user null")
            }

        } catch (e: Exception) {
            Log.e(TAG, "onRegister: error: ${e.localizedMessage}")
            val errorMessage = e.localizedMessage ?: "Authentication failed."
            return AuthResult.Error(errorMessage)
        }
    }

    override suspend fun onLogin(email: String, password: String): AuthResult {

        try {
            Firebase.auth
                .signInWithEmailAndPassword(email, password)
                .await()
            return AuthResult.LoginSuccess

        } catch (e: Exception) {
            val errorMessage = e.localizedMessage ?: "Authentication failed."
            Log.e(TAG, "onLogin: error: $errorMessage")
            return AuthResult.Error(errorMessage)
        }
    }

    companion object {
        private const val TAG = "AuthRepo"
    }
}

sealed class AuthResult {
    data class RegisterSuccess(val uid: String) : AuthResult()
    data object AddUsernameSuccess : AuthResult()
    data object LoginSuccess : AuthResult()
    data class Error(val error: String) : AuthResult()
}