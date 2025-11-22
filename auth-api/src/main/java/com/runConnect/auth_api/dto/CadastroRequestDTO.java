package com.runConnect.auth_api.dto;

import java.time.LocalDate;

import com.runConnect.auth_api.model.enums.Genero;

public record CadastroRequestDTO(
        String nome,
        String email,
        String senha,
        String cpf,
        String telefone,
        LocalDate dataNascimento,
        Genero genero
) {
     
}
