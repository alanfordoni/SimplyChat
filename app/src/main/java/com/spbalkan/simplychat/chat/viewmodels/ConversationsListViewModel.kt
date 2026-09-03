package com.spbalkan.simplychat.chat.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.spbalkan.simplychat.chat.ChatRepository
import com.spbalkan.simplychat.chat.ChatResult
import com.spbalkan.simplychat.chat.models.Conversation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConversationsListViewModel @Inject constructor (
    private val chatRepository: ChatRepository
): ViewModel() {

    private val _state = MutableStateFlow(ConversationsState())
    val state = _state.asStateFlow()

    private val _eventChannel = Channel<ConversationsEvents>(Channel.BUFFERED)
    val event = _eventChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            val result = chatRepository.getUserConversations()
            when (result) {
                is ChatResult.AllConversationsSuccess -> {
                    _state.update { currentState ->
                        currentState.copy(
                            conversations = result.list
                        )
                    }
                }
                
                else -> {
                    // TODO:  
                }
            }
        }
    }

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
                        showNewChatDialog = false,
                        isLoading = true
                    )
                }
                createChat(action.newChatUsername, action.newChatUid)
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

    private fun createChat(newChatUsername: String, newChatUid: String) {
        viewModelScope.launch {
            val result = chatRepository.createNewChat(
                otherUsername = newChatUsername,
                otherUserId = newChatUid
            )

            when (result) {
                is ChatResult.ChatCreated -> {
                    _state.update { currentState ->
                        currentState.copy(
                            isLoading = false
                        )
                    }
                    _eventChannel.send(ConversationsEvents.EventOpenNewChat(result.chatId, newChatUsername))
                }

                is ChatResult.Error -> {
                    // TODO: show error
                    _state.update { currentState ->
                        currentState.copy(
                            isLoading = false
                        )
                    }
                }

                else -> {}
            }
        }
    }

    data class ConversationsState(
        val showNewChatDialog: Boolean = false,
        val conversations: List<Conversation> = emptyList(),
        val isLoading: Boolean = false
    )

    sealed interface ActionIntent {
        data object OnSearchNewChat : ActionIntent
        data class OnOpenNewChat(val newChatUsername: String, val newChatUid: String) : ActionIntent
        data object OnLogout : ActionIntent
        data object OnNewChatDismiss : ActionIntent
    }

    sealed interface ConversationsEvents {
        data class EventOpenNewChat(val chatId: String, val newChatUsername: String) : ConversationsEvents
    }
}