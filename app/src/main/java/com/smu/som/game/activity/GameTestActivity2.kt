package com.smu.som.game.activity

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.media.SoundPool
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import com.beust.klaxon.Klaxon
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.gmail.bishoybasily.stomp.lib.Event
import com.gmail.bishoybasily.stomp.lib.StompClient
import com.smu.som.MasterApplication
import com.smu.som.Question
import com.smu.som.R
import com.smu.som.chat.model.response.Chat
import com.smu.som.databinding.ActivityOnlineGame2Binding
import com.smu.som.game.dialog.AnsweringDialog
import com.smu.som.game.GameChatActivity
import com.smu.som.game.GameConstant
import com.smu.som.game.dialog.GameEndDialog
import com.smu.som.game.dialog.GameRuleDialog
import com.smu.som.game.dialog.GetAnswerResultDialog
import com.smu.som.game.dialog.GetQuestionDialog
import com.smu.som.game.response.Game
import com.smu.som.game.response.QnAResponse
import com.smu.som.game.response.ScoreInfo
import com.smu.som.game.response.GameMalResponse
import com.smu.som.game.service.GameMalStompService
import com.smu.som.game.service.MalMoveUtils
import com.smu.som.game.service.GameStompService
import com.smu.som.game.service.YutGifService
import com.smu.som.game.wish.AnsweringPassDialog
import com.smu.som.game.wish.AnsweringWishDialog
import com.smu.som.game.wish.WishDialog
import com.smu.som.gameroom.GameRoomApi
import com.smu.som.gameroom.activity.GameRoomListActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_online_game.btn_chat
import kotlinx.android.synthetic.main.activity_online_game.tv_nickname_p1
import kotlinx.android.synthetic.main.activity_online_game.tv_nickname_p2
import okhttp3.OkHttpClient
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.TimeUnit

class GameTestActivity2 : AppCompatActivity()  {

    private lateinit var binding: ActivityOnlineGame2Binding

    var jsonObject = JSONObject()

    lateinit var stompConnection: Disposable
    lateinit var topic: Disposable

    private var btnState : Boolean = false
    val constant: GameConstant = GameConstant

    //1. STOMP init
    // url: ws://[도메인]/[엔드포인트]/ws
    val intervalMillis = 5000L
    val client = OkHttpClient.Builder()
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .connectTimeout(10, TimeUnit.SECONDS)
        .build()

    val stomp = StompClient(client, intervalMillis)

    // 가나가 필요해서 정의한 변수
    private val subscribes : MutableList<Disposable> = ArrayList() // stomp 구독들
    private val playerId : String = "2P" // 고정값
    private var oppQuestionDialog: GetQuestionDialog? // 상대방이 질문받은 내용 팝업창
    private var gameMalStompService: GameMalStompService = GameMalStompService(stomp)
    private lateinit var malMoveUtils:MalMoveUtils
    private lateinit var malInList : Array<ImageView> // 윷판에 있는 내 말
    private lateinit var oppMalInList: Array<ImageView> // 상대방의 윷판에 있는 말
    private lateinit var catHandList: Array<ImageView> // 내 고양이 발
    private lateinit var oppCatHandList: Array<ImageView> // 상대방 고양이 발
    // 끝-가나

    private val gameStomp = GameStompService(stomp)
    var num = 0
    var category: String? = null  // COUPLE, MARRIED, PARENT
    var adult: String? = null // ON, OFF
    var passCard_cnt = 0     // 패스권 개수
    var penalty = 0          // 패널티 개수

