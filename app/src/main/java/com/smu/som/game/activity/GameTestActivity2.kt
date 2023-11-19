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
import com.smu.som.chat.Constant
import com.smu.som.databinding.ActivityOnlineGame2Binding
import com.smu.som.databinding.ActivityOnlineGameBinding
import com.smu.som.dialog.AnswerDialog
import com.smu.som.game.GameChatActivity
import com.smu.som.game.GameConstant
import com.smu.som.game.response.Game
import com.smu.som.game.service.GameApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_online_game.btn_throw_yut
import kotlinx.android.synthetic.main.activity_online_game.player1_name
import kotlinx.android.synthetic.main.activity_online_game.player2_name
import kotlinx.android.synthetic.main.dialog_set_name.btn_cancel
import kotlinx.android.synthetic.main.dialog_set_name.btn_enter
import kotlinx.android.synthetic.main.dialog_set_name.tv_title
import kotlinx.android.synthetic.main.question_item_view.category
import okhttp3.OkHttpClient
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnlineGame2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        // 채팅방 입장 클릭 이벤트 리스너
        binding.btnChat.setOnClickListener {
            moveChatDialog(intent.getBundleExtra("myBundle"))
        }

        val intent = getIntent()
        val bundle = intent.getBundleExtra("myBundle")

        // 게임 설정 불러오기 (bundle)
        var category : String? = ""
        var adult : String? = ""
        var kcategory : String? = ""

        if (bundle != null) {
            constant.set(bundle.getString("sender")!!, bundle.getString("gameRoomId")!!)
        }

        player2_name.text = constant.SENDER


        var yuts = IntArray(6, { 0 } )                        // 윷 결과 저장 리스트
        val soundPool = SoundPool.Builder().build()                // 게임 소리 실행 설정
        val gamesound = IntArray(8, { 0 } )

        //1. STOMP init
        // url: ws://[도메인]/[엔드포인트]/ws
        val url = constant.URL
        val intervalMillis = 5000L
        val client = OkHttpClient.Builder()
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .connectTimeout(10, TimeUnit.SECONDS)
            .build()

        val stomp = StompClient(client, intervalMillis).apply { this@apply.url = url }

        // 2. connect
        stompConnection = stomp.connect()
            .subscribeOn(Schedulers.io()) // 네트워크 작업을 백그라운드 스레드에서 수행
            .observeOn(AndroidSchedulers.mainThread()) // UI 업데이트를 메인 스레드에서 수행
            .subscribe {
                when (it.type) {
                    Event.Type.OPENED -> {

                        // subscribe 채널구독
                        gametopic = stomp.join("/topic/game/room/" + constant.GAMEROOM_ID)
                            .subscribe { stompMessage ->
                                val result = Klaxon()
                                    .parse<Game>(stompMessage)
                                runOnUiThread {
                                    if (result?.messageType == GameConstant.GAME_STATE_WAIT) {
                                        binding.btnThrowYut2.isEnabled = true // 로직 완성되면 false로 바꾸기 (현재 1명 들어와있는 상태에서 테스트 하기 위함)
                                    }
                                    if (result?.messageType == GameConstant.GAME_STATE_START){
                                        binding.btnThrowYut2.isEnabled = false // 2P는 비활성화
                                        val name = result.userNameList // message에 [1P,2P] 이름이 들어있음

                                        if (name.split(",")[1] == constant.SENDER) {
                                            player1_name.text = name.split(",")[0]
                                            player2_name.text = constant.SENDER
                                        } else {
                                            player1_name.text = name.split(",")[1]
                                            player2_name.text = constant.SENDER
                                        }

                                        // category adult 설정
                                        category = result.gameCategory.split(",")[0]
                                        kcategory = result.gameCategory.split(",")[1]
                                        adult = result.gameCategory.split(",")[2]
                                        Log.d("category", category.toString())


                                    }


                                    if(result?.turnChange == GameConstant.TURN_CHANGE) {
                                        btnState = !btnState
                                        binding.btnThrowYut2.isEnabled = btnState
                                        // 말버튼
                                    }

                                    if (result?.messageType == GameConstant.QUESTION) {
                                        if(!btnState)
                                            showQuestion(result.questionMessage)
                                    }

                                }

                            }

                        throwTopic = stomp.join("/topic/game/throw/" + constant.GAMEROOM_ID).subscribe { stompMessage ->
                            val result = Klaxon()
                                .parse<Game>(stompMessage)
                            runOnUiThread {
                                yuts[0] = result?.yut!!.toInt()
                                showYutResult(yuts[0])
                            }

                        }

                        questionTopic = stomp.join("/topic/game/question/" + constant.GAMEROOM_ID).subscribe { stompMessage ->
                            val result = Klaxon()
                                .parse<Game>(stompMessage)
                            runOnUiThread {
                                if(!btnState)
                                    result?.questionMessage?.let { it1 -> showQuestion(it1) }
                            }

                        }

                        answerTopic = stomp.join("/topic/game/answer/" + constant.GAMEROOM_ID).subscribe { stompMessage ->
                            val result = Klaxon()
                                .parse<Game>(stompMessage)
                            runOnUiThread {
                                // 답변 결과
                                val builder = AlertDialog.Builder(this)
                                builder.setTitle("답변").setMessage(result?.answerMessage.toString())
                                    .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->
                                    })

                                builder.setCancelable(false).show()
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
                                                val answerDialog = AnswerDialog(this@GameTestActivity2, question)
                                                answerDialog.show()
                                                answerDialog.tv_title.text = question[0].question.toString()
                                                var answer = answerDialog.findViewById<EditText>(R.id.et_name)

                                                answerDialog.btn_cancel.setOnClickListener {
                                                    answerDialog.tv_title.text = question[1].question.toString()

                                                    jsonObject.put("messageType", "QUESTION")
                                                    jsonObject.put("gameRoomId", constant.GAMEROOM_ID)
                                                    jsonObject.put("sender", constant.SENDER)
                                                    jsonObject.put(
                                                        "questionMessage",
                                                        question[1].question.toString()
                                                    )

                                                    stomp.send("/app/game/question", jsonObject.toString())
                                                        .subscribe()
                                                }

                                                answerDialog.btn_enter.setOnClickListener {
                                                    if (answer != null) {
                                                        // 답변을 입력하지 않고 입장하기 버튼을 눌렀을 때
                                                        if (answer.text.toString() == "") {
                                                            Toast.makeText(this@GameTestActivity2, "답변을 입력해 주세요.", Toast.LENGTH_SHORT).show()
                                                            return@setOnClickListener
                                                        }
                                                        try {
                                                            jsonObject.put("messageType", "ANSWER")
                                                            jsonObject.put("gameRoomId", constant.GAMEROOM_ID
                                                            )
                                                            jsonObject.put("sender", constant.SENDER)
                                                            jsonObject.put("questionMessage", question[0].question.toString())
                                                            jsonObject.put("answerMessage", answer.text.toString())
                                                        } catch (e: JSONException) {
                                                            e.printStackTrace()
                                                        }

                                                        stomp.send("/app/game/answer", jsonObject.toString()).subscribe()
                                                        answerDialog.dismiss()
                                                    }
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

    private fun showQuestion(question : String) {

        // 질문창
        val builder = AlertDialog.Builder(this)
        builder.setTitle("질문").setMessage(question)
            .setPositiveButton("상대 플레이어 답변 중 . . .", DialogInterface.OnClickListener { dialog, id ->
            })


        builder.setCancelable(false).show()
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