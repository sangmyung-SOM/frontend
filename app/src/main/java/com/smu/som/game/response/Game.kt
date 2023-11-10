package com.smu.som.game.response

import com.beust.klaxon.Json

data class Game(
    @Json(name = "turn") // 1p : 1, 2p : 2
    val gameTurn: String,

//    @Json(name = "gameState") // 윷 정보, 말 갯수, 말 위치
//    val gameState: String,

    @Json(name = "message")
    val message: String,

    @Json(name = "sender")
    val sender: String,

    @Json(name = "messageType")
    val messageType: String,

    @Json(name = "gameState")
    val gameState: String



)