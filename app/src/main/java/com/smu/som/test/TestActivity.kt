package com.smu.som.test

import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.smu.som.R


class TestActivity : AppCompatActivity(), View.OnTouchListener{
    var textView: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)
        textView = findViewById<TextView>(R.id.myText)
        textView!!.setOnTouchListener(this)
    }

    var oldXvalue = 0f
    var oldYvalue = 0f

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        val width = (v.parent as ViewGroup).width - v.width
        val height = (v.parent as ViewGroup).height - v.height
        if (event.action == MotionEvent.ACTION_DOWN) {
            oldXvalue = event.x
            oldYvalue = event.y
//              Log.i("Tag1", "Action Down X" + event.getX() + "," + event.getY());
            Log.i("Tag1", "Action Down rX " + event.rawX + "," + event.rawY)
        } else if (event.action == MotionEvent.ACTION_MOVE) {
            v.x = event.rawX - oldXvalue
            v.y = event.rawY - (oldYvalue + v.height)
//              Log.i("Tag2", "Action Down " + event.getRawX() + "," + event.getRawY());
        } else if (event.action == MotionEvent.ACTION_UP) {
            if (v.x > width && v.y > height) {
                v.x = width.toFloat()
                v.y = height.toFloat()
            } else if (v.x < 0 && v.y > height) {
                v.x = 0f
                v.y = height.toFloat()
            } else if (v.x > width && v.y < 0) {
                v.x = width.toFloat()
                v.y = 0f
            } else if (v.x < 0 && v.y < 0) {
                v.x = 0f
                v.y = 0f
            } else if (v.x < 0 || v.x > width) {
                if (v.x < 0) {
                    v.x = 0f
                    v.y = event.rawY - oldYvalue - v.height
                } else {
                    v.x = width.toFloat()
                    v.y = event.rawY - oldYvalue - v.height
                }
            } else if (v.y < 0 || v.y > height) {
                if (v.y < 0) {
                    v.x = event.rawX - oldXvalue
                    v.y = 0f
                } else {
                    v.x = event.rawX - oldXvalue
                    v.y = height.toFloat()
                }
            }
        }
        return true
    }
}