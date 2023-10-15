package com.smu.som

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_make_game_room.view.confirmTextView

class MakeGameRoomActivity : AppCompatActivity() {
    private lateinit var popupManager: PopupManager

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_setting)

        popupManager = PopupManager(this)

        val showPopupButton = findViewById<Button>(R.id.start)
        showPopupButton.setOnClickListener {
            popupManager.showPopup()
        }

        val textView = findViewById<TextView>(R.id.confirmTextView)
        textView.confirmTextView.text = "게임방 ID"



    }

}