package com.smu.som.gameroom

import com.smu.som.GameRoomResponse
import com.smu.som.chat.model.network.RetrofitCreator
import com.smu.som.gameroom.model.GameRoom
import io.reactivex.Single
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface GameRoomApi {
    @POST("/game/room") // 실제 엔드포인트 경로를 지정하세요.
    fun makeGameRoom(@Query("name") name: String,
                     @Query("category") category: String,
                     @Query("adult") adult: String,
    ): Call<GameRoomResponse>

    interface GameRoomImpl{
        @GET("/game/rooms") // 백엔드 경로 (나중에 수정 -> /game/rooms)
        fun getGameRoom(): Single<List<GameRoom>>
    }

    companion object{
        fun getGameRooms(): Single<List<GameRoom>>{
            return RetrofitCreator.create(GameRoomImpl::class.java).getGameRoom()
        }
    }
}

