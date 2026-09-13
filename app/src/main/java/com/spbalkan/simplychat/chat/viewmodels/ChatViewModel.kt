package com.spbalkan.simplychat.chat.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.spbalkan.simplychat.Screen
import com.spbalkan.simplychat.chat.ChatRepository
import com.spbalkan.simplychat.chat.ChatResult
import com.spbalkan.simplychat.chat.models.Message
import com.spbalkan.simplychat.utils.toDailyTime
import com.spbalkan.simplychat.utils.toDateString
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val chatRepository: ChatRepository
) : ViewModel() {

    val args = savedStateHandle.toRoute<Screen.Chat>()
    private val _inputState = MutableStateFlow("")
    val inputState = _inputState.asStateFlow()

    val messagesState: StateFlow<MessagesState> = chatRepository
        .listenToMessages(args.chatId)
        .map { result ->
            when (result) {
                is ChatResult.ChatMessagesSuccess -> {
                    MessagesState(
                        messagesList = transformToUiItems(result.list),
                        error = ""
                    )
                }
                is ChatResult.Error -> MessagesState(error = result.error ?: "")
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
                    chatRepository.sendMessage(
                        message = _inputState.value,
                        chatId = args.chatId,
                    )
                    _inputState.update { "" }
                }
            }
        }
    }

    private fun transformToUiItems(messages: List<Message>): List<ChatWrapperItem> {
        if (messages.isEmpty()) return emptyList()

        // group messages by LocalDate (ignoring time portion)
        val groupedMessages: Map<LocalDate, List<Message>> = messages.groupBy { message ->
            val epochMillis = message.timestamp.toDate().time
            Instant.ofEpochMilli(epochMillis)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
        }

        val resultList = mutableListOf<ChatWrapperItem>()

        groupedMessages.forEach { (date, messagesForDay) ->
            // add Messages for this day
            messagesForDay.forEach { msg ->
                resultList.add(
                    ChatWrapperItem.MessageBubble(
                        id = msg.id,
                        text = msg.text,
                        isReceivedMessage = msg.isReceivedMessage,
                        time = msg.timestamp.toDailyTime()
                    )
                )
            }

            // add Date Header
            resultList.add(
                ChatWrapperItem.DateHeader(
                    id = "header_$date",
                    dateText = formatDateHeader(date)
                )
            )
        }

        return resultList
    }

    private fun formatDateHeader(date: LocalDate): String {
        return when {
            date.isEqual(LocalDate.now()) -> "Today"
            date.isEqual(LocalDate.now().minusDays(1)) -> "Yesterday"
            else -> date.toDateString()
        }
    }

    data class MessagesState(
        val messagesList: List<ChatWrapperItem> = emptyList(),
        val error: String = ""
    )

    sealed interface ActionIntent {
        data class OnInputChange(val messageText: String) : ActionIntent
        data object OnSendMessage : ActionIntent
    }

    sealed interface ChatWrapperItem {
        val id: String // Shared key property for all items

        data class DateHeader(
            override val id: String,
            val dateText: String
        ) : ChatWrapperItem

        data class MessageBubble(
            override val id: String,
            val text: String,
            val isReceivedMessage: Boolean,
            val time: String
        ) : ChatWrapperItem
    }
}