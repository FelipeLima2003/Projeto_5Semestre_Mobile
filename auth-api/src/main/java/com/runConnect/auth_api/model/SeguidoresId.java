package com.runConnect.auth_api.model;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Classe para representar a entidade "auxiliar" para a tabela de seguidores

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable 
public class SeguidoresId implements Serializable {
    
    @Column(name = "seguidor_id")
    private Integer seguidorId;
    @Column(name = "seguido_id")
    private Integer seguidoId;
    
}