package com.example.projetointegrador

import com.google.gson.annotations.SerializedName

data class CorridaResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("distancia") val distancia: Double,
    @SerializedName("tempo_final") val tempoFinal: String?,
    @SerializedName("tempo_inicial") val tempoInicial: String?,
    @SerializedName("data_corrida") val dataCorrida: String?
)
