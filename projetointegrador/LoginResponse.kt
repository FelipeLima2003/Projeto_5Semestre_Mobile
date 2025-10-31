package com.example.projetointegrador

import com.google.gson.annotations.SerializedName
data class LoginResponse(

    @SerializedName("id")
    val usuarioId: Int,

    @SerializedName("nome")
    val usuarioNome: String,

    @SerializedName("email")
    val usuarioEmail: String,

    @SerializedName("cpf")
    val usuarioCpf: String
)