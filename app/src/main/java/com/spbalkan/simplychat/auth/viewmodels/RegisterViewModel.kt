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

class RegisterViewModel : ViewModel() {

    private val _state = MutableStateFlow(RegisterState())
    val state = _state.asStateFlow()

    private val _eventChannel = Channel<RegisterEvent>(Channel.BUFFERED)
    val event = _eventChannel.receiveAsFlow()

    fun onActionIntent(action: ActionIntent) {
        when (action) {
            is ActionIntent.OnEmailChange -> {
                val email = action.email
                val emailError = !action.email.isValidEmail()

                _state.update { currentState ->
                    currentState.copy(
                        email = email,
                        emailError = emailError,
                        registerEnabled = isRegisterEnabled(
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

    fun register() {
        val email = _state.value.email
        val password = _state.value.password

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            try {
                val authResult = Firebase.auth
                    .createUserWithEmailAndPassword(email, password)
                    .await()

                Log.d(TAG, "register: success: ${authResult.user}")
                val userId = authResult.user?.uid.orEmpty()
                _eventChannel.send(RegisterEvent.Success(userId))
            } catch (e: Exception) {
                Log.e(TAG, "register: error: ${e.localizedMessage}")
                val errorMessage = e.localizedMessage ?: "Authentication failed."
                _eventChannel.send(RegisterEvent.Error(errorMessage))
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun isRegisterEnabled(
        emailError: Boolean,
        passwordError: Boolean,
        confirmPasswordError: Boolean
    ): Boolean {

        return !emailError &&
                !passwordError &&
                !confirmPasswordError
    }

    data class RegisterState(
        val email: String = "",
        val password: String = "",
        val confirmPassword: String = "",
        val isLoading: Boolean = false,
        val emailError: Boolean = false,
        val passwordError: Boolean = false,
        val confirmPasswordError: Boolean = false,
        val registerEnabled: Boolean = false
    )

    sealed class ActionIntent {
        data class OnEmailChange(val email: String) : ActionIntent()
        data class OnPasswordChange(val password: String) : ActionIntent()
        data class OnConfirmPasswordChange(val confirmPassword: String) : ActionIntent()
        data object OnRegister : ActionIntent()
    }

    sealed interface RegisterEvent {
        data class Success(val msg: String) : RegisterEvent
        data class Error(val error: String) : RegisterEvent
    }

    companion object {
        private const val TAG = "RegisterVM"
    }
}