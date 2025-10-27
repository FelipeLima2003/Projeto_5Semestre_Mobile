package com.runConnect.auth_api.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.runConnect.auth_api.dto.CorridaRequestDto;
import com.runConnect.auth_api.dto.PontosGpsDto;
import com.runConnect.auth_api.model.Corrida;
import com.runConnect.auth_api.model.PontosGps;
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
    public ResponseEntity<Corrida> registrarCorrida(@RequestBody CorridaRequestDto dto){

        // Encontrar o usuaario que fez a corrida pela ID
        Usuario usuario = usuarioRepository.findById(dto.usuarioId())
            .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        // Criar a entidade Corrida
        Corrida novaCorrida = new Corrida();
        novaCorrida.setUsuario(usuario);
        novaCorrida.setDistancia(dto.distancia());
        novaCorrida.setTempoInicial(dto.tempoInicial());
        novaCorrida.setTempoFinal(dto.tempoFinal());
        
        // Conveter os DTOs de PontosGPS para corrida
      
        List<PontosGps> listaDePontos = new ArrayList<>();

        if(dto.pontosGpsLista() != null){
            for (PontosGpsDto pontoDto : dto.pontosGpsLista()) {
                PontosGps ponto = new PontosGps();
                ponto.setLatitude(pontoDto.latitude());
                ponto.setLongitude(pontoDto.longitude());
                ponto.setOrdem(pontoDto.ordem());
                ponto.setCorrida(novaCorrida); // Associar o ponto à corrida
                listaDePontos.add(ponto);
            }
        }

        // Associar a lista de pontos à corrida
        novaCorrida.setPontosGps(listaDePontos);

        // Salvar a corrida

        Corrida corridaSalva = corridaRepository.save(novaCorrida);

        return ResponseEntity.status(201).body(corridaSalva);

    }

    // Endpoint para ver o FEED de Corridas (das pessoas que sigo)

    @GetMapping("/feed")
    public ResponseEntity<List<Corrida>> getFeedCorridas(
        @RequestParam Integer meuId) {

      List<Corrida> feed = corridaRepository.findFeedCorridas(meuId);
      return ResponseEntity.ok(feed);
    }
    
        // --- Endpoint para ver os DETALHES de UMA Corrida ---

     @GetMapping("/{id}")
    public ResponseEntity<Corrida> getCorridaPorId(@PathVariable Integer id) {
        return corridaRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}