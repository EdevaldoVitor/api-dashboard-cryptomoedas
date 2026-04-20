package com.api.crypto.dashboard.dto;

import lombok.Data;

import jakarta.validation.constraints.NotNull;

@Data
public class AiResumoRequestDTO {

    @NotNull(message = "O texto para resumo é obrigatório")
    private String texto;
}
