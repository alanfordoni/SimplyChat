package com.spbalkan.simplychat.chat.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.google.firebase.Timestamp
import com.spbalkan.simplychat.Screen
import com.spbalkan.simplychat.ServiceLocator
import com.spbalkan.simplychat.chat.ChatUiState
import com.spbalkan.simplychat.chat.models.Message
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NewChatViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    val args = savedStateHandle.toRoute<Screen.NewChat>()

    init {
        createChat(userId = args.uid, username = args.username)
    }

    private val _state = MutableStateFlow(ChatState())
    val state = _state.asStateFlow()

    fun createChat(username: String, userId: String) {
        viewModelScope.launch {
            ServiceLocator.chatRepository.createNewChat(
                username = username,
                userId = userId
            )
        }
    }

    fun getUser(username: String) {
        viewModelScope.launch {
            ServiceLocator.chatRepository.getUserByUsername(username)
        }
    }

    fun onAction(action: ChatViewModel.ActionIntent) {
        when (action) {
            is ChatViewModel.ActionIntent.OnInputChange -> {
                _state.update { currentState ->
                    currentState.copy(
                        inputText = action.message
                    )
                }
            }

            is ChatViewModel.ActionIntent.OnSendMessage -> {
                viewModelScope.launch {
                    /*ServiceLocator.chatRepository.sendMessage(
                        message = _state.value.inputText,
                        username = args.username,
                        userId = args.uid
                    )*/
                }
            }
        }
    }

    data class ChatState(
        override val inputText: String = "",
        override val messagesList: List<Message> = emptyList(),
        override val dataError: Boolean = false,
        override val lastUpdated: Timestamp? = null
    ) : ChatUiState
}