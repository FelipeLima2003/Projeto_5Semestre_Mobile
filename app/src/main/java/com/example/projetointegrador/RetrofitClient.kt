package com.example.projetointegrador

import android.content.Context
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://runconnect-api.onrender.com/"

    private var authToken: String? = null


    fun setAuthToken(token: String) {

        val cleanToken = if (token.startsWith("Bearer ", ignoreCase = true)) {
            token.substring(7)
        } else {
            token
        }
        Log.d("RetrofitClient", "Token definido na memória (truncado): ${cleanToken.take(10)}...")
        authToken = cleanToken
    }

    fun ensureTokenIsLoaded(context: Context) {
        if (authToken == null) {
            val savedToken = AppPreferences.getToken(context)
            if (savedToken != null) {
                setAuthToken(savedToken)
                Log.d("RetrofitClient", "Token recuperado das preferências.")
            } else {
                Log.e("RetrofitClient", "AVISO: Nenhum token salvo encontrado!")
            }
        }
    }

    fun clearAuthToken() {
        authToken = null
        Log.d("RetrofitClient", "Token removido da memória.")
    }

    private val client: OkHttpClient by lazy {

        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)

        OkHttpClient.Builder()

            .addInterceptor { chain ->
                val originalRequest = chain.request()
                val requestBuilder = originalRequest.newBuilder()
                    .header("User-Agent", "Android/RunConnect")
                    .header("Accept", "application/json")

                val isAuthRoute = originalRequest.url.toString().contains("/auth/")

                if (!authToken.isNullOrBlank() && !isAuthRoute) {
                    requestBuilder.header("Authorization", "Bearer $authToken")
                }

                val newRequest = requestBuilder.build()
                chain.proceed(newRequest)
            }

            .addInterceptor(logging)
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
