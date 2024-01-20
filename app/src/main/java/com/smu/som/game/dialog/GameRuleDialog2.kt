package com.smu.som.game.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import com.smu.som.R
import kotlinx.android.synthetic.main.activity_online_gamerule2.btn_cancel_2
import kotlinx.android.synthetic.main.activity_online_gamerule2.btn_pre

class GameRuleDialog2(context: Context) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_online_gamerule2)
        setCanceledOnTouchOutside(false)

        clickEnterBtn()
        clickCancelBtn()
    }

    private fun clickEnterBtn() {
        btn_pre.setOnClickListener {
            val dialog = GameRuleDialog3(context)
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
