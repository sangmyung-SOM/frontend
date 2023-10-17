package com.smu.som

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_make_game_room.view.confirmTextView


class GetGameRoomIdDialog(private val activity: Activity) {

    private var gameRoomId : String = "1234"

    fun getRoomId(gameRoomId: String) {
        this.gameRoomId = gameRoomId
        showPopup()
    }

    fun showPopup() {
        val dialogView = LayoutInflater.from(activity).inflate(R.layout.activity_make_game_room, null)
        val closeButton = dialogView.findViewById<View>(R.id.noButton)
        val enterButton = dialogView.findViewById<View>(R.id.enterButton)
        val textView = dialogView.findViewById<TextView>(R.id.confirmTextView)
        textView.confirmTextView.text = gameRoomId

        val alertDialog = AlertDialog.Builder(activity).setView(dialogView).create()
        alertDialog.show()

        enterButton.setOnClickListener {
            val intent = Intent(activity, GameActivity::class.java)
            activity.startActivity(intent)
            activity.finish()
        }
        closeButton.setOnClickListener {
            alertDialog.dismiss()
        }
    }

}
