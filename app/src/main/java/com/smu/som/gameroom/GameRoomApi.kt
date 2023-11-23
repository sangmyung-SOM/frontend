package com.smu.som

import io.reactivex.Single
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface GameRoomApi {
    @POST("/chat/room?name=") // 실제 엔드포인트 경로를 지정하세요.
    fun makeGameRoom(@Body makeGameRoom: MakeGameRoom): Call<GameRoomResponse>



    @GET("/chat/rooms") // 실제 엔드포인트 경로를 지정하세요.
    fun getChatRoom(): Single<List<MakeGameRoom>>

//    companion object{
//        fun getMovie(): Single<List<MakeGameRoom>>{
//            return RetrofitCreator.create(GameRoomApi::class.java).getChatRoom()
//        }
//    }
}

