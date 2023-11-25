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

    private val bigCircleR : Float

    private var coordinates: Array<Coordinate> // 좌표값
    private var point: Point = Point()

    init {
        boardWidth = board.width
        boardHeight = board.height
        malWidth = mal.width
        malHeight = mal.height

        bigCircleR = boardWidth * 0.15f

        gapXOutline = boardWidth/5
        gapYOutline = boardHeight/5
        gapXDiagonal = boardWidth/6
        gapYDiagonal = boardHeight/6

        coordinates = Array(31, {i -> Coordinate(i.toFloat(), i.toFloat()) })

        setX()
        setY()

        Log.i("som-gana", "boardWidth=${board.width}")
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

    private fun setX(){
        // 대각선에 있는 점들
        for(i in 1..5){
            val temp : List<Int> = point.sameXDiagonal[i-1]

            for( t in temp){
                coordinates[t].x = (i * gapXDiagonal).toFloat() - (malWidth/2)
                when(i){
                    1 -> coordinates[t].x += (malWidth/2)
                    5 -> coordinates[t].x -= (malWidth/2)
                }
            }
        }

        // 겉에 있는 점들
        for(i in 0..5){
            val temp : List<Int> = point.sameXOutline[i]

            for( t in temp){
                coordinates[t].x = (i * gapXOutline).toFloat() - (malWidth/2)
                when(i){
                    0 -> coordinates[t].x += (malWidth/2)
                    5 -> coordinates[t].x -= (malWidth/2)
                }
            }
        }
    }

    private fun setY(){
        // 대각선에 있는 점들
        for(i in 1..5){
            val temp : List<Int> = point.sameYDiagonal[i-1]

            for( t in temp){
                coordinates[t].y = (i * gapYDiagonal).toFloat() - (malHeight/2)
                when(i){
                    1 -> coordinates[t].y += (malHeight/2)
                    5 -> coordinates[t].y -= (malHeight/2)
                }
            }
        }

        // 겉에 있는 점들
        for(i in 0..5){
            val temp : List<Int> = point.sameYOutline[i]

            for( t in temp){
                coordinates[t].y = (i * gapYOutline).toFloat() - (malHeight/2)

                when(i){
                    0 -> coordinates[t].y += (malHeight/2)
                    5 -> coordinates[t].y -= (malHeight/2)
                }
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