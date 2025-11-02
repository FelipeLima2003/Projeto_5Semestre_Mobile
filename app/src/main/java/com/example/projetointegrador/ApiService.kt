package com.example.projetointegrador

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("usuario/login")
    suspend fun login(
        @Query("email") email: String,
        @Query("senha") senha: String
    ): Response<List<LoginResponse>>

    @POST("usuario/cadastrar")
    suspend fun cadastrar(
        @Body cadastroRequest: CadastroRequest
    ): Response<CadastroResponse>

    @GET("usuario")
    suspend fun getUsuarios(): Response<List<UsuarioResponse>>

    @POST("usuario/{idUsuario}/seguir")
    suspend fun seguirUsuario(
        @Path("idUsuario") idUsuarioASerSeguido: Int, // Parte do caminho (Ex: /2/)
        @Query("seguidorId") idDoSeguidor: Int         // Parâmetro de consulta (Ex: ?seguidorId=4)
    ): Response<Unit>
}
