package com.smu.som.game.activity

import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.drawable.Drawable
import android.media.SoundPool
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import com.beust.klaxon.Klaxon
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.gmail.bishoybasily.stomp.lib.Event
import com.gmail.bishoybasily.stomp.lib.StompClient
import com.google.gson.GsonBuilder
import com.smu.som.MasterApplication
import com.smu.som.Question
import com.smu.som.R
import com.smu.som.databinding.ActivityOnlineGame2Binding
import com.smu.som.game.dialog.AnsweringDialog
import com.smu.som.game.GameChatActivity
import com.smu.som.game.GameConstant
import com.smu.som.game.dialog.GameEndDialog
import com.smu.som.game.dialog.GetAnswerResultDialog
import com.smu.som.game.dialog.GetQuestionDialog
import com.smu.som.game.response.Game
import com.smu.som.game.response.QnAResponse
import com.smu.som.game.response.ScoreInfo
import com.smu.som.game.response.GameMalResponse
import com.smu.som.game.service.GameApi
import com.smu.som.game.service.GameMalService
import com.smu.som.game.service.GameMalStompService
import com.smu.som.game.service.MalMoveUtils
import com.smu.som.game.service.GameStompService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_online_game.tv_nickname_p1
import kotlinx.android.synthetic.main.activity_online_game.tv_nickname_p2
import okhttp3.OkHttpClient
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Stack
import java.util.concurrent.TimeUnit

class GameTestActivity2 : AppCompatActivity()  {

    private lateinit var binding: ActivityOnlineGame2Binding

    var jsonObject = JSONObject()

    lateinit var stompConnection: Disposable
    lateinit var topic: Disposable
    private lateinit var gametopic: Disposable
    private lateinit var answerTopic: Disposable
    private lateinit var questionTopic: Disposable
    private lateinit var throwTopic: Disposable

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
    private val playerId : String = "2P" // 고정값
    private val yutResultStack : Stack<Int> = Stack() // 윷 결과들 저장. 임시로 쓰는거라 stack으로 만들어둠
    private var gameMalStompService: GameMalStompService = GameMalStompService(stomp)
    private val gameMalService:GameMalService = GameMalService()
    private lateinit var malMoveUtils:MalMoveUtils
    private lateinit var malInList : Array<ImageView> // 윷판에 있는 내 말
    private lateinit var malOutList : Array<ImageView> // 윷판 밖에 있는 내 말
    private lateinit var oppMalInList: Array<ImageView> // 상대방의 윷판에 있는 말

    var count = 1
    // 끝-가나

