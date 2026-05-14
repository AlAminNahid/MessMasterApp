package com.example.messmaster.network

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.messmaster.BuildConfig

object RetrofitClient {

    private lateinit var _cookieJar: PersistentCookieJar
    
    val cookieJar: PersistentCookieJar
        get() = if (::_cookieJar.isInitialized) _cookieJar else throw UninitializedPropertyAccessException("RetrofitClient must be initialized with context first")

    fun init(context: Context) {
        if (!::_cookieJar.isInitialized) {
            _cookieJar = PersistentCookieJar(context)
        }
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply{
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .cookieJar(cookieJar)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val authService: com.example.messmaster.auth.network.ApiService by lazy {
        retrofit.create(com.example.messmaster.auth.network.ApiService::class.java)
    }

    val messService: com.example.messmaster.commondashboard.network.ApiService by lazy {
        retrofit.create(com.example.messmaster.commondashboard.network.ApiService::class.java)
    }
}
