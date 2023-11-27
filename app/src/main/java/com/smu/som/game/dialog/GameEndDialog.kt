package com.smu.som.game.dialog

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.smu.som.R

class GameEndDialog(context: Context) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // 배경 투명하게 만들기
        super.onCreate(savedInstanceState)
        setCanceledOnTouchOutside(false) // 다이얼로그 바깥 부분 눌러도 안닫히게
    }

    fun showPopup() {
        show()
        val builder = AlertDialog.Builder(context)
        builder.setTitle("게임 종료")
        builder.setMessage("게임 승리!")
        builder.setPositiveButton("확인", DialogInterface.OnClickListener { dialog, which ->
            dialog.dismiss()
        })
        builder.show()

    }

    fun losePopup() {
        show()
        val builder = AlertDialog.Builder(context)
        builder.setTitle("게임 종료")
        builder.setMessage("게임 패배!")
        builder.setPositiveButton("확인", DialogInterface.OnClickListener { dialog, which ->
            dialog.dismiss()
        })
        builder.show()

    }

}
