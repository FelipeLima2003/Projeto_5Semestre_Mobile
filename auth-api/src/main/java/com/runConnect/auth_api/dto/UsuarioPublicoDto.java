package com.runConnect.auth_api.dto;

import java.time.LocalDate;

import com.runConnect.auth_api.model.Usuario;

public record UsuarioPublicoDto(
    Integer id,
        String nome,
        LocalDate dataNascimento,
        String email,
        String telefone,
        String genero, 
        String imagemUrl
    ) 
     
{
     public UsuarioPublicoDto(Usuario usuario) {
        this(
                usuario.getId(),
                usuario.getNome(),
                usuario.getDataNascimento(),
                usuario.getEmail(),
                usuario.getTelefone(),
                usuario.getGenero()!= null ? usuario.getGenero().toString() : null,
                usuario.getImagemUrl()
        );
    }
}

