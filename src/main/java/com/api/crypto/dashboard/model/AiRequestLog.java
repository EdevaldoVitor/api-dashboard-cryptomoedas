package com.api.crypto.dashboard.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "ai_request_log")
@Data
public class AiRequestLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 4000, nullable = false)
    private String prompt;

    @Column(length = 8000)
    private String resposta;

    private Integer promptTokens;
    private Integer completionTokens;
    private Integer totalTokens;

    @Column(nullable = false)
    private String modelo;

    @Column(nullable = false)
    private LocalDateTime dataHora;

    private Double custoEstimado;

}
