package com.example.projetointegrador
import com.google.gson.annotations.SerializedName

data class UsuarioPublicoResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("nome") val nome: String,
    @SerializedName("email") val email: String,
    @SerializedName("genero") val genero: String,
    @SerializedName("imagemUrl") val imagemUrl: String?,
    @SerializedName("descricao") val descricao: String?

)
