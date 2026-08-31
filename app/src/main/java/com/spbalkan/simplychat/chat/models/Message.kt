package com.spbalkan.simplychat.chat.models

import com.google.firebase.Timestamp

data class Message(
    val id: String,
    val text: String,
    val isReceivedMessage: Boolean,
    val timestamp: Timestamp?
)
