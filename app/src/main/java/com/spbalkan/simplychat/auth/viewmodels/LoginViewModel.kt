package com.spbalkan.simplychat.auth.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spbalkan.simplychat.auth.AuthRepository
import com.spbalkan.simplychat.auth.AuthResult
import com.spbalkan.simplychat.utils.isPasswordValid
import com.spbalkan.simplychat.utils.isValidEmail
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state = _state.asStateFlow()

    private val _eventChannel = Channel<LoginEvent>(Channel.BUFFERED)
    val event = _eventChannel.receiveAsFlow()

    fun onAction(action: ActionIntent) {
        when (action) {
            is ActionIntent.OnEmailChange -> {
                val email = action.email
                val emailError = !email.isValidEmail()

                _state.update { currentState ->
                    currentState.copy(
                        email = email,
                        emailError = emailError,
                        loginEnabled = isLoginEnabled(emailError, currentState.passwordError)
                    )
                }
            }

            is ActionIntent.OnPasswordChange -> {
                val password = action.password
                val passwordError = !password.isPasswordValid()
                _state.update { currentState ->
                    currentState.copy(
                        password = password,
                        passwordError = passwordError,
                        loginEnabled = isLoginEnabled(currentState.emailError, passwordError)
                    )
                }
            }

            is ActionIntent.OnLogin -> {
                login()
            }
        }
    }

    private fun isLoginEnabled(emailError: Boolean, passwordError: Boolean): Boolean {
        return !emailError && !passwordError
    }

    fun login() {
        val email = _state.value.email
        val password = _state.value.password

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val result = authRepository.onLogin(email, password)
            when (result) {
                is AuthResult.LoginSuccess -> {
                    _eventChannel.send(LoginEvent.Success)
                }

                is AuthResult.Error -> {
                    _eventChannel.send(LoginEvent.Error(result.error))
                }

                else -> {}
            }
            _state.update { it.copy(isLoading = false) }
        }
    }

    data class LoginState(
        val email: String = "",
        val password: String = "",
        val isLoading: Boolean = false,
        val emailError: Boolean = false,
        val passwordError: Boolean = false,
        val loginEnabled: Boolean = false
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
}