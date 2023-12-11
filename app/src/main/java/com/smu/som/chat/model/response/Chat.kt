package com.smu.som.chat.model.response

import com.beust.klaxon.Json

data class Chat (
    @Json(name = "messageType")
    val messageType: String,
    @Json(name = "sender")
    val sender: String,
    @Json(name = "message")
    val message: String,
    val gameRoomMsg: String
)