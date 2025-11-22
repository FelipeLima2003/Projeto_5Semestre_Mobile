package com.runConnect.auth_api.dto;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.runConnect.auth_api.model.Corrida; 
import java.math.BigDecimal;
import java.sql.Timestamp;

public record CorridaResponseDTO(
        Integer id,
        Integer usuarioId,
        // Variavel para facilitar o nome do usuario no android studio
        String nomeUsuario,
        BigDecimal distancia,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") 
        Timestamp tempoInicial,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") 
        Timestamp tempoFinal
     
) {
        

    // Construtor usado ao criar a DTO a partir da entidade
    public CorridaResponseDTO(Corrida corrida) {
        
         this(
            corrida.getId(),
            corrida.getUsuario().getId(),
            corrida.getUsuario().getNome(), // Pega o nome do usuario relacionado
            corrida.getDistancia(),
            corrida.getTempoInicial(),
            corrida.getTempoFinal()
        );
        
    }

   
}

