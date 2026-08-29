package com.spbalkan.simplychat.chat.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.spbalkan.simplychat.ServiceLocator
import com.spbalkan.simplychat.chat.ChatResult
import com.spbalkan.simplychat.chat.models.ConversationUser
import com.spbalkan.simplychat.chat.models.Conversation
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

    init {
        viewModelScope.launch {
            val result = ServiceLocator.chatRepository.getAllConversations()
            when (result) {
                is ChatResult.AllConversationsSuccess -> {
                    val conversations = result.list.map { convoDTO ->
                        Conversation(
                            id = convoDTO.id,
                            participants = convoDTO.participants,
                            otherUser = getOtherUserUsername(convoDTO.participants),
                            lastUpdated = convoDTO.lastUpdated
                        )
                    }

                    _state.update { currentState ->
                        currentState.copy(
                            conversations = conversations
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
                        showNewChatDialog = false
                    )
                }
                viewModelScope.launch {
                    _eventChannel.send(ConversationsEvents.EventOpenNewChat(
                        action.username,
                        action.uid
                    ))
                }
            }

            is ActionIntent.OnOpenChat -> {
                // TODO:
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

    private fun getOtherUserUsername(participants: Map<String, String>): ConversationUser {
        val myUid = ServiceLocator.authRepository.getUserAuthId()
        for (user in participants) {
            if (user.key != myUid) return ConversationUser(
                uid = user.key,
                username = user.value
            )
        }
        throw RuntimeException("$TAG getOtherUserUsername")
    }

    data class ConversationsState(
        val showNewChatDialog: Boolean = false,
        val conversations: List<Conversation> = emptyList()
    )

    sealed interface ActionIntent {
        data object OnSearchNewChat : ActionIntent
        data class OnOpenNewChat(val username: String, val uid: String) : ActionIntent
        data class OnOpenChat(val conversationUser: ConversationUser) : ActionIntent
        data object OnLogout : ActionIntent
        data object OnNewChatDismiss : ActionIntent
    }

    sealed interface ConversationsEvents {
        data class EventOpenNewChat(val username: String, val uid: String) : ConversationsEvents
    }
}