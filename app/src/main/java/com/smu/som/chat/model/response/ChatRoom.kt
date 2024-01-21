package com.smu.som.chat.model.response

data class ChatRoom (
        val roomId: String,
        val roomName: String,
        val chatList: ArrayList<Chat>
)