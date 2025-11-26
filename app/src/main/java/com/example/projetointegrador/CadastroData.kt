package com.example.projetointegrador
import com.google.gson.annotations.SerializedName

enum class Genero(val valor: String) {
    @SerializedName("MASCULINO")
    MASCULINO("masculino"),

    @SerializedName("FEMININO")
    FEMININO("feminino"),


    @SerializedName("PREFIRO_NAO_INFORMAR")
    PREFIRO_NAO_INFORMAR("prefiro_nao_informar")
}

data class CadastroRequest(
    @SerializedName("nome")
    val nome: String,

    @SerializedName("dataNascimento")
    val dataNascimento: String,

    @SerializedName("cpf")
    val cpf: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("telefone")
    val telefone: String,

    @SerializedName("genero")
    val genero: Genero,

    @SerializedName("senha")
    val senha: String
    
)
data class CadastroResponse(
    @SerializedName("message")
    val message: String,

    @SerializedName("userId")
    val userId: Int?
)
