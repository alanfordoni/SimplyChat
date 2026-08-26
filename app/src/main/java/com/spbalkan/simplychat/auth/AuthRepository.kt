package com.spbalkan.simplychat.auth

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.tasks.await

interface AuthRepository {
    fun isUserSignedIn(): Boolean
    suspend fun onRegister(email: String, password: String): AuthResult
    suspend fun onLogin(email: String, password: String): AuthResult
}

class AuthRepositoryImp(
    private val auth: FirebaseAuth
) : AuthRepository {

    override fun isUserSignedIn(): Boolean {
        val currentUser = auth.currentUser
        return currentUser != null
    }

    override suspend fun onRegister(email: String, password: String): AuthResult {

        try {
            Firebase.auth
                .createUserWithEmailAndPassword(email, password)
                .await()
            return AuthResult.RegisterSuccess

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
    data object RegisterSuccess : AuthResult()
    data object LoginSuccess : AuthResult()
    data class Error(val error: String) : AuthResult()
}