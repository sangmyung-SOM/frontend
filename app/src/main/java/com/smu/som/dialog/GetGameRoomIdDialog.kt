package com.smu.som.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.smu.som.R
import com.smu.som.game.activity.GameTestActivity
import com.smu.som.gameroom.activity.GameRoomListActivity


class GetGameRoomIdDialog(context: Context) : Dialog(context) {

    private var gameRoomId : String = "1234"
    private var name_1P : String = "1P"
    private var intent : Intent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // 배경 투명하게 만들기
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_make_game_room)
    }

    fun getRoomId(gameRoomId: String, name: String, intent: Intent) {
        show()
        this.gameRoomId = gameRoomId
        this.name_1P = name
        this.intent = intent

        showPopup()
    }

    @SuppressLint("SetTextI18n")
    fun showPopup() {

        val closeButton : Button = findViewById(R.id.noButton)
        val enterButton : Button = findViewById(R.id.enterButton)
        val textView : TextView = findViewById(R.id.confirmTextView)

        // 방 생성이 완료되었습니다
        textView.text = "$name_1P 방 생성이 완료되었습니다."

//        setClickEventToRoomCodeCopyIcon(gameRoomId)

        val bundle: Bundle = Bundle()

        // 입장하기 버튼을 누름
        enterButton.setOnClickListener {
            dismiss()
            intent?.getStringExtra("category")?.let { it1 -> bundle.putString("category", it1) }
            intent?.getStringExtra("kcategory")?.let { it1 -> bundle.putString("kcategory", it1) }
            intent?.getStringExtra("adult")?.let { it1 -> bundle.putString("adult", it1) }
            intent?.getStringExtra("profileUrl")?.let { it1 -> bundle.putString("profileUrl", it1) }

            Log.i("url", intent?.getStringExtra("profileUrl").toString())

            bundle.putString("sender", name_1P)
            bundle.putString("gameRoomId", gameRoomId)
            // 가나-게임방으로 이동하게 수정함
            val intent = Intent(context, GameTestActivity::class.java)
            intent.putExtra("myBundle", bundle)

            ContextCompat.startActivity(context, intent, bundle)


        }

        closeButton.setOnClickListener {
//            alertDialog.dismiss()
            dismiss()
            val intent = Intent(context, GameRoomListActivity::class.java)
            ContextCompat.startActivity(context, intent, bundle)
            // 화면 전환 애니메이션 제거
            (context as GameRoomListActivity).overridePendingTransition(0, 0)
        }
    }

    // 복사하기 아이콘 이벤트 리스너 추가 - 클릭시 게임방 코드 복사됨.
    private fun setClickEventToRoomCodeCopyIcon(gameRoomId: String){
        val roomCodeCopyIcon : ImageView = findViewById(R.id.img_room_code_copy_icon)

        roomCodeCopyIcon.setOnClickListener{
            val clipboard: ClipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("game_room_id", gameRoomId)
            clipboard.setPrimaryClip(clip)
        }
    }

}
