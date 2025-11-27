package com.runConnect.auth_api.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.runConnect.auth_api.model.enums.Genero;

public record CadastroRequestDTO(
        String nome,
        String email,
        String senha,
        String cpf,
        String telefone,
        @JsonFormat(pattern = "dd/MM/yyyy")
        LocalDate dataNascimento,
        String imagemUrl,
        Genero genero
) {
     
}