    init {
        stomp.url = GameConstant.URL
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnlineGame2Binding.inflate(layoutInflater)
        setContentView(binding.root)


        // 채팅방 입장 클릭 이벤트 리스너
        binding.btnChat.setOnClickListener {
            moveChatDialog(intent.getBundleExtra("myBundle"))
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

        // 말 추가하기 버튼을 눌렀을 때 이벤트 리스너
        binding.btnAddToken2.setOnClickListener{
            gameMalStompService.sendMalNextPosition(GameConstant.GAMEROOM_ID, playerId, yutResultStack.peek())

            // [가나] 말 이동 테스트 - 지우지 말아주세요
//            malInList[0].visibility = View.VISIBLE
//            malMoveUtils.move(malInList[0], count)
//            count++
//            if(count == 28){
//                count++
//            }
        }

        val intent = intent
        val bundle = intent.getBundleExtra("myBundle")

        // 게임 설정 불러오기 (bundle)
        var category = bundle?.getString("category") // COUPLE, MARRIED, PARENT
        var adult = bundle?.getString("adult")

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
                        stomp.join("/topic/game/" + GameConstant.GAMEROOM_ID + "/mal")
                            .subscribe(
                                { success ->
                                    val response = Klaxon().parse<GameMalResponse.GetMalMovePosition>(success)
                                    Log.i("som-gana", "성공")
                                    // 말 클릭 이벤트 리스너 등록
                                    if(response!!.playerId == playerId){ // 나에게 해당하는 응답이라면
                                        val yutResult = yutResultStack.pop()
                                        runOnUiThread{ setMalEventListener(response, yutResult) }
                                    }
                                },
                                { throwable -> Log.i("som-gana", throwable.toString()) }
                            )

                        // 말 이동하기 구독
                        stomp.join("/topic/game/" + GameConstant.GAMEROOM_ID + "/mal/move")
                            .subscribe(
                                { success ->
                                    val response = Klaxon().parse<GameMalResponse.MoveMalDTO>(success)
                                    Log.i("som-gana", "성공")

                                    runOnUiThread{ moveMal(response!!) }
                                },
                                { throwable -> Log.i("som-gana", throwable.toString()) }
                            )


                        // 스코어 구독
                        stomp.join("/topic/game/score/" + GameConstant.GAMEROOM_ID).subscribe {
                                stompMessage ->
                            val result = Klaxon()
                                .parse<ScoreInfo>(stompMessage)
                            runOnUiThread {
                                if (result?.player2Score != null && result?.player1Score != null)
                                    scoreUIChange(result.player1Score, result.player2Score)


                            }
                        }

                        // 게임종료
                        stomp.join("/topic/game/" + GameConstant.GAMEROOM_ID + "/end").subscribe { stompMessage ->
                            val result = Klaxon()
                                .parse<Game.GameWinner>(stompMessage)
                            runOnUiThread {
                                // score가 4점이 되면  게임 종료
                                if (result?.winner == playerId) { // 내가 이겼을 때
                                    val gameEndDialog = GameEndDialog(this)
                                    gameEndDialog.showPopup()
//                                    finish()
                                }
                                if (result?.loser == playerId) {
                                    val gameEndDialog = GameEndDialog(this)
                                    gameEndDialog.losePopup()
                                }
                            }
                        }

                        // subscribe 채널구독
                        gametopic = stomp.join("/topic/game/room/" + constant.GAMEROOM_ID)
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
                                            // 1P가 올 때까지 기다리는 다이얼로그
                                            val builder = AlertDialog.Builder(this)
                                            builder.setTitle("대기중")
                                            builder.setMessage("1P가 들어오지 않았습니다. 잠시만 기다려주세요.")
                                            builder.setPositiveButton("확인", DialogInterface.OnClickListener { dialog, which ->
                                                dialog.dismiss()
                                            })
                                            builder.show()
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

                        throwTopic = stomp.join("/topic/game/throw/" + constant.GAMEROOM_ID).subscribe { stompMessage ->
                            val result = Klaxon()
                                .parse<Game.GetThrowResult>(stompMessage)
                            runOnUiThread {
                                yuts[0] = result?.yut!!.toInt()
                                showYutResult(yuts[0])
                                if (result?.messageType == GameConstant.ONE_MORE_THROW) {
                                    binding.btnThrowYut2.isEnabled = true
                                }

                            }

                        }

                        questionTopic = stomp.join("/topic/game/question/" + constant.GAMEROOM_ID).subscribe { stompMessage ->
                            val result = Klaxon()
                                .parse<QnAResponse.GetQuestion>(stompMessage)
                            runOnUiThread {
                                if(result?.playerId == "1P") {
                                    val questionMessage = result.question
                                    val questionView = GetQuestionDialog(this, questionMessage)
                                    questionView.showPopup()
                                    // 새로운 질문이 들어오면 기존의 질문 다이얼로그는 dismiss
                                    if (questionView.isShowing) {
                                        questionView.dismiss()
                                    }

                                }
                            }

                        }

                        answerTopic = stomp.join("/topic/game/answer/" + constant.GAMEROOM_ID).subscribe { stompMessage ->
                            val result = Klaxon()
                                .parse<QnAResponse.GetAnswer>(stompMessage)
                            runOnUiThread {
                                // 답변 결과
                                val answer = result?.answer
                                val answerResult = GetAnswerResultDialog(this, answer!!)
                                answerResult.showPopup()

                                if(result?.turnChange == GameConstant.TURN_CHANGE) {
                                    btnState = !btnState
                                    binding.btnThrowYut2.isEnabled = btnState
                                    setTurnChangeUI()
                                }
                            }

                        }

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


                        // 윷 던지기 버튼 클릭 이벤트 리스너
                        var throwCount = 0
                        binding.btnThrowYut2.setOnClickListener() {
                            var num = playGame(soundPool, gamesound, yuts.sum())
                            yutResultStack.push(num) // 가나-임시로 윷 결과값 저장

                            throwCount++
                            var jsonObject = JSONObject()

                            if (throwCount == 1) {
                                try {
                                    jsonObject.put("messageType", "FIRST_THROW")
                                    jsonObject.put("room_id", constant.GAMEROOM_ID)
                                    jsonObject.put("yut", "$num")
                                    jsonObject.put("player_id", constant.GAME_TURN)
                                } catch (e: JSONException) {
                                    e.printStackTrace()
                                }
                            } else {
                                try {
                                    jsonObject.put("messageType", "THROW")
                                    jsonObject.put("room_id", constant.GAMEROOM_ID)
                                    jsonObject.put("yut", "$num")
                                    jsonObject.put("player_id", constant.GAME_TURN)

                                } catch (e: JSONException) {
                                    e.printStackTrace()
                                }
                            }

                            stomp.send("/app/game/throw", jsonObject.toString()).subscribe()

                            if (num != 4 && num != 5) {

                                // API 로 질문을 받는 함수
                                (application as MasterApplication).service.getQuestion(
                                    category!!, adult!!
                                ).enqueue(object : Callback<ArrayList<Question>> {
                                    // 성공
                                    override fun onResponse(
                                        call: Call<ArrayList<Question>>,
                                        response: Response<ArrayList<Question>>
                                    ) {
                                        if (response.isSuccessful) {
                                            val question = response.body()
                                            val questionId = question?.get(0)!!.id

                                            val questionStomp = GameStompService(stomp)
                                            questionStomp.sendQuestion(question[0].question.toString(), questionId )

                                            // git 모션 끝나면 질문 다이얼로그 띄우기
                                            Handler(Looper.getMainLooper()).postDelayed({
                                                val answeringDialog = AnsweringDialog(this@GameTestActivity2, question, stomp)
                                                answeringDialog.showPopup()
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
                            } // if (num != 4 || num != 5) 끝




                        }

                    }

                    Event.Type.CLOSED -> {


                    }

                    Event.Type.ERROR -> {

                    }
                }
            }
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
        Handler(Looper.getMainLooper()).postDelayed({
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
        }, 4000)
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

    private fun showYutResult(num: Int) {
        Log.i("som-gana", "윷 결과 = ${num}")
        val gifImageView = findViewById<ImageView>(R.id.gifImageView)

        gifImageView.visibility = View.VISIBLE
        when(num) {
            1 -> {
                // Drawable 리소스에 있는 GIF 파일을 로딩하여 표시
                var gifResourceId = R.drawable.yut_do
                gifImageView(gifResourceId)

            }
            2 -> {
                var gifResourceId = R.drawable.yut_gae
                gifImageView(gifResourceId)

            }
            3 -> {
                var gifResourceId = R.drawable.yut_gul
                gifImageView(gifResourceId)

            }
            4 -> {
                var gifResourceId = R.drawable.yut_yut
                gifImageView(gifResourceId)

            }
            5 -> {
                var gifResourceId = R.drawable.yut_mo
                gifImageView(gifResourceId)

            }
            0 -> {
                var gifResourceId = R.drawable.yut_backdo
                gifImageView(gifResourceId)

            }

        }

    }

    private fun gifImageView(gifResourceId: Int) {
        val gifImageView = findViewById<ImageView>(R.id.gifImageView)
        Glide.with(this)
            .asGif()
            .load(gifResourceId)
            .listener(object : RequestListener<GifDrawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<GifDrawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    return false
                }

                override fun onResourceReady(
                    resource: GifDrawable?,
                    model: Any?,
                    target: Target<GifDrawable>?,
                    dataSource: com.bumptech.glide.load.DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    // GIF 애니메이션이 완료되면 ImageView를 숨김
                    resource?.setLoopCount(1) // 1회만 재생하도록 설정
                    resource?.registerAnimationCallback(object : Animatable2Compat.AnimationCallback() {
                        override fun onAnimationEnd(drawable: Drawable?) {
                            gifImageView.visibility = View.GONE
                        }
                    })
                    return false
                }
            })
            .into(gifImageView)

    }


    // get 요청으로 turn 여부를 받아옴 true/false
    private fun getBtnState() {

        val gson = GsonBuilder()
            .setLenient()
            .create()

        // Retrofit을 초기화합니다.
        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8080")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        // GameApi 서비스를 생성합니다.
        val gameApi = retrofit.create(GameApi::class.java)

        // GET 요청을 보냅니다.
        val call = gameApi.getTurn()
        call.enqueue(object : Callback<Boolean> {
            override fun onResponse(
                call: Call<Boolean>,
                response: Response<Boolean>
            ) {
                if (response.isSuccessful) {
                    val yutBtnState = response.body() ?:false
                    println("서버로부터 받아온 버튼 상태: $yutBtnState")

                    binding.btnThrowYut2.isEnabled = yutBtnState

                } else {
                    println("GET 요청은 성공했지만 응답은 실패함")
                }
            }

            override fun onFailure(call: Call<Boolean>, t: Throwable) {
                println("GET 요청이 실패함: ${t.localizedMessage}")
            }
        })

    }


    // 채팅방 입장 버튼 클릭 이벤트
    private fun moveChatDialog(bundle: Bundle?) {
        val intent = Intent(this, GameChatActivity::class.java)
        intent.putExtra("myBundle", bundle)
        startActivity(intent)
    }

    private fun playGame(soundPool: SoundPool, gamesound: IntArray, sum: Int): Int {
        val yuts = arrayOf("빽도", "도", "개", "걸", "윷", "모")
        var num = percentage(sum)       // 윷 결과
//        result.setBackgroundResource(resources.getIdentifier("result_$num", "drawable", packageName))
//
//        // 윷 결과 애니메이션
//        Handler(Looper.getMainLooper()).postDelayed({
//            soundPool.play(gamesound[num], 1.0f, 1.0f, 0, 0, 1.0f)
//            result_text.setText(yuts[num])
//            result.setBackgroundResource(resources.getIdentifier("yut_$num", "drawable", packageName))
//        }, 1000)
        return num
    }

    // 윷 확률 설정 함수
    private fun percentage(sum: Int): Int {
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


    // 말 초기화
    private fun malInit(){
        malInList = arrayOf(binding.malWhite0, binding.malWhite1, binding.malWhite2, binding.malWhite3)
        malOutList = arrayOf(binding.malOutWhite0, binding.malOutWhite1, binding.malOutWhite2, binding.malOutWhite3)
        oppMalInList = arrayOf(binding.malBlack0, binding.malBlack1, binding.malBlack2, binding.malBlack3)

        // 말 움직이기 utils 클래스 생성
        malMoveUtils = MalMoveUtils(binding.yutBoard, binding.malBlack0)

        // 말의 초기 위치 지정하기
        malInList.forEach { mal -> malMoveUtils.setPosition(mal, 20) }
        oppMalInList.forEach { mal -> malMoveUtils.setPosition(mal, 20) }

        // 윷판에 있는 말은 숨기기
        malInList.forEach { mal -> mal.visibility = View.GONE }
        oppMalInList.forEach { mal -> mal.visibility = View.GONE }
    }

    // 어늘 말을 이동할지 클릭 이벤트 리스너 등록
    private fun setMalEventListener(response: GameMalResponse.GetMalMovePosition, yutResult: Int){
        // 윷판 안에 있는 말
        for(i in 0 until 4){
            val mal = malInList[i]
            if(mal.visibility != View.GONE) { // GONE이면 윷판 밖에 있는 말임.
                mal.setOnClickListener{sendMoveMal(i, yutResult)}
            }
        }

        // 윷판 밖에 있는 말
        for(i in 0 until 4){
            val mal = malOutList[i]
            if(mal.visibility != View.GONE) { // GONE이면 윷판 안에 있는 말임.
                mal.setOnClickListener{sendMoveMal(i, yutResult)}
            }
        }
    }

    // 서버와 <말 이동하기> 통신하기
    private fun sendMoveMal(malId : Int, yutResult: Int){
        gameMalStompService.sendMalMove(
            gameId = GameConstant.GAMEROOM_ID,
            playerId = playerId,
            yutResult = yutResult,
            malId = malId
        )

        removeMalEventListener() // 어느 말을 이동시킬지 결정한 이후에는 등록된 이벤트 리스너 지워야함.
    }

    // 말에 있는 클릭 이벤트 리스너 모두 지우기
    private fun removeMalEventListener(){
        malInList.forEach { mal ->
            mal.setOnClickListener{}
        }

        malOutList.forEach { mal ->
            mal.setOnClickListener{}
        }
    }

    // 말 이동하기
    public fun moveMal(response: GameMalResponse.MoveMalDTO){

        if(response.playerId == playerId){ // 내 턴인 경우
            if(response.isEnd){ // 도착한 말인지도 확인해야함

            }
            if(response.nextPosition == 0){ // 윷판 밖에 있는 말에 해당함
                malOutList[response.malId].visibility = View.VISIBLE
                malInList[response.malId].visibility = View.GONE
                return
            }

            // 윷판 밖에 있는 말 안보이게 하기
            malOutList[response.malId].visibility = View.GONE
            // 윷판에 있는 말 보이게 하기
            malInList[response.malId].visibility = View.VISIBLE

            // 말 움직이기
            malMoveUtils.move(malInList[response.malId], response.nextPosition)

            if(response.isCatchMal){ // 내가 상대방 말을 잡았을 때
                oppMalInList[response.catchMalId].visibility = View.GONE
            }
            if(response.isUpdaMal){ // 내 말을 업었을 때
                malInList[response.updaMalId].visibility = View.GONE
                when(response.point){
                    2 -> malInList[response.malId].setImageResource(R.drawable.selector_profile_w_cat_2)
                    3 -> malInList[response.malId].setImageResource(R.drawable.selector_profile_w_cat_2) // 임시로 2개 업은걸로 해둠
                    4 -> malInList[response.malId].setImageResource(R.drawable.selector_profile_w_cat_2) // 임시로 2개 업은걸로 해둠
                }

            }
        }
        else { // 상대방 턴인 경우
            if(response.isEnd){ // 도착한 말인지도 확인해야함

            }
            if(response.nextPosition == 0){ // 윷판 밖에 있는 말에 해당함
                oppMalInList[response.malId].visibility = View.GONE
                return
            }

            // 윷판에 있는 말 보이게 하기
            oppMalInList[response.malId].visibility = View.VISIBLE

            // 말 움직이기
            malMoveUtils.move(oppMalInList[response.malId], response.nextPosition)

            if(response.isCatchMal){ // 상대가 내 말을 잡았을 때
                malInList[response.catchMalId].visibility = View.GONE
                malInList[response.catchMalId].setImageResource(R.drawable.selector_profile_cat)
                malOutList[response.catchMalId].visibility = View.VISIBLE
            }
            if(response.isUpdaMal){ // 상대가 자신의 말을 업었을 때
                oppMalInList[response.updaMalId].visibility = View.GONE
                when(response.point){
                    2 -> oppMalInList[response.malId].setImageResource(R.drawable.selector_profile_w_cat_2)
                    3 -> oppMalInList[response.malId].setImageResource(R.drawable.selector_profile_w_cat_2) // 임시로 2개 업은걸로 해둠
                    4 -> oppMalInList[response.malId].setImageResource(R.drawable.selector_profile_w_cat_2) // 임시로 2개 업은걸로 해둠
                }

            }
        }
    }

}