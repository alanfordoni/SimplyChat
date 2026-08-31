package com.spbalkan.simplychat.chat.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.spbalkan.simplychat.Screen
import com.spbalkan.simplychat.ServiceLocator
import com.spbalkan.simplychat.chat.ChatResult
import com.spbalkan.simplychat.chat.models.Message
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    val args = savedStateHandle.toRoute<Screen.Chat>()
    private val _inputState = MutableStateFlow("")
    val inputState = _inputState.asStateFlow()

    val messagesState: StateFlow<MessagesState> = ServiceLocator.chatRepository
        .listenToMessages(args.chatId, args.uid)
        .map { result ->
            when (result) {
                is ChatResult.ChatMessagesSuccess -> {
                    MessagesState(
                        messagesList = result.list,
                        error = ""
                    )
                }
                is ChatResult.Error -> MessagesState(error = result.error)
                else -> MessagesState(error = "Unknown Error")
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = MessagesState()
        )

    fun onAction(action: ActionIntent) {
        when (action) {
            is ActionIntent.OnInputChange -> {
                _inputState.update { action.messageText }
            }

            is ActionIntent.OnSendMessage -> {
                viewModelScope.launch {
                    ServiceLocator.chatRepository.sendMessage(
                        message = _inputState.value,
                        chatId = args.chatId,
                    )
                    _inputState.update { "" }
                }
            }
        }
    }

    data class MessagesState(
        val messagesList: List<Message> = emptyList(),
        val error: String = ""
    )

    sealed interface ActionIntent {
        data class OnInputChange(val messageText: String) : ActionIntent
        data object OnSendMessage : ActionIntent
    }
}