package com.smu.som.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.getSystemService
import com.smu.som.R
import com.smu.som.chat.activity.ChatActivity


class GetGameRoomIdDialog(context: Context) : Dialog(context) {

    private var gameRoomId : String = "1234"

    override fun onCreate(savedInstanceState: Bundle?) {
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // 배경 투명하게 만들기
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_make_game_room)
    }

    fun getRoomId(gameRoomId: String) {
        this.gameRoomId = gameRoomId
        Log.i("som-gana", "game room id = ${gameRoomId}")

        showPopup()
    }

    fun showPopup() {
        // 필요 없으면 삭제 가능 - 가나
//        val dialogView = LayoutInflater.from(context).inflate(R.layout.activity_make_game_room, null)
//        val closeButton = dialogView.findViewById<View>(R.id.noButton)
//        val enterButton = dialogView.findViewById<View>(R.id.enterButton)
//        val textView = dialogView.findViewById<TextView>(R.id.confirmTextView)

        show()

        val closeButton : Button = findViewById(R.id.noButton)
        val enterButton : Button = findViewById(R.id.enterButton)
        val textView : TextView = findViewById(R.id.confirmTextView)

        // 방코드
        textView.setText(gameRoomId)

        setClickEventToRoomCodeCopyIcon(gameRoomId)

        val bundle: Bundle = Bundle()

        // 필요 없으면 삭제 가능 - 가나
//        val alertDialog = AlertDialog.Builder(context).setView(dialogView).create()
//        alertDialog.show()

        // 입장하기 버튼을 누름
        enterButton.setOnClickListener {
            // 게임 화면으로 이동
//            val intent = Intent(activity, GameActivity::class.java)
//            activity.startActivity(intent)
//            activity.finish()

//            val intent = Intent(activity, ChatActivity::class.java)
//            activity.startActivity(intent)
//            activity.finish()

            dismiss()

            // 가나-팝업창 수정해야될 부분! - 이름 입력하는 팝업창임
            // 이름 입력하는 창이 뜨고, 확인을 누르면 채팅방으로 이동
            val setNameDialog : SetNameDialog = SetNameDialog(context, gameRoomId)
            setNameDialog.show()

//            val builder = AlertDialog.Builder(context)
//            val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog, null, false)
//            builder.setView(dialogView)
//                .setPositiveButton("확인") { dialogInterface, i ->
//                    val name = dialogView.findViewById<EditText>(R.id.name)
//                    if(name != null){
//                        bundle.putString("sender", name.text.toString())
//                        bundle.putString("chatRoomId", gameRoomId)
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

        }

        closeButton.setOnClickListener {
//            alertDialog.dismiss()
            dismiss()
        }
    }

    // 복사하기 아이콘 이벤트 리스너 추가 - 클릭시 게임방 코드 복사됨.
    private fun setClickEventToRoomCodeCopyIcon(gameRoomId: String){
        val roomCodeCopyIcon : ImageView = findViewById(R.id.img_room_code_copy_icon)

        roomCodeCopyIcon.setOnClickListener{
            val clipboard: ClipboardManager = context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("game_room_id", gameRoomId)
            clipboard.setPrimaryClip(clip)
        }
    }

}
