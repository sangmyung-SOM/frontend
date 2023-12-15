package com.smu.som.game

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.gmail.bishoybasily.stomp.lib.StompClient
import com.smu.som.gameroom.GameRoomApi
import okhttp3.OkHttpClient
import org.json.JSONException
import org.json.JSONObject
import java.util.concurrent.TimeUnit


class TaskService : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        Log.i("TaskService", "앱 종료")

        if (GameConstant.GAMEROOM_ID != "0") {
            // 게임 방 삭제
            val gameRoomApi = GameRoomApi
            gameRoomApi.deleteGameRoom(GameConstant.GAMEROOM_ID).enqueue(object : retrofit2.Callback<Void> {
                override fun onResponse(call: retrofit2.Call<Void>, response: retrofit2.Response<Void>) {
                    Log.d("deleteGameRoom", "success")
                }

                override fun onFailure(call: retrofit2.Call<Void>, t: Throwable) {
                    Log.d("deleteGameRoom", "fail")
                }
            })
        }



        super.onTaskRemoved(rootIntent)
        stopSelf()
    }
}