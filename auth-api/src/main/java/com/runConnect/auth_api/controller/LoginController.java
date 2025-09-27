package com.runConnect.auth_api.controller;

import com.runConnect.auth_api.model.Usuario;
import com.runConnect.auth_api.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/runconnect")
public class LoginController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping("/login")
    // Altere o retorno para um único objeto Usuario e corrija o nome do parâmetro
    public ResponseEntity<Usuario> login(
            @RequestParam("email") String email, // CORREÇÃO 1: Esperar "email"
            @RequestParam("senha") String senha)
    {
        Optional<Usuario> usuarioOptional = usuarioRepository.findByEmailAndSenha(email, senha);

        // CORREÇÃO 2: Retornar o objeto diretamente ou um erro
        if (usuarioOptional.isPresent()) {
            // Se o usuário for encontrado, retorne 200 OK com o objeto do usuário no corpo
            return ResponseEntity.ok(usuarioOptional.get());
        } else {
            // Se não for encontrado, retorne 401 Unauthorized (Não autorizado).
            // Isso é mais semanticamente correto do que uma lista vazia com 200 OK.
            return ResponseEntity.status(401).build();
        }
    }
}