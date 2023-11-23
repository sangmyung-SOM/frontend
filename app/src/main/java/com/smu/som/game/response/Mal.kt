package com.smu.som.game.response

import com.beust.klaxon.Json

// 임시 data class
data class Mal(
    @Json(name = "user_id")
    val userId: String,
    @Json(name = "player_id")
    val playerId: String,
    @Json(name = "game_id")
    val gameId: String,
    @Json(name = "yut_result")
    val yutResult: String,

)
