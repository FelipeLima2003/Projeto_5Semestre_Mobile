package com.runConnect.auth_api.dto;

public record LoginResponseDTO(
    String token,
    Integer id,       
    String nome,
    String email,
    String cpf
) {
}
