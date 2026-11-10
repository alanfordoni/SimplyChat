package com.spbalkan.simplychat.auth.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.spbalkan.simplychat.utils.isValidEmail
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class ForgotPasswordDialogViewModel @Inject constructor(
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _state = MutableStateFlow(ForgotPasswordState())
    val state = _state.asStateFlow()

    fun onAction(action: ActionIntent) {

        when (action) {
            is ActionIntent.OnEmailTextChange -> {
                val emailError = !action.emailText.isValidEmail()

                _state.update { currentState ->
                    currentState.copy(
                        emailText = action.emailText,
                        emailError = emailError,
                    )
                }
            }

            is ActionIntent.OnSendResetLink -> {
                _state.update { currentState ->
                    currentState.copy(
                        isLoading = true
                    )
                }
                resetPassword(_state.value.emailText)
            }
        }
    }

    fun resetPassword(email: String) {

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                _state.update { currentState ->
                    currentState.copy(
                        isLoading = false
                    )
                }

                if (task.isSuccessful) {
                    Log.d("zzForgo", "Email sent.")
                } else {
                    Log.e("zzForgo", "resetPassword: ${task.exception?.message}" )
                }
            }
    }

    data class ForgotPasswordState(
        val emailText: String = "",
        val emailError: Boolean = false,
        val isLoading: Boolean = false
    )

    sealed interface ActionIntent {
        data class OnEmailTextChange(val emailText: String) : ActionIntent
        data object OnSendResetLink : ActionIntent
    }
}