package com.smu.som.game

object GameConstant {
    var GAME_TURN: String = ""
    lateinit var GAME_STATE: String
    var URL: String = "ws://3.34.55.111:8080/ws"

    var CHATROOM_ID: String = "0"
    var SENDER: String = "DEFAULT"

    val GAME_STATE_START: String = "START"
    val GAME_STATE_END: String = "END"

    val GAME_STATE_TURN: String = "TURN"
    val GAME_STATE_WAIT: String = "WAIT"

    val GAME_1P: String = "1P"
    val GAME_2P: String = "2P"

    val GAME_STATE_THROW = "THROW"

    val GAME_TURN_1P: String = "TURN_1P"
    val GAME_TURN_2P: String = "TURN_2P"

    val GAME_STATE_WIN: String = "WIN"
    val GAME_STATE_LOSE: String = "LOSE"

    fun set(sender: String, gameTurn: String){
        SENDER = sender
        GAME_TURN = gameTurn

    }


}