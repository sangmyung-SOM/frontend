package com.smu.som.game.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import com.smu.som.R
import kotlinx.android.synthetic.main.activity_online_gamerule1.btn_cancel
import kotlinx.android.synthetic.main.activity_online_gamerule1.btn_enter


class GameRuleDialog(context: Context) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_online_gamerule1)
        setCanceledOnTouchOutside(false) // 다이얼로그 바깥 부분 눌러도 안닫히게

        clickEnterBtn()
        clickCancelBtn()
    }

    private fun clickEnterBtn() {
        btn_enter.setOnClickListener {
           val dialog = GameRuleDialog2(context)
            dialog.show()
            dismiss()
            dialog.window?.setWindowAnimations(0)
        }
    }

    private fun clickCancelBtn() {
        btn_cancel.setOnClickListener {
            dismiss()
        }
    }

}
