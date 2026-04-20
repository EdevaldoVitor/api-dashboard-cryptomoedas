package com.api.crypto.dashboard.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;

class TokenServiceTest {

    private TokenService tokenService;

    @BeforeEach
    void setUp() {
        tokenService = new TokenService();
        // Injeta o valor que normalmente viria do application.properties
        ReflectionTestUtils.setField(tokenService, "secret", "minha-chave-secreta-de-teste");
    }

    // ── Geração de token ──────────────────────────────────────────

    @Test
    @DisplayName("Deve gerar um token JWT não-nulo para um username válido")
    void deveGerarTokenParaUsernameValido() {
        // ARRANGE — prepara os dados de entrada
        String username = "joao123";

        // ACT — executa a ação que queremos testar
        String token = tokenService.generateToken(username);

        // ASSERT — verifica o resultado
        assertThat(token)
                .isNotNull()
                .isNotBlank()
                // Um JWT sempre tem 3 partes separadas por ponto
                .contains(".");
    }

    @Test
    @DisplayName("Tokens gerados para usuários diferentes devem ser diferentes")
    void deveGerarTokensDiferentesParaUsuariosDiferentes() {
        String tokenA = tokenService.generateToken("usuario_a");
        String tokenB = tokenService.generateToken("usuario_b");

        assertThat(tokenA).isNotEqualTo(tokenB);
    }

    @Test
    @DisplayName("Deve retornar o username correto ao validar um token válido")
    void deveRetornarUsernameAoValidarTokenValido() {
        String username = "joao123";

        String token = tokenService.generateToken(username);
        String resultado = tokenService.validateToken(token);

        assertThat(resultado).isEqualTo(username);
    }

    @Test
    @DisplayName("Deve retornar string vazia ao validar um token inválido")
    void deveRetornarVazioParaTokenInvalido() {
        String resultado = tokenService.validateToken("token.invalido.qualquer");
        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("Deve retornar string vazia para token nulo ou em branco")
    void deveRetornarVazioParaTokenNuloOuBranco() {
        assertThat(tokenService.validateToken("")).isEmpty();
        assertThat(tokenService.validateToken("   ")).isEmpty();
    }

    @Test
    @DisplayName("Token gerado e validado deve ser idempotente")
    void tokenGeradoEValidadoDeveSerIdempotente() {
        String username = "maria456";

        String token = tokenService.generateToken(username);

        assertThat(tokenService.validateToken(token))
                .isEqualTo(tokenService.validateToken(token))
                .isEqualTo(username);
    }
}
