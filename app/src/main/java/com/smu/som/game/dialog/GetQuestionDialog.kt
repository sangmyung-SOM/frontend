package com.smu.som.game.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import com.smu.som.R

class GetQuestionDialog(context: Context, val question: String) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // 배경 투명하게 만들기
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_question_wait_answer)

    }

    fun showPopup() {

        Handler(Looper.getMainLooper()).postDelayed({
            show()
            val questionText : TextView = findViewById(R.id.question)
            questionText.text = question
        }, 1000)

        dismiss()
    }


}