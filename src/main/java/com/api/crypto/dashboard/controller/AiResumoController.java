package com.api.crypto.dashboard.controller;

import com.api.crypto.dashboard.dto.AiResumoRequestDTO;
import com.api.crypto.dashboard.dto.AiResumoResponseDTO;
import com.api.crypto.dashboard.service.OpenAIService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;

@Tag(name = "Resumo de Texto", description = "API para gerar resumos de texto usando a API do OpenAI")
@Controller
@RequestMapping("/api/ia")
public class AiResumoController {

    private final OpenAIService service;

    public AiResumoController(OpenAIService service) {
        this.service = service;
    }

    @Operation(
            summary = "Gerar resumo de texto",
            description = "Gera um resumo de texto usando a API do OpenAI."
    )
    @PostMapping("/resumo")
    public ResponseEntity<AiResumoResponseDTO> gerarResumo(
            @RequestBody @Valid AiResumoRequestDTO request) {

        String resumo = service.gerarResumo(request.getTexto());

        return ResponseEntity.ok(new AiResumoResponseDTO(resumo));
    }

}
