package com.spbalkan.simplychat.auth.viewmodels

import androidx.lifecycle.ViewModel
import com.spbalkan.simplychat.utils.isValidEmail
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class RegisterViewModel : ViewModel() {

    private val _state = MutableStateFlow(RegisterState())
    val state = _state.asStateFlow()

    fun onActionIntent(action: ActionIntent) {
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
                _state.update { currentState ->
                    currentState.copy(
                        password = action.password,
                        passwordError = false // todo add rules
                    )
                }
            }

            is ActionIntent.OnConfirmPasswordChange -> {
                _state.update { currentState ->
                    currentState.copy(
                        confirmPassword = action.confirmPassword,
                        confirmPasswordError = currentState.password != action.confirmPassword
                    )
                }
            }

            is ActionIntent.OnRegister -> {
                register()
            }
        }
    }

    fun register() {
        // todo
    }

    data class RegisterState(
        val email: String = "",
        val password: String = "",
        val confirmPassword: String = "",
        val isLoading: Boolean = false,
        val emailError: Boolean = false,
        val passwordError: Boolean = false,
        val confirmPasswordError: Boolean = false,
    )

    sealed class ActionIntent {
        data class OnEmailChange(val email: String) : ActionIntent()
        data class OnPasswordChange(val password: String) : ActionIntent()
        data class OnConfirmPasswordChange(val confirmPassword: String) : ActionIntent()
        data object OnRegister : ActionIntent()
    }
}