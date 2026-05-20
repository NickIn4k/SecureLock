package com.example.securelock.network

import com.example.securelock.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {    //Per il debug delle richieste
            level = HttpLoggingInterceptor.Level.BODY
        })
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_URL)
        .client(okHttpClient)   //Retrofit utilizza il client okHttp
        .addConverterFactory(GsonConverterFactory.create()) //Per conversione automatica tra json e kotlin
        .build()

    // Istanza di ApiService creata una volta sola e riutilizzata
    val api: ApiService = retrofit.create(ApiService::class.java)
}