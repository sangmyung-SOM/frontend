package com.smu.som

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.smu.som.chat.activity.ChatActivity
import kotlinx.android.synthetic.main.activity_make_game_room.view.confirmTextView

class GetGameRoomIdDialog(context: Context) : Dialog(context) {

    private var gameRoomId : String = "1234"

    fun getRoomId(gameRoomId: String) {
        this.gameRoomId = gameRoomId
        showPopup()
    }

    fun showPopup() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.activity_make_game_room, null)
        val closeButton = dialogView.findViewById<View>(R.id.noButton)
        val enterButton = dialogView.findViewById<View>(R.id.enterButton)
        val textView = dialogView.findViewById<TextView>(R.id.confirmTextView)
        textView.confirmTextView.text = gameRoomId

        val bundle: Bundle = Bundle()

        val alertDialog = AlertDialog.Builder(context).setView(dialogView).create()
        alertDialog.show()

        // 입장하기 버튼을 누름
        enterButton.setOnClickListener {
            // 게임 화면으로 이동
//            val intent = Intent(activity, GameActivity::class.java)
//            activity.startActivity(intent)
//            activity.finish()

//            val intent = Intent(activity, ChatActivity::class.java)
//            activity.startActivity(intent)
//            activity.finish()

            // 이름 입력하는 창이 뜨고, 확인을 누르면 채팅방으로 이동
            val builder = AlertDialog.Builder(context)
            val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog, null, false)
            builder.setView(dialogView)
                .setPositiveButton("확인") { dialogInterface, i ->
                    val name = dialogView.findViewById<EditText>(R.id.name)
                    if(name != null){
                        bundle.putString("sender", name.text.toString())
                        bundle.putString("chatRoomId", gameRoomId)

                        val intent = Intent(context, ChatActivity::class.java)
                        intent.putExtra("myBundle", bundle)

                        ContextCompat.startActivity(context, intent, bundle)

                    }
                }
                .setNegativeButton("취소") { dialogInterface, i ->
                    /* 취소일 때 아무 액션이 없으므로 빈칸 */
                }
                .show()

        }

        closeButton.setOnClickListener {
            alertDialog.dismiss()
        }
    }

}
