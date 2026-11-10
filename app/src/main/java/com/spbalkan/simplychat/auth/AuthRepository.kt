package com.spbalkan.simplychat.auth

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.spbalkan.simplychat.data.DataStoreRepository
import com.spbalkan.simplychat.utils.Constants
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

interface AuthRepository {
    fun isUserSignedIn(): Boolean
    suspend fun doesUserExist(rawUsername: String): Boolean
    suspend fun addUsername(rawUsername: String, uid: String): AuthResult
    suspend fun onRegister(email: String, password: String): AuthResult
    suspend fun onLogin(email: String, password: String): AuthResult
}

@Singleton
class AuthRepositoryImp @Inject constructor (
    private val auth: FirebaseAuth,
    private val firebaseDb: FirebaseFirestore,
    private val dataStoreRepository: DataStoreRepository
) : AuthRepository {

    override fun isUserSignedIn(): Boolean {
        val currentUser = auth.currentUser
        return currentUser != null
    }

    override suspend fun doesUserExist(rawUsername: String): Boolean {
        val cleanUsername = rawUsername.trim().lowercase()

        return try {
            val docSnapshot = firebaseDb.collection(Constants.DB_USERNAMES_COLLECTION)
                .document(cleanUsername)
                .get()
                .await()

            docSnapshot.exists()

        } catch (e: Exception) {
            Log.e(TAG, "doesUserExist: Error: ${e.message}")
            false
        }
    }

    override suspend fun addUsername(rawUsername: String, uid: String): AuthResult {
        val cleanUsername = rawUsername.trim().lowercase()
        val data = hashMapOf(
            Constants.DB_UID to uid
        )
        try {
            firebaseDb.collection(Constants.DB_USERNAMES_COLLECTION)
                .document(cleanUsername)
                .set(data)
                .await()
            return AuthResult.AddUsernameSuccess

        } catch (e: Exception) {
            Log.e(TAG, "checkUsername: error: ${e.message}")
            val errorMessage = e.message ?: "Authentication failed."
            return AuthResult.Error(errorMessage)
        }
    }

    override suspend fun onRegister(email: String, password: String): AuthResult {

        try {
            val result = auth
                .createUserWithEmailAndPassword(email, password)
                .await()

            return result.user?.uid?.let { uid ->
                saveUsername(uid)
                AuthResult.RegisterSuccess(uid)
            } ?: run {
                Log.e(TAG, "onRegister: error: user null")
                AuthResult.Error("error: user null")
            }

        } catch (e: Exception) {
            Log.e(TAG, "onRegister: error: ${e.message}")
            val errorMessage = e.message ?: "Authentication failed."
            return AuthResult.Error(errorMessage)
        }
    }

    override suspend fun onLogin(email: String, password: String): AuthResult {

        try {
            auth.signInWithEmailAndPassword(email, password)
                .await()

            auth.currentUser?.uid?.let { userId ->
                saveUsername(userId)
            }

            return AuthResult.LoginSuccess

        } catch (e: Exception) {
            val errorMessage = e.message ?: "Authentication failed."
            Log.e(TAG, "onLogin: error: $errorMessage")
            return AuthResult.Error(errorMessage)
        }
    }

    // TODO: and user data removal on log out 

    private suspend fun saveUsername(uid: String) {
        Log.d(TAG, "saveUsername: ")
        
        val usernameSnapshot = firebaseDb.collection(Constants.DB_USERNAMES_COLLECTION)
            .whereEqualTo(Constants.DB_UID, uid)
            .limit(1)
            .get()
            .await()

        dataStoreRepository.saveUsername(usernameSnapshot.documents[0].id)
    }

    companion object {
        private const val TAG = "zzAuthRepo"
    }
}

sealed class AuthResult {
    data class RegisterSuccess(val uid: String) : AuthResult()
    data object AddUsernameSuccess : AuthResult()
    data object LoginSuccess : AuthResult()
    data class Error(val error: String) : AuthResult()
}