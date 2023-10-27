package com.smu.som.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import androidx.core.content.ContextCompat
import com.smu.som.R
import com.smu.som.chat.activity.ChatActivity

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
            Log.i("som-gana", roomId)

            dismiss()

            // 이름 설정 팝업창
            val setNameDialog : SetNameDialog = SetNameDialog(context, roomId)
            setNameDialog.show()

            // 필요없으면 삭제해도 괜찮습니다 - 가나
//            val builder = AlertDialog.Builder(context)
//            val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog, null, false)
//            builder.setView(dialogView)
//                .setPositiveButton("확인") { dialogInterface, i ->
//                    val name = dialogView.findViewById<EditText>(R.id.name)
//                    if(name != null){
//                        bundle.putString("sender", name.text.toString())
//                        bundle.putString("chatRoomId", roomId)
//
//                        val intent = Intent(context, ChatActivity::class.java)
//                        intent.putExtra("myBundle", bundle)
//
//                        ContextCompat.startActivity(context, intent, bundle)
//
//                    }
//                }
//                .setNegativeButton("취소") { dialogInterface, i ->
//                    /* 취소일 때 아무 액션이 없으므로 빈칸 */
//                }
//                .show()
//
//
//
//            dismiss()
        }
    }
}