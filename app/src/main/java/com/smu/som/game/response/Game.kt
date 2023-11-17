package com.smu.som.game.response

import com.beust.klaxon.Json

data class Game(
    @Json(name = "turn") // 1p : 1, 2p : 2
    val gameTurn: String,

    @Json(name = "message")
    val message: String,

    @Json(name = "sender")
    val sender: String,

    @Json(name = "messageType")
    val messageType: String,

    val gameState: String,
    val gameCategory: String,




)