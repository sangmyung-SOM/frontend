package com.smu.som.version1

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.recyclerview.widget.LinearLayoutManager
import com.smu.som.MasterApplication
import com.smu.som.Question
import com.smu.som.R
import kotlinx.android.synthetic.main.activity_question_list.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class QuestionListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question_list)

        val sp = this.getSharedPreferences("game_sp", Context.MODE_PRIVATE)
        val email = sp.getString("email", null)
        var category = intent.getStringExtra("category")
        var title = intent.getStringExtra("title")
        Title.text = title

        // 답변한 질문 리스트 데이터 받아오기
        email?.let {
            (application as MasterApplication).service.usedQuestion(
                it, category!!
            ).enqueue(object : Callback<ArrayList<Question>> {
                // 성공
                override fun onResponse(call: Call<ArrayList<Question>>, response: Response<ArrayList<Question>>) {
                    if (response.isSuccessful) {
                        val questionList = response.body()

                        // 질문 리스트 adapter를 사용해서 삽입
                        val adapter = QuestionAdapter(
                            questionList!!,
                            LayoutInflater.from(this@QuestionListActivity)
                        )
                        used_recyclerview.adapter = adapter
                        used_recyclerview.layoutManager = LinearLayoutManager(this@QuestionListActivity)
                    } else {
                        Log.e(ContentValues.TAG, "잘못된 카테고리 입니다.")
                    }
                }

                // 실패
                override fun onFailure(call: Call<ArrayList<Question>>, t: Throwable) {
                    Log.e(ContentValues.TAG, "서버 오류")
                }
            })
        }

        // 패스한 질문 리스트 데이터 받아오기
        email?.let {
            (application as MasterApplication).service.passQuestion(
                it, category!!
            ).enqueue(object : Callback<ArrayList<Question>> {
                // 성공
                override fun onResponse(call: Call<ArrayList<Question>>, response: Response<ArrayList<Question>>) {
                    if (response.isSuccessful) {
                        val questionList = response.body()

                        // 질문 리스트 adapter를 사용해서 삽입
                        val adapter = QuestionAdapter(
                            questionList!!,
                            LayoutInflater.from(this@QuestionListActivity)
                        )
                        pass_recyclerview.adapter = adapter
                        pass_recyclerview.layoutManager = LinearLayoutManager(this@QuestionListActivity)
                    } else {
                        Log.e(ContentValues.TAG, "잘못된 카테고리 입니다.")
                    }
                }

                // 실패
                override fun onFailure(call: Call<ArrayList<Question>>, t: Throwable) {
                    Log.e(ContentValues.TAG, "서버 오류")
                }
            })
        }

        // home 버튼 클릭 리스너 (홈 화면으로)
        home.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        // back 버튼 클릭 리스너 (뒤로가기)
        back.setOnClickListener {
            startActivity(Intent(this, GameDataActivity::class.java))
            finish()
        }
    }
}