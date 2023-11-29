package com.smu.som.game.response

import com.beust.klaxon.Json

class QnAResponse {

    data class GetQuestion (
        @Json("player_id")
        var playerId: String,
        @Json("question")
        var question: String
    )

    class GetAnswer (
        @Json("player_id")
        var playerId: String,
        @Json("answer")
        var answer: String,
        @Json("turn_change")
        var turnChange: String
    )
}