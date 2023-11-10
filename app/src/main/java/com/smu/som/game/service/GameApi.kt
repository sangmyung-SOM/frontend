package com.smu.som.game.service

import retrofit2.Call
import retrofit2.http.GET

interface GameApi {

    // 윷놀이 버튼 활성화 여부
    @GET("/game/turn")
    fun getTurn(): Call<Boolean>
}

