package com.api.crypto.dashboard.controller;

import com.api.crypto.dashboard.dto.AiResumoRequestDTO;
import com.api.crypto.dashboard.dto.AiResumoResponseDTO;
import com.api.crypto.dashboard.model.Coin;
import com.api.crypto.dashboard.service.OpenAIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;

@Controller
@RequestMapping("/api/ia")
public class AiResumoController {

    private final OpenAIService service;

    public AiResumoController(OpenAIService service) {
        this.service = service;
    }

    @PostMapping("/resumo")
    public ResponseEntity<AiResumoResponseDTO> gerarResumo(
            @RequestBody @Valid AiResumoRequestDTO request) {

        String resumo = service.gerarResumo(request.getTexto());

        return ResponseEntity.ok(new AiResumoResponseDTO(resumo));
    }

}
