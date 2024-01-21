package com.smu.som.game.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.widget.TextView
import com.smu.som.R
import kotlinx.android.synthetic.main.dialog_questions_after_end.btn_cancel

class EndingQuestionDialog(context: Context, val questions : String) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // 배경 투명하게 만들기
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_questions_after_end)
        setCanceledOnTouchOutside(false) // 다이얼로그 바깥 부분 눌러도 안닫히게

        showQuestions()
        clickCancelBtn()
    }

    // 수정 필요(아마도)
    private fun showQuestions(){
        val showQuestionTxt : TextView = findViewById(R.id.questions)
        showQuestionTxt.movementMethod = ScrollingMovementMethod.getInstance() // 스크롤 추가
        showQuestionTxt.text = questions
    }

    private fun clickCancelBtn() {
        btn_cancel.setOnClickListener {
            dismiss()
        }
    }

}
