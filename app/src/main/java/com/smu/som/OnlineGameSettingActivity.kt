package com.smu.som

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import com.google.gson.GsonBuilder
import com.smu.som.dialog.GetGameRoomIdDialog
import kotlinx.android.synthetic.main.activity_game_setting.*
import retrofit2.Callback
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory




// 게임 설정 Activity
class OnlineGameSettingActivity : AppCompatActivity() {
    private lateinit var getGameRoomIdDialog: GetGameRoomIdDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_online_game_setting)

        getGameRoomIdDialog = GetGameRoomIdDialog(this)

        // 기존의 게임 설정 값을 받아와서 기본값으로 설정 (game_sp)
        val sp = this.getSharedPreferences("game_sp", Context.MODE_PRIVATE)
        val categoryMap = hashMapOf("연인" to "COUPLE", "부부" to "MARRIED", "부모자녀" to "PARENT")

        //라디오 버튼
        val radioGroup1 = findViewById<RadioGroup>(R.id.radioGroup) // 라디오버튼 그룹1 관계
        val radioGroup2 = findViewById<RadioGroup>(R.id.radioGroup2) // 라디오버튼 그룹2 성인질문
        val makeRoomBtn = findViewById<Button>(R.id.makeRoomBtn_OnlineGame)

        // 방 만들기 버튼 클릭
        makeRoomBtn.setOnClickListener {
            val selectedRadioButtonId1 = radioGroup1.checkedRadioButtonId
            val selectedRadioButtonId2 = radioGroup2.checkedRadioButtonId

            //라디오 버튼 누르지 않고 makeRoomBtn 누르면 토스트 메시지 띄우기
            if (selectedRadioButtonId1 == -1 || selectedRadioButtonId2 == -1) {
                Toast.makeText(this, "게임 설정을 선택해주세요.", Toast.LENGTH_SHORT).show()
            } else {
                // 라디오 버튼을 다 선택 후 방 만들기 버튼을 누름
                getShowGameRoomId() // 서버로부터 게임 방의 roomID를 받아 팝업을 띄움 (팝업창{GetGameRoomIdDialog}에 입장하기 버튼 있음)

                // 게임 설정 하는 부분 (관계, 성인질문 on/off 설정값)
                val selectedRadioButton1 = findViewById<RadioButton>(selectedRadioButtonId1) // 관계
                val selectedOption = selectedRadioButton1.text.toString()

                val kcategory = selectedOption
                val category = categoryMap[kcategory]
                val editor = sp.edit()

                // 게임 설정값 저장
                editor.putString("kcategory", kcategory)
                editor.putString("category", category)
                editor.commit()

                // 게임 설정값 intent 전송
                intent.putExtra("category", category)
                intent.putExtra("kcategory", kcategory)
            }
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

    private fun getShowGameRoomId() {
        //post 요청
        val gson = GsonBuilder()
            .setLenient()
            .create()

        // Retrofit을 초기화합니다.
        val retrofit = Retrofit.Builder()
            .baseUrl("http://3.34.55.111:8080")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        // GameRoomApi 서비스를 생성합니다.
        val gameRoomApi = retrofit.create(GameRoomApi::class.java)
        // POST 요청을 보낼 데이터를 생성합니다.
        val makeGameRoom = MakeGameRoom("name")
        var GameRoomId = ""

        // POST 요청을 보냅니다.
        val call = gameRoomApi.makeGameRoom(makeGameRoom)
        call.enqueue(object : Callback<GameRoomResponse> {
            override fun onResponse(
                call: Call<GameRoomResponse>,
                response: Response<GameRoomResponse>
            ) {
                if (response.isSuccessful) {
                    val gameRoomResponse = response.body()
                    val roomID = gameRoomResponse?.roomId
                    if (roomID != null) {
                        GameRoomId = roomID
                        println("서버로부터 생성된 게임 방의 roomID: $roomID")
                        // 이제 roomID를 사용할 수 있습니다.
                    } else {
                        println("서버로부터 유효한 roomID를 수신하지 못했습니다.")
                    }
                } else {
                    println("POST 요청은 성공했지만 응답은 실패함")
                }
            }

            override fun onFailure(call: Call<GameRoomResponse>, t: Throwable) {
                println("POST 요청이 실패함: ${t.localizedMessage}")
            }
        })

        //비동기 처리
        Handler(Looper.getMainLooper()).postDelayed({
            //Do something
            getGameRoomIdDialog.getRoomId(GameRoomId)
        }, 500)
    }
}