package com.smu.som.gameroom.model

data class GameRoom(
    val roomId: String,
    val roomName: String,
    val category: String, // 연인, 부모자녀, 부부
    val adult: String, // 성인 ON/OFF
    var malNumLimit: Int
)
