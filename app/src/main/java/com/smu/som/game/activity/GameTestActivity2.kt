package com.smu.som.game.activity

import android.content.ContentValues
import android.content.DialogInterface
import android.content.Intent
import android.graphics.drawable.Drawable
import android.media.SoundPool
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import com.beust.klaxon.Klaxon
import com.bumptech.glide.Glide
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
import com.smu.som.game.dialog.GetAnswerResultDialog
import com.smu.som.game.dialog.GetQuestionDialog
import com.smu.som.game.response.Game
import com.smu.som.game.service.GameApi
import com.smu.som.game.service.GameMalService
import com.smu.som.game.service.GameMalStompService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_online_game.tv_nickname_p1
import kotlinx.android.synthetic.main.activity_online_game.tv_nickname_p2
import kotlinx.android.synthetic.main.dialog_set_name.btn_cancel
import kotlinx.android.synthetic.main.dialog_set_name.btn_enter
import kotlinx.android.synthetic.main.dialog_set_name.tv_title
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

    private val playerId : String = "2P" // 고정값
    private val yutResultStack : Stack<Int> = Stack() // 윷 결과들 저장. 임시로 쓰는거라 stack으로 만들어둠
    private var gameMalStompService: GameMalStompService = GameMalStompService(stomp)
    private val gameMalService: GameMalService = GameMalService()

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

        // 말 추가하기 버튼을 눌렀을 때 이벤트 리스너
        binding.btnAddToken2.setOnClickListener{
            gameMalStompService.sendMal(GameConstant.GAMEROOM_ID, playerId, yutResultStack.peek())
        }

        val intent = getIntent()
        val bundle = intent.getBundleExtra("myBundle")

        // 게임 설정 불러오기 (bundle)
        var category : String? = ""
        var adult : String? = ""
        var kcategory : String? = ""

        if (bundle != null) {
            constant.set(bundle.getString("sender")!!, bundle.getString("gameRoomId")!!, "2P")
        }

        tv_nickname_p2.text = constant.SENDER


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
                                { success -> Log.i("som-gana", success) },
                                { throwable -> Log.i("som-gana", "왜 실패야ㅠㅠ") }
                            )

                        // subscribe 채널구독
                        gametopic = stomp.join("/topic/game/room/" + constant.GAMEROOM_ID)
                            .subscribe { stompMessage ->
                                val result = Klaxon()
                                    .parse<Game>(stompMessage)
                                runOnUiThread {
                                    if (result?.messageType == GameConstant.GAME_STATE_WAIT) {
                                        binding.btnThrowYut2.isEnabled = true // 로직 완성되면 false로 바꾸기 (현재 1명 들어와있는 상태에서 테스트 하기 위함)
                                        binding.viewProfileP1.setBackgroundResource(R.drawable.pick)
                                        binding.profileImgCatP1.isEnabled = true
                                        binding.profileImgCatP2.isEnabled = false


                                        if (result.answerMessage == "1P가 들어오지 않았습니다.") {
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
                                        binding.btnThrowYut2.isEnabled = false // 2P는 비활성화
                                        val name = result.userNameList // message에 [1P,2P] 이름이 들어있음

                                        if (name.split(",")[1] == constant.SENDER) {
                                            tv_nickname_p1.text = name.split(",")[0]
                                            tv_nickname_p2.text = constant.SENDER
                                        } else {
                                            tv_nickname_p1.text = name.split(",")[1]
                                            tv_nickname_p2.text = constant.SENDER
                                        }

                                        // category adult 설정
                                        category = result.gameCategory.split(",")[0]
                                        kcategory = result.gameCategory.split(",")[1]
                                        adult = result.gameCategory.split(",")[2]

                                        settingCategory(kcategory, adult)

                                    }

                                }

                            }

                        throwTopic = stomp.join("/topic/game/throw/" + constant.GAMEROOM_ID).subscribe { stompMessage ->
                            val result = Klaxon()
                                .parse<Game>(stompMessage)
                            runOnUiThread {
                                yuts[0] = result?.yut!!.toInt()
                                showYutResult(yuts[0])

                                if(result?.turnChange == GameConstant.TURN_CHANGE) {
                                    btnState = !btnState
                                    binding.btnThrowYut2.isEnabled = btnState
                                    setTurnChangeUI()
                                    // 말버튼
                                }
                            }

                        }

                        questionTopic = stomp.join("/topic/game/question/" + constant.GAMEROOM_ID).subscribe { stompMessage ->
                            val result = Klaxon()
                                .parse<Game>(stompMessage)
                            runOnUiThread {
                                if(result?.gameTurn == "1P") {
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

                        answerTopic = stomp.join("/topic/game/answer/" + constant.GAMEROOM_ID).subscribe { stompMessage ->
                            val result = Klaxon()
                                .parse<Game>(stompMessage)
                            runOnUiThread {
                                // 답변 결과
                                val answer = result?.answerMessage
                                val answerResult = GetAnswerResultDialog(this, answer!!)
                                answerResult.showPopup()
                            }

                        }

                        // 처음 입장
                        try {
                            jsonObject.put("messageType", "WAIT")
                            jsonObject.put("gameRoomId", constant.GAMEROOM_ID)
                            jsonObject.put("sender", constant.SENDER)
                            jsonObject.put("turn", "2P")
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
                            if(throwCount == 1) {
                                try {
                                    jsonObject.put("messageType", "FIRST_THROW")
                                    jsonObject.put("gameRoomId", constant.GAMEROOM_ID)
                                    jsonObject.put("sender", constant.SENDER)
                                    jsonObject.put("yut", "$num")
                                    jsonObject.put("turn", "2P")
                                } catch (e: JSONException) {
                                    e.printStackTrace()
                                }
                            }
                            else {
                                try {
                                    jsonObject.put("messageType", "THROW")
                                    jsonObject.put("gameRoomId", constant.GAMEROOM_ID)
                                    jsonObject.put("sender", constant.SENDER)
                                    jsonObject.put("yut", "$num")
                                    jsonObject.put("turn", "2P")

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

                                            jsonObject.put("messageType", "QUESTION")
                                            jsonObject.put("gameRoomId", constant.GAMEROOM_ID)
                                            jsonObject.put("sender", constant.SENDER)
                                            jsonObject.put(
                                                "questionMessage",
                                                question[0].question.toString()
                                            )

                                            stomp.send("/app/game/question", jsonObject.toString())
                                                .subscribe()

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

                        // 말버튼 클릭 리스터



                    }

                    Event.Type.CLOSED -> {


                    }

                    Event.Type.ERROR -> {

                    }
                }
            }
    }

    private fun setTurnChangeUI() {
        if (!btnState) // true : 1P 차례
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
    }

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

    private fun showYutResult(num: Int) {
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




}