package com.spbalkan.simplychat

import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.spbalkan.simplychat.auth.AuthRepository
import com.spbalkan.simplychat.auth.AuthRepositoryImp
import com.spbalkan.simplychat.chat.ChatRepository
import com.spbalkan.simplychat.chat.ChatRepositoryImp
import kotlin.getValue

object ServiceLocator {
    private val auth by lazy { Firebase.auth }
    private val firebaseDb by lazy { FirebaseFirestore.getInstance() }

    val authRepository: AuthRepository by lazy { AuthRepositoryImp(
        auth,
        firebaseDb
    ) }

    val chatRepository: ChatRepository by lazy {
        ChatRepositoryImp(
            auth = auth,
            firebaseDb = firebaseDb)
    }
}