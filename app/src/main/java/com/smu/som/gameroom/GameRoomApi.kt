package com.smu.som.gameroom

import android.util.Log
import com.smu.som.GameRoomResponse
import com.smu.som.chat.model.network.RetrofitCreator
import com.smu.som.gameroom.model.GameRoom
import io.reactivex.Single
import retrofit2.Call
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface GameRoomApi {
    @POST("/game/room")
    fun makeGameRoom(@Query("name") name: String,
                     @Query("category") category: String,
                     @Query("adult") adult: String,
    ): Call<GameRoomResponse>

    interface GameRoomImpl{
        @GET("/game/rooms")
        fun getGameRoom(): Single<List<GameRoom>>

        @DELETE("/game/room/{roomId}")
        fun deleteGameRoom(@Path("roomId") roomId: String): Call<Void>
    }

    companion object{
        fun getGameRooms(): Single<List<GameRoom>>{
            return RetrofitCreator.create(GameRoomImpl::class.java).getGameRoom()
        }

        fun deleteGameRoom(roomId: String): Call<Void>{
            return RetrofitCreator.create(GameRoomImpl::class.java).deleteGameRoom(roomId)
        }
    }
}

