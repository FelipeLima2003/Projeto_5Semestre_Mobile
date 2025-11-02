package com.example.projetointegrador

import com.google.gson.annotations.SerializedName

data class UsuarioResponse(
    @SerializedName("id")
    val id: Int,

    @SerializedName("nome")
    val nome: String,
)
