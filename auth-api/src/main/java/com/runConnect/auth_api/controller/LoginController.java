package com.runConnect.auth_api.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.runConnect.auth_api.dto.UsuarioPublicoDto;
import com.runConnect.auth_api.model.Seguidores;
import com.runConnect.auth_api.model.Usuario;
import com.runConnect.auth_api.repository.SeguidoresRepository;
import com.runConnect.auth_api.repository.UsuarioRepository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;


@RestController

@RequestMapping("/usuario")
public class LoginController {

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private SeguidoresRepository seguidoresRepository;
     
    // Login do usuario
    @GetMapping("/login")
    public ResponseEntity<List<Usuario>> login(
     @RequestParam("email") String email,
     @RequestParam("senha") String senha) 
     {
        Optional<Usuario> usuarioOptional = usuarioRepository.findByEmailAndSenha(email, senha);
        if (usuarioOptional.isPresent()) {
            return ResponseEntity.ok(Collections.singletonList(usuarioOptional.get()));
        }else{
            return ResponseEntity.ok(Collections.emptyList());
        }
    
    }
    // Cadastro do usuario
    @PostMapping("/cadastrar")
    public ResponseEntity<Usuario> cadastrar(@RequestBody Usuario novoUsuario){

        if (usuarioRepository.findByEmail(novoUsuario.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        Usuario usuarioSalvo = usuarioRepository.save(novoUsuario);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioSalvo);
    }


    // Endpoint de busca
     @GetMapping
    public ResponseEntity<List<UsuarioPublicoDto>> buscarTodosUsuarios() {
        List<UsuarioPublicoDto> usuariosDTO = usuarioRepository.findAll()
                .stream()
                .map(UsuarioPublicoDto::new) // Converte cada Usuario para DTO
                .collect(Collectors.toList());
        return ResponseEntity.ok(usuariosDTO);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioPublicoDto> buscarUsuarioPorId(@PathVariable Integer id) {
        return usuarioRepository.findById(id)
                .map(UsuarioPublicoDto::new) // Converte para DTO
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // --- Endpoints de Seguir ---

    @PostMapping("/{idSeguido}/seguir")
    public ResponseEntity<Void> seguirUsuario(@PathVariable Integer idSeguido, @RequestParam Integer seguidorId) {
        Usuario seguidor = usuarioRepository.findById(seguidorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario seguidor não encontrado"));
        Usuario seguido = usuarioRepository.findById(idSeguido)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario a ser seguido não encontrado"));

        // Verifica se já não segue
        seguidoresRepository.findById_SeguidorIdAndId_SeguidoId(seguidorId, idSeguido).ifPresent(s -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já segue este utilizador");
        });

        Seguidores novaRelacao = new Seguidores(seguidor, seguido);
        seguidoresRepository.save(novaRelacao);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{idSeguido}/deixar-de-seguir")
    public ResponseEntity<Void> deixarDeSeguirUsuario(@PathVariable Integer idSeguido, @RequestParam Integer seguidorId) {
        // Verifica se a relação existe antes de tentar apagar
        Seguidores relacao = seguidoresRepository.findById_SeguidorIdAndId_SeguidoId(seguidorId, idSeguido)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Não segue este Usuario"));

        seguidoresRepository.delete(relacao);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/seguidores")
    public ResponseEntity<List<UsuarioPublicoDto>> listarSeguidores(@PathVariable Integer id) {
        // Verifica se o utilizador existe
        if (!usuarioRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        List<UsuarioPublicoDto> seguidoresDTO = seguidoresRepository.findSeguidoresByUsuarioId(id)
                .stream()
                .map(UsuarioPublicoDto::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(seguidoresDTO);
    }

    @GetMapping("/{id}/seguindo")
    public ResponseEntity<List<UsuarioPublicoDto>> listarSeguindo(@PathVariable Integer id) {
        if (!usuarioRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        List<UsuarioPublicoDto> seguindoDTO = seguidoresRepository.findSeguindoByUsuarioId(id)
                .stream()
                .map(UsuarioPublicoDto::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(seguindoDTO);
    }

    // --- Endpoint para atualizar descrição ---
    @PutMapping("/{id}/descricao")
    public ResponseEntity<UsuarioPublicoDto> atualizarDescricao(
            @PathVariable Integer id,
            @RequestBody Map<String, String> request) {
        String novaDescricao = request.get("descricao");
        
        return usuarioRepository.findById(id)
                .map(usuario -> {
                    usuario.setDescricao(novaDescricao);
                    Usuario usuarioAtualizado = usuarioRepository.save(usuario);
                    return ResponseEntity.ok(new UsuarioPublicoDto(usuarioAtualizado));
                })
                .orElse(ResponseEntity.notFound().build());
    }
    // --- Endpoint para deletar os dados do usuario ---
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUsuario(@PathVariable Integer id) {
        usuarioService.deletarUsuario(id);
        return ResponseEntity.noContent().build(); 
    }



}






