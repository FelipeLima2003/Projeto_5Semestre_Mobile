package com.runConnect.auth_api.controller;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.runConnect.auth_api.dto.CorridaRequestDTO;
import com.runConnect.auth_api.dto.CorridaResponseDTO;
import com.runConnect.auth_api.model.Corrida;
import com.runConnect.auth_api.model.Usuario;
import com.runConnect.auth_api.repository.CorridaRepository;
import com.runConnect.auth_api.repository.UsuarioRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/corridas")
public class CorridaController {
    
    @Autowired
    private CorridaRepository corridaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @PostMapping
    public ResponseEntity<Corrida> registrarCorrida(@RequestBody CorridaRequestDTO dto){

        // Encontrar o usuaario que fez a corrida pela ID
        Usuario usuario = usuarioRepository.findById(dto.usuarioId())
            .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
 
        // Criar a entidade Corrida
        Corrida novaCorrida = new Corrida();
        novaCorrida.setUsuario(usuario);
        novaCorrida.setDistancia(dto.distancia());
        novaCorrida.setTempoInicial(dto.tempoInicial());
        novaCorrida.setTempoFinal(dto.tempoFinal());

        
        // Salvar a corrida

        Corrida corridaSalva = corridaRepository.save(novaCorrida);

        CorridaResponseDTO responseDTO = new CorridaResponseDTO(corridaSalva);
        URI location = URI.create("/corridas/" + corridaSalva.getId());
        return ResponseEntity.status(201).body(corridaSalva);

    }

    // Endpoint para ver o FEED de Corridas (das pessoas que sigo)

    @GetMapping("/feed")
    public ResponseEntity<List<CorridaResponseDTO>> buscarFeedCorridas(@RequestParam Integer meuId) {
        // Verifica se o utilizador que pede o feed existe
        if (!usuarioRepository.existsById(meuId)) {
             throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilizador não encontrado");
        }

        List<CorridaResponseDTO> feedDTO = corridaRepository.findFeedCorridas(meuId)
                .stream()
                .map(CorridaResponseDTO::new) // Converte cada Corrida para DTO
                .collect(Collectors.toList());
        return ResponseEntity.ok(feedDTO);
    }
    
        // --- Endpoint para ver os DETALHES de UMA Corrida ---

     @GetMapping("/{id}")
    public ResponseEntity<CorridaResponseDTO> getCorridaPorId(@PathVariable Integer id) {
        return corridaRepository.findById(id)
                .map(CorridaResponseDTO::new)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<CorridaResponseDTO>> getCorridasPorUsuario(@PathVariable Integer usuarioId) {
         if (!usuarioRepository.existsById(usuarioId)){
            return ResponseEntity.notFound().build();
         }
           
          List<CorridaResponseDTO> corridasDTO = corridaRepository.findByUsuarioIdOrderByTempoFinalDesc(usuarioId)
                .stream()
                .map(CorridaResponseDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(corridasDTO);
    }

}