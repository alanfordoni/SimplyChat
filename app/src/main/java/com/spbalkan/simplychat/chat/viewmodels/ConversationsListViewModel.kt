package com.spbalkan.simplychat.chat.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ConversationsListViewModel : ViewModel() {

    private val _state = MutableStateFlow(ConversationsState())
    val state = _state.asStateFlow()

    private val _eventChannel = Channel<ConversationsEvents>(Channel.BUFFERED)
    val event = _eventChannel.receiveAsFlow()


    fun onAction(action: ActionIntent) {
        when (action) {
            is ActionIntent.OnSearchNewChat -> {
                _state.update { currentState ->
                    currentState.copy(
                        showNewChatDialog = true
                    )
                }
            }

            is ActionIntent.OnOpenNewChat -> {
                _state.update { currentState ->
                    currentState.copy(
                        showNewChatDialog = false
                    )
                }
                viewModelScope.launch {
                    _eventChannel.send(ConversationsEvents.OpenNewChat(action.username))
                }
            }

            is ActionIntent.OnLogout -> {
                Firebase.auth.signOut()
            }

            is ActionIntent.OnNewChatDismiss -> {
                _state.update { currentState ->
                    currentState.copy(
                        showNewChatDialog = false
                    )
                }
            }
        }
    }

    data class ConversationsState(
        val showNewChatDialog: Boolean = false
    )

    sealed interface ActionIntent {
        data object OnSearchNewChat : ActionIntent
        data class OnOpenNewChat(val username: String) : ActionIntent
        data object OnLogout : ActionIntent
        data object OnNewChatDismiss : ActionIntent
    }

    sealed interface ConversationsEvents {
        data class OpenNewChat(val username: String) : ConversationsEvents
    }
}