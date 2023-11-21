package com.smu.som.game.dialog

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

class AnsweringDialog(context: Context, private val questionList: ArrayList<Question>?, val stomp: StompClient) : Dialog(context) {

    val bundle: Bundle = Bundle()

    var request = JsonObject()


    override fun onCreate(savedInstanceState: Bundle?) {
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // 배경 투명하게 만들기
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_question_answering)

    }

    fun showPopup() {
        show()
        var showQuestionTxt : TextView = findViewById(R.id.question)

        showQuestionTxt.text = questionList?.get(0)?.question

        setClickEventToBtnComplete()
        setClickEventToBtnChange()

    }

    // 입력완료 버튼을 클릭했을 때
    private fun setClickEventToBtnComplete(){
        val btnEnter : Button = findViewById(R.id.completeButton)

        btnEnter.setOnClickListener {
            val answer : EditText = findViewById(R.id.user_answer)

            if(answer.text.toString() == ""){
                Toast.makeText(context, "답변을 입력해 주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            try {
                request.addProperty("messageType", "ANSWER")
                request.addProperty("gameRoomId", GameConstant.GAMEROOM_ID)
                request.addProperty("sender", GameConstant.SENDER)
                request.addProperty("questionMessage", questionList?.get(0)?.question)
                request.addProperty("answerMessage", answer.text.toString())

            } catch (e: JSONException) {
                e.printStackTrace()
            }

            stomp.send("/app/game/answer", request.toString()).subscribe()
            dismiss()
        }
    }

    // 질문변경 버튼을 클릭했을 때
    private fun setClickEventToBtnChange(){
        var showQuestionTxt : TextView = findViewById(R.id.question)
        val btnChange : Button = findViewById(R.id.changeButton)
        btnChange.setOnClickListener {
            showQuestionTxt.text = questionList?.get(1)?.question

            try {
                request.addProperty("messageType", "QUESTION")
                request.addProperty("gameRoomId", GameConstant.GAMEROOM_ID)
                request.addProperty("sender", GameConstant.SENDER)
                request.addProperty("questionMessage", questionList?.get(1)?.question)

                stomp.send("/app/game/question", request.toString())
                    .subscribe()
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }

    }
}