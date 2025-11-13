package com.example.projetointegrador

import com.google.gson.annotations.SerializedName

// Dados que a API RETORNA para a tela de perfil
data class PerfilUsuarioResponse(
    @SerializedName("id")
    val id: Int,

    @SerializedName("nome")
    val nome: String,

    @SerializedName("genero")
    val genero: String,

    // Supondo que a API agora retorne um campo "descricao"
    @SerializedName("descricao")
    val descricao: String? // Usamos '?' para o caso de o usuário ainda não ter uma descrição
)

// Dados que vamos ENVIAR para a API para atualizar a descrição
data class UpdateDescricaoRequest(
    @SerializedName("descricao")
    val descricao: String
)
