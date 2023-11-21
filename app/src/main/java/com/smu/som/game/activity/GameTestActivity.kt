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
import com.smu.som.dialog.AnswerDialog
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_online_game.tv_nickname_p1
import kotlinx.android.synthetic.main.activity_online_game.tv_nickname_p2
import kotlinx.android.synthetic.main.dialog_set_name.btn_cancel
import kotlinx.android.synthetic.main.dialog_set_name.btn_enter
import kotlinx.android.synthetic.main.dialog_set_name.tv_title

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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnlineGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
            constant.set(bundle.getString("sender")!!, bundle.getString("gameRoomId")!!)
        }

        // 1P 이름 설정
        tv_nickname_p1.text = constant.SENDER

        var yuts = IntArray(6, { 0 })                        // 윷 결과 저장 리스트
        val soundPool = SoundPool.Builder().build()                // 게임 소리 실행 설정
        val gamesound = IntArray(8, { 0 })


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

                                    if (result?.turnChange == GameConstant.TURN_CHANGE) {
                                        btnState = !btnState
                                        binding.btnThrowYut.isEnabled = btnState
                                        setTurnChangeUI()
                                    }
                            }

                        }

                        questionTopic = stomp.join("/topic/game/question/" + constant.GAMEROOM_ID).subscribe { stompMessage ->
                            val result = Klaxon()
                                .parse<Game>(stompMessage)
                            runOnUiThread {
                                    if(result?.gameTurn == "2P")
                                        result?.questionMessage?.let { it1 -> showQuestion(it1) }
                            }

                        }

                        // 답변 결과를 받는 채널
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
                                                val answerDialog = AnswerDialog(this@GameTestActivity, question)
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
                                                            Toast.makeText(this@GameTestActivity, "답변을 입력해 주세요.", Toast.LENGTH_SHORT).show()
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

    // 질문 다이얼로그
    private fun showQuestion(question : String) {

        // 질문창
        val builder = AlertDialog.Builder(this)
        builder.setTitle("질문").setMessage(question)
            .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->
            })


        builder.setCancelable(false).show()
    }

    private fun moveCharacter(yutResult: Int) {
        // 캐릭터 말 이동 로직

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

}