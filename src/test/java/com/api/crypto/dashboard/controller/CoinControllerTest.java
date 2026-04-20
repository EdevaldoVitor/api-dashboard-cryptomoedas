package com.api.crypto.dashboard.controller;

import com.api.crypto.dashboard.model.Coin;
import com.api.crypto.dashboard.model.User;
import com.api.crypto.dashboard.repository.UserRepository;
import com.api.crypto.dashboard.service.CoinService;
import com.api.crypto.dashboard.service.OpenAIService;
import com.api.crypto.dashboard.service.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CoinControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CoinService coinService;

    @MockitoBean
    private OpenAIService openAIService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private TokenService tokenService;

    private User usuarioTeste;
    private Coin bitcoin;
    private Coin ethereum;

    @BeforeEach
    void setUp() {
        usuarioTeste = new User("João Silva", "joao123", "hash");

        bitcoin = new Coin();
        bitcoin.setCoinId("bitcoin");
        bitcoin.setSymbol("btc");
        bitcoin.setName("Bitcoin");
        bitcoin.setCurrentPrice(95000.0);
        bitcoin.setUser(usuarioTeste);

        ethereum = new Coin();
        ethereum.setCoinId("ethereum");
        ethereum.setSymbol("eth");
        ethereum.setName("Ethereum");
        ethereum.setCurrentPrice(3500.0);
        ethereum.setUser(usuarioTeste);
    }

    @Test
    @WithMockUser(username = "joao123")   // simula usuário autenticado
    @DisplayName("GET /api/coins/markets — deve retornar 200 com a lista de moedas")
    void deveRetornarListaDeMoedas() throws Exception {
        when(coinService.getAllCoinsFromAPI()).thenReturn(List.of("bitcoin", "ethereum"));

        mockMvc.perform(get("/api/coins/markets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("GET /api/coins/markets — deve retornar 403 sem autenticação")
    void deveRetornar403SemAutenticacao() throws Exception {
        mockMvc.perform(get("/api/coins/markets"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "joao123")
    @DisplayName("POST /api/coins/favorite — deve salvar moeda favorita e retornar 200")
    void deveSalvarMoedaFavorita() throws Exception {
        when(userRepository.findByUserName("joao123"))
                .thenReturn(Optional.of(usuarioTeste));
        when(coinService.saveFavoriteCoin(any(Coin.class))).thenReturn(bitcoin);

        // Monta o JSON de entrada (sem o campo "user", que é resolvido internamente)
        String requestBody = """
                {
                    "coinId": "bitcoin",
                    "symbol": "btc",
                    "name": "Bitcoin",
                    "currentPrice": 95000.0,
                    "image": "https://example.com/btc.png"
                }
                """;

        mockMvc.perform(
                        post("/api/coins/favorite")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.coinId").value("bitcoin"))
                .andExpect(jsonPath("$.name").value("Bitcoin"))
                .andExpect(jsonPath("$.currentPrice").value(95000.0));

        verify(coinService, times(1)).saveFavoriteCoin(any(Coin.class));
    }

    @Test
    @WithMockUser(username = "joao123")
    @DisplayName("GET /api/coins/favorites — deve retornar lista de favoritos do usuário")
    void deveRetornarFavoritosDoUsuario() throws Exception {
        when(userRepository.findByUserName("joao123"))
                .thenReturn(Optional.of(usuarioTeste));
        when(coinService.getFavoriteCoins(usuarioTeste))
                .thenReturn(List.of(bitcoin, ethereum));

        mockMvc.perform(get("/api/coins/favorites"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].coinId").value("bitcoin"))
                .andExpect(jsonPath("$[1].coinId").value("ethereum"));
    }

    @Test
    @WithMockUser(username = "joao123")
    @DisplayName("GET /api/coins/favorites — deve retornar lista vazia quando não há favoritos")
    void deveRetornarListaVaziaQuandoNaoHaFavoritos() throws Exception {
        when(userRepository.findByUserName("joao123"))
                .thenReturn(Optional.of(usuarioTeste));
        when(coinService.getFavoriteCoins(usuarioTeste)).thenReturn(List.of());

        mockMvc.perform(get("/api/coins/favorites"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser(username = "joao123")
    @DisplayName("GET /api/coins/favorites/resume — deve retornar resumo IA quando há favoritos")
    void deveRetornarResumoIAComFavoritos() throws Exception {
        when(userRepository.findByUserName("joao123"))
                .thenReturn(Optional.of(usuarioTeste));
        when(coinService.getFavoriteCoins(usuarioTeste))
                .thenReturn(List.of(bitcoin, ethereum));
        when(openAIService.gerarResumoFavoritos(any()))
                .thenReturn("Bitcoin e Ethereum apresentam tendência de alta.");

        mockMvc.perform(get("/api/coins/favorites/resume"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resumo")
                        .value("Bitcoin e Ethereum apresentam tendência de alta."))
                .andExpect(jsonPath("$.totalMoedas").value(2));
    }

    @Test
    @WithMockUser(username = "joao123")
    @DisplayName("GET /api/coins/favorites/resume — deve retornar mensagem padrão sem favoritos")
    void deveRetornarMensagemPadraoSemFavoritos() throws Exception {
        when(userRepository.findByUserName("joao123"))
                .thenReturn(Optional.of(usuarioTeste));
        when(coinService.getFavoriteCoins(usuarioTeste)).thenReturn(List.of());

        mockMvc.perform(get("/api/coins/favorites/resume"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resumo")
                        .value("Você ainda não possui moedas favoritas cadastradas."))
                .andExpect(jsonPath("$.totalMoedas").value(0));

        // Garante que a IA NÃO foi chamada (otimização: lista vazia)
        verify(openAIService, never()).gerarResumoFavoritos(any());
    }

    @Test
    @WithMockUser(username = "joao123")
    @DisplayName("DELETE /api/coins/{id} — deve retornar 204 ao remover moeda existente")
    void deveDeletarMoedaERetornar204() throws Exception {
        doNothing().when(coinService).deleteById(1L);

        mockMvc.perform(delete("/api/coins/1"))
                .andExpect(status().isNoContent());

        verify(coinService, times(1)).deleteById(1L);
    }

    @Test
    @WithMockUser(username = "joao123")
    @DisplayName("DELETE /api/coins/{id} — deve retornar 500 quando moeda não existe")
    void deveRetornarErroAoDeletarMoedaInexistente() throws Exception {
        doThrow(new RuntimeException("Moeda não encontrada"))
                .when(coinService).deleteById(99L);

        mockMvc.perform(delete("/api/coins/99"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Moeda não encontrada"));
    }
}