package com.smu.som.dialog

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.core.content.ContextCompat
import com.smu.som.R
import com.smu.som.chat.activity.ChatActivity
import kotlin.contracts.contract

class SetNameDialog(context: Context, val roomId : String) : Dialog(context) {

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

        btnEnter.setOnClickListener {
            val name : EditText = findViewById(R.id.et_name)

            if(name != null) {
                bundle.putString("sender", name.text.toString())
                bundle.putString("chatRoomId", roomId)

                val intent = Intent(context, ChatActivity::class.java)
                intent.putExtra("myBundle", bundle)

                ContextCompat.startActivity(context, intent, bundle)
            }
        }
    }

    // 취소 버튼을 클릭했을 때
    private fun setClickEventToBtnCancel(){
        val btnCancel : Button = findViewById(R.id.btn_cancel)

        btnCancel.setOnClickListener {
            dismiss()
        }
    }
}