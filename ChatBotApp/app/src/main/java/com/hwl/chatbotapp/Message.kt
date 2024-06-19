package com.hwl.chatbotapp

import java.util.UUID

//data class Message(var content: String, val isUser: Boolean)
const val USER_PREFIX = "user"
const val MODEL_PREFIX = "model"
data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    var message: String = "",
    val author: String,
    val isLoading: Boolean = false
) {
    val isFromUser: Boolean
        get() = author == USER_PREFIX
}
