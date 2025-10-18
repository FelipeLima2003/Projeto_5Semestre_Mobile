package com.runConnect.auth_api.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.runConnect.auth_api.model.Usuario;
import com.runConnect.auth_api.repository.UsuarioRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;


@RestController

@RequestMapping("/usuario")
public class LoginController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping("/login")
    public ResponseEntity<List<Usuario>> login(
     @RequestParam("usuario") String email,
     @RequestParam("senha") String senha) 
     {
        Optional<Usuario> usuarioOptional = usuarioRepository.findByEmailAndSenha(email, senha);
        if (usuarioOptional.isPresent()) {
            return ResponseEntity.ok(Collections.singletonList(usuarioOptional.get()));
        }else{
            return ResponseEntity.ok(Collections.emptyList());
        }
    
    }

    @PostMapping("/cadastrar")
    public ResponseEntity<Usuario> cadastrar(@RequestBody Usuario novoUsuario){

        if (usuarioRepository.findByEmail(novoUsuario.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        Usuario usuarioSalvo = usuarioRepository.save(novoUsuario);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioSalvo);
    }

}