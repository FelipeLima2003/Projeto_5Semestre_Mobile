package com.runConnect.auth_api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.runConnect.auth_api.dto.LoginResponseDTO;
import com.runConnect.auth_api.dto.CadastroRequestDTO;
import com.runConnect.auth_api.dto.LoginRequestDTO;
import com.runConnect.auth_api.model.Usuario;
import com.runConnect.auth_api.repository.UsuarioRepository;
import com.runConnect.auth_api.services.TokenService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {
    
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
        public ResponseEntity login(@RequestBody @Valid LoginRequestDTO data) {
        var usernamePassword = new UsernamePasswordAuthenticationToken(data.email(), data.senha());
        var auth = this.authenticationManager.authenticate(usernamePassword);
        var usuario = (Usuario) auth.getPrincipal();
        var token = tokenService.generateToken(usuario);
        LoginResponseDTO response = new LoginResponseDTO(
                token,
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getCpf()
    );
    return ResponseEntity.ok(response);
}

    @PostMapping("/cadastrar")
    public ResponseEntity<Void> cadastrar(@RequestBody @Valid CadastroRequestDTO data) {
        // Verifica se o email já existe
        if (this.usuarioRepository.findByEmail(data.email()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail já cadastrado");
        }

        // Criptografa a senha antes de salvar
        String encryptedPassword = passwordEncoder.encode(data.senha());

        Usuario newUser = new Usuario();
        newUser.setNome(data.nome());
        newUser.setDataNascimento(data.dataNascimento());
        newUser.setCpf(data.cpf());
        newUser.setEmail(data.email());
        newUser.setTelefone(data.telefone());
        newUser.setGenero(data.genero());
        newUser.setSenha(encryptedPassword); 
        this.usuarioRepository.save(newUser);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
