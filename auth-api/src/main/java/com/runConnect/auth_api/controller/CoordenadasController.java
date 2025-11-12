package com.runConnect.auth_api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.runConnect.auth_api.model.PontoGeografico; // Seu novo modelo
import com.runConnect.auth_api.repository.PontoGeograficoRepository; // Você precisa criar esta interface

import java.util.List;

@RestController
@RequestMapping("/coordenadas") // Endpoint base: /coordenadas
public class PontoGeograficoController {

    // Injeta a dependência do repositório para acesso ao banco de dados
    @Autowired
    private PontoGeograficoRepository pontoGeograficoRepository;
    
    // Endpoint para Listar Todos os Pontos Geográficos
    // GET /coordenadas
    @GetMapping
    public ResponseEntity<List<PontoGeografico>> listarTodosPontos() {
        // Busca todos os registros na tabela PONTOS_GEOGRAFICOS
        List<PontoGeografico> pontos = pontoGeograficoRepository.findAll();
        
        // Retorna a lista com status 200 OK
        return ResponseEntity.ok(pontos);
    }
    
    // Endpoint para Buscar um Ponto Geográfico por ID
    // GET /coordenadas/{id}
    @GetMapping("/{id}")
    public ResponseEntity<PontoGeografico> buscarPontoPorId(@PathVariable Integer id) {
        // Usa o map para buscar e, se presente, retornar o ponto.
        // Caso contrário, retorna 404 Not Found (igual aos seus outros métodos).
        return pontoGeograficoRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    // Se precisar de CREATE/UPDATE/DELETE (CRUD completo), você pode adicionar:
    // @PostMapping para inclusão, @PutMapping para edição e @DeleteMapping para exclusão, 
    // seguindo o padrão dos seus métodos de cadastro.
}
