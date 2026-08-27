package com.spbalkan.simplychat.chat.models

import com.google.firebase.Timestamp

data class Chat(
    val participants: String,
    val username: String,
    val lastUpdated: Timestamp
)
