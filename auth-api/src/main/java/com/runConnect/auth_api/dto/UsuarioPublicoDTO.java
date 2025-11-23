package com.runConnect.auth_api.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.runConnect.auth_api.model.Usuario;

public record UsuarioPublicoDTO(
    Integer id,
        String nome,
        @JsonFormat(pattern = "dd/MM/yyyy")
        LocalDate dataNascimento,
        String email,
        String telefone,
        String genero, 
        String imagemUrl,
        String descricao
    ) 
     
{
     public UsuarioPublicoDTO(Usuario usuario) {
        this(
                usuario.getId(),
                usuario.getNome(),
                usuario.getDataNascimento(),
                usuario.getEmail(),
                usuario.getTelefone(),
                usuario.getGenero()!= null ? usuario.getGenero().toString() : null,
                usuario.getImagemUrl(),
                usuario.getDescricao()
        );
    }
}

