package com.smu.som.game.dialog

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import com.gmail.bishoybasily.stomp.lib.StompClient
import com.smu.som.R
import com.smu.som.StartActivity
import com.smu.som.game.GameConstant
import com.smu.som.game.reportQnA.dialog.AnswerReportDialog
import com.smu.som.gameroom.GameRoomApi
import com.smu.som.gameroom.activity.GameRoomListActivity
import kotlinx.android.synthetic.main.dialog_game_end.btn_enter
import kotlinx.android.synthetic.main.dialog_game_end.textView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GameEndDialog(context: Context, val stomp: StompClient, val pos: Int) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // 배경 투명하게 만들기
        setContentView(R.layout.dialog_game_end)
        clickEnterBtn()
        clickCancelBtn()

        // GameRoomApi 에서 게임 방 삭제
        val gameRoomApi = GameRoomApi
        gameRoomApi.deleteGameRoom(GameConstant.GAMEROOM_ID).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                Log.d("deleteGameRoom", "success")
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.d("deleteGameRoom", "fail")
            }
        })

    }
    private fun clickEnterBtn() {
        btn_enter.setOnClickListener {
            val dialog = AnswerReportDialog(context, stomp, pos)
            dialog.showPopup()
            //dismiss()
            dialog.window?.setWindowAnimations(0)
        }
    }
    private fun clickCancelBtn() {
        textView.setOnClickListener {
            dismiss()

            OnDismissListener()
        }
    }
    fun showPopup() {
        show()
        // 클릭 감지 리스너
        // setClickListener()
        // 다이얼로그 닫히면 게임방 목록으로 이동
        OnDismissListener()
    }

    fun losePopup() {
        show()
        // 패배 이미지로 변경
        val winOrLoseImg : ImageView = findViewById(R.id.win_or_lose)
        winOrLoseImg.setImageResource(R.drawable.lose_)



       // setClickListener()
        // 클릭하면 게임방 목록으로 이동
        //OnDismissListener()

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
