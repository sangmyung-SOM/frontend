package com.smu.som.chat.model.service

import com.smu.som.chat.model.network.RetrofitCreator
import com.smu.som.chat.model.response.ChatRoom

import io.reactivex.Single
import retrofit2.http.GET

interface ChatRoomApi {

    interface ChatRoomImpl{
        @GET("/chat/rooms")
        fun getChatRoom(): Single<List<ChatRoom>>
    }

    companion object{
        fun getMovie(): Single<List<ChatRoom>>{
            return RetrofitCreator.create(ChatRoomImpl::class.java).getChatRoom()
        }
    }
}