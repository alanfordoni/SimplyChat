package com.spbalkan.simplychat.chat.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.google.firebase.Timestamp
import com.spbalkan.simplychat.Screen
import com.spbalkan.simplychat.ServiceLocator
import com.spbalkan.simplychat.chat.ChatResult
import com.spbalkan.simplychat.chat.ChatUiState
import com.spbalkan.simplychat.chat.models.Message
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    val args = savedStateHandle.toRoute<Screen.Chat>()
    private val _state = MutableStateFlow(ChatState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val result = ServiceLocator.chatRepository.getChatMessages(args.chatId, args.uid)
            when (result) {
                is ChatResult.ChatMessagesSuccess -> {
                    _state.update { currentState ->
                        currentState.copy(
                            messagesList = result.list
                        )
                    }
                }

                is ChatResult.Error -> {

                }

                else -> {}
            }
        }

    }


    data class ChatState(
        override val inputText: String = "",
        override val messagesList: List<Message> = emptyList(),
        override val dataError: Boolean = false,
        override val lastUpdated: Timestamp? = null
    ) : ChatUiState

    sealed interface ActionIntent {
        data class OnInputChange(val message: String) : ActionIntent
        data object OnSendMessage : ActionIntent
    }
}