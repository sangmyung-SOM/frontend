package com.smu.som.game.wish.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.gmail.bishoybasily.stomp.lib.StompClient
import com.google.gson.JsonObject
import com.smu.som.R
import com.smu.som.game.GameConstant
import kotlinx.android.synthetic.main.dialog_input_qna.user_answer
import org.json.JSONException

class AnsweringWishDialog(context : Context, val question: String?, val stomp: StompClient) : Dialog(context) {

    val request = JsonObject()

    override fun onCreate(savedInstanceState: Bundle?) {
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // 배경 투명하게 만들기
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_input_qna)
        setCanceledOnTouchOutside(false) // 다이얼로그 바깥 부분 눌러도 안닫히게

        val questionTxt : TextView = findViewById(R.id.info)
        questionTxt.movementMethod = ScrollingMovementMethod.getInstance()
        questionTxt.text = question

        user_answer.hint = "답변을 입력해주세요."

        clickCompleteBtn()

        // 질문기록 버튼 숨김 처리
        val reportBtn : Button = findViewById(R.id.reportButton)
        reportBtn.visibility = Button.GONE
    }

    private fun clickCompleteBtn() {
        val btnEnter : Button = findViewById(R.id.completeButton)

        btnEnter.setOnClickListener {
            val answer : EditText = findViewById(R.id.user_answer)

            if(answer.text.toString() == ""){
                Toast.makeText(context, "답변을 입력해 주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            try {
                request.addProperty("room_id", GameConstant.GAMEROOM_ID)
                request.addProperty("player_id", GameConstant.GAME_TURN)
                request.addProperty("answer", answer.text.toString()) // 추가 질문권 사용 시 답변 내용

            } catch (e: JSONException) {
                e.printStackTrace()
            }

            stomp.send("/app/game/answer", request.toString()).subscribe()
            dismiss()
        }
    }

}
