package com.smu.som

import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import kotlinx.android.synthetic.main.activity_start.*

// 시작 화면 Activity
class StartActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        // start 버튼 클릭 리스너 (메인 화면으로 이동)
        start.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        clickEnterGameroonBtn()
    }

    // 게임방 입장하기 버튼 클릭했을 때
    private fun clickEnterGameroonBtn(){
        val btn_enter_gameroom : Button = findViewById(R.id.enterRoom)

        btn_enter_gameroom.setOnClickListener {
            val dialog : Dialog = FindGameRoomDialog(this)
            dialog.show()
        }
    }
}