package com.smu.som.game.wish.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.gmail.bishoybasily.stomp.lib.StompClient
import com.google.gson.JsonObject
import com.smu.som.R
import com.smu.som.game.GameConstant
import com.smu.som.game.reportQnA.dialog.AnswerReportDialog
import org.json.JSONException

class WishAddQuestionDialog(context: Context, val stomp: StompClient) : Dialog(context) {

    // 추가 질문권을 사용한 경우 질문 입력
    // 질문 입력 후 서버로 전송
    val request = JsonObject()

    override fun onCreate(savedInstanceState: Bundle?) {
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // 배경 투명하게 만들기
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_input_qna)
        setCanceledOnTouchOutside(false) // 다이얼로그 바깥 부분 눌러도 안닫히게

        // 입력 완료 버튼 클릭 시
        clickCompleteBtn()
        // 질문기록 버튼 클릭 시
        clickReportBtn()
    }

    private fun clickCompleteBtn() {
        val btnEnter : Button = findViewById(R.id.completeButton)

        btnEnter.setOnClickListener {
            val answer : EditText = findViewById(R.id.user_answer)

            if(answer.text.toString() == ""){
                Toast.makeText(context, "질문을 입력해 주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            try {
                request.addProperty("room_id", GameConstant.GAMEROOM_ID)
                request.addProperty("player_id", GameConstant.GAME_TURN)
                request.addProperty("answer", answer.text.toString()) // 추가 질문권 사용 시 질문 내용

            } catch (e: JSONException) {
                e.printStackTrace()
            }

            stomp.send("/app/game/question/wish", request.toString()).subscribe()
            dismiss()
        }
    }

    private fun clickReportBtn() {
        val btnEnter : Button = findViewById(R.id.reportButton)

        btnEnter.setOnClickListener {
            val dialog = AnswerReportDialog(context, stomp, 1)
            dialog.showPopup()
            dismiss()
        }
    }

}