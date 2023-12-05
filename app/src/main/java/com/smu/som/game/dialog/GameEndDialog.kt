package com.smu.som.game.dialog

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import com.smu.som.R
import com.smu.som.StartActivity
import com.smu.som.gameroom.activity.GameRoomListActivity
import kotlinx.android.synthetic.main.dialog_game_end.win_or_lose

class GameEndDialog(context: Context) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // 배경 투명하게 만들기
        setContentView(R.layout.dialog_game_end)

    }

    fun showPopup() {
        show()
        // 클릭 감지 리스너
        setClickListener()
        // 다이얼로그 닫히면 게임방 목록으로 이동
        OnDismissListener()
    }

    fun losePopup() {
        show()
        // 패배 이미지로 변경
        val winOrLoseImg : ImageView = findViewById(R.id.win_or_lose)
        winOrLoseImg.setImageResource(R.drawable.lose_)


        setClickListener()
        // 클릭하면 게임방 목록으로 이동
        OnDismissListener()

    }

    private fun OnDismissListener() {
        setOnDismissListener {
            val Intent = Intent(context, StartActivity::class.java)
            context.startActivity(Intent)
        }
    }

    private fun setClickListener() {
        val touch = findViewById<ConstraintLayout>(R.id.game_end_constraintLayout)
        touch.setOnClickListener() {
            dismiss()
        }
    }

}
