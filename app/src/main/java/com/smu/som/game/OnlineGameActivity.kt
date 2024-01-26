package com.smu.som.game

import android.content.ContentValues.TAG
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.media.SoundPool
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.smu.som.GameResult
import com.smu.som.GameResultActivity
import com.smu.som.version1.GameSettingActivity
import com.smu.som.version1.MainActivity
import com.smu.som.MasterApplication
import com.smu.som.Question
import com.smu.som.R
import com.smu.som.databinding.ActivityOnlineGameBinding
import kotlinx.android.synthetic.main.activity_game.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Math.abs


// 윷놀이 게임 Activity
class OnlineGameActivity : AppCompatActivity() {

    // 뷰 바인딩입니다. findViewById()를 하지 않고도 뷰에 쉽게 접근하게 해줄 수 있는 라이브러리입니다.
    private lateinit var binding: ActivityOnlineGameBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnlineGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 채팅방 입장 클릭 이벤트 리스너
        binding.btnChat.setOnClickListener {
            moveChatDialog(intent.getBundleExtra("myBundle"))
        }

//        mainGameCode()
    }

    // 채팅방 입장 버튼 클릭 이벤트
    private fun moveChatDialog(bundle: Bundle?){
        val intent = Intent(this, GameChatActivity::class.java)
        intent.putExtra("myBundle", bundle)

        startActivity(intent)
    }

    // onCreate()에 코드가 몰아져있어서 따로 분리함....-가나
    fun mainGameCode(){
        // 게임 설정 불러오기 (game_sp)
        val sp = this.getSharedPreferences("game_sp", Context.MODE_PRIVATE)
        val isAdult = sp.getString("isAdult", "n")                 // 성인 질문 유무
        val email = sp.getString("email", null)                    // 카카오톡 계정 이메일
        val sound = sp.getBoolean("sound", false)                  // 게임 소리 유무
        var char1 = sp.getInt("character1", 0)                     // 게임 캐릭터 설정 (1P)
        var char2 = sp.getInt("character2", 11)                    // 게임 캐릭터 설정 (2P)
        var rand1 = char1 + 1
        var rand2 = char2 + 1

        // 게임 설정 불러오기 (intent)
        val category = intent.getStringExtra("category")     // API 요청 시 필요한 카테고리 (영어)
        var kcategory = intent.getStringExtra("kcategory")   // 사용자에게 보여질 카테고리 (한글)
        val name_1p = intent.getStringExtra("name1")         // 사용자 이름 (1P)
        val name_2p = intent.getStringExtra("name2")         // 사용자 이름 (2P)

        // 게임 결과 저장 리스트
        val yutName = arrayOf("빽도", "도", "개", "걸", "윷", "모")    // 윷 결과 리스트
        val SIZE = 30                                              // 윷판 크기
        var arr = IntArray(SIZE, { 0 } )                           // 윷판 리스트 (각 자리의 말 수 저장)
        var yuts = IntArray(6, { 0 } )                        // 윷 결과 저장 리스트
        var players: ArrayList<TextView> = ArrayList()             // 윷판의 TextView 리스트 (화면)
        var used = arrayOf<Int>()                                  // 답변한 질문 리스트
        var pass = arrayOf<Int>()                                  // 패스한 질문 리스트

        // 게임 기록
        var player1 = 4                                            // 남은 말의 수 (1P)
        var player2 = 4                                            // 남은 말의 수 (2P)
        var score1 = 0                                             // 들어온 말의 수 (1P)
        var score2 = 0                                             // 들어온 말의 수 (2P)
        var catch1 = false                                         // 상대방 말을 잡았는지 체크 (1P)
        var catch2 = false                                         // 상대방 말을 잡았는지 체크 (2P)
        var turn = true                                            // 게임 차례 (1P - true, 2P - false)

        // 팝업과 소리 설정
        var builder = AlertDialog.Builder(this)            // 팝업 설정
        val soundPool = SoundPool.Builder().build()                // 게임 소리 실행 설정
        val gamesound = IntArray(8, { 0 } )                   // 게임 소리 리스트

        // 윷판의 모든 Textview 저장
        for (i in 0..SIZE)
            players.add(findViewById(getResources().getIdentifier("board" + i, "id", packageName)))

        // 게임 소리 설정
        if (sound)
            for (i in 0..7)
                gamesound[i] = soundPool.load(this, resources.getIdentifier("sound_$i", "raw", packageName), 1)

        // 게임 화면에 사용자 이름, 카테고리, 화면 설정
        name1.text = name_1p
        name2.text = name_2p
        Category.text = kcategory
        drawGame(arr, player1, player2, score1, score2, turn, rand1, rand2, category, kcategory, name_1p, name_2p, email, used, pass, yuts)

        // 게임 설명 버튼 클릭 리스너 설정
        game_rule.setOnClickListener { showPopup() }

        // 일시 정지 버튼 클릭 리스너 설정
        stop.setOnClickListener {
            val categoryArray = arrayOf("홈으로", "관계 선택으로", "다시하기")       // 일시 정지 선택지
            val builder = AlertDialog.Builder(this)
            builder.setTitle("일시정지").setItems(categoryArray, DialogInterface.OnClickListener { dialog, which ->
                if (which == 0) {           // 홈 화면으로
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else if (which == 1) {    // 관계 선택 화면으로
                    val intent = Intent(this, GameSettingActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {                    // 다시하기
                    val intent = Intent(this, OnlineGameActivity::class.java)
                    intent.putExtra("category", category)
                    intent.putExtra("kcategory", kcategory)
                    intent.putExtra("name1", name_1p)
                    intent.putExtra("name2", name_2p)
                    startActivity(intent)
                    finish()
                }
            }).setNegativeButton("취소", null).show()
        }

        // 윷 버튼 클릭 가능 표시
        yut.setBackgroundResource(R.drawable.pick)

        // 윷 버튼 클릭 리스너 설정
        yut.setOnClickListener {
            yut.setBackgroundResource(R.drawable.nopick)                                                    // 윷 버튼 클릭 가능 표시 해제
            yut.isClickable = false                                                                         // 윷 버튼 클릭이 안 되도록 설정
            soundPool.play(gamesound[6], 1.0f, 1.0f, 0, 0, 1.0f)     // 윷 던지는 소리 재생

            var num = playGame(soundPool, gamesound, yuts.sum())                                            // 윷 던지기

            // 윷이나 모일 경우 (한 번 더 윷 던지기)
            if (num == 4 || num == 5) {
                yut.isClickable = true
                builder.setTitle("한 번 더!").setPositiveButton("확인", null).show()
                yut.setBackgroundResource(R.drawable.pick)
            }

            // 윷판에 말이 없을 때 빽도가 나온 경우 (다시 던지기)
            if (num == 0 && checkGo(arr, turn) && yuts.sum() == 0) {
                yut.isClickable = true
                start.isClickable = false
                builder.setTitle("한 번 더 던지세요!").setPositiveButton("확인", null).show()
                yut.setBackgroundResource(R.drawable.pick)
                start.setBackgroundResource(R.drawable.nopick)
            } else {
                yuts[num] += 1                  // 윷 결과 저장
                if (num != 4 && num!= 5) {      // 윷이나 모가 아닌 경우 (질문 받기)
                    var builder = AlertDialog.Builder(this)
                    var ecategory = category
                    if (num == 0 || num == 3)     // 윷이 빽도나 걸인 경우 (공통 카테고리로 변경)
                        ecategory = "COMMON"

                    // API 로 질문을 받는 함수
                    (application as MasterApplication).service.getQuestion(
                        ecategory!!, isAdult!!
                    ).enqueue(object : Callback<ArrayList<Question>> {
                        // 성공
                        override fun onResponse(call: Call<ArrayList<Question>>, response: Response<ArrayList<Question>>) {
                            if (response.isSuccessful) {
                                val question = response.body()
                                val questionId = question?.get(0)!!.id

                                // 팝업으로 질문 보여주기
                                builder.setTitle("질문").setMessage(question?.get(0)?.question.toString())
                                    .setPositiveButton("답변", DialogInterface.OnClickListener { dialog, id ->
                                        used = used.plus(questionId)            // 답변한 질문 리스트에 추가
                                    })
                                    .setNegativeButton("질문변경", DialogInterface.OnClickListener { dialog, id ->
                                        builder.setMessage(question?.get(1)?.question.toString())
                                            .setPositiveButton("답변", DialogInterface.OnClickListener { dialog, id ->
                                                pass = pass.plus(questionId)        // 패스한 질문 리스트에 추가
                                                turn = !turn                        // 턴 변경
                                                yuts = IntArray(6, { 0 } )     // 윷 내역 초기화
                                                yut.setBackgroundResource(R.drawable.pick)
                                                start.setBackgroundResource(R.drawable.nopick)
                                                drawGame(arr, player1, player2, score1, score2, turn, rand1, rand2, category, kcategory, name_1p, name_2p, email, used, pass, yuts)
                                                start.setOnClickListener(null)
                                                for ((index,item) in arr.withIndex())
                                                    if (item!=0 && index!=0)
                                                        players[index]?.setOnClickListener(null)
                                                yut.isClickable = true
                                            })
                                            .setNegativeButton("", null).show()
                                    })

                                // 상대방 말을 잡은 경우 (추가질문권, 패스)
                                if ((turn && catch1) || (!turn && catch2)) {
                                    builder.setPositiveButton("추가질문권", DialogInterface.OnClickListener { dialog, id ->
                                        used = used.plus(questionId)
                                    }).setNegativeButton("패스", DialogInterface.OnClickListener { dialog, id ->
                                        pass = pass.plus(questionId)
                                    })
                                    catch1 = false
                                    catch2 = false
                                }
                                builder.setCancelable(false).show()
                            } else {
                                Log.e(TAG, "잘못된 카테고리 입니다.")
                            }
                        }

                        // 실패
                        override fun onFailure(call: Call<ArrayList<Question>>, t: Throwable) {
                            Log.e(TAG, "서버 오류")
                        }
                    })
                }
            }
            showResult(turn, yuts)  // 윷 결과 리스트 화면에 보이기

            // 윷이나 모가 아니며 윷 결과 리스트의 값이 1개일 경우
            if (yuts.sum() == 1 && num != 4 && num != 5) {
                for ((index,item) in arr.withIndex()) {     // 말판의 모든 칸
                    if (item != 0 && index != 0) {          // 해당 칸에 말이 있으며 0번째 칸이 아닌 경우
                        if (turn == item > 0) {             // 현재 턴 사용자의 말
                            var pick: LinearLayout = findViewById(getResources().getIdentifier("pick" + index, "id", packageName))
                            pick.setBackgroundResource(R.drawable.pick)     // 선택 가능 표시

                            // 사용자 말에 클릭 리스너 설정
                            players[index].setOnClickListener {
                                yuts[num] -= 1                  // 윷 결과 리스트에서 윷 사용
                                if (num == 0)                   // 빽도인 경우
                                    num = -1
                                var idx = getIndex(index, num)  // 윷판 위치를 설정

                                if (turn) {                             // 1P 턴
                                    if (arr[idx] < 0 && idx != 0) {     // 상대방 말을 잡은 경우
                                        player2 -= arr[idx]             // 2P 말의 수 계산
                                        arr[idx] = item                 // 1P 말 이동
                                        catch1 = true
                                        showCatch(soundPool, gamesound) // 결과 표시
                                    }
                                    else {
                                        if (idx == 0)                    // 말이 들어간 경우
                                            score1 += item               // 들어간 말 더하기
                                        else
                                            arr[idx] += item             // 그 자리에 말 더하기
                                    }
                                }
                                else {                                  // 2P 턴
                                    if (arr[idx] > 0 && idx != 0) {     // 상대방 말을 잡을 경우
                                        player1 += arr[idx]             // 1P 말의 수 계산
                                        arr[idx] = item                 // 2P 말 이동
                                        catch2 = true
                                        showCatch(soundPool, gamesound) // 결과 표시
                                    }
                                    else {
                                        if (idx == 0)                   // 말이 들어간 경우
                                            score2 -= item              // 들어간 말 더하기
                                        else
                                            arr[idx] += item            // 그 자리에 말 더하기
                                    }
                                }

                                // 말을 잡지 못한 경우 (턴 변경과 윷 리스트 초기화)
                                if (!catch1 && !catch2) {
                                    turn = !turn
                                    yuts = IntArray(6, { 0 } )
                                }
                                arr[index] = 0
                                drawGame(arr, player1, player2, score1, score2, turn, rand1, rand2, category, kcategory, name_1p, name_2p, email, used, pass, yuts)

                                // 윷 추가 및 말 클릭 리스터 초기화
                                start.setBackgroundResource(R.drawable.nopick)
                                start.setOnClickListener(null)
                                for ((index,item) in arr.withIndex())
                                    if (item!=0 && index!=0)
                                        players[index]?.setOnClickListener(null)
                                yut.setBackgroundResource(R.drawable.pick)
                                yut.isClickable = true
                            }
                        }
                    }
                }

                // 빽도가 아니며 남은 말이 있을 경우 (말 추가 버튼 클릭 가능)
                if (num != 0 && checkBoard(turn, player1, player2) != 0) {
                    start.setBackgroundResource(R.drawable.pick)    // 선택 가능 표시

                    // 윷 추가 버튼 클릭 리스너 설정
                    start.setOnClickListener {
                        yuts[num] -= 1                          // 윷 결과 리스트에서 윷 사용
                        if (num == 0)                           // 빽도인 경우
                            num = -1
                        if (turn) {                             // 1P 턴
                            if (arr[num] < 0) {                 // 상대방 말을 잡을 경우
                                player2 -= arr[num]             // 2P 말의 수 계산
                                arr[num] = 1                    // 1P 말 이동
                                catch1 = true
                                showCatch(soundPool, gamesound) // 결과 표시
                            }
                            else
                                arr[num] += 1                   // 1P 말 이동
                            player1 -= 1                        // 1P 말의 수 계산
                        }
                        else {                                  // 2P 턴
                            if (arr[num] > 0) {                 // 상대방 말을 잡을 경우
                                player1 += arr[num]             // 1P 말의 수 계산
                                arr[num] = -1                   // 2P 말 이동
                                catch2 = true
                                showCatch(soundPool, gamesound) // 결과 표시
                            }
                            else
                                arr[num] -= 1                   // 2P 말 이동
                            player2 -= 1                        // 2P 말의 수 계산
                        }

                        // 말을 잡지 못한 경우 (턴 변경과 윷 리스트 초기화)
                        if (!catch1 && !catch2) {
                            turn = !turn
                            yuts = IntArray(6, { 0 } )
                        }
                        drawGame(arr, player1, player2, score1, score2, turn, rand1, rand2, category, kcategory, name_1p, name_2p, email, used, pass, yuts)

                        // 윷 추가 및 말 클릭 리스터 초기화
                        start.setBackgroundResource(R.drawable.nopick)
                        start.setOnClickListener(null)
                        for ((index,item) in arr.withIndex())
                            if (item!=0 && index!=0)
                                players[index]?.setOnClickListener(null)
                        yut.setBackgroundResource(R.drawable.pick)
                        yut.isClickable = true
                    }
                }
            }
            else {  // 윷이나 모이거나 윷 결과 리스트의 값이 2개 이상일 경우
                for ((index,item) in arr.withIndex()) {             // 말판의 모든 칸
                    if (item!=0 && index!=0 && yuts.sum() == 2) {   // 해당 칸에 말이 있으며 0번째 칸이 아니고 윷을 모두 던진 경우
                        if (turn == item > 0) {                     // 현재 턴 사용자의 말
                            var pick: LinearLayout = findViewById(getResources().getIdentifier("pick" + index, "id", packageName))
                            pick.setBackgroundResource(R.drawable.pick)     // 선택 가능 표시

                            // 사용자 말에 클릭 리스너 설정
                            players[index].setOnClickListener {
                                // 윷 선택지 팝업 생성
                                var builder2 = AlertDialog.Builder(this)
                                var size = 0
                                var yutArray = arrayOf("", "", "", "", "", "")
                                var yutss: ArrayList<Int> = ArrayList()
                                for ((index,item) in yuts.withIndex()) {
                                    if (item > 0) {
                                        val name = yutName[index]
                                        yutss.add(index)
                                        if (item == 1)
                                            yutArray[size] = "$name"
                                        else
                                            yutArray[size] = "$name * $item"
                                        size += 1
                                    }
                                }
                                yutArray = yutArray.sliceArray(0..size - 1)

                                // 윷 선택 팝업 생성
                                builder2.setTitle("윷 선택").setItems(yutArray, DialogInterface.OnClickListener { dialog, which ->
                                    num = yutss[which]              // 윷 선택
                                    yuts[num] -= 1                  // 윷 결과 리스트에서 윷 사용
                                    if (num == 0)                   // 빽도인 경우
                                        num = -1
                                    var idx = getIndex(index, num)  // 윷판 위치를 설정

                                    if (turn) {                             // 1P 턴
                                        if (arr[idx] < 0 && idx != 0) {     // 상대방 말을 잡은 경우
                                            player2 -= arr[idx]             // 2P 말의 수 계산
                                            arr[idx] = item                 // 1P 말 이동
                                            catch1 = true
                                            showCatch(soundPool, gamesound) // 결과 표시
                                        }
                                        else {
                                            if (idx == 0)                   // 말이 들어간 경우
                                                score1 += item              // 들어간 말 더하기
                                            else
                                                arr[idx] += item            // 그 자리에 말 더하기
                                        }
                                    }
                                    else {
                                        if (arr[idx] > 0 && idx != 0) {     // 상대방 말을 잡을 경우
                                            player1 += arr[idx]
                                            arr[idx] = item
                                            catch2 = true
                                            showCatch(soundPool, gamesound) // 결과 표시
                                        }
                                        else {
                                            if (idx == 0)                   // 말이 들어간 경우
                                                score2 -= item              // 들어간 말 더하기
                                            else
                                                arr[idx] += item            // 그 자리에 말 더하기
                                        }
                                    }
                                    arr[index] = 0
                                    drawGame(arr, player1, player2, score1, score2, turn, rand1, rand2, category, kcategory, name_1p, name_2p, email, used, pass, yuts)

                                    // 말을 잡은 경우 (다시 윷 던지기, 클릭 리스너 초기화)
                                    if ((turn && catch1) || (!turn && catch2)) {
                                        start.setBackgroundResource(R.drawable.nopick)
                                        start.setOnClickListener(null)
                                        for ((index,item) in arr.withIndex())
                                            if (item!=0 && index!=0)
                                                players[index]?.setOnClickListener(null)
                                        yut.setBackgroundResource(R.drawable.pick)
                                        yut.isClickable = true
                                    } else {
                                        num = findOne(yuts)     // 윷 결과 리스트에서 하나의 윷 꺼내기

                                        for ((index,item) in arr.withIndex()) {     // 말판의 모든 칸
                                            if (item != 0 && index != 0) {          // 해당 칸에 말이 있으며 0번째 칸이 아닌 경우
                                                if (turn == item > 0) {             // 현재 턴 사용자의 말
                                                    var pick: LinearLayout = findViewById(getResources().getIdentifier("pick" + index, "id", packageName))
                                                    pick.setBackgroundResource(R.drawable.pick)     // 선택 가능 표시

                                                    // 사용자 말에 클릭 리스너 설정
                                                    players[index].setOnClickListener {
                                                        yuts[num] -= 1                  // 윷 결과 리스트에서 윷 사용
                                                        if (num == 0)                   // 빽도인 경우
                                                            num = -1
                                                        var idx = getIndex(index, num)  // 윷판 위치를 설정

                                                        if (turn) {                             // 1P 턴
                                                            if (arr[idx] < 0 && idx != 0) {     // 상대방 말을 잡은 경우
                                                                player2 -= arr[idx]             // 2P 말의 수 계산
                                                                arr[idx] = item                 // 1P 말 이동
                                                                catch1 = true
                                                                showCatch(soundPool, gamesound) // 결과 표시
                                                            }
                                                            else {
                                                                if (idx == 0)                    // 말이 들어간 경우
                                                                    score1 += item               // 들어간 말 더하기
                                                                else
                                                                    arr[idx] += item             // 그 자리에 말 더하기
                                                            }
                                                        }
                                                        else {                                  // 2P 턴
                                                            if (arr[idx] > 0 && idx != 0) {     // 상대방 말을 잡을 경우
                                                                player1 += arr[idx]             // 1P 말의 수 계산
                                                                arr[idx] = item                 // 2P 말 이동
                                                                catch2 = true
                                                                showCatch(soundPool, gamesound) // 결과 표시
                                                            }
                                                            else {
                                                                if (idx == 0)                   // 말이 들어간 경우
                                                                    score2 -= item              // 들어간 말 더하기
                                                                else
                                                                    arr[idx] += item            // 그 자리에 말 더하기
                                                            }
                                                        }

                                                        // 말을 잡지 못한 경우 (턴 변경과 윷 리스트 초기화)
                                                        if (!catch1 && !catch2) {
                                                            turn = !turn
                                                            yuts = IntArray(6, { 0 } )
                                                        }
                                                        arr[index] = 0
                                                        drawGame(arr, player1, player2, score1, score2, turn, rand1, rand2, category, kcategory, name_1p, name_2p, email, used, pass, yuts)

                                                        // 윷 추가 및 말 클릭 리스터 초기화
                                                        start.setBackgroundResource(R.drawable.nopick)
                                                        start.setOnClickListener(null)
                                                        for ((index,item) in arr.withIndex())
                                                            if (item!=0 && index!=0)
                                                                players[index]?.setOnClickListener(null)
                                                        yut.setBackgroundResource(R.drawable.pick)
                                                        yut.isClickable = true
                                                    }
                                                }
                                            }
                                        }

                                        // 빽도가 아니며 남은 말이 있을 경우 (말 추가 버튼 클릭 가능)
                                        if (num != 0 && checkBoard(turn, player1, player2) != 0) {
                                            start.setBackgroundResource(R.drawable.pick)    // 선택 가능 표시

                                            // 윷 추가 버튼 클릭 리스너 설정
                                            start.setOnClickListener {
                                                yuts[num] -= 1                          // 윷 결과 리스트에서 윷 사용
                                                if (num == 0)                           // 빽도인 경우
                                                    num = -1
                                                if (turn) {                             // 1P 턴
                                                    if (arr[num] < 0) {                 // 상대방 말을 잡을 경우
                                                        player2 -= arr[num]             // 2P 말의 수 계산
                                                        arr[num] = 1                    // 1P 말 이동
                                                        catch1 = true
                                                        showCatch(soundPool, gamesound) // 결과 표시
                                                    }
                                                    else
                                                        arr[num] += 1                   // 1P 말 이동
                                                    player1 -= 1                        // 1P 말의 수 계산
                                                }
                                                else {                                  // 2P 턴
                                                    if (arr[num] > 0) {                 // 상대방 말을 잡을 경우
                                                        player1 += arr[num]             // 1P 말의 수 계산
                                                        arr[num] = -1                   // 2P 말 이동
                                                        catch2 = true
                                                        showCatch(soundPool, gamesound) // 결과 표시
                                                    }
                                                    else
                                                        arr[num] -= 1                   // 2P 말 이동
                                                    player2 -= 1                        // 2P 말의 수 계산
                                                }

                                                // 말을 잡지 못한 경우 (턴 변경과 윷 리스트 초기화)
                                                if (!catch1 && !catch2) {
                                                    turn = !turn
                                                    yuts = IntArray(6, { 0 } )
                                                }
                                                drawGame(arr, player1, player2, score1, score2, turn, rand1, rand2, category, kcategory, name_1p, name_2p, email, used, pass, yuts)

                                                // 윷 추가 및 말 클릭 리스터 초기화
                                                start.setBackgroundResource(R.drawable.nopick)
                                                start.setOnClickListener(null)
                                                for ((index,item) in arr.withIndex())
                                                    if (item!=0 && index!=0)
                                                        players[index]?.setOnClickListener(null)
                                                yut.setBackgroundResource(R.drawable.pick)
                                                yut.isClickable = true
                                            }
                                        }
                                    }
                                }).setNegativeButton("취소", null).show()
                            }

                        }
                    }
                }

                // 윷 추가 버튼 클릭 리스너 설정
                start.setOnClickListener {
                    if (checkBoard(turn, player1, player2) != 0) {
                        var builder2 = AlertDialog.Builder(this)
                        var size = 0
                        var yutArray = arrayOf("", "", "", "", "", "")      // 윷 선택지 팝업 생성
                        var yutss: ArrayList<Int> = ArrayList()
                        for ((index,item) in yuts.withIndex()) {
                            if (item > 0 && index != 0) {
                                val name = yutName[index]
                                yutss.add(index)
                                if (item == 1)
                                    yutArray[size] = "$name"
                                else
                                    yutArray[size] = "$name * $item"
                                size += 1
                            }
                        }
                        yutArray = yutArray.sliceArray(0..size - 1)

                        // 윷 선택 팝업 생성
                        builder2.setTitle("윷 선택").setItems(yutArray, DialogInterface.OnClickListener { dialog, which ->
                            num = yutss[which]                          // 윷 선택
                            yuts[num] -= 1                              // 윷 결과 리스트에서 윷 사용
                            if (turn) {                                 // 1P 턴
                                if (arr[num] < 0) {                     // 상대방 말을 잡을 경우
                                    player2 -= arr[num]                 // 2P 말의 수 계산
                                    arr[num] = 1                        // 1P 말 이동
                                    catch1 = true
                                    showCatch(soundPool, gamesound)     // 결과 표시
                                }
                                else
                                    arr[num] += 1                       // 1P 말 이동
                                player1 -= 1                            // 1P 말의 수 계산
                            }
                            else {                                      // 2P 턴
                                if (arr[num] > 0) {                     // 상대방 말을 잡을 경우
                                    player1 += arr[num]                 // 1P 말의 수 계산
                                    arr[num] = -1                       // 2P 말 이동
                                    catch2 = true
                                    showCatch(soundPool, gamesound)     // 결과 표시
                                }
                                else
                                    arr[num] -= 1                       // 2P 말 이동
                                player2 -= 1                            // 2P 말의 수 계산
                            }
                            drawGame(arr, player1, player2, score1, score2, turn, rand1, rand2, category, kcategory, name_1p, name_2p, email, used, pass, yuts)

                            // 말을 잡은 경우 (다시 윷 던지기, 클릭 리스너 초기화)
                            if ((turn && catch1) || (!turn && catch2)) {
                                start.setBackgroundResource(R.drawable.nopick)
                                start.setOnClickListener(null)
                                for ((index,item) in arr.withIndex())
                                    if (item!=0 && index!=0)
                                        players[index]?.setOnClickListener(null)
                                yut.setBackgroundResource(R.drawable.pick)
                                yut.isClickable = true
                            } else {
                                num = findOne(yuts)     // 윷 결과 리스트에서 하나의 윷 꺼내기

                                for ((index,item) in arr.withIndex()) {     // 말판의 모든 칸
                                    if (item != 0 && index != 0) {          // 해당 칸에 말이 있으며 0번째 칸이 아닌 경우
                                        if (turn == item > 0) {             // 현재 턴 사용자의 말
                                            var pick: LinearLayout = findViewById(getResources().getIdentifier("pick" + index, "id", packageName))
                                            pick.setBackgroundResource(R.drawable.pick)     // 선택 가능 표시

                                            // 사용자 말에 클릭 리스너 설정
                                            players[index].setOnClickListener {
                                                yuts[num] -= 1                  // 윷 결과 리스트에서 윷 사용
                                                if (num == 0)                   // 빽도인 경우
                                                    num = -1
                                                var idx = getIndex(index, num)  // 윷판 위치를 설정

                                                if (turn) {                             // 1P 턴
                                                    if (arr[idx] < 0 && idx != 0) {     // 상대방 말을 잡은 경우
                                                        player2 -= arr[idx]             // 2P 말의 수 계산
                                                        arr[idx] = item                 // 1P 말 이동
                                                        catch1 = true
                                                        showCatch(soundPool, gamesound) // 결과 표시
                                                    }
                                                    else {
                                                        if (idx == 0)                    // 말이 들어간 경우
                                                            score1 += item               // 들어간 말 더하기
                                                        else
                                                            arr[idx] += item             // 그 자리에 말 더하기
                                                    }
                                                }
                                                else {                                  // 2P 턴
                                                    if (arr[idx] > 0 && idx != 0) {     // 상대방 말을 잡을 경우
                                                        player1 += arr[idx]             // 1P 말의 수 계산
                                                        arr[idx] = item                 // 2P 말 이동
                                                        catch2 = true
                                                        showCatch(soundPool, gamesound) // 결과 표시
                                                    }
                                                    else {
                                                        if (idx == 0)                   // 말이 들어간 경우
                                                            score2 -= item              // 들어간 말 더하기
                                                        else
                                                            arr[idx] += item            // 그 자리에 말 더하기
                                                    }
                                                }

                                                // 말을 잡지 못한 경우 (턴 변경과 윷 리스트 초기화)
                                                if (!catch1 && !catch2) {
                                                    turn = !turn
                                                    yuts = IntArray(6, { 0 } )
                                                }
                                                arr[index] = 0
                                                drawGame(arr, player1, player2, score1, score2, turn, rand1, rand2, category, kcategory, name_1p, name_2p, email, used, pass, yuts)

                                                // 윷 추가 및 말 클릭 리스터 초기화
                                                start.setBackgroundResource(R.drawable.nopick)
                                                start.setOnClickListener(null)
                                                for ((index,item) in arr.withIndex())
                                                    if (item!=0 && index!=0)
                                                        players[index]?.setOnClickListener(null)
                                                yut.setBackgroundResource(R.drawable.pick)
                                                yut.isClickable = true
                                            }
                                        }
                                    }
                                }

                                // 빽도가 아니며 남은 말이 있을 경우 (말 추가 버튼 클릭 가능)
                                if (num != 0 && checkBoard(turn, player1, player2) != 0) {
                                    start.setBackgroundResource(R.drawable.pick)    // 선택 가능 표시

                                    // 윷 추가 버튼 클릭 리스너 설정
                                    start.setOnClickListener {
                                        yuts[num] -= 1                          // 윷 결과 리스트에서 윷 사용
                                        if (num == 0)                           // 빽도인 경우
                                            num = -1
                                        if (turn) {                             // 1P 턴
                                            if (arr[num] < 0) {                 // 상대방 말을 잡을 경우
                                                player2 -= arr[num]             // 2P 말의 수 계산
                                                arr[num] = 1                    // 1P 말 이동
                                                catch1 = true
                                                showCatch(soundPool, gamesound) // 결과 표시
                                            }
                                            else
                                                arr[num] += 1                   // 1P 말 이동
                                            player1 -= 1                        // 1P 말의 수 계산
                                        }
                                        else {                                  // 2P 턴
                                            if (arr[num] > 0) {                 // 상대방 말을 잡을 경우
                                                player1 += arr[num]             // 1P 말의 수 계산
                                                arr[num] = -1                   // 2P 말 이동
                                                catch2 = true
                                                showCatch(soundPool, gamesound) // 결과 표시
                                            }
                                            else
                                                arr[num] -= 1                   // 2P 말 이동
                                            player2 -= 1                        // 2P 말의 수 계산
                                        }

                                        // 말을 잡지 못한 경우 (턴 변경과 윷 리스트 초기화)
                                        if (!catch1 && !catch2) {
                                            turn = !turn
                                            yuts = IntArray(6, { 0 } )
                                        }
                                        drawGame(arr, player1, player2, score1, score2, turn, rand1, rand2, category, kcategory, name_1p, name_2p, email, used, pass, yuts)

                                        // 윷 추가 및 말 클릭 리스터 초기화
                                        start.setBackgroundResource(R.drawable.nopick)
                                        start.setOnClickListener(null)
                                        for ((index,item) in arr.withIndex())
                                            if (item!=0 && index!=0)
                                                players[index]?.setOnClickListener(null)
                                        yut.setBackgroundResource(R.drawable.pick)
                                        yut.isClickable = true
                                    }
                                }
                            }
                        }).setNegativeButton("취소", null).show()
                    }
                }

                start.isClickable = false   // 말 추가 버튼이 클릭 안 되도록 설정
                // 윷을 모두 던지고 남은 말이 있을 경우 (말 추가 버튼 클릭 가능)
                if (yuts.sum() == 2 && checkBoard(turn, player1, player2) != 0) {
                    start.setBackgroundResource(R.drawable.pick)
                    start.isClickable = true
                }
            }
        }
    }

    // 윷판 위치를 설정하는 함수
    fun getIndex(index: Int, num: Int): Int {
        var idx = index

        if (num == -1) {
            if (idx == 1)
                return 20
            else if (idx == 21)
                return 5
            else if (idx == 26)
                return 10
            else if (idx == 28)
                return 23
            else
                return index + num
        } else {
            if (idx == 5)
                idx = 20
            else if (idx == 10) {
                if (num in 1..2)
                    idx = 25
                else if (num == 3)
                    return 23
                else
                    idx = 24
            }
            else if (idx == 23)
                idx = 27
            else if (idx in 16..20) {
                if (idx + num > 20)
                    return 0
            }
            else if (idx in 21..25) {
                if (idx + num > 25)
                    idx -= 11
            }
            else if (idx in 26..27) {
                if (idx + num == 28)
                    return 23
                if (idx + num > 28)
                    idx--
            }
        }
        if (idx+num == 30)
            return 20
        else if (idx+num > 30)
            return 0
        else
            return idx + num
    }

    // 윷을 던지는 함수
    fun playGame(soundPool: SoundPool, gamesound: IntArray, sum: Int): Int {
        val yuts = arrayOf("빽도", "도", "개", "걸", "윷", "모")
        var num = percentage(sum)       // 윷 결과
        result.setBackgroundResource(resources.getIdentifier("result_$num", "drawable", packageName))

        // 윷 결과 애니메이션
        Handler(Looper.getMainLooper()).postDelayed({
            soundPool.play(gamesound[num], 1.0f, 1.0f, 0, 0, 1.0f)
            result_text.setText(yuts[num])
            result.setBackgroundResource(resources.getIdentifier("yut_$num", "drawable", packageName))
        }, 1000)
        return num
    }

    // 윷 확률 설정 함수
    fun percentage(sum: Int): Int {
        val range = (1..16)
        var per = arrayOf(1, 3, 6, 4, 1, 1)     // 윷 확률
        if (sum > 0)
            per = arrayOf(1, 4, 6, 5)           // 확률 재설정
        var num = range.random()

        for ((index,item) in per.withIndex()) {
            if (num <= item)
                return index
            num -= item
        }
        return -1
    }

    // 윷 결과 리스트에 저장된 하나의 윷을 찾는 함수
    fun findOne(array: IntArray): Int {
        for ((index, item) in array.withIndex())
            if (item == 1)
                return index
        return -1
    }

    // 윷놀이 결과를 화면에 보이는 함수
    fun drawGame(array: IntArray, player01: Int, player02: Int, score01: Int, score02: Int, turn: Boolean, rand1: Int, rand2: Int, category: String?, kcategory: String?, name1: String?, name2: String?, email: String?, used: Array<Int>, pass: Array<Int>, result: IntArray) {
        // 말판 그리기
        for ((index,item) in array.withIndex()) {
            if (index!=0) {
                // 말판의 TextView 찾기
                var player: TextView = findViewById(getResources().getIdentifier("board" + index, "id", packageName))
                var pick: LinearLayout = findViewById(getResources().getIdentifier("pick" + index, "id", packageName))
                pick.setBackgroundResource(R.drawable.nopick)

                if (item!=0) {          // 말이 있는 경우
                    if (item > 0)       // 1P 말
                        player.setBackgroundResource(resources.getIdentifier(String.format("player_%d_%d", item, rand1), "drawable", packageName))
                    else                // 2P 말
                        player.setBackgroundResource(resources.getIdentifier(String.format("player_%d_%d", abs(item), rand2),"drawable", packageName))
                }
                else                    // 말이 없는 경우
                    player.setBackgroundResource(R.drawable.board)
            }
        }

        // 남은 말 표시
        for (num in 1..4) {
            var player1: TextView = findViewById(getResources().getIdentifier("player1_" + num, "id", packageName))
            var player2: TextView = findViewById(getResources().getIdentifier("player2_" + num, "id", packageName))
            if (num <= player01)
                player1.setBackgroundResource(resources.getIdentifier(String.format("player_1_%d", rand1), "drawable", packageName))
            else
                player1.setBackgroundResource(resources.getIdentifier(String.format("noplayer_%d", rand1), "drawable", packageName))

            if (num <= player02)
                player2.setBackgroundResource(resources.getIdentifier(String.format("player_1_%d", rand2), "drawable", packageName))
            else
                player2.setBackgroundResource(resources.getIdentifier(String.format("noplayer_%d", rand2), "drawable", packageName))
        }
        showResult(turn, result)            // 윷 결과 리스트 표시
        score1.text = score01.toString()    // 들어온 말의 개수 표시 (1P)
        score2.text = score02.toString()    // 들어온 말의 개수 표시 (2P)
        checkWin(score01, score02, category, kcategory, name1, name2, email, used, pass)    // 게임 종료 유무 판별
        showTurn(turn, name1, name2)        // 순서 표시
    }

    // 남은 말의 개수 확인
    fun checkBoard(turn: Boolean, player01: Int, player02: Int): Int {
        if (turn)
            return player01
        return player02
    }

    // 게임 종료 판별 함수
    fun checkWin(score01: Int, score02: Int, category: String?, kcategory: String?, name1: String?, name2: String?, email: String?, used: Array<Int>, pass: Array<Int>) {
        // 두 사용자 중 들어온 말의 개수가 4인 경우
        if (score01 == 4 || score02 == 4) {
            var result = ""
            if (score01 == 4) {
                result = "$name1 승리!"
            } else {
                result = "$name2 승리!"
            }

            email?.let { category?.let { it1 -> saveResult(it, it1, used, pass) } }     // 게임 결과 저장 (API)

            // 게임 결과 화면으로 이동
            val intent = Intent(this, GameResultActivity::class.java)     // 게임 결과 화면 intent
            intent.putExtra("result", result)
            intent.putExtra("kcategory", kcategory)
            intent.putExtra("category", category)
            intent.putExtra("name1", name1)
            intent.putExtra("name2", name2)
            Handler(Looper.getMainLooper()).postDelayed({
                startActivity(intent)
                finish()
            }, 1000)
        }
    }

    // 말판에 사용자의 말이 있는 지 확인하는 함수
    fun checkGo(array: IntArray, turn: Boolean): Boolean {
        for ((index, item) in array.withIndex())
            if (index != 0 && item != 0)
                if (turn == item > 0)
                    return false
        return true
    }

    // 사용자 턴을 보여주는 함수
    fun showTurn(turn: Boolean, name1: String?, name2: String?) {
        if (turn) {     // 1P 턴
            player1.setBackgroundResource(R.drawable.check_box)
            player2.setBackgroundResource(R.drawable.white_box)
            result_text.setText("$name1 차례")
        } else {        // 2P 턴
            player1.setBackgroundResource(R.drawable.white_box)
            player2.setBackgroundResource(R.drawable.check_box)
            result_text.setText("$name2 차례")
        }
    }

    // 윷 결과 리스트를 화면에 보여주는 함수
    fun showResult(turn: Boolean, array: IntArray) {
        val t = !turn
        for ((index, item) in array.withIndex()) {
            var result1: TextView = findViewById(getResources().getIdentifier(turn.toString()+index.toString(), "id", packageName))
            var result2: TextView = findViewById(getResources().getIdentifier(t.toString()+index.toString(), "id", packageName))
            if (item == 0)          // 윷 0개
                result1.setBackgroundResource(R.drawable.nopick)
            else if (item == 1)     // 윷 1개
                result1.setBackgroundResource(resources.getIdentifier("result$index", "drawable", packageName))
            else                    // 윷 2개 이상
                result1.setBackgroundResource(resources.getIdentifier("result$index"+"_2", "drawable", packageName))
            result2.setBackgroundResource(R.drawable.nopick)
        }
    }

    // 게임 설명을 보여주는 함수
    fun showPopup() {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view1 = inflater.inflate(R.layout.activity_online_gamerule1, null)
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
        val view2 = inflater.inflate(R.layout.activity_online_gamerule2, null)
        val alertDialog2 = AlertDialog.Builder(this).setTitle("게임 방법")
            .setPositiveButton("확인", null)
            .setNegativeButton("다음") { dialog, which ->
                showThirdPage(inflater) // 설명 3 페이지를 보여주는 함수 호출
            }
        alertDialog2.setView(view2)
        alertDialog2.setCancelable(false).show()
    }

    fun showThirdPage(inflater: LayoutInflater) {
        val view3 = inflater.inflate(R.layout.activity_online_gamerule3, null)
        val alertDialog3 = AlertDialog.Builder(this).setTitle("게임 방법")
            .setPositiveButton("확인", null)
            .setNegativeButton("이전") { dialog, which ->
                showFirstPage(inflater) // 처음 설명 페이지를 보여주는 함수 호출
            }
        alertDialog3.setView(view3)
        alertDialog3.setCancelable(false).show()
    }

    fun showFirstPage(inflater: LayoutInflater) {
        val view1 = inflater.inflate(R.layout.activity_online_gamerule1, null)
        val alertDialog1 = AlertDialog.Builder(this)
            .setTitle("게임 방법")
            .setPositiveButton("다음") { dialog, which ->
                showSecondPage(inflater) // 설명 2페이지를 보여주는 함수 호출
            }
            .setNegativeButton("취소", null)
        alertDialog1.setView(view1)
        alertDialog1.setCancelable(false).show()
    }

    // 말을 잡았을 때 화면에 팝업을 띄우는 함수
    fun showCatch(soundPool: SoundPool, gamesound: IntArray) {
        soundPool.play(gamesound[7], 1.0f, 1.0f, 0, 0, 1.0f)    // 말을 잡았을 때 소리
        val alertDialog = AlertDialog.Builder(this)     // 말을 잡았을 때 팝업
            .setTitle("말을 잡았습니다!")
            .setIcon(R.drawable.ccatch)
            .setPositiveButton("확인") { dialog, which -> }
            .create()
        alertDialog.show()
    }

    // 게임 결과를 저장하는 함수 (API)
    fun saveResult(email: String, category: String, used: Array<Int>, pass: Array<Int>) {
        val result = GameResult(used, pass)
        var ecategory = category
        if (category == "PARENT")
            ecategory = "FAMILY"
        // 게임 결과 리스트 저장
        (application as MasterApplication).service.saveResult(
            email, ecategory, result
        ).enqueue(object : Callback<Boolean> {
            // 성공
            override fun onResponse(call: Call<Boolean>, response: Response<Boolean>) {
                if (response.isSuccessful) {
                    Log.e(TAG, "저장 완료")
                } else {
                    Log.e(TAG, "저장 오류")
                }
            }

            // 실패
            override fun onFailure(call: Call<Boolean>, t: Throwable) {
                Log.e(TAG, "서버 오류")
            }
        })
    }
}
