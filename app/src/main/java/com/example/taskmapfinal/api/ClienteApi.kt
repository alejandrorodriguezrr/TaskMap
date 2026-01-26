package com.example.taskmapfinal.api

import ApiTaskMap
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ClienteApi {

    private const val urlBase = "http://10.0.2.2/taskmap/"  // Emulador

    private val interceptorLog = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val clienteHttp = OkHttpClient.Builder()
        .addInterceptor(interceptorLog)
        .build()

    val api: ApiTaskMap = Retrofit.Builder()
        .baseUrl(urlBase)
        .client(clienteHttp)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ApiTaskMap::class.java)
}
