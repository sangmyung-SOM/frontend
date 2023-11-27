package com.smu.som.game.response

import com.beust.klaxon.Json

data class ScoreInfo(
    @Json("game_id")
    var gameId : String,

    @Json("player_id")
    var playerId : String,

    @Json("1P_score")
    var player1Score : Int,

    @Json("2P_score")
    var player2Score : Int
)
