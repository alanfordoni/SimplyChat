package com.spbalkan.simplychat.chat.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class MessageDTO(
    @DocumentId
    val id: String = "", // Firestore automatically sets this to the document ID
    val text: String = "",
    val senderId: String= "",
    val timestamp: Timestamp? = null
)
