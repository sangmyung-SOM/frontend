package com.smu.som.game

import java.lang.RuntimeException

object YutConverter {

    fun toYutString(yut : Int) : String{
        when(yut){
            0 -> return "BACK_DO"
            1-> return "DO"
            2 -> return "GAE"
            3 -> return "GIRL"
            4 -> return "YUT"
            5 -> return "MO"
        }

        throw RuntimeException("윷 결과의 숫자가 0~5사이가 아님")
    }

    fun toYutInt(yut : String) : Int{
        when(yut){
            "BACK_DO" -> return 0
            "DO" -> return 1
            "GAE" -> return 2
            "GIRL" -> return 3
            "YUT" -> return 4
            "MO" -> return 5
        }

        throw RuntimeException("윷 결과의 숫자가 0~5사이가 아님")
    }
}