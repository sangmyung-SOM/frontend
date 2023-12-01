package com.smu.som.game.service

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.util.Log
import android.view.View
import android.widget.ImageView
import com.smu.som.game.response.GameMalResponse

class GameMalService {

    // 임시로 만든 메소드
    public fun moveMal(mal : ImageView, board : View){
        mal.x = 0f
        mal.y = 0f

        val interval = 30
        val wd = board.width - mal.width
        val hd = board.height - mal.height
        val wd2 = wd/interval
        val hd2 = hd/interval

        Log.i("som-gana", "x=${mal.x}")
        Log.i("som-gana", "y=${mal.y}")
        Log.i("som-gana", "wd=${wd}")
        Log.i("som-gana", "hd=${hd}")
        Log.i("som-gana", "wd2=${wd2}")
        Log.i("som-gana", "hd2=${hd2}")

        val x = ObjectAnimator.ofFloat(mal, "translationX", wd.toFloat())
        val y = ObjectAnimator.ofFloat(mal, "translationY", hd.toFloat())
        val AnimatorSet = AnimatorSet()
        AnimatorSet.playTogether(x,y)
        AnimatorSet.duration = 1000
        AnimatorSet.start();
    }

    public fun moveMal(mal : ImageView, x:Float, y:Float){
        mal.x = 0f
        mal.y = 0f

        val x2 = ObjectAnimator.ofFloat(mal, "translationX", x.toFloat())
        val y2 = ObjectAnimator.ofFloat(mal, "translationY", y.toFloat())
        val AnimatorSet = AnimatorSet()
        AnimatorSet.playTogether(x2,y2)
        AnimatorSet.duration = 1000
        AnimatorSet.start();
    }
}