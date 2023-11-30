package com.smu.som.game.service

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.util.Log
import android.view.View
import android.widget.ImageView
import kotlin.math.log

class MalMoveUtils (val board : View, val mal : ImageView){

    private val boardWidth : Int
    private val boardHeight: Int
    private val malWidth: Int
    private val malHeight: Int

    private val gapXOutline: Int
    private val gapYOutline: Int
    private val gapXDiagonal: Int
    private val gapYDiagonal: Int

    private var coordinates: Array<Coordinate> // 좌표값
    private val regularsOutline : Array<Float> // 말 위치의 정규화 위한 값
    private val regularsDiagonal : Array<Float> // 말 위치의 정규화 위한 값
    private var point: Point = Point()

    init {
        boardWidth = board.width - mal.width // 윷판 이미지 구성상 mal.width를 빼야했음.
        boardHeight = board.height - mal.height // 마찬가지. 윷판 이미지 구성이 달라진다면 mal 안빼도 됨.
        malWidth = mal.width
        malHeight = mal.height

        gapXOutline = boardWidth/5
        gapYOutline = boardHeight/5
        gapXDiagonal = boardWidth/6
        gapYDiagonal = boardHeight/6

        // 윷판 이미지에 따라 값이 변경되어야하기 때문에 하드코딩 된 감이 없잖아 있음.
        // 하드코딩 하고싶지 않다면 윷판 이미지 변경 필요.
        // 이러면, 가변적인 윷판 크기에 대해선 대응을 못하게 됨.
        regularsOutline = arrayOf(-0.5f, -0.3f, 0f, 0f, 0.3f, 0.5f)
        regularsDiagonal = arrayOf(0f, -0.3f, -0.1f, 0f, 0.1f, 0.3f, 0f)

        coordinates = Array(31, {i -> Coordinate(i.toFloat(), i.toFloat()) })

        setX()
        setY()

        Log.i("som-gana", "boardWidth=${boardWidth}")
        Log.i("som-gana", "boardHeight=${boardHeight}")
        Log.i("som-gana", "malWidth=${malWidth}")
        Log.i("som-gana", "malHeight=${malHeight}")

        Log.i("som-gana", "gapXOutline=${gapXOutline}")
        Log.i("som-gana", "gapYOutline=${gapYOutline}")
        Log.i("som-gana", "gapXDiagonal=${gapXDiagonal}")
        Log.i("som-gana", "gapYDiagonal=${gapYDiagonal}")

        for(i in 0 until 31){
            Log.i("som-gana", "${i}: x= ${coordinates[i].x}, y= ${coordinates[i].y}")
        }
    }

    // 특정 위치의 좌표찾기
    public fun getPosition(idx : Int) : Pair<Float, Float>{
        val coordinate = coordinates[idx]
        return Pair(coordinate.x, coordinate.y)
    }

    // 말 움직이기
    public fun move(mal:ImageView, idx:Int){
        val coordinate = coordinates[idx]

        Log.i("som-gana", "trans x=${coordinate.x}")
        Log.i("som-gana", "trans y=${coordinate.y}")

        val moveX = ObjectAnimator.ofFloat(mal, "translationX", coordinate.x)
        val moveY = ObjectAnimator.ofFloat(mal, "translationY", coordinate.y)
        val AnimatorSet = AnimatorSet()
        AnimatorSet.playTogether(moveX,moveY)
        AnimatorSet.duration = 300
        AnimatorSet.start();
    }

    public fun setPosition(mal:ImageView, idx:Int){
        mal.x = coordinates[idx].x
        mal.y = coordinates[idx].y
    }

    private fun setX(){
        // 대각선에 있는 점들
        for(i in 1..5){
            val temp : List<Int> = point.sameXDiagonal[i-1]
            val gap : Float = regularsDiagonal[i]

            for( t in temp){
                coordinates[t].x = (i * gapXDiagonal).toFloat() - (malWidth * gap)
            }
        }

        // 겉에 있는 점들
        for(i in 0..5){
            val temp : List<Int> = point.sameXOutline[i]
            val gap : Float = regularsOutline[i]

            for( t in temp){
                coordinates[t].x = (i * gapXOutline).toFloat() - (malWidth * gap)
            }
        }
    }

    private fun setY(){
        // 대각선에 있는 점들
        for(i in 1..5){
            val temp : List<Int> = point.sameYDiagonal[i-1]
            val gap : Float = regularsDiagonal[i]

            for( t in temp){
                coordinates[t].y = (i * gapYDiagonal).toFloat() - (malHeight * gap)
            }
        }

        // 겉에 있는 점들
        for(i in 0..5){
            val temp : List<Int> = point.sameYOutline[i]
            val gap : Float = regularsOutline[i]

            for( t in temp){
                coordinates[t].y = (i * gapYOutline).toFloat() - (malHeight * gap)
            }
        }
    }

    private class Point{
        var sameXOutline : Array<List<Int>>
        var sameYOutline : Array<List<Int>>

        var sameXDiagonal : Array<List<Int>> // 대각선에 있는 점
        var sameYDiagonal : Array<List<Int>> // 대각선에 있는 점

        init {
            sameXOutline = arrayOf(
                arrayListOf(10, 11, 12, 13, 14, 15),
                arrayListOf(9, 16),
                arrayListOf(8, 17),
                arrayListOf(7, 18),
                arrayListOf(6, 19),
                arrayListOf(1, 2, 3, 4, 5, 20)
            )

            sameYOutline = arrayOf(
                arrayListOf(5, 6, 7, 8, 9, 10),
                arrayListOf(4, 11),
                arrayListOf(3, 12),
                arrayListOf(2, 13),
                arrayListOf(1, 14),
                arrayListOf(15, 16, 17, 18, 19, 20)
            )

            sameXDiagonal = arrayOf(
                arrayListOf(26, 25),
                arrayListOf(27, 24),
                arrayListOf(23),
                arrayListOf(22, 29),
                arrayListOf(21, 30)
            )

            sameYDiagonal = arrayOf(
                arrayListOf(26, 21),
                arrayListOf(27, 22),
                arrayListOf(23),
                arrayListOf(24, 29),
                arrayListOf(25, 30)
            )
        }
    }

    private class Coordinate{
        var x : Float
        var y : Float

        constructor(x:Float, y:Float){
            this.x = x
            this.y = y
        }
    }
}