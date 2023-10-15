package com.smu.som

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText

class FindGameRoomDialog(context: Context) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // 배경 투명하게 만들기
        super.onCreate(savedInstanceState)
        setContentView(R.layout.find_gameroom_dialog)

        setEnterGameroom()
    }

    // 입장하기 버튼 눌렀을 때 이벤트 리스너 등록
    fun setEnterGameroom(){
        val et_gamroom_id : EditText = findViewById(R.id.et_gameroom_id)
        val btn_enter : Button = findViewById(R.id.btn_enter)

        btn_enter.setOnClickListener{
            val text : String = et_gamroom_id.text.toString()
            Log.i("som-gana", text)
            dismiss()
        }
    }
}