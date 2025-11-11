package com.example.seuprojeto.model;
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

public class PontoGeografico {

    private int pontoId;
    private String localNome;
    private float coordX;
    private float coordY;

    // Construtores, Getters e Setters aqui
    // Se estiver usando Lombok, use @Data e @AllArgsConstructor
}
