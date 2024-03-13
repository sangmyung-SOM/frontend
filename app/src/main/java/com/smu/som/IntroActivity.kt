package com.smu.som

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.kakao.sdk.user.UserApiClient
import com.smu.som.game.service.TaskService

// 인트로 Activity
class IntroActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)
        startService(Intent(this, TaskService::class.java))

        var handler = Handler()
        val sp = this.getSharedPreferences("game_sp", Context.MODE_PRIVATE)
        val editor = sp.edit()
        editor.putString("isAdult", "n")
        editor.commit()

        // 카카오톡 토큰 정보 보기
        UserApiClient.instance.accessTokenInfo { tokenInfo, error ->
            if (error != null) {    // 토큰 정보 보기 실패 (로그인 필요 - 로그인 화면으로)
                handler.postDelayed({
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                }, 3600)
            }
            else if (tokenInfo != null) {   // 토큰 정보 보기 성공 (로그인 - 시작 화면으로)
                handler.postDelayed({
                    val intent = Intent(this, StartActivity::class.java)
                    startActivity(intent)
                    finish()
                }, 3600)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        finish()
    }
}