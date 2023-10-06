package com.smu.som

import java.io.Serializable

// 게임 횟수 데이터 Serializable
class Data(
    var userId : String? = null,
    var couple : Int? = null,
    var married : Int? = null,
    var family: Int? = null
): Serializable