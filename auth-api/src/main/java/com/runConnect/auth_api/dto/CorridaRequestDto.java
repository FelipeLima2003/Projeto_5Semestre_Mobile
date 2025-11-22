package com.runConnect.auth_api.dto;

import java.math.BigDecimal;
import java.sql.Timestamp;


import com.fasterxml.jackson.annotation.JsonFormat;




public record CorridaRequestDto(
    Integer usuarioId,
    BigDecimal distancia,
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    Timestamp tempoInicial,
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    Timestamp tempoFinal
    
    
){}






