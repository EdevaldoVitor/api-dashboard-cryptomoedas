package com.api.crypto.dashboard.controller;

import com.api.crypto.dashboard.dto.AuthDTO;
import com.api.crypto.dashboard.model.User;
import com.api.crypto.dashboard.repository.UserRepository;
import com.api.crypto.dashboard.security.SecurityUser;
import com.api.crypto.dashboard.service.TokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private TokenService tokenService;

    private User usuarioTeste;

    @BeforeEach
    void setUp() {
        usuarioTeste = new User(
                "João Silva",
                "joao123",
                "$2a$10$hashedpassword");
    }


    @Test
    @DisplayName("POST /auth/login — deve retornar 200 e o token JWT com credenciais válidas")
    void loginDeveRetornar200ComCredenciaisValidas() throws Exception {
        var authToken = new UsernamePasswordAuthenticationToken(
                usuarioTeste, null, List.of()
        );
        SecurityUser securityUser = new SecurityUser(usuarioTeste);

        when(authenticationManager.authenticate(any()))
                .thenReturn(new UsernamePasswordAuthenticationToken(
                        securityUser, null, securityUser.getAuthorities()));

        when(userRepository.findByUserName("joao123"))
                .thenReturn(Optional.of(usuarioTeste));

        when(tokenService.generateToken("joao123"))
                .thenReturn("jwt-token-simulado");

        AuthDTO requestBody = new AuthDTO("joao123", "senha@123");

        // ACT + ASSERT
        mockMvc.perform(
                        post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestBody))
                )
                // Verifica o status HTTP
                .andExpect(status().isOk())
                // Verifica campos do JSON de resposta
                .andExpect(jsonPath("$.username").value("joao123"))
                .andExpect(jsonPath("$.fullName").value("João Silva"))
                .andExpect(jsonPath("$.token").value("jwt-token-simulado"));
    }

    @Test
    @DisplayName("POST /auth/login — deve retornar 401 com credenciais inválidas")
    void loginDeveRetornar401ComCredenciaisInvalidas() throws Exception {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Credenciais inválidas"));

        AuthDTO requestBody = new AuthDTO("joao123", "senha-errada");

        mockMvc.perform(
                        post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestBody))
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /auth/register — deve retornar 200 e os dados do usuário criado")
    void registerDeveRetornar200ComDadosValidos() throws Exception {
        when(userRepository.findByUserName("maria456")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(usuarioTeste);
        String requestBody = """
                {
                    "fullName": "Maria Souza",
                    "username": "maria456",
                    "password": "senha@456"
                }
                """;

        mockMvc.perform(
                        post("/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").exists())
                .andExpect(jsonPath("$.userName").exists());
    }

    @Test
    @DisplayName("POST /auth/register — deve retornar 409 quando o username já existir")
    void registerDeveRetornar409QuandoUsernameJaExiste() throws Exception {
        when(userRepository.findByUserName("joao123"))
                .thenReturn(Optional.of(usuarioTeste));

        String requestBody = """
                {
                    "fullName": "João Silva",
                    "username": "joao123",
                    "password": "senha@123"
                }
                """;

        mockMvc.perform(
                        post("/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("O Usuário informado já está cadastrado!"));
    }

    @Test
    @DisplayName("POST /auth/login — deve retornar 401 quando o body estiver incompleto")
    void loginDeveRetornar401ComBodyIncompleto() throws Exception {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new org.springframework.security.authentication
                        .BadCredentialsException("Credenciais ausentes"));

        mockMvc.perform(
                        post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}")
                )
                .andExpect(status().isUnauthorized());
    }
}