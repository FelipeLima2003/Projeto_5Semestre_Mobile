package com.example.projetointegrador

import android.util.Log
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://runconnect-api.onrender.com/"

    private var authToken: String? = null

    fun setAuthToken(token: String) {
        Log.d("RetrofitClient", "Token SALVO na memória: $token") // Log para confirmar
        authToken = token
    }

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                val originalRequest = chain.request()
                val requestBuilder = originalRequest.newBuilder()

                if (authToken != null) {
                    Log.d("RetrofitClient", "Adicionando Token no cabeçalho: Bearer $authToken") // Log para ver se está enviando
                    requestBuilder.header("Authorization", "Bearer $authToken")
                } else {
                    Log.e("RetrofitClient", "ERRO GRAVE: O Token está NULO! O login não foi feito ou a memória foi limpa.")
                }

                val newRequest = requestBuilder.build()
                chain.proceed(newRequest)
            }
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}