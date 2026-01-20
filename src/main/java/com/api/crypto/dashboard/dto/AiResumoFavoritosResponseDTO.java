package com.api.crypto.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AiResumoFavoritosResponseDTO {

    private String resumo;
    private int totalMoedas;
}
