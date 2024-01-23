package com.smu.som.game.dialog

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
import com.smu.som.Question
import com.smu.som.R
import com.smu.som.game.GameConstant
import com.smu.som.game.reportQnA.model.response.ReportResponse
import com.smu.som.game.service.GameStompService
import com.smu.som.game.reportQnA.model.service.SaveQnAService
import org.json.JSONException

// 잘문에 대한 답변을 입력하는 다이얼로그 (질문과 답변 UI 구성)
class AnsweringDialog(context: Context, private val questionList: ArrayList<Question>?, val stomp: StompClient, private val penalty: Int) : Dialog(context) {

    val bundle: Bundle = Bundle()
    var request = JsonObject()
    private var questionChange : Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // 배경 투명하게 만들기
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_question_answering)
        setCanceledOnTouchOutside(false) // 다이얼로그 바깥 부분 눌러도 안닫히게
    }

    fun showPopup() {
        show()
        val showQuestionTxt : TextView = findViewById(R.id.question)

        showQuestionTxt.movementMethod = ScrollingMovementMethod.getInstance()
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
                request.addProperty("room_id", GameConstant.GAMEROOM_ID)
                request.addProperty("player_id", GameConstant.GAME_TURN)
                request.addProperty("answer", answer.text.toString())

            } catch (e: JSONException) {
                e.printStackTrace()
            }

            stomp.send("/app/game/answer", request.toString()).subscribe()
            dismiss()


            // 질문 변경을 했을 경우 변경한 질문 내용을 서버로 전송
            val reportResponseAndQuestionList = if (questionChange) {
                ReportResponse.AnswerAndQuestionList(
                    answer = answer.text.toString(),
                    question = questionList?.get(1)?.question,
                    playerId = GameConstant.GAME_TURN
                )
            } else {
                ReportResponse.AnswerAndQuestionList(
                    answer = answer.text.toString(),
                    question = questionList?.get(0)?.question,
                    playerId = GameConstant.GAME_TURN
                )
            }

            val qnaService = SaveQnAService()
            qnaService.saveQnA(reportResponseAndQuestionList)

        }
    }

    // 질문변경 버튼을 클릭했을 때 패널티 있는 패스 1회만 허용
    private fun setClickEventToBtnChange(){
        val showQuestionTxt : TextView = findViewById(R.id.question)
        val btnChange : Button = findViewById(R.id.changeButton)

        btnChange.setOnClickListener {
            if (penalty == 1) {
                Toast.makeText(context, "패널티가 있어 질문을 변경할 수 없습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            showQuestionTxt.movementMethod = ScrollingMovementMethod.getInstance()
            val questionMsg = questionList?.get(1)?.question
            showQuestionTxt.text = questionMsg
            val questionId = questionList?.get(1)?.id

            try {
                val qStompService = GameStompService(stomp)
                qStompService.sendQuestionPass(questionMsg!!, questionId!!)
            } catch (e: JSONException) {
                e.printStackTrace()
            }

            questionChange = true // 질문 변경 했음을 표시
            btnChange.isEnabled = false
            btnChange.setOnClickListener(null)
            Toast.makeText(context, "질문 변경 1회 사용!\n 더이상 변경이 불가능 합니다.", Toast.LENGTH_SHORT).show()
        }

    }
}