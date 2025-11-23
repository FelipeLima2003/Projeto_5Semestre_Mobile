package com.example.projetointegrador

import com.google.gson.annotations.SerializedName

data class CorridaRequest(
    @SerializedName("usuarioId") val usuarioId: Int,
    @SerializedName("distancia") val distancia: Double,
    @SerializedName("duracaoMs") val duracaoMs: Long
)
