package com.smu.som.game.reportQnA.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.recyclerview.widget.LinearLayoutManager
import com.gmail.bishoybasily.stomp.lib.StompClient
import com.google.gson.JsonObject
import com.smu.som.R
import com.smu.som.game.GameConstant
import com.smu.som.game.reportQnA.adapter.ReportAdapter
import com.smu.som.game.reportQnA.model.api.AnswerReportAPI
import com.smu.som.game.reportQnA.model.response.ReportResponse
import com.smu.som.game.wish.dialog.WishAddQuestionDialog
import kotlinx.android.synthetic.main.dialog_report.btn_cancel
import kotlinx.android.synthetic.main.dialog_report.rv_question_answer
import org.json.JSONException

// 질문답변 기록을 보여주는 다이얼로그
// pos : 전송 버튼 유무 1: 전송 버튼 있음, 0: 전송 버튼 없음
class AnswerReportDialog(context: Context, val stomp: StompClient, val pos: Int) : Dialog(context),
    ReportAdapter.OnItemClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // 배경 투명하게 만들기
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_report)
        setCanceledOnTouchOutside(false) // 다이얼로그 바깥 부분 눌러도 안닫히게

        btn_cancel.setOnClickListener {
            dismiss()
            if (pos == 1) {
                WishAddQuestionDialog(context, stomp).show()
            }
        }
    }

    fun showPopup() {
        showReport()
        show()
    }

    private fun showReport() {
        val api = AnswerReportAPI
        api.getQnA(GameConstant.GAMEROOM_ID).enqueue(
            object : retrofit2.Callback<ArrayList<ReportResponse.AnswerAndQuestionList>> {
                override fun onResponse(
                    call: retrofit2.Call<ArrayList<ReportResponse.AnswerAndQuestionList>>,
                    response: retrofit2.Response<ArrayList<ReportResponse.AnswerAndQuestionList>>
                ) {
                    if (response.isSuccessful) {
                        val qnaList = response.body()
                        if (qnaList != null) {
                            // 질문 리스트 adapter를 사용해서 삽입
                            val adapter = ReportAdapter(
                                qnaList,
                                LayoutInflater.from(context),
                                this@AnswerReportDialog
                            ).apply {
                                if (pos == 0) {
                                    setViewType(ReportAdapter.VIEW_TYPE_WITHOUT_BUTTON)
                                } else {
                                    setViewType(ReportAdapter.VIEW_TYPE_WITH_BUTTON)
                                }
                            }

                            rv_question_answer.adapter = adapter
                            rv_question_answer.layoutManager = LinearLayoutManager(context)
                            Log.d("AnswerReportDialog", "success: $response")

                        }
                    } else {
                        Log.d("AnswerReportDialog", "fail: $response")
                    }
                }

                override fun onFailure(
                    call: retrofit2.Call<ArrayList<ReportResponse.AnswerAndQuestionList>>,
                    t: Throwable
                ) {
                    Log.d("AnswerReportDialog", "onFailure: $t")
                }
            }
        )
    }

    override fun onSendButtonClick(reportResponseAndQuestionList: ReportResponse.AnswerAndQuestionList) {
        val request = JsonObject()

        try {
            request.addProperty("room_id", GameConstant.GAMEROOM_ID)
            request.addProperty("player_id", GameConstant.GAME_TURN)
            request.addProperty("answer", reportResponseAndQuestionList.question) // 추가 질문권 사용 시 질문 내용

        } catch (e: JSONException) {
            e.printStackTrace()
        }

        stomp.send("/app/game/question/wish", request.toString()).subscribe()

        dismiss()
    }


}