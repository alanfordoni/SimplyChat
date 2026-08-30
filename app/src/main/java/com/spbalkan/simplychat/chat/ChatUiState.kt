package com.spbalkan.simplychat.chat

import com.google.firebase.Timestamp
import com.spbalkan.simplychat.chat.models.Message

interface ChatUiState {
    val inputText: String
    val messagesList: List<Message>
    val dataError: Boolean
    val lastUpdated: Timestamp?
}