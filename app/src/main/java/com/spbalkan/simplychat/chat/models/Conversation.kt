package com.spbalkan.simplychat.chat.models

import com.google.firebase.Timestamp

data class Conversation(
    val id: String = "",
    val participants: Map<String, String> = mapOf(),
    val otherUser: ConversationUser,
    val lastUpdated: Timestamp? = null
)
