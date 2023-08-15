package com.smu.som

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.gmail.bishoybasily.stomp.lib.Event
import com.gmail.bishoybasily.stomp.lib.StompClient
import com.kakao.sdk.common.util.Utility
import com.kakao.sdk.user.UserApiClient
import com.kakao.sdk.user.model.AgeRange
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.OkHttpClient
import org.json.JSONObject


// 메인 화면 Activity
class MainActivity : AppCompatActivity() {

    // 안드로이드 애뮬에서 테스트 할때는 localhost 대신 10.0.2.2 사용
    val url = "ws://10.0.2.2:8080/ws"

    fun runStomp(){
        val intervalMillis = 1000L
        val client = OkHttpClient()
        val stomp = StompClient(client, intervalMillis)
        stomp.url = url;

        // connect
        var stompConnection = stomp.connect().subscribe {
            when (it.type) {
                Event.Type.OPENED -> {
                    Log.i(TAG, it.toString())
                }
                Event.Type.CLOSED -> {
                    Log.i(TAG, it.toString())
                }
                Event.Type.ERROR -> {
                    Log.i(TAG, it.toString())
                }
            }
        }

        // subscribe
        var topic = stomp.join("/topic/public").subscribe {
            Log.i(TAG, it)
        }

        /*
        {
    "type": "TALK",
    "sender": "som",
    "message": "test"
         */

        val data = JSONObject()
        data.put("type", "TALK")
        data.put("sender", "som")
        data.put("message", "test")

        // send
        stomp.send("/app/chat.sendMessage", data.toString()).subscribe {
            if (it) {
                Log.i(TAG, it.toString())
            }
        }

        //topic.dispose()
        // disconnect
        //stompConnection.dispose()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // runStomp();

        // 카카오톡 사용자 정보 요청 (기본)
        UserApiClient.instance.me { user, error ->
            val ageRange = user?.kakaoAccount?.ageRange     // 사용자 연령대
            val email = user?.kakaoAccount?.email           // 사용자 이메일
            var adult = false                               // 성인 여부

            // 성인 판별 (나이가 20대 이상일 경우)
            if (ageRange == AgeRange.AGE_20_29 || ageRange == AgeRange.AGE_30_39 || ageRange == AgeRange.AGE_40_49 || ageRange == AgeRange.AGE_50_59
                || ageRange == AgeRange.AGE_60_69 || ageRange == AgeRange.AGE_70_79 || ageRange == AgeRange.AGE_80_89 || ageRange == AgeRange.AGE_90_ABOVE) {
                adult = true
            }

            // 사용자 정보 저장 (game_sp)
            val sp = this.getSharedPreferences("game_sp", Context.MODE_PRIVATE)
            val editor = sp.edit()
            editor.putBoolean("adult", adult)
            editor.putString("email", email)
            editor.commit()
        }


        // start 버튼 클릭 리스너 (게임 설정 화면으로)
        start.setOnClickListener {
            startActivity(Intent(this, GameSettingActivity::class.java))
            finish()
        }

        // mypage 버튼 클릭 리스너 (마이페이지로)
        mypage.setOnClickListener {
            val intent = Intent(this, MypageActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}