package com.runConnect.auth_api.model;
import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @Column(name = "NOME")
    private String nome;

    @Column(name = "CPF")
    private String cpf;

    @Column(name = "EMAIL")
    private String email;

    @Column(name = "TELEFONE")
    private String telefone;

    @Enumerated(EnumType.STRING)
    @Column(name = "GENERO")
    private Genero genero;

    @JsonIgnore
    @Column(name = "SENHA")
    private String senha;

  
}