package com.smu.som.game.service

import com.gmail.bishoybasily.stomp.lib.StompClient
import com.google.gson.JsonObject
import com.smu.som.game.GameConstant

class GameStompService(private val stomp : StompClient) {

    fun sendQuestion(question : String, questionId : Int){
        var request = JsonObject()

        try {
            request.addProperty("room_id", GameConstant.GAMEROOM_ID)
            request.addProperty("player_id", GameConstant.GAME_TURN)
            request.addProperty("question_id", questionId)
            request.addProperty("question", question)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        stomp.send("/app/game/question", request.toString())
            .subscribe()
    }

    fun sendThrowResult(messageType : String) {
        var request = JsonObject()

        try {
            request.addProperty("messageType", messageType)
            request.addProperty("room_id", GameConstant.GAMEROOM_ID)
            request.addProperty("player_id", GameConstant.GAME_TURN)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        stomp.send("/app/game/throw", request.toString())
            .subscribe()
    }

    fun sendQuestionPass(questionMsg: String, questionId: Int) {
        var request = JsonObject()

        try {
            request.addProperty("room_id", GameConstant.GAMEROOM_ID)
            request.addProperty("player_id", GameConstant.GAME_TURN)
            request.addProperty("question_id", questionId)
            request.addProperty("question", questionMsg)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        stomp.send("/app/game/question/pass", request.toString())
            .subscribe()

    }
}