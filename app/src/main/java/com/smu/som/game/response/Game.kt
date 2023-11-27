package com.smu.som.game.response

import com.beust.klaxon.Json

data class Game(
    @Json(name = "messageType")
    val messageType: String,
    @Json(name = "turn") // 1p : 1, 2p : 2
    val gameTurn: String,
    @Json(name = "sender")
    val sender: String,

    val turnChange: String,
    val gameCategory: String,
    val questionMessage: String,
    val answerMessage: String,
    val userNameList: String,
    val yut: String,
    val mal: String,
    val player1Score: Int,
    val player2Score: Int,
    val winner: String,




)