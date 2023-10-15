package com.smu.som

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_game_setting.view.textView
import kotlinx.android.synthetic.main.activity_make_game_room.view.confirmTextView

class PopupManager(private val activity: Activity) {

    private var gameRoomId : String = "1234"

    fun getRoomId(gameRoomId: String): String {
        this.gameRoomId = gameRoomId

        return gameRoomId
    }

    fun showPopup() {
        val dialogView = LayoutInflater.from(activity).inflate(R.layout.activity_make_game_room, null)
        val closeButton = dialogView.findViewById<View>(R.id.noButton)
        val enterButton = dialogView.findViewById<View>(R.id.enterButton)
        val textView = dialogView.findViewById<TextView>(R.id.confirmTextView)
        textView.text = getRoomId(gameRoomId)



        val alertDialog = AlertDialog.Builder(activity).setView(dialogView).create()
        alertDialog.show()

        enterButton.setOnClickListener {
            val intent = Intent(activity, GameActivity::class.java)

            //생성된 roomId로 입장함



            activity.startActivity(intent)

        }
        closeButton.setOnClickListener {
            alertDialog.dismiss()
        }
    }

}
