package com.smu.som.game.response

import com.beust.klaxon.Json

class QnAResponse {

    data class GetQuestion (
        @Json("player_id")
        var playerId: String,
        @Json("question_id")
        var questionId: Int,
        @Json("question")
        var question: String,
        @Json("penalty")
        var penalty: Int
    )

    class GetAnswer (
        @Json("player_id")
        var playerId: String,
        @Json("answer")
        var answer: String,
    )
}