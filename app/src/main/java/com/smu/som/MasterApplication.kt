package com.smu.som

import android.app.Application
import android.content.Context
import com.facebook.stetho.Stetho
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.kakao.sdk.common.KakaoSdk
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


// 마스터 애플리케이션
class MasterApplication : Application() {

    lateinit var service: RetrofitService

    override fun onCreate() {
        super.onCreate()

        // retrofit 생성
        Stetho.initializeWithDefaults(this)
        createRetrofit()

        // 카카오톡 로그인 sdk 설정
        KakaoSdk.init(this, "bf210ef78fb5cdbfc6072c511ac2d6fd")
    }


    // retrofit 생성 함수
    fun createRetrofit() {
        // header 설정
        val header = Interceptor {
            val original = it.request()
            it.proceed(original)
        }

        // client 설정
        val client = OkHttpClient.Builder()
            .addInterceptor(header)
            .addNetworkInterceptor(StethoInterceptor())
            .build()

        // retrofit 설정
        val retrofit = Retrofit.Builder()
            .baseUrl("http://52.78.92.194:8080")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

        // retrofit 생성 후 주입
        service = retrofit.create(RetrofitService::class.java)
    }
}