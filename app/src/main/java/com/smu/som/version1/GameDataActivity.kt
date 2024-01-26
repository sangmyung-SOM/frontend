package com.smu.som.version1

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.smu.som.Data
import com.smu.som.MasterApplication
import com.smu.som.R
import kotlinx.android.synthetic.main.activity_game_data.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// 게임 기록 Activity
class GameDataActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_data)

        // 게임 설정 불러오기 (game_sp)
        val sp = this.getSharedPreferences("game_sp", Context.MODE_PRIVATE)
        val email = sp.getString("email", null)     // 카카오톡 계정 이메일

        var cnt_couple = 0          // 커플 게임 횟수
        var cnt_married = 0         // 부부 게임 횟수
        var cnt_family = 0          // 부모자녀 게임 횟수

        // 카테고리별 게임 횟수 불러오기 (API)
        email?.let {
            (application as MasterApplication).service.getData(
                it
            ).enqueue(object : Callback<Data> {
                // 성공
                override fun onResponse(call: Call<Data>, response: Response<Data>) {
                    if (response.isSuccessful) {
                        val data = response.body()
                        cnt_couple = data?.couple!!
                        cnt_married = data?.married!!
                        cnt_family = data?.family!!
                        couple_cnt.text = cnt_couple.toString()
                        married_cnt.text = cnt_married.toString()
                        family_cnt.text = cnt_family.toString()
                    } else {
                        Log.e(TAG, "불러오기 오류")
                    }
                }

                // 실패
                override fun onFailure(call: Call<Data>, t: Throwable) {
                    Log.e(TAG, "서버 오류")
                }
            })
        }

        // couple 버튼 클릭 리스너 (커플 게임 기록으로 이동)
        couple.setOnClickListener {
            if (cnt_couple > 0) {   // 게임 기록 있음
                intent = Intent(this, QuestionListActivity::class.java)
                intent.putExtra("category", "couple")
                intent.putExtra("title", "연인 질문 기록")
                startActivity(intent)
                finish()
            } else {    // 게임 기록 없음
                Toast.makeText(this@GameDataActivity, "커플 카테고리의 게임 내역이 없습니다.", Toast.LENGTH_LONG).show()
            }
        }

        // married 버튼 클릭 리스너 (부부 게임 기록으로 이동)
        married.setOnClickListener{
            if (cnt_married > 0) {  // 게임 기록 있음
                intent = Intent(this, QuestionListActivity::class.java)
                intent.putExtra("category", "married")
                intent.putExtra("title", "부부 질문 기록")
                startActivity(intent)
                finish()
            } else {    // 게임 기록 없음
                Toast.makeText(this@GameDataActivity, "부부 카테고리의 게임 내역이 없습니다.", Toast.LENGTH_LONG).show()
            }
        }

        // family 버튼 클릭 리스너 (부모자녀 게임 기록으로 이동)
        family.setOnClickListener {
            if (cnt_family > 0) {   // 게임 기록 있음
                intent = Intent(this, QuestionListActivity::class.java)
                intent.putExtra("category", "family")
                intent.putExtra("title", "부모자녀 질문 기록")
                startActivity(intent)
                finish()
            } else {    // 게임 기록 없음
                Toast.makeText(this@GameDataActivity, "부모자녀 카테고리의 게임 내역이 없습니다.", Toast.LENGTH_LONG).show()
            }
        }

        // home 버튼 클릭 리스너 (홈화면으로 이동)
        home.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        // back 버튼 클릭 리스너 (뒤로가기)
        back.setOnClickListener {
            startActivity(Intent(this, MypageActivity::class.java))
            finish()
        }
    }
}