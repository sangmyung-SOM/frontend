package com.smu.som.game.activity

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.beust.klaxon.Klaxon
import com.gmail.bishoybasily.stomp.lib.Event
import com.gmail.bishoybasily.stomp.lib.StompClient
import com.smu.som.R
import com.smu.som.chat.Constant
import com.smu.som.chat.adapter.ChatAdapter
import com.smu.som.chat.model.response.Chat
import com.smu.som.game.GameConstant
import com.smu.som.gameroom.model.api.GameRoomApi
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_chat.*
import okhttp3.OkHttpClient
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response
import java.util.concurrent.TimeUnit

class GameChatActivity : AppCompatActivity() {

    lateinit var cAdapter: ChatAdapter

    var jsonObject = JSONObject()

    lateinit var stompConnection: Disposable
    lateinit var topic: Disposable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_chat)

        val bundle = intent.getBundleExtra("myBundle")
        val constant: Constant = Constant
        if(bundle != null) {
            constant.set(bundle.getString("sender")!!, bundle.getString("gameRoomId")!!)
        }

        cAdapter = ChatAdapter(this)
        recycler_chat.adapter = cAdapter
        recycler_chat.layoutManager = LinearLayoutManager(this)
        recycler_chat.setHasFixedSize(true)

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
                    // 메세지 받아오기
                    topic = stomp.join("/topic/chat/room/" + constant.CHATROOM_ID).subscribe{
                        stompMessage ->
                        val result = Klaxon()
                                .parse<Chat>(stompMessage)
                        runOnUiThread {
                            if (result != null) {
                                cAdapter.addItem(result)
                                recycler_chat.smoothScrollToPosition(cAdapter.itemCount)
                            }
                        }
                    }

                    // 처음 입장
                    try {
                        jsonObject.put("messageType", "ENTER")
                        jsonObject.put("chatRoomId", constant.CHATROOM_ID)
                        jsonObject.put("sender", constant.SENDER)
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                    stomp.send("/app/chat/message", jsonObject.toString()).subscribe()

                    val api = retrofit2.Retrofit.Builder()
                        .baseUrl(GameConstant.API_URL)
                        .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
                        .build()
                        .create(GameRoomApi::class.java)

                        api.getChatLogs(GameConstant.GAMEROOM_ID)
                        .enqueue(object : retrofit2.Callback<ArrayList<Chat>>{
                        override fun onResponse(
                            call: Call<ArrayList<Chat>>,
                            response: Response<ArrayList<Chat>>
                        ) {
                            if (response.isSuccessful){
                                val chats = response.body()
                                if (chats != null) {
                                    for (chat in chats!!){
                                        cAdapter.addItem(chat)
                                        recycler_chat.smoothScrollToPosition(cAdapter.itemCount)
                                    }
                                }
                            }
                            else {
                                Log.e(ContentValues.TAG, "이전 대화 목록 불러오기 실패. 응답 코드: ${response.code()}")
                            }
                        }

                        override fun onFailure(call: Call<ArrayList<Chat>>, t: Throwable) {
                            Log.e(ContentValues.TAG, "서버 오류")
                        }
                    })
                    //채팅기록이 있다면 불러오기

                    send.setOnClickListener {
                        try {
                            jsonObject.put("messageType", "TALK")
                            jsonObject.put("chatRoomId", constant.CHATROOM_ID)
                            jsonObject.put("sender", constant.SENDER)
                            jsonObject.put("message", message.text.toString())
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                        // send
                        stomp.send("/app/chat/message", jsonObject.toString()).subscribe()
                        message.text = null
                    }
                    // unsubscribe
                    //topic.dispose()
                }
                Event.Type.CLOSED -> {

                }
                Event.Type.ERROR -> {

                }
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        stompConnection.dispose()
        topic.dispose()
    }
}