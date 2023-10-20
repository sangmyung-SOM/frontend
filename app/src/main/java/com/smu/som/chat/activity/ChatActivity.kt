package com.smu.som.chat.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.beust.klaxon.Klaxon

import com.gmail.bishoybasily.stomp.lib.Event
import com.gmail.bishoybasily.stomp.lib.StompClient
import com.smu.som.R
import com.smu.som.chat.Constant
import com.smu.som.chat.adapter.ChatAdapter
import com.smu.som.chat.model.response.Chat
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_chat.*
import okhttp3.OkHttpClient
import org.json.JSONException
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class ChatActivity : AppCompatActivity() {

    lateinit var cAdapter: ChatAdapter

    var jsonObject = JSONObject()

    lateinit var stompConnection: Disposable
    lateinit var topic: Disposable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val intent = getIntent()
        val bundle = intent.getBundleExtra("myBundle")
        val constant: Constant = Constant
        if(bundle != null) {
            constant.set(bundle.getString("sender")!!, bundle.getString("chatRoomId")!!)
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
}