package com.smu.som.game.activity

import android.content.ContentValues
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
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import com.beust.klaxon.Klaxon
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.gmail.bishoybasily.stomp.lib.Event
import com.gmail.bishoybasily.stomp.lib.StompClient

import com.smu.som.R

import com.smu.som.databinding.ActivityOnlineGameBinding
import com.smu.som.game.GameChatActivity
import com.smu.som.game.GameConstant
import com.smu.som.game.response.Game
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_online_game.btn_throw_yut

import okhttp3.OkHttpClient
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.TimeUnit
import com.bumptech.glide.request.target.Target
import com.smu.som.MasterApplication
import com.smu.som.Question
import com.smu.som.game.service.GameMalStompService
import java.util.Stack
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

import com.smu.som.game.dialog.AnsweringDialog
import com.smu.som.game.dialog.GetAnswerResultDialog
import com.smu.som.game.dialog.GetQuestionDialog
import com.smu.som.game.response.GameMalResponse
import com.smu.som.game.service.GameMalService
import com.smu.som.game.service.MalMoveUtils

import kotlinx.android.synthetic.main.activity_online_game.tv_nickname_p1
import kotlinx.android.synthetic.main.activity_online_game.tv_nickname_p2

class GameTestActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnlineGameBinding

    var jsonObject = JSONObject()

    private lateinit var stompConnection: Disposable
    private lateinit var topic: Disposable
    private lateinit var gametopic: Disposable
    private lateinit var answerTopic: Disposable
    private lateinit var questionTopic: Disposable
    private lateinit var throwTopic: Disposable

    private var btnState : Boolean = true

    val constant: GameConstant = GameConstant

    //1. STOMP init
    // url: ws://[도메인]/[엔드포인트]/ws
    private val url = constant.URL

    private val intervalMillis = 5000L
    private val client = OkHttpClient.Builder()
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .connectTimeout(10, TimeUnit.SECONDS)
        .build()

    private val stomp = StompClient(client, intervalMillis)

    // 가나가 필요해서 정의한 변수
    private val playerId : String = "1P" // 고정값
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
        stomp.url = constant.URL
    }

    val SIZE = 30                                              // 윷판 크기
    var arr = IntArray(SIZE, { 0 } )                           // 윷판 리스트 (각 자리의 말 수 저장)
    var yuts = IntArray(6, { 0 } )                        // 윷 결과 저장 리스트
    var players: ArrayList<TextView> = ArrayList()             // 윷판의 TextView 리스트 (화면)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnlineGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 이렇게 안하면, 뷰가 다 그려지지 않은 시점에서 malInit이 호출돼 초기화가 제대로 안됨.
        binding.yutBoard.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                binding.yutBoard.viewTreeObserver.removeOnGlobalLayoutListener(this)
                // 말 초기화
                malInit()
            }
        })

        // 말 추가하기 버튼을 눌렀을 때 이벤트 리스너
        binding.btnAddToken.setOnClickListener{
            gameMalStompService.sendMalNextPosition(GameConstant.GAMEROOM_ID, playerId, yutResultStack.peek())

            // [가나] 말 이동 테스트 - 지우지 말아주세요
//            malInList[0].visibility = View.VISIBLE
//            malMoveUtils.move(malInList[0], count)
//            count++
//            if(count == 28){
//                count++
//            }
        }

        // 채팅방 입장 클릭 이벤트 리스너
        binding.btnChat.setOnClickListener {
            moveChatDialog(intent.getBundleExtra("myBundle"))
        }
        val bundle = intent.getBundleExtra("myBundle")

        // 게임 설정 불러오기 (bundle)
        val category = bundle?.getString("category")    // API 요청 시 필요한 카테고리 (영어)
        var kcategory = bundle?.getString("kcategory")   // 사용자에게 보여질 카테고리 (한글)
        val adult = bundle?.getString("adult")          // 성인 여부

        settingCategory(kcategory, adult)

        if (bundle != null) {
            constant.set(bundle.getString("sender")!!, bundle.getString("gameRoomId")!!, "1P")
        }

        // 1P 이름 설정
        tv_nickname_p1.text = constant.SENDER

        var yuts = IntArray(6, { 0 })                        // 윷 결과 저장 리스트
        val soundPool = SoundPool.Builder().build()                // 게임 소리 실행 설정
        val gamesound = IntArray(8, { 0 })



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

                        // subscribe 채널구독
                        gametopic = stomp.join("/topic/game/room/" + constant.GAMEROOM_ID)
                            .subscribe { stompMessage ->
                                val result = Klaxon()
                                    .parse<Game>(stompMessage)
                                runOnUiThread {

                                    if (result?.messageType == GameConstant.GAME_STATE_WAIT) {
                                        binding.btnThrowYut.isEnabled = true // 로직 완성되면 false로 바꾸기 (현재 1명 들어와있는 상태에서 테스트 하기 위함)
                                        binding.viewProfileP1.setBackgroundResource(R.drawable.pick)
                                        binding.profileImgCatP1.isEnabled = true
                                        binding.profileImgCatP2.isEnabled = false

                                    }
                                    if (result?.messageType == GameConstant.GAME_STATE_START) {
                                        binding.btnThrowYut.isEnabled = true
                                        val name = result.userNameList // message에 [1P,2P] 이름이 들어있음

                                        if (name.split(",")[0] == constant.SENDER) {
                                            tv_nickname_p1.text = constant.SENDER
                                            tv_nickname_p2.text = name.split(",")[1]
                                        } else {
                                            tv_nickname_p1.text = name.split(",")[1]
                                            tv_nickname_p2.text = constant.SENDER
                                        }

                                        binding.viewProfileP1.setBackgroundResource(R.drawable.pick)

                                    }

                                }

                            }
                        throwTopic = stomp.join("/topic/game/throw/" + constant.GAMEROOM_ID).subscribe { stompMessage ->
                            val result = Klaxon()
                                .parse<Game>(stompMessage)
                            runOnUiThread {
                                    yuts[0] = result?.yut!!.toInt()
                                    showYutResult(yuts[0])

                                // 로직 완성되면 주석 풀기
//                                    if (result?.turnChange == GameConstant.TURN_CHANGE) {
//                                        btnState = !btnState
//                                        binding.btnThrowYut.isEnabled = btnState
//                                        setTurnChangeUI()
//                                    }
                            }

                        }

                        questionTopic = stomp.join("/topic/game/question/" + constant.GAMEROOM_ID).subscribe { stompMessage ->
                            val result = Klaxon()
                                .parse<Game>(stompMessage)
                            runOnUiThread {
                                    if(result?.gameTurn == "2P") {
                                        val questionMessage = result?.questionMessage
                                        val questionView = GetQuestionDialog(this, questionMessage)
                                        questionView.showPopup()
                                        // 새로운 질문이 들어오면 기존의 질문 다이얼로그는 dismiss
                                        if (questionView.isShowing) {
                                            questionView.dismiss()
                                        }
                                    }
                            }

                        }

                        // 답변 결과를 받는 채널
                        answerTopic = stomp.join("/topic/game/answer/" + constant.GAMEROOM_ID).subscribe { stompMessage ->
                            val result = Klaxon()
                                .parse<Game>(stompMessage)
                            runOnUiThread {
                                val answer = result?.answerMessage
                                val answerResult = GetAnswerResultDialog(this, answer!!)
                                answerResult.showPopup()
                            }

                        }

                        // 말 추가 결과를 받는 채널 ( 말 추가 버튼을 누른 경우 )
                        /*
                        topic = stomp.join("/topic/game/mal/" + constant.GAMEROOM_ID).subscribe { stompMessage ->
                            val result = Klaxon()
                                .parse<Mal>(stompMessage)
                            runOnUiThread {
                                val yut = result?.yutResult
                                if (yut != null) {
                                    arr[yut.toInt()] += 1 // 말 추가
                                }

                                // 말판 UI 변경 -> arr[윷 결과] = 말 수
                                for ((index,item) in arr.withIndex()) {
                                    Log.d("arr", "$index : $item")
                                    if (index!=0) {
                                        // 말판의 TextView 찾기
                                        var player: TextView = findViewById(getResources().getIdentifier("board" + index, "id", packageName))
                                        var pick: LinearLayout = findViewById(getResources().getIdentifier("pick" + index, "id", packageName))
                                        pick.setBackgroundResource(R.drawable.nopick)

                                        if (item!=0) {          // 말이 있는 경우
                                            if (item > 0)       // 1P 말
                                                player.setBackgroundResource(resources.getIdentifier(String.format("player_%d_%d", item, 1), "drawable", packageName))
                                            else                // 2P 말
                                                player.setBackgroundResource(resources.getIdentifier(String.format("player_%d_%d",
                                                    Math.abs(item), 1),"drawable", packageName))
                                        }
                                        else                    // 말이 없는 경우
                                            player.setBackgroundResource(R.drawable.board)
                                    }
                                }
//                                for ((index,item) in arr.withIndex())
//                                    if (item!=0 && index!=0)
//                                        players[index]?.setOnClickListener(null)

                            }

                        }
                        */


                        // 처음 입장
                        try {
                            jsonObject.put("messageType", "WAIT")
                            jsonObject.put("gameRoomId", constant.GAMEROOM_ID)
                            jsonObject.put("sender", constant.SENDER)
                            jsonObject.put("turn", "1P")
                            jsonObject.put("gameCategory", "$category,$kcategory,$adult")
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                        stomp.send("/app/game/message", jsonObject.toString()).subscribe()


                        // 윷 던지기 버튼 클릭 이벤트
                        var throwCount = 0
                        btn_throw_yut.setOnClickListener() {
                            var num = playGame(soundPool, gamesound, yuts.sum())
                            yutResultStack.push(num) // 가나-임시로 윷 결과값 저장
                            throwCount++
                            if (throwCount == 1) {
                                try {
                                    jsonObject.put("messageType", "FIRST_THROW")
                                    jsonObject.put("gameRoomId", constant.GAMEROOM_ID)
                                    jsonObject.put("sender", constant.SENDER)
                                    jsonObject.put("yut", "$num")
                                    jsonObject.put("turn", "1P")
                                } catch (e: JSONException) {
                                    e.printStackTrace()
                                }
                            } else {
                                try {
                                    jsonObject.put("messageType", "THROW")
                                    jsonObject.put("gameRoomId", constant.GAMEROOM_ID)
                                    jsonObject.put("sender", constant.SENDER)
                                    jsonObject.put("yut", "$num")
                                    jsonObject.put("turn", "1P")

                                } catch (e: JSONException) {
                                    e.printStackTrace()
                                }
                            }

                            stomp.send("/app/game/throw", jsonObject.toString()).subscribe()

                            // 윷이나 모가 아닌 경우
                            if (num != 4 && num != 5) {
                                // API 로 질문을 받는 함수
                                (application as MasterApplication).service.getQuestion(
                                    category!!, adult!!
                                ).enqueue(object : Callback<ArrayList<Question>> {
                                    // 성공
                                    override fun onResponse(
                                        call: Call<ArrayList<Question>>,
                                        response: Response<ArrayList<Question>>
                                    )
                                    {
                                        if (response.isSuccessful) {
                                            val question = response.body()
                                            val questionId = question?.get(0)!!.id

                                            jsonObject.put("messageType", "QUESTION")
                                            jsonObject.put("gameRoomId", constant.GAMEROOM_ID)
                                            jsonObject.put("sender", constant.SENDER)
                                            jsonObject.put("questionMessage", question[0].question.toString())

                                            stomp.send("/app/game/question", jsonObject.toString())
                                                .subscribe()

                                            // git 모션 끝나면 질문 다이얼로그 띄우기
                                            Handler(Looper.getMainLooper()).postDelayed({
                                                val answeringDialog = AnsweringDialog(this@GameTestActivity, question, stomp)
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

                        // 말 추가 버튼 클릭 이벤트

                    }

                    Event.Type.CLOSED -> {


                    }

                    Event.Type.ERROR -> {

                    }
                }
            }
    }

    private fun setTurnChangeUI() {
        Handler(Looper.getMainLooper()).postDelayed({
            if (btnState) // true : 1P 차례
             {
                binding.viewProfileP1.setBackgroundResource(R.drawable.pick)
                binding.viewProfileP2.setBackgroundResource(R.color.game_dark_brown)
            }
            else {
                binding.viewProfileP2.setBackgroundResource(R.drawable.pick)
                binding.viewProfileP1.setBackgroundResource(R.color.game_dark_brown)
            }
            binding.profileImgCatP1.isEnabled = !binding.profileImgCatP1.isEnabled
            binding.profileImgCatP2.isEnabled = !binding.profileImgCatP2.isEnabled
        }, 4000)
    }

    // 카테고리 설정에 따른 UI 변경
    private fun settingCategory(kcategory: String?, adult: String?) {

        when(kcategory) {
            "연인" -> setImage(R.drawable.couple)
            "부부" -> setImage(R.drawable.married)
            "부모자녀" -> setImage(R.drawable.parent)
        }
        binding.imgGameSettingAdult.isEnabled = adult == "ON"
    }
    private fun setImage(image: Int) {
        binding.imgGameSettingCategory.setImageResource(image)
    }

    private fun moveCharacter(yutResult: Int) {
        // 캐릭터 말 이동 로직

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

    // 채팅방 입장 버튼 클릭 이벤트
    private fun moveChatDialog(bundle: Bundle?) {
        val intent = Intent(this, GameChatActivity::class.java)
        intent.putExtra("myBundle", bundle)
        startActivity(intent)
    }

    private fun playGame(soundPool: SoundPool, gamesound: IntArray, sum: Int): Int {
        val yuts = arrayOf("빽도", "도", "개", "걸", "윷", "모")
        var num = percentage(sum)       // 윷 결과

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
        malInList = arrayOf(binding.malBlack0, binding.malBlack1, binding.malBlack2, binding.malBlack3)
        malOutList = arrayOf(binding.malOutBlack0, binding.malOutBlack1, binding.malOutBlack2, binding.malOutBlack3)
        oppMalInList = arrayOf(binding.malWhite0, binding.malWhite1, binding.malWhite2, binding.malWhite3)

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