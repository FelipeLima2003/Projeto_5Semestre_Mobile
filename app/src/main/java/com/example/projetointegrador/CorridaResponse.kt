package com.example.projetointegrador

import com.google.gson.annotations.SerializedName

data class CorridaResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("distancia") val distancia: Double,
    // Ajustado para coincidir com o JSON do servidor (camelCase)
    @SerializedName("tempoFinal") val tempoFinal: String?,
    @SerializedName("tempoInicial") val tempoInicial: String?,
    @SerializedName("data_corrida") val dataCorrida: String?
)
