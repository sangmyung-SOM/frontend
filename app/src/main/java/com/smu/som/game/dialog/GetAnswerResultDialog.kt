package com.smu.som.game.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.TextView
import com.smu.som.R
import kotlinx.android.synthetic.main.dialog_question_complete.closeButton


class GetAnswerResultDialog(context: Context, val answer : String) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // 배경 투명하게 만들기
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_question_complete)
        setCanceledOnTouchOutside(false) // 다이얼로그 바깥 부분 눌러도 안닫히게
    }

    fun showPopup() {
        show()

        val showQuestionTxt : TextView = findViewById(R.id.question)
        showQuestionTxt.text = answer

        setClickCloseBtn()
    }

    private fun setClickCloseBtn() {
        closeButton.setOnClickListener() {
            dismiss()
        }
    }
}