package com.smu.som

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AlertDialog
import com.kakao.sdk.talk.TalkApiClient
import com.kakao.sdk.user.UserApiClient
import com.smu.som.gameroom.activity.GameRoomListActivity
import com.smu.som.test.TestActivity
import kotlinx.android.synthetic.main.activity_start.*

// 시작 화면 Activity
class StartActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)


        background.setOnClickListener {
            startActivity(Intent(this, StartActivityClicked::class.java))
            finish()
        }

        val anim = AnimationUtils.loadAnimation(this,R.anim.blink_animation)
        blinkText.startAnimation(anim)
    }
}