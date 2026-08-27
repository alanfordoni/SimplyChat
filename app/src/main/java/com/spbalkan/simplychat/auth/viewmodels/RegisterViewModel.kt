package com.spbalkan.simplychat.auth.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spbalkan.simplychat.ServiceLocator
import com.spbalkan.simplychat.auth.AuthResult
import com.spbalkan.simplychat.utils.isPasswordValid
import com.spbalkan.simplychat.utils.isValidEmail
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegisterViewModel : ViewModel() {

    private val _state = MutableStateFlow(RegisterState())
    val state = _state.asStateFlow()

    private val _eventChannel = Channel<RegisterEvent>(Channel.BUFFERED)
    val event = _eventChannel.receiveAsFlow()

    fun onActionIntent(action: ActionIntent) {
        when (action) {
            is ActionIntent.OnUsernameChange -> {
                val username = action.username
                _state.update { currentState ->
                    currentState.copy(
                        username = username,
                        usernameAvailable = false
                    )
                }

                if (username.length > 4) {
                    viewModelScope.launch {
                        val isAvailable = !ServiceLocator.authRepository.doesUserExist(username)
                        _state.update { currentState ->
                            currentState.copy(
                                usernameAvailable = isAvailable
                            )
                        }
                    }
                } else {
                    _state.update { currentState ->
                        currentState.copy(
                            usernameAvailable = false
                        )
                    }
                }
            }

            is ActionIntent.OnEmailChange -> {
                val email = action.email
                val emailError = !action.email.isValidEmail()

                _state.update { currentState ->
                    currentState.copy(
                        email = email,
                        emailError = emailError,
                        registerEnabled = isRegisterEnabled(
                            isUsernameAvailable = currentState.usernameAvailable,
                            emailError = emailError,
                            passwordError = currentState.passwordError,
                            confirmPasswordError = currentState.confirmPasswordError
                        )
                    )
                }
            }

            is ActionIntent.OnPasswordChange -> {
                val password = action.password
                val passwordError = !action.password.isPasswordValid()

                _state.update { currentState ->
                    currentState.copy(
                        password = password,
                        passwordError = passwordError,
                        confirmPasswordError = password != currentState.confirmPassword,
                        registerEnabled = isRegisterEnabled(
                            isUsernameAvailable = currentState.usernameAvailable,
                            emailError = currentState.emailError,
                            passwordError = passwordError,
                            confirmPasswordError = currentState.confirmPasswordError
                        )
                    )
                }
            }

            is ActionIntent.OnConfirmPasswordChange -> {
                val confirmPassword = action.confirmPassword
                val confirmPasswordError = !confirmPassword.isPasswordValid()

                _state.update { currentState ->
                    currentState.copy(
                        confirmPassword = action.confirmPassword,
                        confirmPasswordError = currentState.password != action.confirmPassword,
                        registerEnabled = isRegisterEnabled(
                            isUsernameAvailable = currentState.usernameAvailable,
                            emailError = currentState.emailError,
                            passwordError = currentState.passwordError,
                            confirmPasswordError = confirmPasswordError
                        )
                    )
                }
            }

            is ActionIntent.OnRegister -> {
                register()
            }
        }
    }

    private fun isRegisterEnabled(
        isUsernameAvailable: Boolean,
        emailError: Boolean,
        passwordError: Boolean,
        confirmPasswordError: Boolean
    ): Boolean {

        return isUsernameAvailable &&
            !emailError &&
            !passwordError &&
            !confirmPasswordError
    }

    fun register() {
        val email = _state.value.email
        val password = _state.value.password

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            Log.d(TAG, "register: ")

            val result = ServiceLocator.authRepository.onRegister(email, password)
            Log.d(TAG, "register: 2")

            when (result) {
                is AuthResult.RegisterSuccess -> {
                    addUsername(result.uid)
                    _eventChannel.send(RegisterEvent.Success)
                }

                is AuthResult.Error -> {
                    _state.update { it.copy(isLoading = false) }
                    _eventChannel.send(RegisterEvent.Error(result.error))
                }

                else -> {}
            }
        }
    }

    suspend fun addUsername(uid: String) {
        ServiceLocator.authRepository.addUsername(_state.value.username, uid)

        _state.update { it.copy(isLoading = false) }
    }

    data class RegisterState(
        val username: String = "",
        val usernameAvailable: Boolean = false,
        val email: String = "",
        val password: String = "",
        val confirmPassword: String = "",
        val isLoading: Boolean = false,
        val emailError: Boolean = false,
        val passwordError: Boolean = false,
        val confirmPasswordError: Boolean = false,
        val registerEnabled: Boolean = false
    )

    sealed interface ActionIntent {
        data class OnUsernameChange(val username: String) : ActionIntent
        data class OnEmailChange(val email: String) : ActionIntent
        data class OnPasswordChange(val password: String) : ActionIntent
        data class OnConfirmPasswordChange(val confirmPassword: String) : ActionIntent
        data object OnRegister : ActionIntent
    }

    sealed interface RegisterEvent {
        data object Success : RegisterEvent
        data class Error(val error: String) : RegisterEvent
    }

    companion object {
        private const val TAG = "RegisterVM"
    }
}