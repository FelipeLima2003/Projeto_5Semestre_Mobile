package com.runConnect.auth_api.model;

import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import java.sql.Timestamp;


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

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "tempo_inicial", nullable = false)
    private Timestamp tempoInicial;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "tempo_final", nullable = false)
    private Timestamp tempoFinal;    

}