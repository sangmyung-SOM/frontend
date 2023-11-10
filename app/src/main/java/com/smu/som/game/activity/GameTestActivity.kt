package com.smu.som.game.activity

import android.content.Intent
import android.graphics.drawable.Drawable
import android.media.SoundPool
import android.os.Bundle

import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import com.beust.klaxon.Klaxon
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.gmail.bishoybasily.stomp.lib.Event
import com.gmail.bishoybasily.stomp.lib.StompClient
import com.google.gson.GsonBuilder

import com.smu.som.R
import com.smu.som.chat.Constant

import com.smu.som.databinding.ActivityOnlineGameBinding
import com.smu.som.game.GameChatActivity
import com.smu.som.game.GameConstant
import com.smu.som.game.response.Game
import com.smu.som.game.service.GameApi
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_online_game.btn_throw_yut
import kotlinx.android.synthetic.main.activity_online_game.player1_name
import kotlinx.android.synthetic.main.activity_online_game.player2_name
import okhttp3.OkHttpClient
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import com.bumptech.glide.request.target.Target

class GameTestActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnlineGameBinding

    var jsonObject = JSONObject()

    lateinit var stompConnection: Disposable
    lateinit var topic: Disposable
    lateinit var gametopic: Disposable
    lateinit var gameTurn: Disposable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnlineGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 채팅방 입장 클릭 이벤트 리스너
        binding.btnChat.setOnClickListener {
            moveChatDialog(intent.getBundleExtra("myBundle"))
        }

        binding.btnThrowYut.isEnabled = false


        val intent = getIntent()
        val bundle = intent.getBundleExtra("myBundle")
        val constant: Constant = Constant
        if (bundle != null) {
            constant.set(bundle.getString("sender")!!, bundle.getString("chatRoomId")!!)
        }

        val gameConstant: GameConstant = GameConstant
        // 연결된 클라이언트 저장 배열
        val clients = ArrayList<String>()

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

        stompConnection = stomp.connect().subscribe {
            when (it.type) {
                Event.Type.OPENED -> {

                    // subscribe 채널구독
                    gametopic = stomp.join("/topic/game/room/" + constant.CHATROOM_ID)
                        .subscribe { stompMessage ->
                            val result = Klaxon()
                                .parse<Game>(stompMessage)
                            runOnUiThread {
                                if (result?.gameTurn == GameConstant.GAME_1P) {
                                    player1_name.text = result?.sender
                                    gameConstant.set(result?.sender!!, result?.gameTurn!!)

                                } else if (result?.gameTurn == GameConstant.GAME_2P) {
                                    player2_name.text = result?.sender
                                    gameConstant.set(result?.sender!!, result?.gameTurn!!)
                                }

                                if (result?.messageType == GameConstant.GAME_STATE_WAIT) {
                                   binding.btnThrowYut.isEnabled = true // 로직 완성되면 false로 바꾸기 (현재 1명 들어와있는 상태에서 테스트 하기 위함)
                                }
                                if (result?.messageType == GameConstant.GAME_STATE_START){
                                    binding.btnThrowYut.isEnabled = true
                                }


                                // 1P가 던진 윷 결과
                                if(result?.messageType == GameConstant.GAME_STATE_THROW
                                    && result?.gameState == GameConstant.GAME_TURN_1P) {
                                    yuts[0] = result?.message!!.toInt()
                                    showYutResult(yuts[0])

                                }

                                // 2P가 던진 윷 결과
                                if(result?.messageType == GameConstant.GAME_STATE_THROW
                                    && result?.gameState == GameConstant.GAME_TURN_2P) {
                                    yuts[1] = result?.message!!.toInt()
                                    showYutResult(yuts[1])
                                }

                            }

                        }

                    // 처음 입장
                    try {
                        jsonObject.put("messageType", "WAIT")
                        jsonObject.put("chatRoomId", constant.CHATROOM_ID)
                        jsonObject.put("sender", constant.SENDER)
                        jsonObject.put("turn", GameConstant.GAME_TURN)
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                    stomp.send("/app/game/message", jsonObject.toString()).subscribe()


                    btn_throw_yut.setOnClickListener() {
                        var num = playGame(soundPool, gamesound, yuts.sum())

                        try {
                            jsonObject.put("messageType", "THROW")
                            jsonObject.put("chatRoomId", constant.CHATROOM_ID)
                            jsonObject.put("sender", constant.SENDER)
                            jsonObject.put("message", "$num")
                            jsonObject.put("turn", GameConstant.GAME_TURN)

                            if(GameConstant.GAME_TURN == GameConstant.GAME_1P) {
                                jsonObject.put("gameState", GameConstant.GAME_TURN_1P)
                            } else if(GameConstant.GAME_TURN == GameConstant.GAME_2P) {
                                jsonObject.put("gameState", GameConstant.GAME_TURN_2P)
                            }


                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }

                        stomp.send("/app/game/message", jsonObject.toString()).subscribe()

                    }


                }

                Event.Type.CLOSED -> {


                }

                Event.Type.ERROR -> {

                }
            }
        }
    }

    private fun showYutResult(num: Int) {
        val gifImageView = findViewById<ImageView>(R.id.gifImageView)

        gifImageView.visibility = View.VISIBLE
        when(num) {
            1 -> {
                // Drawable 리소스에 있는 GIF 파일을 로딩하여 표시
                var gifResourceId = R.drawable.yut_1
                gifImageView(gifResourceId)
            }
            2 -> {
                var gifResourceId = R.drawable.yut_2
                gifImageView(gifResourceId)
            }
            3 -> {
                var gifResourceId = R.drawable.yut_3
                gifImageView(gifResourceId)
            }
            4 -> {
                var gifResourceId = R.drawable.yut_4
                gifImageView(gifResourceId)
            }
            5 -> {
                var gifResourceId = R.drawable.yut_5
                gifImageView(gifResourceId)
            }
            0 -> {
                var gifResourceId = R.drawable.yut_0
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

                    binding.btnThrowYut.isEnabled = yutBtnState

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