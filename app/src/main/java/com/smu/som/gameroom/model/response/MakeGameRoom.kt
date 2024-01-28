package com.smu.som.gameroom.model.response

import com.google.gson.annotations.SerializedName

data class MakeGameRoom(
    @SerializedName("name")
    val name: String?,
    @SerializedName("category")
    val category: String?,
    @SerializedName("adult")
    val adult: String?,
)