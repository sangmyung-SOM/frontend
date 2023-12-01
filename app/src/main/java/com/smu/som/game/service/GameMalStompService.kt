package com.smu.som.game.service

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.gmail.bishoybasily.stomp.lib.StompClient
import com.google.gson.JsonObject
import com.smu.som.game.YutConverter
import org.json.JSONException

class GameMalStompService (val stomp: StompClient){

    fun sendMalNextPosition(gameId : String, playerId: String, yutResult: Int){
        var request = JsonObject()
        request.addProperty("user_id", 1)
        request.addProperty("player_id", playerId)
        request.addProperty("game_id", gameId)
        request.addProperty("yut_result", YutConverter.toYutString(yutResult))

        stomp.send("/app/game/mal", request.toString()).subscribe()
        Log.i("som-gana", "말 이동 위치 조회 메시지 보내기")
    }

    // 말 터치 이벤트 리스너 등록
    // 말을 터치하면 서버로 해당 말 이동한다고 메시지 보냄
    fun sendMalMove(gameId: String, playerId: String, malId : Int, yutResult: Int){
        var request = JsonObject()
        request.addProperty("user_id", 1)
        request.addProperty("player_id", playerId)
        request.addProperty("game_id", gameId)
        request.addProperty("mal_id", malId)
        request.addProperty("yut_result", YutConverter.toYutString(yutResult))

        stomp.send("/app/game/mal/move", request.toString()).subscribe()
        Log.i("som-gana", "말 이동하기 메시지 보내기")

        // 2초 후에 점수 조회 메시지 보내기
        Handler(Looper.getMainLooper()).postDelayed({

            var request = JsonObject()
            try {
                request.addProperty("game_id", gameId)
                request.addProperty("player_id", playerId)
            } catch (e: JSONException) {
                e.printStackTrace()
            }

            stomp.send("/app/game/score", request.toString()).subscribe()

        }, 2000)
    }
}