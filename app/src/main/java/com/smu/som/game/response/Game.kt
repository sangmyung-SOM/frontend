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
        val profileURL_2P: String,
        @Json("mal_num_limit")
        var malNumLimit: Int
    )

    data class GetThrowResult(
        @Json(name = "messageType")
        val messageType: String,
        @Json(name = "player_id")
        val playerId: String,
        @Json(name = "yut")
        val yut: String
    )

    data class GameWinner (
        @Json("winner")
        var winner : String,
        @Json("loser")
        var loser : String
    )

    data class turnChange(
        @Json(name = "messageType")
        val messageType: String,
        @Json(name = "player_id")
        val playerId: String
    )
    data class GetGameDisconnect(
        @Json("messageType")
        var messageType: String,
        @Json("room_id")
        var roomId: String?,
        @Json("player_id")
        var playerId: String?
    )
    data class PassWish (
        @Json("room_id")
        var roomId: String?,
        @Json("player_id")
        var playerId: String?,
        @Json("pass_card")
        var passCard: Int?
    )

}