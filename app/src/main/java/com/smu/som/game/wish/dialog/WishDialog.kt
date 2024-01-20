package com.smu.som.game.wish.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.Button
import com.gmail.bishoybasily.stomp.lib.StompClient
import com.google.gson.JsonObject
import com.smu.som.R
import com.smu.som.game.GameConstant
import org.json.JSONException

class WishDialog(context: Context, val stomp: StompClient) : Dialog(context) {

    // 말을 잡은 경우 뜨는 소원권
    // 추가 질문권을 선택한 경우 : 상대방에게 질문을 할 수 있음
    // 패스권을 선택한 경우 : 윷을 던지고 나서 패스를 누르고 말을 놓을 수 있음 (패널티 없는 패스)
    val request = JsonObject()

    override fun onCreate(savedInstanceState: Bundle?) {
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // 배경 투명하게 만들기
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_wish)
        setCanceledOnTouchOutside(false) // 다이얼로그 바깥 부분 눌러도 안닫히게

        clickAddQuestionBtn()
        clickPassBtn()
    }

    private fun clickAddQuestionBtn() {

        val button : Button = findViewById(R.id.addtionQuestionButton)
        button.setOnClickListener {
            // 추가 질문권 버튼 클릭 시
            // 추가 질문권을 사용한 경우 질문 입력
            val dialog = WishAddQuestionDialog(context, stomp)
            dialog.show()
            dismiss()

            try {
                request.addProperty("room_id", GameConstant.GAMEROOM_ID)
                request.addProperty("player_id", GameConstant.GAME_TURN)
                request.addProperty("answer", "추가 질문권 사용")

            } catch (e: JSONException) {
                e.printStackTrace()
            }

            stomp.send("/app/game/question/wish", request.toString()).subscribe()
        }
    }

    private fun clickPassBtn() {

        val button : Button = findViewById(R.id.passButton)

        button.setOnClickListener {
            // 패스 버튼 클릭 시 서버에 패스권 적립 요청
            try {
                request.addProperty("room_id", GameConstant.GAMEROOM_ID)
                request.addProperty("player_id", GameConstant.GAME_TURN)
                request.addProperty("pass_card", 1)

            } catch (e: JSONException) {
                e.printStackTrace()
            }

            stomp.send("/app/game/room/wish/pass", request.toString()).subscribe()
            dismiss()
        }
    }


}