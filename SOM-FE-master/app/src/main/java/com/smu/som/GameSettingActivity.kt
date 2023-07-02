package com.smu.som

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.activity_game_setting.*

// 게임 설정 Activity
class GameSettingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_setting)

        // 기존의 게임 설정 값을 받아와서 기본값으로 설정 (game_sp)
        val sp = this.getSharedPreferences("game_sp", Context.MODE_PRIVATE)
        var player1 = sp.getString("name1", "1P")
        var player2 = sp.getString("name2", "2P")
        var category = sp.getInt("category", 0)
        var character1 = sp.getInt("character1", 0)
        var character2 = sp.getInt("character2", 1)

        // 선택할 수 있는 값이 저장된 리스트
        val characterArray = arrayOf("토끼", "병아리", "고양이", "곰")   // 캐릭터 리스트
        val categoryArray = arrayOf("연인", "부부", "부모자녀")         // 카테고리 리스트
        val categoryMap = hashMapOf("연인" to "COUPLE", "부부" to "MARRIED", "부모자녀" to "PARENT")

        // 카테고리 선택
        var adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, categoryArray)
        spinner.adapter = adapter
        spinner.setSelection(category)

        // 캐릭터 선택 설정
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, characterArray)
        spinner1.adapter = adapter
        spinner2.adapter = adapter
        spinner1.setSelection(character1)
        spinner2.setSelection(character2)

        // 캐릭터 선택 (1P)
        spinner1.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                if (p2 == spinner2.selectedItemPosition) {
                    var idx = p2 + 1
                    if (idx == 4)
                        idx = 2
                    spinner2.setSelection(idx)
                }
            }
            override fun onNothingSelected(p0: AdapterView<*>?) { }
        }

        // 캐릭터 선택 (2P)
        spinner2.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                if (p2 == spinner1.selectedItemPosition) {
                    var idx = p2 + 1
                    if (idx == 4)
                        idx = 2
                    spinner1.setSelection(idx)
                }
            }
            override fun onNothingSelected(p0: AdapterView<*>?) { }
        }

        // 사용자 이름 설정 (기존의 저장된 값)
        name1.setText(player1)
        name2.setText(player2)

        // start 버튼 클릭 리스너 (게임 시작 - 설정값 저장 및 intent 전송)
        start.setOnClickListener {
            val intent = Intent(this, GameActivity::class.java)
            val kcategory = spinner.getSelectedItem().toString()
            val category = categoryMap[kcategory]
            val editor = sp.edit()

            // 게임 설정값 저장
            editor.putString("name1", name1.text.toString())
            editor.putString("name2", name2.text.toString())
            editor.putString("kcategory", kcategory)
            editor.putInt("category", spinner.selectedItemPosition)
            editor.putInt("character1", spinner1.selectedItemPosition)
            editor.putInt("character2", spinner2.selectedItemPosition)
            editor.commit()

            // 게임 설정값 intent 전송
            intent.putExtra("category", category)
            intent.putExtra("kcategory", kcategory)
            intent.putExtra("name1", name1.text.toString())
            intent.putExtra("name2", name2.text.toString())
            startActivity(intent)
            finish()
        }

        // home 버튼 클릭 리스너 (홈 화면으로)
        home.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        // back 버튼 클릭 리스너 (뒤로가기)
        back.setOnClickListener{
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}