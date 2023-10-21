package com.smu.som.chat

object Constant{

    val MESSAGE_TYPE_ENTER: String = "ENTER"
    val MESSAGE_TYPE_TALK: String = "TALK"
    var SENDER: String = "DEFAULT"
    val URL: String = "ws://3.34.55.111:8080/ws"
    var CHATROOM_ID: String = "0"

    fun set(sender: String, chatRoomId: String){
        SENDER = sender
        CHATROOM_ID = chatRoomId
    }
}
