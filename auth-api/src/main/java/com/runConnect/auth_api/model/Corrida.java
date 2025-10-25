package com.runConnect.auth_api.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.annotation.Generated;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "CORRIDA")
public class Corrida {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "distancia", nullable = false)
    private BigDecimal distancia;

    @Column(name = "tempo_segundos", nullable = false)
    private Integer tempoSegundos;

    @Column(name = "data_corrida")
    private LocalDateTime dataCorrida = LocalDateTime.now();

     @OneToMany(mappedBy = "corrida", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PontosGps> pontosGps;

}