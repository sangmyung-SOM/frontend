package com.smu.som.gameroom.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.smu.som.OnlineGameSettingDialog
import com.smu.som.R
import com.smu.som.dialog.SetNameDialog
import com.smu.som.gameroom.GameRoomApi
import com.smu.som.gameroom.adapter.GameRoomAdapter
import com.smu.som.gameroom.model.GameRoom
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_gameroom_list.createBtn
import kotlinx.android.synthetic.main.activity_gameroom_list.enterButton
import kotlinx.android.synthetic.main.activity_gameroom_list.recycler_gameroom
import kotlinx.android.synthetic.main.activity_gameroom_list.refresh

class  GameRoomListActivity : AppCompatActivity() {

    private val cAdapter: GameRoomAdapter by lazy {
        GameRoomAdapter(this)
    }

    private val gameSettingList = ArrayList<GameRoom>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gameroom_list)

        recycler_gameroom.adapter = cAdapter
        recycler_gameroom.layoutManager = LinearLayoutManager(this)
        recycler_gameroom.setHasFixedSize(true)

        val compositeDisposable = CompositeDisposable()
        compositeDisposable.add(
            GameRoomApi.getGameRooms()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.newThread())
            .subscribe({ response: List<GameRoom> ->
                for (item in response) {
                    cAdapter.addItem(item)
                }

            }, { error: Throwable ->
                error.localizedMessage?.let { Log.d("GameRoom: ", it) }
            }))

        enterButton.isEnabled = false

        createBtn.setOnClickListener {
            val dialog = OnlineGameSettingDialog(this)
            dialog.show()
        }

        refresh.setOnClickListener {
            finish() //인텐트 종료
            overridePendingTransition(0, 0) //인텐트 효과 없애기
            val intent = intent //인텐트
            startActivity(intent) //액티비티 열기
            overridePendingTransition(0, 0) //인텐트 효과 없애기
            Log.d("refresh", "refresh")
        }

        enterButton.setOnClickListener {
            val dialog : SetNameDialog = SetNameDialog(this, gameSettingList)
            dialog.show()

        }

        // 방 목록에서 방을 선택했을 때
        cAdapter.setOnItemClickListener(object : GameRoomAdapter.OnItemClickListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onItemClick(v: GameRoomAdapter.Holder, data: GameRoom, pos: Int) {
                Log.d("onItemClick", "onItemClick")
                Log.d("isActivated - 1", v.roomItem.isActivated.toString())
                // 방을 선택하면 선택된 방의 isActivated 값이 true로 바뀜
                v.roomItem.isActivated = !v.roomItem.isActivated

                // 방이 선택되었을 때 enterButton 활성화
                enterButton.isEnabled = v.roomItem.isActivated

                // 선택된 방의 position을 selectItems에 저장
                if(v.roomItem.isActivated) {
                    cAdapter.selectItems.add(pos)
                    gameSettingList.add(data)
                } else {
                    cAdapter.selectItems.remove(pos)
                    gameSettingList.remove(data)
                }

                // 선택된 방이 1개 이상일 때
                if (cAdapter.selectItems.size > 1 ) {
                    // 방이 선택되었을 때 enterButton 활성화
                    enterButton.isEnabled = v.roomItem.isActivated
                    // 방이 1개 이상일 때 방을 선택하면 선택된 방의 isActivated 값이 false로 바뀜
                    v.roomItem.isActivated = !v.roomItem.isActivated
                    // 선택된 방의 position을 selectItems에서 제거
                    cAdapter.selectItems.remove(pos)
                    gameSettingList.remove(data)
                }

                Log.d("selectItems", cAdapter.selectItems.toString())
                Log.d("gameSettingList", gameSettingList.toString())
            }
        })

    }


}