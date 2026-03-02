package com.example.smartcard.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

    // ✅ ВСТАВЬ тот адрес, который ОТКРЫЛСЯ в браузере эмулятора:
    // Если работает 10.0.2.2:
    // private const val BASE_URL = "http://10.0.2.2:8001/"
    //
    // Если работает твой IP:
    private const val BASE_URL = "http://10.5.5.20:8001/"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val http = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    val authApi: AuthApi = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(http)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(AuthApi::class.java)
}
