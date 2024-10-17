package com.example.eventdicoding.retrofit

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import com.example.eventdicoding.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object APIConfig {
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_URL) // Mengambil BASE_URL dari BuildConfig
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    fun create(): APIService {
        return retrofit.create(APIService::class.java)
    }
}