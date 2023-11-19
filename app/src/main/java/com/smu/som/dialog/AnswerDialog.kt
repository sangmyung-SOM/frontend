package com.smu.som.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.smu.som.Question
import com.smu.som.R

class AnswerDialog(context: Context, val question: ArrayList<Question>?) : Dialog(context) {

    val bundle: Bundle = Bundle()

    override fun onCreate(savedInstanceState: Bundle?) {
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // 배경 투명하게 만들기
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_set_name)

        setClickEventToBtnEnter()
        setClickEventToBtnCancel()
    }

    // 입장하기 버튼을 클릭했을 때
    private fun setClickEventToBtnEnter(){
        val btnEnter : Button = findViewById(R.id.btn_enter)
        btnEnter.text = "답변완료"

        btnEnter.setOnClickListener {
            val answer : EditText = findViewById(R.id.et_name)

            if(answer != null) {
                // 이름을 입력하지 않고 입장하기 버튼을 눌렀을 때
                if(answer.text.toString() == ""){
                    Toast.makeText(context, "답변을 입력해 주세요.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }


            }
        }
    }

    // 취소 버튼을 클릭했을 때
    private fun setClickEventToBtnCancel(){
        val btnCancel : Button = findViewById(R.id.btn_cancel)
        btnCancel.text = "질문변경"
        btnCancel.setOnClickListener {
            dismiss()
        }
    }
}