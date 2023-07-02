package com.smu.som

// 답변한, 패스한 질문 데이터
data class GameResult(
    val used: Array<Int>,
    val pass: Array<Int>
)