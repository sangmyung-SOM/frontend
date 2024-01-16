package com.smu.som.game.reportQnA.model.service

import android.util.Log
import com.google.gson.GsonBuilder
import com.smu.som.chat.model.network.RetrofitCreator
import com.smu.som.game.GameConstant
import com.smu.som.game.reportQnA.model.api.AnswerReportAPI
import com.smu.som.game.reportQnA.model.response.ReportResponse
import com.smu.som.gameroom.GameRoomApi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

// 질문과 답변을 서버에 저장하기 위한 서비스
class SaveQnAService() {
    fun saveQnA(qnaList: ReportResponse.AnswerAndQuestionList) {

        val AnswerAPI = AnswerReportAPI
        AnswerAPI.saveQnA(GameConstant.GAMEROOM_ID, qnaList).enqueue(
            object : Callback<Boolean> {
                override fun onResponse(
                    call: Call<Boolean>,
                    response: Response<Boolean>
                ) {
                    if (response.isSuccessful) {
                        Log.d("saveQnAService - 1", "onResponse: $response")
                    } else {
                        Log.d("saveQnAService - 1", "onResponse: $response")
                    }
                }

                override fun onFailure(call: Call<Boolean>, t: Throwable) {
                    Log.d("saveQnAService", "onFailure: $t")
                }
            }
        )

//        // Post 요청 보내기
//        //post 요청
//        val gson = GsonBuilder()
//            .setLenient()
//            .create()
//
//        // Retrofit을 초기화합니다.
//        val retrofit = Retrofit.Builder()
//            .baseUrl(RetrofitCreator.BASE_URL)
//            .addConverterFactory(GsonConverterFactory.create(gson))
//            .build()
//
//        val gameRoomApi = retrofit.create(AnswerReportAPI::class.java)
//
//        val call = qnaList.question?.let {
//            gameRoomApi.sendQnA(GameConstant.GAMEROOM_ID, qnaList.answer,
//                it, qnaList.playerId)
//        }
//
//        call?.enqueue(object : Callback<Boolean> {
//            override fun onResponse(
//                call: Call<Boolean>,
//                response: Response<Boolean>
//            ) {
//                if (response.isSuccessful) {
//                    Log.d("saveQnAService", "onResponse: $response")
//
//                } else {
//                    Log.d("saveQnAService", "onResponse: $response")
//                }
//                // 404 에러가 발생할 경우 response.errorBody()를 통해 확인
//                try {
//                    val errorBodyString = response.errorBody()?.string()
//                    Log.d("saveQnAService", "Error Body: $errorBodyString")
//                } catch (e: IOException) {
//                    e.printStackTrace()
//                }
//            }
//
//            override fun onFailure(call: Call<Boolean>, t: Throwable) {
//                Log.d("saveQnAService", "onFailure: $t")
//            }
//        })
//

    }

}