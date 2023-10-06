package com.smu.som

import java.io.Serializable

// 질문 데이터 Serializable
class Question(
    var id : Int,
    var target : String? = null,
    var question : String? = null,
    var isAdult: String? = null,
    var category: String? = null
): Serializable