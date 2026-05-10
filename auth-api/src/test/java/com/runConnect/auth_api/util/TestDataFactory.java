package com.runConnect.auth_api.util;

import com.runConnect.auth_api.model.Usuario;
import com.runConnect.auth_api.model.enums.Genero;
import java.time.LocalDate;

public class TestDataFactory {

    public static Usuario criarUsuarioValido(Integer id, String nome, String email) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setNome(nome);
        u.setEmail(email);
        u.setCpf("123.456.789-00");
        u.setTelefone("(11) 99999-9999");
        u.setDataNascimento(LocalDate.of(1990, 1, 1));
        u.setGenero(Genero.MASCULINO);
        u.setSenha("Senha@123");
        u.setDescricao("Corredor amador");
        u.setImagemUrl("http://img.com/foto.png");
        return u;
    }
}
