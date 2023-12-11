package com.smu.som.game.wish

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.gmail.bishoybasily.stomp.lib.StompClient
import com.google.gson.JsonObject
import com.smu.som.Question
import com.smu.som.R
import com.smu.som.game.GameConstant
import org.json.JSONException


class AnsweringPassDialog(context: Context, private val questionList: ArrayList<Question>?, val stomp: StompClient) : Dialog(context) {

    val bundle: Bundle = Bundle()
    var request = JsonObject()
    // 소원권 중 질문패스권이 있는 경우
    // 윷을 던지고 나서 패스를 누르고 말을 놓을 수 있음 (패널티 없는 패스)
    override fun onCreate(savedInstanceState: Bundle?) {
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // 배경 투명하게 만들기
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_quesiton_wish_pass)
        setCanceledOnTouchOutside(false) // 다이얼로그 바깥 부분 눌러도 안닫히게

        // passBtn 왼쪽 위에 패스권 갯수 뱃지로 표시 -- 나중에 구현
    }

    fun showPopup() {
        show()
        val showQuestionTxt : TextView = findViewById(R.id.question)

        showQuestionTxt.text = questionList?.get(0)?.question

        clickPassBtn()
        clickCloseBtn()

    }

    private fun clickPassBtn() {
        // 패스 버튼 클릭 시
        // 패널티 없이 말을 놓을 수 있음
        val btnPass: Button = findViewById(R.id.passButton)
        btnPass.setOnClickListener {
            try {
                request.addProperty("room_id", GameConstant.GAMEROOM_ID)
                request.addProperty("player_id", GameConstant.GAME_TURN)
                request.addProperty("answer", "패스권을 사용했습니다.")

            } catch (e: JSONException) {
                e.printStackTrace()
            }

            stomp.send("/app/game/answer", request.toString()).subscribe()

            decreasePassCard()
            dismiss()
        }
    }

    private fun decreasePassCard() {
        val request = JsonObject()
        // 패스 버튼 클릭 시 서버에 패스권 -1
        try {
            request.addProperty("room_id", GameConstant.GAMEROOM_ID)
            request.addProperty("player_id", GameConstant.GAME_TURN)
            request.addProperty("pass_card", 0)

        } catch (e: JSONException) {
            e.printStackTrace()
        }

        stomp.send("/app/game/room/wish/pass", request.toString()).subscribe()
    }

    private fun clickCloseBtn() {
        val btnEnter: Button = findViewById(R.id.completeButton)

        btnEnter.setOnClickListener {
            val answer: EditText = findViewById(R.id.user_answer)

            if (answer.text.toString() == "") {
                Toast.makeText(context, "답변을 입력해 주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            try {
                request.addProperty("room_id", GameConstant.GAMEROOM_ID)
                request.addProperty("player_id", GameConstant.GAME_TURN)
                request.addProperty("answer", answer.text.toString())

            } catch (e: JSONException) {
                e.printStackTrace()
            }

            stomp.send("/app/game/answer", request.toString()).subscribe()
            dismiss()
        }
    }
}
