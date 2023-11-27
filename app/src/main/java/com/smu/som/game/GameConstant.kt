package com.smu.som.game

object GameConstant {
    val TURN_CHANGE: String = "TURN_CHANGE"

    var GAME_TURN: String = ""

    val QUESTION : String = "QUESTION"
    val ANSWER : String = "ANSWER"
    val ANSWER_RESULT : String = "ANSWER_RESULT"

    lateinit var GAME_STATE: String
    var URL: String = "ws://10.0.2.2:8080/ws"
//    var URL: String = "ws://3.37.84.188:8080/ws"
    var GAMEROOM_ID: String = "0"
    var SENDER: String = "DEFAULT"

    val GAME_STATE_START: String = "START"
    val GAME_STATE_END: String = "END"

    val GAME_STATE_WAIT: String = "WAIT"

    val GAME_STATE_THROW = "THROW"

    val GAME_TURN_1P: String = "TURN_1P"
    val GAME_TURN_2P: String = "TURN_2P"

    val GAME_STATE_WIN: String = "WIN"
    val GAME_STATE_LOSE: String = "LOSE"

    val FIRST_THROW: String = "FIRST_THROW"

    fun set(sender: String, gameRoomId : String, turn : String){
        SENDER = sender
        GAMEROOM_ID = gameRoomId
        GAME_TURN = turn

    }


}