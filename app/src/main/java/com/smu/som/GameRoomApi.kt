package com.smu.som

import io.reactivex.Single
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface GameRoomApi {
    @POST("/chat/room?name=")
    fun makeGameRoom(@Body makeGameRoom: MakeGameRoom): Call<GameRoomResponse>
}

