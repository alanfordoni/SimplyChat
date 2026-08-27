package com.spbalkan.simplychat.chat.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spbalkan.simplychat.ServiceLocator
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NewChatDialogViewModel : ViewModel() {

    private val _state = MutableStateFlow(NewChatState())
    val state = _state.asStateFlow()

    private val _eventChannel = Channel<String>(Channel.BUFFERED)
    val eventDoesUserExist = _eventChannel.receiveAsFlow()

    fun onAction(action: ActionIntent) {
        when (action) {
            is ActionIntent.OnSearchTextChange -> {
                val searchText = action.searchText
                _state.update { currentState ->
                    currentState.copy(
                        searchText = searchText,
                        isSearchTextError = searchText.length > 4
                    )
                }
            }

            is ActionIntent.OnCheckIfUserExist -> {
                _state.update { currentState -> currentState.copy(isLoading = true) }
                checkUser()
            }
        }
    }

    private fun checkUser() {
        viewModelScope.launch {
            val searchText = _state.value.searchText
            val doesUserExist = ServiceLocator.authRepository.doesUserExist(searchText)
            _state.update { currentState -> currentState.copy(isLoading = false) }

            if (doesUserExist) {
                _eventChannel.send(searchText)
            } else {
                // todo
            }
        }
    }

    data class NewChatState(
        val searchText: String = "",
        val isSearchTextError: Boolean = false,
        val isLoading: Boolean = false
    )

    sealed interface ActionIntent {
        data class OnSearchTextChange(val searchText: String) : ActionIntent
        data object OnCheckIfUserExist : ActionIntent
    }
}