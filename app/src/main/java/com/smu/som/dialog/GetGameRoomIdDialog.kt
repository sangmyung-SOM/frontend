package com.smu.som.dialog

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
        this.gameRoomId = gameRoomId
        this.name_1P = name
        this.intent = intent

        showPopup()
    }

    fun showPopup() {
        show()

        val closeButton : Button = findViewById(R.id.noButton)
        val enterButton : Button = findViewById(R.id.enterButton)
        val textView : TextView = findViewById(R.id.confirmTextView)

        // 방코드
        textView.setText(gameRoomId)

        setClickEventToRoomCodeCopyIcon(gameRoomId)

        val bundle: Bundle = Bundle()


        // 입장하기 버튼을 누름
        enterButton.setOnClickListener {
            dismiss()
            intent?.getStringExtra("category")?.let { it1 -> bundle.putString("category", it1) }
            intent?.getStringExtra("kcategory")?.let { it1 -> bundle.putString("kcategory", it1) }
            intent?.getStringExtra("adult")?.let { it1 -> bundle.putString("adult", it1) }

            bundle.putString("sender", name_1P)
            bundle.putString("gameRoomId", gameRoomId)
            // 가나-게임방으로 이동하게 수정함
            val intent = Intent(context, GameTestActivity::class.java)
            intent.putExtra("myBundle", bundle)


            // 임시로 저장해둠 - start
//            intent.putExtra("category", "COUPLE")
//            intent.putExtra("kcategory", "연인")
//            intent.putExtra("name1", "이솜")
//            intent.putExtra("name2", "박슴우")
            // 임시로 저장해둠 - end

            ContextCompat.startActivity(context, intent, bundle)

            // 가나-팝업창 수정해야될 부분! - 이름 입력하는 팝업창임
            // 이름 입력하는 창이 뜨고, 확인을 누르면 채팅방으로 이동
//            val setNameDialog : SetNameDialog = SetNameDialog(context, gameRoomId)
//            setNameDialog.show()


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
