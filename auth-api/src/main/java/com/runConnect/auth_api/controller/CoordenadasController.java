package com.runConnect.auth_api.controller;
import com.example.seuprojeto.model.PontoGeografico;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api") // Prefixo para todos os endpoints neste controller
public class CoordenadasController {

    @GetMapping("/coordenadas") // O endpoint final será /api/coordenadas
    public List<PontoGeografico> getCoordenadas() {
        // **IDEAL:** Aqui você chamaria o seu Service/Repository para buscar os dados do MySQL.
        // **SIMULAÇÃO:** Retornamos dados fixos para teste:
        return Arrays.asList(
            new PontoGeografico(1, "Sede Principal", -23.5505f, -46.6333f),
            new PontoGeografico(2, "Filial Norte", -23.4900f, -46.8000f),
            new PontoGeografico(3, "Ponto de Encontro", -23.6000f, -46.5000f)
        );
    }
}
