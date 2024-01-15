package com.smu.som.game.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import com.smu.som.R
import kotlinx.android.synthetic.main.activity_online_gamerule3.btn_cancel_2
import kotlinx.android.synthetic.main.activity_online_gamerule3.btn_pre

class GameRuleDialog3(context: Context) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_online_gamerule3)
        setCanceledOnTouchOutside(false)

        clickPreBtn()
        clickCancelBtn()
    }

    private fun clickPreBtn() {
        btn_pre.setOnClickListener {
            val dialog = GameRuleDialog(context)
            dialog.show()
            dismiss()
            dialog.window?.setWindowAnimations(0)
        }
    }

    private fun clickCancelBtn() {
        btn_cancel_2.setOnClickListener {
            dismiss()
        }
    }

}
