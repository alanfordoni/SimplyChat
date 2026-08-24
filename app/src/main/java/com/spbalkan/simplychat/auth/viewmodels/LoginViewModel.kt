package com.spbalkan.simplychat.auth.viewmodels

import androidx.lifecycle.ViewModel
import com.spbalkan.simplychat.utils.isValidEmail
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class LoginViewModel : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state = _state.asStateFlow()

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
                _state.update { currentState ->
                    currentState.copy(
                        password = action.password,
                        passwordError = true
                    )
                }
            }

            is ActionIntent.OnLogin -> {
                login()
            }
        }
    }

    fun login() {
        // todo
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
}