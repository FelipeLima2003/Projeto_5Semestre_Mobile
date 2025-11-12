package com.runConnect.auth_api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.runConnect.auth_api.model.PontoGeografico;
import com.runConnect.auth_api.repository.PontoGeograficoRepository;

import java.util.List;

@RestController
@RequestMapping("/coordenadas")
public class PontoGeograficoController {

    @Autowired
    private PontoGeograficoRepository pontoGeograficoRepository;
    
    // Este é o método que retorna todos os pontos para o gráfico.
    // Mapeia para GET /coordenadas
    @GetMapping
    public ResponseEntity<List<PontoGeografico>> listarTodosPontos() {
        // 1. Busca todos os registros na tabela PONTOS_GEOGRAFICOS
        List<PontoGeografico> pontos = pontoGeograficoRepository.findAll();
        
        // 2. Retorna a lista completa com status 200 OK
        return ResponseEntity.ok(pontos);
    }
    
    // Você pode remover o método de busca por ID e o de busca por nome, se quiser manter
    // apenas este método de listagem.
}
