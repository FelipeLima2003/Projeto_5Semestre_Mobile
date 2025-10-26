package com.runConnect.auth_api.dto;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;






public record CorridaRequestDto(
    Integer usuarioId,
    BigDecimal distancia,
    

    Timestamp tempoInicial,
    
    
    Timestamp tempoFinal,
    
    List<PontosGpsDto> pontosGpsLista 
) {}
