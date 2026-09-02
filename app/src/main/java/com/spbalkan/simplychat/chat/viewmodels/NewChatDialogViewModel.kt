package com.spbalkan.simplychat.chat.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spbalkan.simplychat.ServiceLocator
import com.spbalkan.simplychat.chat.ChatResult
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NewChatDialogViewModel : ViewModel() {

    private val _state = MutableStateFlow(NewChatState())
    val state = _state.asStateFlow()

    private val _eventChannel = Channel<NewChatEvents>(Channel.BUFFERED)
    val eventDoesUserExist = _eventChannel.receiveAsFlow()

    fun onAction(action: ActionIntent) {
        when (action) {
            is ActionIntent.OnSearchTextChange -> {
                val searchText = action.searchText
                _state.update { currentState ->
                    currentState.copy(
                        searchText = searchText,
                        isSearchTextError = searchText.length < 5
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
            val result = ServiceLocator.chatRepository.findUserByKeyword(searchText)

            _state.update { currentState -> currentState.copy(isLoading = false) }
            when (result) {
                is ChatResult.UserFound -> {
                    Log.d(TAG, "checkUser: username exist")
                    _eventChannel.send(NewChatEvents.EventUserFound(
                        username = result.username,
                        uid = result.userId
                    ))
                }

                else -> {
                    // TODO:
                }
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

    sealed interface NewChatEvents {
        data class EventUserFound(val username: String, val uid: String) : NewChatEvents
    }

    companion object {
        const val TAG = "zzNewChatDiagVM"
    }
}