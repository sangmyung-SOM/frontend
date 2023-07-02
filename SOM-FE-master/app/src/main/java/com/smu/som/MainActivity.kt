package com.smu.som

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.kakao.sdk.common.util.Utility
import com.kakao.sdk.user.UserApiClient
import com.kakao.sdk.user.model.AgeRange
import kotlinx.android.synthetic.main.activity_main.*


// 메인 화면 Activity
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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