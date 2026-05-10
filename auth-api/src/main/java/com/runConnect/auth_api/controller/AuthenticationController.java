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
import java.util.regex.Pattern;
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

    // Padrões de validação de força de senha
    private static final Pattern SENHA_NUMERO = Pattern.compile(".*\\d.*");
    private static final Pattern SENHA_ESPECIAL = Pattern.compile(".*[!@#$%^&*()\\-_=+\\[\\]{};:'\",.<>?/\\\\|`~].*");

    private void validarForcaSenha(String senha) {
        if (senha == null || senha.length() < 8) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "A senha deve ter no mínimo 8 caracteres");
        }
        if (!SENHA_NUMERO.matcher(senha).matches()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "A senha deve conter pelo menos um número");
        }
        if (!SENHA_ESPECIAL.matcher(senha).matches()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "A senha deve conter pelo menos um caractere especial");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody @Valid LoginRequestDTO data) {
        var usernamePassword = new UsernamePasswordAuthenticationToken(data.email(), data.senha());
        var auth = this.authenticationManager.authenticate(usernamePassword);
        var usuario = (Usuario) auth.getPrincipal();
        var token = tokenService.gerarToken(usuario);
        LoginResponseDTO response = new LoginResponseDTO(
                token,
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getCpf());
        return ResponseEntity.ok(response);
    }

    private void validarCamposObrigatorios(CadastroRequestDTO data) {
        if (data.nome() == null || data.nome().isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Nome é obrigatório");
        }

        if (data.email() == null || data.email().isBlank()) { 
        throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                "E-mail é obrigatório");
        }
        if (data.cpf() == null || data.cpf().isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "CPF é obrigatório");
        }
        if (data.telefone() == null || data.telefone().isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Telefone é obrigatório");
        }
        if (data.dataNascimento() == null) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Data de nascimento é obrigatória");
        }
        if (data.genero() == null) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Gênero é obrigatório");
        }
    }

    @PostMapping("/cadastrar")
    public ResponseEntity<Void> cadastrar(@RequestBody @Valid CadastroRequestDTO data) {
        // Verifica se o email já existe

        if (this.usuarioRepository.findByEmail(data.email()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail já cadastrado");
        }

        validarForcaSenha(data.senha());

        validarCamposObrigatorios(data);

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
        newUser.setImagemUrl(data.imagemUrl());
        this.usuarioRepository.save(newUser);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
