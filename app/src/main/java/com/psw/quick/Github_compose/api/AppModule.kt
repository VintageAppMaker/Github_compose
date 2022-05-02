package com.psw.quick.Github_compose.datasource

import com.psw.quick.Github_compose.api.GithubService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

// 통신모듈
object Api {
    const val TIME_OUT = 30L
    const val BASE = "https://api.github.com"

    private fun getOkHttpClient() =
        OkHttpClient.Builder()
            .connectTimeout(TIME_OUT, TimeUnit.SECONDS)
            .readTimeout(TIME_OUT, TimeUnit.SECONDS)
            .writeTimeout(TIME_OUT, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()

    val github: GithubService
        get() {
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE)
                .addConverterFactory(GsonConverterFactory.create())
                .client(getOkHttpClient())
                .build()

            return retrofit.create<GithubService>(GithubService::class.java!!)
        }

}