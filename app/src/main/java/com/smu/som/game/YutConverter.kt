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
}