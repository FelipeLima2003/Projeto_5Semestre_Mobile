package com.example.projetointegrador

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("login")
    suspend fun login(
        @Query("email") email: String,
        @Query("senha") senha: String
    ): Response<LoginResponse>
}