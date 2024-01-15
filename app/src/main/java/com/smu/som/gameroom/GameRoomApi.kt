package com.smu.som.gameroom

import android.util.Log
import com.smu.som.GameRoomResponse
import com.smu.som.chat.model.network.RetrofitCreator
import com.smu.som.gameroom.model.GameRoom
import io.reactivex.Single
import retrofit2.Call
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface GameRoomApi {
    @POST("/game/room")
    fun makeGameRoom(@Query("name") name: String,
                     @Query("category") category: String,
                     @Query("adult") adult: String,
                     @Query("mal") mal: Int
    ): Call<GameRoomResponse>

    interface GameRoomImpl{
        @GET("/game/rooms")
        fun getGameRooms(): Single<List<GameRoom>>
        @GET("/game/room")
        fun getGameRoom(@Query("pageNumber") page: Int,
                        @Query("pageSize") pageSize: Int)
        : Single<List<GameRoom>>

        @DELETE("/game/room/{roomId}")
        fun deleteGameRoom(@Path("roomId") roomId: String): Call<Void>

        @PATCH("/game/room/{roomId}/update")
        fun updateGameState(@Path("roomId") roomId: String, @Query("state") state: Boolean): Call<Void>
    }

    companion object{
        fun getGameRooms(): Single<List<GameRoom>>{
            return RetrofitCreator.create(GameRoomImpl::class.java).getGameRooms()
        }

        fun deleteGameRoom(roomId: String): Call<Void>{
            return RetrofitCreator.create(GameRoomImpl::class.java).deleteGameRoom(roomId)
        }
        fun getGameRoom(page: Int, pageSize: Int): Single<List<GameRoom>> {
            return RetrofitCreator.create(GameRoomImpl::class.java).getGameRoom(page, pageSize)
        }

        fun updateGameState(gameroomId: String, state: Boolean) : Call<Void> {
            return RetrofitCreator.create(GameRoomImpl::class.java).updateGameState(gameroomId, state)
        }
    }
}

