package com.runConnect.auth_api.model;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.runConnect.auth_api.model.enums.Genero;

import jakarta.persistence.*;
import lombok.Data;
@Data
@Entity
@Table(name = "USUARIO")
public class Usuario {
   
   
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    
    @Column(name = "ID")
    private Integer id;

    @Column(name = "NOME",nullable = false)
    private String nome;

    @Column(name = "DATA_NASCIMENTO",nullable = false)
    private LocalDate dataNascimento;

    @Column(name = "CPF", nullable = false, unique = true)
    private String cpf;

    @Column(name = "EMAIL", nullable = false, unique = true)
    private String email;

    @Column(name = "TELEFONE",nullable = false)
    private String telefone;

    @Enumerated(EnumType.STRING)
    @Column(name = "GENERO",nullable = false)
    private Genero genero;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "SENHA",nullable = false)
    private String senha;

    @Column(name = "IMAGEM_URL")
    private String imagemUrl;
    
    @Column(name = "descricao")
    private String descricao;
  

}
