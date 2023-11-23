package com.smu.som

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import com.smu.som.test.TestActivity
import kotlinx.android.synthetic.main.activity_start.*

// 시작 화면 Activity
class StartActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        offlineStart.setOnClickListener {
            startActivity(Intent(this, TestActivity::class.java))
            finish()
//            startActivity(Intent(this, MainActivity::class.java))
//            finish()
        }
//        // start 버튼 클릭 리스너 (메인 화면으로 이동)
//        start.setOnClickListener {
//            startActivity(Intent(this, MainActivity::class.java))
//            finish()
//        }
        onlineStart.setOnClickListener {
            startActivity(Intent(this, StartActivityClicked::class.java))
            finish()
        }
        explain.setOnClickListener {
            showPopup()
        }
    }

    // 게임 설명을 보여주는 함수
    fun showPopup() {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view1 = inflater.inflate(R.layout.activity_gamerule1, null)
        val alertDialog1 = AlertDialog.Builder(this)
            .setTitle("게임 방법")
            .setPositiveButton("다음") { dialog, which ->
                showSecondPage(inflater) // 설명 2페이지를 보여주는 함수 호출
            }
            .setNegativeButton("취소", null)
        alertDialog1.setView(view1)
        alertDialog1.setCancelable(false).show()
    }

    fun showSecondPage(inflater: LayoutInflater) {
        val view2 = inflater.inflate(R.layout.activity_gamerule2, null)
        val alertDialog2 = AlertDialog.Builder(this).setTitle("게임 방법")
            .setPositiveButton("확인", null)
            .setNegativeButton("이전") { dialog, which ->
                showFirstPage(inflater) // 이전 설명 페이지를 보여주는 함수 호출
            }
        alertDialog2.setView(view2)
        alertDialog2.setCancelable(false).show()
    }

    fun showFirstPage(inflater: LayoutInflater) {
        val view1 = inflater.inflate(R.layout.activity_gamerule1, null)
        val alertDialog1 = AlertDialog.Builder(this)
            .setTitle("게임 방법")
            .setPositiveButton("다음") { dialog, which ->
                showSecondPage(inflater) // 설명 2페이지를 보여주는 함수 호출
            }
            .setNegativeButton("취소", null)
        alertDialog1.setView(view1)
        alertDialog1.setCancelable(false).show()
    }
}