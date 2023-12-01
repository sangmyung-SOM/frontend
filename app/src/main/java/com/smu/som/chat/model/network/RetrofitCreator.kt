package com.smu.som.chat.model.network
import com.smu.som.game.GameConstant
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitCreator {

    companion object{

        val BASE_URL = GameConstant.API_URL

        private fun retrofit(BASE_URL:String): Retrofit{
            return Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build()
        }

        fun <T> create(service: Class<T>): T{
            return retrofit(BASE_URL).create(service)
        }
    }
}