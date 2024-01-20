package com.smu.som.game.reportQnA.model.api

import com.smu.som.chat.model.network.RetrofitCreator
import com.smu.som.game.reportQnA.model.response.ReportResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface AnswerReportAPI {

//    @POST("/game/report/{gameRoomId}/qna")
//    fun sendQnA(
//        @Path("gameRoomId") gameRoomId: String,
//        @Body qnaList: ReportResponse.AnswerAndQuestionList
//    ): Call<Boolean>

    @POST("/game/room/reports/{roomId}/qna")
    fun sendQnA(
        @Path("roomId") roomId: String,
        @Query("answer") answer: String,
        @Query("question") question: String,
        @Query("playerId") playerId: String
    ): Call<Boolean>

    interface AnswerImpl{
        @POST("/reports/{roomId}/qna")
        fun sendQnA(
            @Path("roomId") gameRoomId: String,
            @Body qnaList: ReportResponse.AnswerAndQuestionList
        ): Call<Boolean>

        @GET("/reports/{roomId}/qna")
        fun getQnA(@Path("roomId") gameRoomId: String) : Call<ArrayList<ReportResponse.AnswerAndQuestionList>>
    }

    companion object{
        fun saveQnA(roomId: String, qnaList: ReportResponse.AnswerAndQuestionList): Call<Boolean> {
            return RetrofitCreator.create(AnswerImpl::class.java).sendQnA(roomId, qnaList)
        }

        fun getQnA(gameroomId: String): Call<ArrayList<ReportResponse.AnswerAndQuestionList>> {
            return RetrofitCreator.create(AnswerImpl::class.java).getQnA(gameroomId)
        }

    }
}