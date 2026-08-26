package com.spbalkan.simplychat

import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.spbalkan.simplychat.auth.AuthRepository
import com.spbalkan.simplychat.auth.AuthRepositoryImp
import kotlin.getValue

object ServiceLocator {
    private val auth by lazy { Firebase.auth }
    val authRepository: AuthRepository by lazy { AuthRepositoryImp(auth) }
}