    init {
        stomp.url = GameConstant.URL
        oppQuestionDialog = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnlineGame2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        // 채팅방 입장 클릭 이벤트 리스너
        binding.btnChat.setOnClickListener {
            moveChatDialog(intent.getBundleExtra("myBundle"))
            binding.btnChat.setImageResource(R.drawable.chat_icon)
        }

        // 이렇게 안하면, 뷰가 다 그려지지 않은 시점에서 malInit이 호출돼 초기화가 제대로 안됨.
        binding.yutBoard.viewTreeObserver.addOnGlobalLayoutListener(
            object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                binding.yutBoard.viewTreeObserver.removeOnGlobalLayoutListener(this)
                // 말 초기화
                malInit()
            }
        })

        var mLastClickTime = 0L
        var firstThrow = true
        // 윷 던지기 버튼 클릭 이벤트
        binding.btnThrowYut2.setOnClickListener {
            // 중복 클릭 시간 차이 1초
            if (SystemClock.elapsedRealtime() - mLastClickTime < 3000){
                return@setOnClickListener
            }
            mLastClickTime = SystemClock.elapsedRealtime()

            if (firstThrow) {
                firstThrow = false
                gameStomp.sendThrowResult(GameConstant.GAME_STATE_FIRST_THROW)
            }
            else {
                gameStomp.sendThrowResult(GameConstant.GAME_STATE_THROW)
            }
            binding.btnThrowYut2.isEnabled = false
        }

        // 게임방법 설명
        // 게임방법 설명
        binding.btnRule.setOnClickListener {
            val dialog = GameRuleDialog(this)
            dialog.show()
        }


        val intent = intent
        val bundle = intent.getBundleExtra("myBundle")

        // 게임 설정 불러오기 (bundle)
        category = bundle?.getString("category") // COUPLE, MARRIED, PARENT
        adult = bundle?.getString("adult")

        settingCategory(category, adult)

        // 게임 설정 불러오기 (online_game_sp)
        val sp = this.getSharedPreferences("online_game_sp", Context.MODE_PRIVATE)
        val profileUrl = sp.getString("profileUrl", null)          // 카카오톡 프로필 사진


        if (bundle != null) {
            constant.set(bundle.getString("sender")!!, bundle.getString("gameRoomId")!!, "2P")
        }

        updateProfile(profileUrl, constant.GAME_TURN)

        tv_nickname_p2.text = constant.SENDER
        binding.profileImgCatP1.isEnabled = true
        binding.profileImgCatP2.isEnabled = false

        var yuts = IntArray(6, { 0 } )                        // 윷 결과 저장 리스트
        val soundPool = SoundPool.Builder().build()                // 게임 소리 실행 설정
        val gamesound = IntArray(8, { 0 } )

        // 2. connect
        stompConnection = stomp.connect()
            .subscribeOn(Schedulers.io()) // 네트워크 작업을 백그라운드 스레드에서 수행
            .observeOn(AndroidSchedulers.mainThread()) // UI 업데이트를 메인 스레드에서 수행
            .subscribe {
                when (it.type) {
                    Event.Type.OPENED -> {

                        // 말 이동 위치 조회 구독
                        val getMalsNextPositionSubscribe = stomp.join("/topic/game/" + GameConstant.GAMEROOM_ID + "/mal")
                            .subscribe(
                                { success ->
                                    val response = Klaxon().parse<GameMalResponse.GetMalMovePosition>(success)
                                    Log.i("som-gana", "말 이동 위치조회 요청 성공")
                                    runOnUiThread{ getMalsNextPositionHandler(response!!) }
                                },
                                { throwable -> Log.i("som-gana", "말 이동 위치조회 실패: ${throwable.toString()}") }
                            )
                        subscribes.add(getMalsNextPositionSubscribe)

                        // 말 이동하기 구독
                        val moveMalSubscribe = stomp.join("/topic/game/" + GameConstant.GAMEROOM_ID + "/mal/move")
                            .subscribe(
                                { success ->
                                    val response = Klaxon().parse<GameMalResponse.MoveMalDTO>(success)
                                    Log.i("som-gana", "말 이동하기 요청 성공")

                                    runOnUiThread{ moveMalHandler(response!!) }
                                },
                                { throwable -> Log.i("som-gana", "말 이동하기 실패: ${throwable.toString()}") }
                            )
                        subscribes.add(moveMalSubscribe)

                        // 턴 변경 구독
                        val turnChangeSubscribe = stomp.join("/topic/game/turn/" + GameConstant.GAMEROOM_ID)
                            .subscribe(
                                { success ->
                                    val response = Klaxon().parse<Game.turnChange>(success)
                                    runOnUiThread{

                                        if (response?.messageType == GameConstant.TURN_CHANGE) {
                                            Log.i("som-jsy-2", "턴 변경")
                                            btnState = !btnState
                                            binding.btnThrowYut2.isEnabled = btnState
                                            setTurnChangeUI()
                                        }
                                    }
                                },
                                { throwable -> Log.i("som-jsy", throwable.toString()) }
                            )
                        subscribes.add(turnChangeSubscribe)

                        // 스코어 구독
                        val getScoreSubscribe = stomp.join("/topic/game/score/" + GameConstant.GAMEROOM_ID).subscribe {
                                stompMessage ->
                            val result = Klaxon()
                                .parse<ScoreInfo>(stompMessage)
                            runOnUiThread {
                                if (result?.player2Score != null && result?.player1Score != null)
                                    scoreUIChange(result.player1Score, result.player2Score)


                            }
                        }
                        subscribes.add(getScoreSubscribe)

                        // 게임종료
                        val gameOverSubscribe = stomp.join("/topic/game/" + GameConstant.GAMEROOM_ID + "/end").subscribe { stompMessage ->
                            val result = Klaxon()
                                .parse<Game.GameWinner>(stompMessage)
                            runOnUiThread {
                                // score가 4점이 되면  게임 종료
                                if (result?.winner == playerId) { // 내가 이겼을 때
                                    val gameEndDialog = GameEndDialog(this)
                                    gameEndDialog.showPopup()
//                                    finish()
                                }
                                else {
                                    val gameEndDialog = GameEndDialog(this)
                                    gameEndDialog.losePopup()
                                }
                            }
                        }
                        subscribes.add(gameOverSubscribe)

                        // subscribe 채널구독
                        val gametopic = stomp.join("/topic/game/room/" + constant.GAMEROOM_ID)
                            .subscribe { stompMessage ->
                                val result = Klaxon()
                                    .parse<Game.GetGameInfo>(stompMessage)
                                runOnUiThread {
                                    if (result?.messageType == GameConstant.GAME_STATE_WAIT) {
                                        binding.btnThrowYut2.isEnabled = false // 로직 완성되면 false로 바꾸기 (현재 1명 들어와있는 상태에서 테스트 하기 위함)
                                        binding.viewProfilePick1P.setBackgroundResource(R.drawable.pick)
                                        binding.profileImgCatP1.isEnabled = true
                                        binding.profileImgCatP2.isEnabled = false

                                        if (result.message == "1P가 들어오지 않았습니다.") {
                                            Toast.makeText(this, "상대방이 들어오지 않았습니다.", Toast.LENGTH_SHORT).show()
                                        }

                                    }
                                    if (result?.messageType == GameConstant.GAME_STATE_START){
                                        binding.viewProfilePick1P.setBackgroundResource(R.drawable.pick)
                                        binding.btnThrowYut2.isEnabled = false // 2P는 비활성화
                                        val name = result.userNameList // message에 [1P,2P] 이름이 들어있음
                                        val profileUrl = result.profileURL_1P
                                        updateProfile(profileUrl, "1P")

                                        if (name.split(",")[1] == constant.SENDER) {
                                            tv_nickname_p1.text = name.split(",")[0]
                                            tv_nickname_p2.text = constant.SENDER
                                        } else {
                                            tv_nickname_p1.text = name.split(",")[1]
                                            tv_nickname_p2.text = constant.SENDER
                                        }
                                    }
                                }
                            }
                        subscribes.add(gametopic)

                        // 상대방 연결 끊긴 경우 sub 구독
                        val disconnectSubscribe = stomp.join("/topic/game/disconnect/" + GameConstant.GAMEROOM_ID).subscribe { stompMessage ->
                            val result = Klaxon()
                                .parse<Game.GetGameDisconnect>(stompMessage)
                            runOnUiThread {
                                    val dialog = AlertDialog.Builder(this)
                                        .setTitle("상대방 연결 끊김")
                                        .setMessage("상대방이 연결을 끊었습니다.")
                                        .setPositiveButton("확인") { dialog, which ->
                                            dialog.dismiss()
                                            val intent = Intent(this, GameRoomListActivity::class.java)
                                            startActivity(intent)
                                            finish()
                                        }
                                        .create()
                                    dialog.show()

                            }
                        }
                        subscribes.add(disconnectSubscribe)

                        val throwTopic = stomp.join("/topic/game/throw/" + constant.GAMEROOM_ID).subscribe { stompMessage ->
                            val result = Klaxon()
                                .parse<Game.GetThrowResult>(stompMessage)
                            runOnUiThread {
                                if (result?.messageType == "CATCH_MAL" && result.playerId == "2P") {
                                    binding.btnThrowYut2.isEnabled = true
                                    val dialog = WishDialog(this, stomp)
                                    dialog.show()
                                    binding.layoutYutResult.visibility = View.INVISIBLE
                                    Toast.makeText(this, "상대방의 말을 잡았습니다! 한 번 더!", Toast.LENGTH_SHORT).show()
                                }
                                else if (result?.messageType == "CATCH_MAL" && result.playerId == "1P") {
                                    Toast.makeText(this, "상대방이 당신의 말을 잡았습니다!", Toast.LENGTH_SHORT).show()
                                }
                                else {
                                    num = result?.yut!!.toInt()

                                    // 윷 gif 재생
                                    val yutService = YutGifService(this)
                                    yutService.showYutGif(num) 
                                    // 윷이나 모인 경우 한번 더
                                    if (result.messageType == GameConstant.ONE_MORE_THROW && result.playerId == playerId) {
                                        binding.btnThrowYut2.isEnabled = true
                                        yutService.setOnDismissListener {
                                            addYutResult(num)
                                        }

                                    }else {  // 윷이나 모가 아닌 경우
                                        // 첫 던진 윷이 빽도인 경우
                                        if ((result.messageType == GameConstant.GAME_STATE_FIRST_THROW)
                                            && (result.playerId == playerId)
                                            && (num == 0)
                                        ) {
                                            binding.btnThrowYut2.isEnabled = true
                                            firstThrow = true
                                            Toast.makeText(this, "빽도입니다. 한 번 더!", Toast.LENGTH_SHORT).show()
                                        }
                                        // 내 턴이면 질문 받아오기
                                        else if (result.playerId == playerId) {
                                            getQuestion()
                                            yutService.setOnDismissListener {
                                                addYutResult(num)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        subscribes.add(throwTopic)

                        val questionTopic = stomp.join("/topic/game/question/" + constant.GAMEROOM_ID).subscribe { stompMessage ->
                            val result = Klaxon()
                                .parse<QnAResponse.GetQuestion>(stompMessage)
                            runOnUiThread {
                                if(result?.playerId == "1P") {
                                    val questionMessage = result.question
                                    oppQuestionDialog?.dismiss()
                                    oppQuestionDialog = GetQuestionDialog(this, questionMessage)
                                    oppQuestionDialog?.showPopup()
//                                    questionView.dismiss() // 이 코드 왜 1P에는 없고 2P에만 있나요?

                                }

                                if (result?.playerId == playerId) {
                                    if(result.penalty > penalty){ // 패널티로 모든 윷 결과 삭제
                                        binding.layoutYutResult.removeAllViews()
                                    }
                                    penalty = result.penalty
                                }
                            }
                        }
                        subscribes.add(questionTopic)

                        // 상대방이 추가 질문권을 사용하고 있는 것을 알려주는 채널
                        val noticeAddQuestion = stomp.join("/topic/game/question/wish/notice" + constant.GAMEROOM_ID).subscribe { stompMessage ->
                            val result = Klaxon()
                                .parse<QnAResponse.GetAnswer>(stompMessage)
                            runOnUiThread {
                                if (result?.playerId != playerId) {
                                    Toast.makeText(this, "상대방이 추가 질문권을 사용하고 있습니다.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                        subscribes.add(noticeAddQuestion)

                        // 상대방이 추가 질문권을 사용하여 대답 해야 하는 경우
                        val addQuestionSubscribe = stomp.join("/topic/game/question/wish" + constant.GAMEROOM_ID).subscribe { stompMessage ->
                            val result = Klaxon()
                                .parse<QnAResponse.GetAnswer>(stompMessage)
                            runOnUiThread {
                                if (result?.playerId != playerId) {
                                    val dialog = AnsweringWishDialog(this, result?.answer, stomp)
                                    dialog.show()
                                }
                                else {
                                    oppQuestionDialog = GetQuestionDialog(this, result.answer)
                                    oppQuestionDialog?.waitPopup()
                                }
                            }
                        }
                        subscribes.add(addQuestionSubscribe)

                        val answerTopic = stomp.join("/topic/game/answer/" + constant.GAMEROOM_ID).subscribe { stompMessage ->
                            val result = Klaxon()
                                .parse<QnAResponse.GetAnswer>(stompMessage)
                            runOnUiThread {
                                if(!result?.playerId.equals(playerId)){
                                    oppQuestionDialog?.dismiss() // 상대방이 받은 질문 팝업창 닫기
                                }
                                else{
                                    unlockYutResults() // 질문에 대한 답변까지 하고 나서야 윷 결과 클릭 가능
                                }

                                // 답변 결과
                                val answer = result?.answer
                                val answerResult = GetAnswerResultDialog(this, answer!!)
                                answerResult.showPopup()
                            }
                        }
                        subscribes.add(answerTopic)

                        // 패스권 적립 결과를 받는 채널
                        val passTicketSubscribe = stomp.join("/topic/game/room/" + constant.GAMEROOM_ID + "/wish/pass")
                            .subscribe(
                                { success ->
                                    val response = Klaxon().parse<Game.PassWish>(success)
                                    if (response?.playerId == playerId) {
                                        runOnUiThread {
                                            // 패스권 개수가 기존보다 증가했을 때
                                            if (passCard_cnt < response.passCard!!) {
                                                Toast.makeText(this, "패스권이 적립되었습니다.", Toast.LENGTH_SHORT).show()
                                            }
                                            // 패스권 개수가 기존보다 감소했을 때
                                            else {
                                                Toast.makeText(this, "패스권이 사용되었습니다.", Toast.LENGTH_SHORT).show()
                                            }
                                            passCard_cnt = response.passCard!!
                                        }
                                    }
                                    if (response?.playerId != playerId) {
                                        runOnUiThread {
                                            Toast.makeText(this, "상대방이 패스권을 선택했습니다.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                { throwable -> Log.i("som-jsy", throwable.toString()) }
                            )
                        subscribes.add(passTicketSubscribe)

                        // 채팅방 메세지 받는 채널
                        val chatSubscribe = stomp.join("/topic/game/chat/room/" + constant.GAMEROOM_ID).subscribe {
                                stompMessage ->
                            val result = Klaxon()
                                .parse<Chat>(stompMessage)
                            runOnUiThread {
                                if (result != null) {
                                    val message = result.gameRoomMsg
                                    binding.textView.text = message
                                    btn_chat.setImageResource(R.drawable.chat_icon_red)
                                }
                            }
                        }
                        subscribes.add(chatSubscribe)

                        // 처음 입장
                        try {
                            jsonObject.put("messageType", "WAIT")
                            jsonObject.put("room_id", constant.GAMEROOM_ID)
                            jsonObject.put("sender", constant.SENDER)
                            jsonObject.put("player_id", constant.GAME_TURN)
                            jsonObject.put("profileURL_2P", "$profileUrl")
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                        stomp.send("/app/game/message", jsonObject.toString()).subscribe()

                    }

                    Event.Type.CLOSED -> {

                    }

                    Event.Type.ERROR -> {

                    }
                }
            }
    }
    // 연결 끊긴 경우
    override fun onDestroy() {
        super.onDestroy()
        val request = JSONObject()
        try {
            request.put("messageType", "END")
            request.put("room_id", constant.GAMEROOM_ID)
            request.put("player_id", constant.GAME_TURN)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        stomp.send("/app/game/disconnect", request.toString()).subscribe()

        // 구독 취소
        stompConnection.dispose()
        subscribes.forEach{subscribe -> subscribe.dispose()}

        // GameRoomApi 에서 게임 방 삭제
        val gameRoomApi = GameRoomApi
        gameRoomApi.deleteGameRoom(constant.GAMEROOM_ID).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                Log.d("deleteGameRoom", "success")
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.d("deleteGameRoom", "fail")
            }
        })

        // 이전 화면으로 이동
        val intent = Intent(this, GameRoomListActivity::class.java)
        startActivity(intent)

        finish() // 현재 액티비티 종료
    }

    // 질문 받아오기
    // API 로 질문을 받는 함수
    private fun getQuestion() {

        (application as MasterApplication).service.getQuestion(
            category!!, adult!!
        )
            .enqueue(object : Callback<ArrayList<Question>> {
                // 성공
                override fun onResponse(
                    call: Call<ArrayList<Question>>,
                    response: Response<ArrayList<Question>>
                )
                {
                    if (response.isSuccessful) {
                        val question = response.body()
                        val questionId = question?.get(0)!!.id

                        gameStomp.sendQuestion(question[0].question.toString(), questionId )

                        // gif 모션 끝나면 질문 다이얼로그 띄우기
                        Handler(Looper.getMainLooper()).postDelayed({
                            if (passCard_cnt > 0) {
                                val dialog = AnsweringPassDialog(this@GameTestActivity2, question, stomp,passCard_cnt)
                                dialog.showPopup()
                            }
                            else {
                                val answeringDialog = AnsweringDialog(this@GameTestActivity2, question, stomp, penalty)
                                answeringDialog.showPopup()
                            }
                        }, 4000)


                    }
                }

                // 실패
                override fun onFailure(
                    call: Call<ArrayList<Question>>,
                    t: Throwable
                ) {
                    Log.e(ContentValues.TAG, "서버 오류")
                }
            })
    }

    private fun updateProfile(profileUrl: String?, playerId : String) {

        val imageUrl = profileUrl
        var imageView: ImageView

        // 1P 프로필 설정
        if (playerId == constant.GAME_TURN)
            imageView = findViewById(R.id.view_profile_p2)

        // 2P 프로필 설정
        else
            imageView = findViewById(R.id.view_profile_p1)

        // Glide를 사용하여 이미지 로드
        Glide.with(this)
            .load(imageUrl)
            .diskCacheStrategy(DiskCacheStrategy.ALL) // 디스크 캐싱 전략 설정
            .into(imageView)
    }

    // 2P 스코어 UI 변경
    private fun scoreUIChange(score1P: Int, score2p: Int) {
        binding.tvPlayer1Score.text = score1P.toString()
        binding.tvPlayer2Score.text = score2p.toString()
    }

    private fun setTurnChangeUI() {

        if (!btnState) // true : 1P 차례
        {
            binding.viewProfilePick1P.setBackgroundResource(R.drawable.pick)
            binding.viewProfilePick2P.setBackgroundResource(R.drawable.not_pick)
        }
        else {
            binding.viewProfilePick1P.setBackgroundResource(R.drawable.not_pick)
            binding.viewProfilePick2P.setBackgroundResource(R.drawable.pick)
        }
        binding.profileImgCatP1.isEnabled = !binding.profileImgCatP1.isEnabled
        binding.profileImgCatP2.isEnabled = !binding.profileImgCatP2.isEnabled
    }

    private fun settingCategory(category: String?, adult: String?) {

        when(category) {
            "COUPLE" -> setImage(R.drawable.couple)
            "MARRIED" -> setImage(R.drawable.married)
            "PARENT" -> setImage(R.drawable.parent)
        }
        binding.imgGameSettingAdult.isEnabled = adult == "ON"
    }
    private fun setImage(image: Int) {
        binding.imgGameSettingCategory.setImageResource(image)
    }

    // 채팅방 입장 버튼 클릭 이벤트
    private fun moveChatDialog(bundle: Bundle?) {
        val intent = Intent(this, GameChatActivity::class.java)
        intent.putExtra("myBundle", bundle)
        startActivity(intent)
    }


    // 윷 결과 화면에 표시하기
    private fun addYutResult(yutResult: Int){
        binding.layoutYutResult.visibility = View.VISIBLE

        var yut : ImageView = ImageView(this)

        // 크기 설정
        val params: LinearLayout.LayoutParams = LinearLayout.LayoutParams(dpToPx(this, 60f), dpToPx(this, 50f))
        yut.layoutParams = params
        yut.setPadding(dpToPx(this, 5f), 0, dpToPx(this, 5f), 0)

        when(yutResult){
            0 -> yut.setImageResource(R.drawable.yut1_back_do)
            1 -> yut.setImageResource(R.drawable.yut1_do)
            2 -> yut.setImageResource(R.drawable.yut1_gae)
            3 -> yut.setImageResource(R.drawable.yut1_gul)
            4 -> yut.setImageResource(R.drawable.yut1_yut)
            5 -> yut.setImageResource(R.drawable.yut1_mo)
        }

        // 클릭 이벤트 리스너 등록
        yut.setOnClickListener{
            gameMalStompService.sendMalNextPosition(GameConstant.GAMEROOM_ID, playerId, yutResult)
            binding.layoutYutResult.removeView(it) // 해당 윷결과 뷰 삭제

            // 말 이동 완료하기 전까지 다른 윷 결과 클릭 못하게 막기
            lockYutResults()
        }

        // 레이아웃에 추가
        binding.layoutYutResult.addView(yut)
        lockYutResults()
    }

    private fun lockYutResults(){
        for(yutResult in binding.layoutYutResult.children){
            yutResult.isEnabled = false // 다른 윷 결과 클릭 불가
        }
    }

    private fun unlockYutResults(){
        for(yutResult in binding.layoutYutResult.children){
            yutResult.isEnabled = true // 다른 윷 결과 클릭 가능
        }
    }

    // dp -> px 단위 변경
    private fun dpToPx(context: Context, dp: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            context.resources.displayMetrics
        ).toInt()
    }

    // 말 초기화
    private fun malInit(){
        malInList = arrayOf(binding.malWhite0, binding.malWhite1, binding.malWhite2, binding.malWhite3)
        oppMalInList = arrayOf(binding.malBlack0, binding.malBlack1, binding.malBlack2, binding.malBlack3)
        catHandList = arrayOf(binding.catHandW0, binding.catHandW1, binding.catHandW2, binding.catHandW3)
        oppCatHandList = arrayOf(binding.catHandB0, binding.catHandB1, binding.catHandB2, binding.catHandB3)

        // 말 움직이기 utils 클래스 생성
        malMoveUtils = MalMoveUtils(binding.yutBoard, binding.malBlack0)

        // 말의 초기 위치 지정하기
        malInList.forEach { mal -> malMoveUtils.initPosition(mal) }
        oppMalInList.forEach { mal -> malMoveUtils.initPosition(mal) }

        // 윷판에 있는 말은 숨기기
        malInList.forEach { mal -> mal.visibility = View.GONE }
        oppMalInList.forEach { mal -> mal.visibility = View.GONE }

        // 말 추가하기 버튼 비활성화
        binding.btnAddMal.isEnabled = false
    }

    // 어느 말을 이동할지 클릭 이벤트 리스너 등록
    private fun getMalsNextPositionHandler(response: GameMalResponse.GetMalMovePosition){
        if(!response.playerId.equals(playerId)){ // 나에게 해당하는 응답이 아니라면
            return;
        }

        // 새로 추가할 수 있는 말이 있다면
        if(response.newMalId != -1){
            binding.btnAddMal.isEnabled = true
            binding.btnAddMal.setOnClickListener{
                sendMoveMal(response.newMalId, response.yutResult)
            }
        }

        // 윷판 안에 있는 말
        for(i in 0 until 4){
            val mal = malInList[i]
            if(mal.visibility != View.GONE) { // GONE이면 윷판 밖에 있는 말임.
                mal.setOnClickListener{
                    sendMoveMal(i, response.yutResult)
                }
            }
        }
    }

    // 서버와 <말 이동하기> 통신하기
    private fun sendMoveMal(malId : Int, yutResult: String){
        gameMalStompService.sendMalMove(
            gameId = GameConstant.GAMEROOM_ID,
            playerId = playerId,
            yutResult = yutResult,
            malId = malId
        )

        removeMalEventListener() // 어느 말을 이동시킬지 결정한 이후에는 등록된 이벤트 리스너 지워야함.

        binding.btnAddMal.isEnabled = false

        unlockYutResults()
    }

    // 말에 있는 클릭 이벤트 리스너 모두 지우기
    private fun removeMalEventListener(){
        malInList.forEach { mal ->
            mal.setOnClickListener{}
        }
    }

    // 말 이동하기
    public fun moveMalHandler(response: GameMalResponse.MoveMalDTO){
        if(response.playerId == playerId){ // 내 턴인 경우
            // 말 움직이기
            malMoveUtils.move(malInList[response.malId], response.movement)
            catHandList[response.malId].isEnabled = false // 고양이 발 점수

            if(response.isCatchMal){ // 내가 상대방 말을 잡았을 때
                response.catchMalList.forEach { catchMalId ->
                    oppMalInList[catchMalId].visibility = View.GONE
                    oppMalInList[catchMalId].setImageResource(R.drawable.selector_profile_cat)
                    malMoveUtils.initPosition(oppMalInList[catchMalId])
                    oppCatHandList[catchMalId].isEnabled = true
                }
            }
            if(response.isUpdaMal){ // 내 말을 업었을 때
                malInList[response.updaMalId].visibility = View.GONE
                when(response.point){
                    2 -> malInList[response.malId].setImageResource(R.drawable.cat_w_2)
                    3 -> malInList[response.malId].setImageResource(R.drawable.cat_w_3)
                    4 -> malInList[response.malId].setImageResource(R.drawable.cat_w_4)
                }

            }
        }
        else { // 상대방 턴인 경우
            // 말 움직이기
            malMoveUtils.move(oppMalInList[response.malId], response.movement)
            oppCatHandList[response.malId].isEnabled = false // 고양이 발 점수

            if(response.isCatchMal){ // 상대가 내 말을 잡았을 때
                response.catchMalList.forEach { catchMalId ->
                    malInList[catchMalId].visibility = View.GONE
                    malInList[catchMalId].setImageResource(R.drawable.selector_profile_w_cat)
                    malMoveUtils.initPosition(malInList[catchMalId])
                    catHandList[catchMalId].isEnabled = true
                }
            }
            if(response.isUpdaMal){ // 상대가 자신의 말을 업었을 때
                oppMalInList[response.updaMalId].visibility = View.GONE
                when(response.point){
                    2 -> oppMalInList[response.malId].setImageResource(R.drawable.cat_b_2)
                    3 -> oppMalInList[response.malId].setImageResource(R.drawable.cat_b_3)
                    4 -> oppMalInList[response.malId].setImageResource(R.drawable.cat_b_4)
                }

            }
        }
    }
}