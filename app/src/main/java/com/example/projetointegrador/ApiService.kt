package com.example.projetointegrador

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @POST("auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>

    @POST("auth/cadastrar")
    suspend fun cadastrar(@Body cadastroRequest: CadastroRequest): Response<CadastroResponse>


    @GET("usuario")
    suspend fun getUsuarios(): Response<List<UsuarioPublicoResponse>>

    @GET("usuario/{id}")
    suspend fun getUsuarioById(@Path("id") userId: Int): Response<UsuarioPublicoResponse>

    @PUT("usuario/{id}/descricao")
    suspend fun updateDescricao(
        @Path("id") userId: Int,
        @Body request: UpdateDescricaoRequest
    ): Response<Unit>

    @DELETE("usuario/{id}")
    suspend fun excluirUsuario(@Path("id") userId: Int): Response<Unit>

    @POST("usuario/{idUsuario}/seguir")
    suspend fun seguirUsuario(
        @Path("idUsuario") idUsuarioASerSeguido: Int,
        @Query("seguidorId") idDoSeguidor: Int
    ): Response<Unit>

    @POST("corridas")
    suspend fun salvarCorrida(@Body corridaRequest: CorridaRequest): Response<Unit>


    @GET("corridas/usuario/{id}")
    suspend fun getCorridasDoUsuario(@Path("id") userId: Int): Response<List<CorridaResponse>>

    @Multipart
    @POST("uploads/imagem")
    suspend fun uploadImagem(@Part file: MultipartBody.Part): Response<String>


    @PUT("usuario/{id}/imagem")
    suspend fun atualizarImagemPerfil(
        @Path("id") id: Int,
        @Body body: Map<String, String>
    ): Response<UsuarioPublicoResponse>

}
