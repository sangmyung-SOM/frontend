package com.smu.som.chat

object Constant{

    val MESSAGE_TYPE_ENTER: String = "ENTER"
    val MESSAGE_TYPE_TALK: String = "TALK"
    var SENDER: String = "DEFAULT"
    val URL: String = "ws://3.37.84.188:8080/ws"
//    val URL: String = "ws://10.0.2.2:8080/ws"
    var CHATROOM_ID: String = "0"

    fun set(sender: String, chatRoomId: String){
        SENDER = sender
        CHATROOM_ID = chatRoomId
    }
}
