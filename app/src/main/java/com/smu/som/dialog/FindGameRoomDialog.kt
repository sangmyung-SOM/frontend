package com.smu.som.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import com.smu.som.R

// 방 목록 UI 변경으로 인해 사용 안함
class FindGameRoomDialog(context: Context) : Dialog(context) {

    val bundle: Bundle = Bundle()
    override fun onCreate(savedInstanceState: Bundle?) {
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // 배경 투명하게 만들기
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_find_gameroom)

        setEnterGameroom()
    }

    // 입장하기 버튼 눌렀을 때 이벤트 리스너 등록
    fun setEnterGameroom(){
        val et_gamroom_id : EditText = findViewById(R.id.et_gameroom_id)
        val btn_enter : Button = findViewById(R.id.btn_enter)

        btn_enter.setOnClickListener{
            val roomId : String = et_gamroom_id.text.toString()
            if (roomId == ""){
                return@setOnClickListener
            }

            // roomId not exist


            dismiss()

            // 이름 설정 팝업창
//            val setNameDialog : SetNameDialog = SetNameDialog(context, roomId)
//            setNameDialog.show()

        }
    }
}