package com.example.projetointegrador

import com.google.gson.annotations.SerializedName


data class UpdateDescricaoRequest(

    @SerializedName("descricao") val descricao: String

)
