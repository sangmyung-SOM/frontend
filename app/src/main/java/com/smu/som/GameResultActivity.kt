package com.smu.som

import android.content.Context
import android.content.Intent
import android.media.SoundPool
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import kotlinx.android.synthetic.main.activity_game_result.*

class GameResultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_result)

        // 게임 결과와 설정 불러오기
        val category = intent.getStringExtra("category")
        val kcategory = intent.getStringExtra("kcategory")
        val name_1p = intent.getStringExtra("name1")
        val name_2p = intent.getStringExtra("name2")
        val result = intent.getStringExtra("result")
        val sp = this.getSharedPreferences("game_sp", Context.MODE_PRIVATE)
        val sound = sp.getBoolean("sound", false)

        result_text.text = result   // 게임 결과 출력

        if (sound) {    // 소리 설정이 ON 인 경우
            val soundPool = SoundPool.Builder().build()
            val gamesound = soundPool.load(this, resources.getIdentifier("gameover", "raw", packageName), 1)

            // 게임 종료 소리 출력
            Handler(Looper.getMainLooper()).postDelayed({
                soundPool.play(gamesound, 1.0f, 1.0f, 0, 0, 1.0f)
            }, 500)
        }

        // game 버튼 클릭 리스너 (게임 다시 시작)
        game.setOnClickListener {
            val intent = Intent(this, GameActivity::class.java)
            intent.putExtra("category", category)
            intent.putExtra("kcategory", kcategory)
            intent.putExtra("name1", name_1p)
            intent.putExtra("name2", name_2p)
            startActivity(intent)
            finish()
        }

        // setting 버튼 클릭 리스너 (게임 설정 화면으로)
        setting.setOnClickListener {
            val intent = Intent(this, GameSettingActivity::class.java)
            startActivity(intent)
            finish()
        }

        // home 버튼 클릭 리스너 (홈 화면으로)
        home.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}