package com.smu.som.game.service

import android.util.Log
import com.gmail.bishoybasily.stomp.lib.StompClient
import com.google.gson.JsonObject

class GameMalService (val stomp: StompClient){

    fun sendMal(playerId: String, yutResult: Int){
        var request = JsonObject()
        request.addProperty("user_id", 1)
        request.addProperty("player_id", playerId)
        request.addProperty("game_id", "asdfasdf")
        request.addProperty("yut_result", yutResult)

        stomp.send("/app/game/mal", request.toString()).subscribe()
        Log.i("som-gana", "말 이동하기 메시지 보내기")
    }
}