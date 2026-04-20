package com.api.crypto.dashboard.controller;

import com.api.crypto.dashboard.service.OpenAIService;
import com.api.crypto.dashboard.service.TokenService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AiResumoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OpenAIService openAIService;

    @MockitoBean
    private TokenService tokenService;

    @Test
    @WithMockUser
    @DisplayName("POST /api/ia/resumo — deve retornar 200 com o resumo gerado pela IA")
    void deveRetornarResumoGeradoPelaIA() throws Exception {
        when(openAIService.gerarResumo(anyString()))
                .thenReturn("Bitcoin é uma criptomoeda descentralizada criada em 2009.");

        mockMvc.perform(
                        post("/api/ia/resumo")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        { "texto": "O Bitcoin é a primeira criptomoeda descentralizada do mundo." }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resumo")
                        .value("Bitcoin é uma criptomoeda descentralizada criada em 2009."));

        verify(openAIService, times(1)).gerarResumo(anyString());
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/ia/resumo — deve retornar 400 quando o campo 'texto' for nulo")
    void deveRetornar400QuandoTextoForNulo() throws Exception {
        mockMvc.perform(
                        post("/api/ia/resumo")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        { "texto": null }
                                        """)
                )
                .andExpect(status().isBadRequest());

        verify(openAIService, never()).gerarResumo(anyString());
    }

    @Test
    @DisplayName("POST /api/ia/resumo — deve retornar 403 sem autenticação")
    void deveRetornar403SemAutenticacao() throws Exception {
        mockMvc.perform(
                        post("/api/ia/resumo")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        { "texto": "Algum texto" }
                                        """)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/ia/resumo — deve retornar 500 quando a IA lançar exceção")
    void deveRetornar500QuandoIALancarExcecao() throws Exception {
        when(openAIService.gerarResumo(anyString()))
                .thenThrow(new RuntimeException("Erro de comunicação com a OpenAI"));

        mockMvc.perform(
                        post("/api/ia/resumo")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        { "texto": "Texto qualquer para resumo." }
                                        """)
                )
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error")
                        .value("Erro de comunicação com a OpenAI"));
    }
}