package com.spbalkan.simplychat.chat.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class MessageDTO(
    @DocumentId
    val id: String = "",
    val text: String = "",
    val senderId: String= "",
    val timestamp: Timestamp? = null
)
