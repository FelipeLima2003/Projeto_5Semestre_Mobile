package com.runConnect.auth_api.model; // Ajuste o pacote conforme a estrutura do seu projeto

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data; // Se você estiver usando Lombok

/**
 * Mapeia a tabela PONTOS_GEOGRAFICOS do banco de dados.
 */
@Data // Gera Getters, Setters, toString, equals e hashCode
@Entity
@Table(name = "PONTOS_GEOGRAFICOS")
public class PontoGeografico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PONTO_ID")
    private Integer pontoId; // Mapeia para PONTO_ID (chave primária)

    @Column(name = "LOCAL_NOME", nullable = false)
    private String localNome; // Mapeia para LOCAL_NOME

    @Column(name = "COORD_X", nullable = false)
    private Float coordX; // Mapeia para COORD_X (Latitude/Eixo X)

    @Column(name = "COORD_Y", nullable = false)
    private Float coordY; // Mapeia para COORD_Y (Longitude/Eixo Y)
}
