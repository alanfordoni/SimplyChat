package com.spbalkan.simplychat.auth.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.spbalkan.simplychat.utils.isPasswordValid
import com.spbalkan.simplychat.utils.isValidEmail
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LoginViewModel : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state = _state.asStateFlow()

    private val _eventChannel = Channel<LoginEvent>(Channel.BUFFERED)
    val event = _eventChannel.receiveAsFlow()

    fun onAction(action: ActionIntent) {
        when (action) {
            is ActionIntent.OnEmailChange -> {
                _state.update { currentState ->
                    currentState.copy(
                        email = action.email,
                        emailError = !action.email.isValidEmail()
                    )
                }
            }

            is ActionIntent.OnPasswordChange -> {
                val password = action.password
                _state.update { currentState ->
                    currentState.copy(
                        password = password,
                        passwordError = !password.isPasswordValid()
                    )
                }
            }

            is ActionIntent.OnLogin -> {
                login()
            }
        }
    }

    fun login() {
        val email = _state.value.email
        val password = _state.value.password

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            try {
                Firebase.auth
                    .signInWithEmailAndPassword(email, password)
                    .await()
                _eventChannel.send(LoginEvent.Success)

            } catch (e: Exception) {
                Log.e(TAG, "login: error: ${e.localizedMessage}")
                val errorMessage = e.localizedMessage ?: "Authentication failed."
                _eventChannel.send(LoginEvent.Error(errorMessage))

            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    data class LoginState(
        val email: String = "",
        val password: String = "",
        val isLoading: Boolean = false,
        val emailError: Boolean = false,
        val passwordError: Boolean = false
    )

    sealed class ActionIntent {
        data class OnEmailChange(val email: String) : ActionIntent()
        data class OnPasswordChange(val password: String) : ActionIntent()
        data object OnLogin : ActionIntent()
    }

    sealed interface LoginEvent {
        data object Success : LoginEvent
        data class Error(val error: String) : LoginEvent
    }

    companion object {
        const val TAG = "LoginVM"
    }
}