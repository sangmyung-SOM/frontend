package com.smu.som.game.response

import com.beust.klaxon.Json

class Game {

    data class GetGameInfo(
        @Json(name = "messageType")
        val messageType: String,
        @Json(name = "player_id") // 1p : 1, 2p : 2
        val playerId: String,
        @Json(name = "sender")
        val sender: String,
        val message: String,
        val userNameList: String,
        val profileURL_1P: String,
        val profileURL_2P: String
    )

    data class GetThrowResult(
        @Json(name = "messageType")
        val messageType: String,
        @Json(name = "yut")
        val yut: String
    )

    data class GameWinner (
        @Json("winner")
        var winner : String,
        @Json("loser")
        var loser : String
    )

}