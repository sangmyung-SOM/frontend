package com.smu.som

import com.smu.som.chat.model.response.Chat
import retrofit2.Call
import retrofit2.http.*


// retrofit 인터페이스 (API 소통)
interface RetrofitService {

    // 해당 카테고리의 질문 리스트를 받아오는 함수
    @GET("api/question/{category}/")
    fun getQuestion(
        @Path("category") category: String,
        @Query("isAdult") isAdult: String
    ): Call<ArrayList<Question>>

    // 해당 사용자, 카테고리의 답변한 질문 리스트를 받아오는 함수
    @GET("api/question/{kakaoID}/{category}/used")
    fun usedQuestion(
        @Path("kakaoID") kakaoID: String,
        @Path("category") category: String
    ): Call<ArrayList<Question>>

    // 해당 사용자, 카테고리의 패스한 질문 리스트를 받아오는 함수
    @GET("api/question/{kakaoID}/{category}/pass")
    fun passQuestion(
        @Path("kakaoID") kakaoID: String,
        @Path("category") category: String
    ): Call<ArrayList<Question>>

    // 게임의 질문 기록을 저장하는 함수
    @POST("api/question/{kakaoID}/{category}")
    fun saveResult(
        @Path("kakaoID") kakaoID: String,
        @Path("category") category: String,
        @Body result: GameResult
    ): Call<Boolean>

    // 카테고리별 게임 횟수 데이터를 받아오는 함수
    @GET("api/question/playcount/{kakaoID}")
    fun getData(
        @Path("kakaoID") kakaoID: String,
    ): Call<Data>

    @GET("/room/{roomId}/chatLogs")
    fun getChatLogs(
        @Path("roomId") roomId: String
    ):Call<ArrayList<Chat>>
}