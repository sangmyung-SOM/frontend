package com.smu.som

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import com.google.gson.GsonBuilder
import com.smu.som.chat.model.network.RetrofitCreator
import com.smu.som.databinding.ActivityOnlineGameSettingBinding
import com.smu.som.dialog.GetGameRoomIdDialog
import com.smu.som.game.GameConstant
import com.smu.som.gameroom.GameRoomApi
import com.smu.som.gameroom.MakeGameRoom
import kotlinx.android.synthetic.main.activity_online_game_setting.name_1P_OG
import kotlinx.android.synthetic.main.activity_online_game_setting.noButton
import retrofit2.Callback
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


// 게임 설정 Activity
class OnlineGameSettingDialog(context: Context) : Dialog(context) {
    private lateinit var getGameRoomIdDialog: GetGameRoomIdDialog
    private lateinit var name: EditText
    private var intent = Intent()
    private lateinit var binding: ActivityOnlineGameSettingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        super.onCreate(savedInstanceState)
        binding = ActivityOnlineGameSettingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getGameRoomIdDialog = GetGameRoomIdDialog(context)

        val categoryMap = hashMapOf("연인" to "COUPLE", "부부" to "MARRIED", "부모자녀" to "PARENT")
        val characterArray = arrayOf("토끼", "병아리", "고양이", "곰")   // 캐릭터 리스트

        var adapter = ArrayAdapter(context, android.R.layout.simple_list_item_1, characterArray)
        val bundle: Bundle = Bundle()

        setBtnMalNumMinus()
        setBtnMalNumPlus()

        //라디오 버튼
        val rgCategory = findViewById<RadioGroup>(R.id.rg_category) // 라디오버튼 그룹1 관계
        val rgAdult = findViewById<RadioGroup>(R.id.rg_adult) // 라디오버튼 그룹2 성인질문
        val makeRoomBtn = findViewById<Button>(R.id.makeRoomButton)

        // 방 만들기 버튼 클릭
        makeRoomBtn.setOnClickListener {

            GameConstant.MAL_NUM_LIMIT = binding.tvMalNum.text.toString().toInt()

            // 이름 설정
            name = findViewById(R.id.name_1P_OG)
            if (name.text.toString() == "") {
                Toast.makeText(context, "이름을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedCategory = rgCategory.checkedRadioButtonId
            val selectedAdult = rgAdult.checkedRadioButtonId

            //라디오 버튼 누르지 않고 makeRoomBtn 누르면 토스트 메시지 띄우기
            if (selectedCategory == -1) {
                Toast.makeText(context, "카테고리를 선택해주세요.", Toast.LENGTH_SHORT).show()
            }
            else if(selectedAdult == -1){
                Toast.makeText(context, "성인질문 포함 여부를 선택해주세요.", Toast.LENGTH_SHORT).show()
            }
            else {

                // 게임 설정 하는 부분 (관계, 성인질문 on/off 설정값)
                val selectedRadioButton1 = findViewById<RadioButton>(selectedCategory) // 관계
                val selectedRadioButton2 = findViewById<RadioButton>(selectedAdult) // 성인질문
                val selectedOption = selectedRadioButton1.text.toString()

                val kcategory = selectedOption
                val category = categoryMap[kcategory]
                val adult = selectedRadioButton2.text.toString()
//                val editor = sp.edit()

//                // 게임 설정값 저장
//                editor.putString("kcategory", kcategory)
//                editor.putString("category", category)
//                editor.putString("adult", selectedRadioButton2.text.toString())
//                editor.commit()

                // 게임 설정값 intent 전송
//                val intent = Intent(context, OnlineGameSettingActivity::class.java)
                intent.putExtra("category", category)
                intent.putExtra("kcategory", kcategory)
                intent.putExtra("adult", adult)

                // 라디오 버튼을 다 선택 후 방 만들기 버튼을 누름
                // 관계, 성인질문 설정값을 서버로 보내고, 서버로부터 게임 방의 roomID를 받아 팝업을 띄움 (팝업창{GetGameRoomIdDialog}에 입장하기 버튼 있음)
                getShowGameRoomId(category, adult)

            }
        }

        // 취소 버튼 클릭
        noButton.setOnClickListener {
            dismiss()
        }

    }


    private fun getShowGameRoomId(category: String?, adult: String?) {
        //post 요청
        val gson = GsonBuilder()
            .setLenient()
            .create()

        // Retrofit을 초기화합니다.
        val retrofit = Retrofit.Builder()
            .baseUrl(RetrofitCreator.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        val gameRoomApi = retrofit.create(GameRoomApi::class.java)
        val makeGameRoom = MakeGameRoom(name_1P_OG.text.toString(), category, adult)
        var GameRoomId = ""
        val malNum = binding.tvMalNum.text.toString().toInt()

        // POST 요청을 보냅니다.
        val call = gameRoomApi.makeGameRoom(makeGameRoom.name!!, makeGameRoom.category!!, makeGameRoom.adult!!, malNum)
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
                        Log.d("POST 요청 성공", "response.body(): ${response.body()}")
                    }
                }
            }

            override fun onFailure(call: Call<GameRoomResponse>, t: Throwable) {
                println("POST 요청이 실패함: ${t.localizedMessage}")
            }
        })

        //비동기 처리
        Handler(Looper.getMainLooper()).postDelayed({
            if (GameRoomId == "") {
                Toast.makeText(context, "게임방 생성에 실패했습니다.", Toast.LENGTH_SHORT).show()
                return@postDelayed
            }
            else {
                getGameRoomIdDialog.getRoomId(GameRoomId, name_1P_OG.text.toString(), intent)
                dismiss()
            }
        }, 500)
    }

    private fun setBtnMalNumPlus(){
        binding.btnMalPlus.setOnClickListener {
            var malNum: Int = binding.tvMalNum.text.toString().toInt()

            malNum++
            if(4 < malNum){
                malNum = 1
            }

            binding.tvMalNum.text = malNum.toString()
        }
    }

    private fun setBtnMalNumMinus(){
        binding.btnMalMinus.setOnClickListener {
            var malNum: Int = binding.tvMalNum.text.toString().toInt()

            malNum--
            if(malNum < 1){
                malNum = 4
            }

            binding.tvMalNum.text = malNum.toString()
        }
    }
}