package com.spbalkan.simplychat.chat.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class ConversationDTO(
    @DocumentId
    val id: String = "", // Firestore automatically sets this to the document ID
    val participants: Map<String, String> = mapOf(),
    val lastUpdated: Timestamp? = null
)

