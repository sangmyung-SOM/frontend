package com.smu.som.game.response

import com.beust.klaxon.Json

data class ScoreInfo(
    @Json("game_id")
    var gameId : String,

    @Json("player_id")
    var playerId : String,

    @Json("player1Score")
    var player1Score : Int,

    @Json("player2Score")
    var player2Score : Int

)
