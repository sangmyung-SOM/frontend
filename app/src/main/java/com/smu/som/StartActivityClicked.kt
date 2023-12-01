package com.smu.som

import android.app.Dialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.kakao.sdk.talk.TalkApiClient
import com.kakao.sdk.talk.model.TalkProfile
import com.kakao.sdk.user.UserApiClient
import com.kakao.sdk.user.model.Profile
import com.smu.som.dialog.FindGameRoomDialog
import com.smu.som.gameroom.activity.GameRoomListActivity
import kotlinx.android.synthetic.main.activity_start.offlineStart
import kotlinx.android.synthetic.main.activity_start_clicked.*
import java.lang.reflect.Executable
import java.net.URL
import java.util.concurrent.Executors

class StartActivityClicked : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_clicked)

        // 카카오톡 프로필 업데이트
        var url : String? = ""
        val profileImageView = findViewById<ImageView>(R.id.profile)
        var userName : String? = ""

        // 카카오톡 프로필 가져오기
        TalkApiClient.instance.profile { profile, error ->
            if (error != null) {
                Log.e(TAG, "카카오톡 프로필 가져오기 실패", error)
            }
            else if (profile != null) {
                Log.i(TAG, "카카오톡 프로필 가져오기 성공" +
                        "\n닉네임: ${profile.nickname}" +
                        "\n프로필사진: ${profile.thumbnailUrl}")

                val profile: TalkProfile? = profile
                val profileImageUrl: String? = profile?.thumbnailUrl
                userName = profile?.nickname
                url = profileImageUrl

                if (!profileImageUrl.isNullOrEmpty()) {
                    Glide.with(this)
                        .load(profileImageUrl)
                        .error(R.drawable.profile)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(profileImageView)
                } else {
                    profileImageView.setImageResource(R.drawable.profile)
                }
            }
        }



        offlineStart.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        explain_btn.setOnClickListener {
            showPopup()
        }

        onlineStart.setOnClickListener {
            // 이동할 액티비티 위에 있는 액티비티 모두 삭제
            // A-B-C-D 이렇게 네개의 액티비티가 스택에 쌓여있을 경우 B를 호출하게 되면 B위에 쌓여있던 C, D는 제거 되고 A와 B만 남게된다.
            val intent = Intent(this, GameRoomListActivity::class.java)
            intent.putExtra("profileUrl", url)
            intent.putExtra("userName", userName)
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            finish()
        }
    }
    

    // 게임 설명을 보여주는 함수
    fun showPopup() {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view1 = inflater.inflate(R.layout.activity_gamerule1, null)
        val alertDialog1 = AlertDialog.Builder(this)
            .setTitle("게임 방법")
            .setPositiveButton("다음") { dialog, which ->
                showSecondPage(inflater) // 설명 2페이지를 보여주는 함수 호출
            }
            .setNegativeButton("취소", null)
        alertDialog1.setView(view1)
        alertDialog1.setCancelable(false).show()
    }

    fun showSecondPage(inflater: LayoutInflater) {
        val view2 = inflater.inflate(R.layout.activity_gamerule2, null)
        val alertDialog2 = AlertDialog.Builder(this).setTitle("게임 방법")
            .setPositiveButton("확인", null)
            .setNegativeButton("이전") { dialog, which ->
                showFirstPage(inflater) // 이전 설명 페이지를 보여주는 함수 호출
            }
        alertDialog2.setView(view2)
        alertDialog2.setCancelable(false).show()
    }

    fun showFirstPage(inflater: LayoutInflater) {
        val view1 = inflater.inflate(R.layout.activity_gamerule1, null)
        val alertDialog1 = AlertDialog.Builder(this)
            .setTitle("게임 방법")
            .setPositiveButton("다음") { dialog, which ->
                showSecondPage(inflater) // 설명 2페이지를 보여주는 함수 호출
            }
            .setNegativeButton("취소", null)
        alertDialog1.setView(view1)
        alertDialog1.setCancelable(false).show()
    }

//    private fun clickMakeGameRoomBtn(){
//        makeRoom.setOnClickListener {
//            val dialog : Dialog = OnlineGameSettingDialog(this)
//            dialog.show()
//        }
//    }
}