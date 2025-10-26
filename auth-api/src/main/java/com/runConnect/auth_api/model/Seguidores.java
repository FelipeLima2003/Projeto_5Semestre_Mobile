package com.runConnect.auth_api.model;


import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "SEGUIDORES")
public class Seguidores {
    
    //  Usa a classe de ID composto como chave primária
    @EmbeddedId
    private SeguidoresId id;

     // Mapeia o campo seguidorId da nossa Chave Compost
    @ManyToOne
    @MapsId("seguidorId")
    @JoinColumn(name = "seguidor_id")
    private Usuario seguidor;

     // Mapeia o campo seguidoId da nossa Chave Compost
    @ManyToOne
    @MapsId("seguidoId")
    @JoinColumn(name = "seguido_id")
    private Usuario seguido;
}