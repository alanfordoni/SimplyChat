package com.spbalkan.simplychat.chat.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.spbalkan.simplychat.Screen
import com.spbalkan.simplychat.ServiceLocator
import com.spbalkan.simplychat.chat.ChatResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NewChatViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    val args = savedStateHandle.toRoute<Screen.Chat>()
    private val _inputState = MutableStateFlow("")
    val inputState = _inputState.asStateFlow()

    private val _messagesState = MutableStateFlow(ChatViewModel.MessagesState())
    val messagesState = _messagesState.asStateFlow()

    init {
        createChat(userId = args.uid, username = args.username)
    }

    fun createChat(username: String, userId: String) {
        viewModelScope.launch {
            val result = ServiceLocator.chatRepository.createNewChat(
                username = username,
                userId = userId
            )

            when (result) {
                is ChatResult.ChatCreated -> {
                    setMessagesListener(result.chatId)
                }

                is ChatResult.Error -> {
                    // TODO:
                }

                else -> {}
            }
        }
    }

    fun setMessagesListener(chatId: String) {

        ServiceLocator.chatRepository
            .listenToMessages(chatId, args.uid)
            .map { result ->
                when (result) {
                    is ChatResult.ChatMessagesSuccess -> {
                        _messagesState.update { messagesState ->
                            messagesState.copy(
                                messagesList = result.list,
                                error = ""
                            )
                        }
                    }

                    is ChatResult.Error -> {
                        _messagesState.update { messagesState ->
                            messagesState.copy(
                                error = result.error
                            )
                        }
                    }

                    else -> {}
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = ChatViewModel.MessagesState()
            )
    }

    fun onAction(action: ChatViewModel.ActionIntent) {
        when (action) {
            is ChatViewModel.ActionIntent.OnInputChange -> {
                _inputState.update { action.messageText }
            }

            is ChatViewModel.ActionIntent.OnSendMessage -> {
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
}