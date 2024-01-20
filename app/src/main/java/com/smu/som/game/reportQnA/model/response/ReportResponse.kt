package com.smu.som.game.reportQnA.model.response

class ReportResponse {
    // 질문과 답변을 받아오기 위한 Response
    data class AnswerAndQuestionList(
        var answer: String,
        var question: String? = null,
        var playerId: String, // 1P, 2P
    )
}
