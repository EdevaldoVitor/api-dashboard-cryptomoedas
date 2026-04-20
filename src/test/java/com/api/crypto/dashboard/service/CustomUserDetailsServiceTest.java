package com.api.crypto.dashboard.service;

import com.api.crypto.dashboard.model.User;
import com.api.crypto.dashboard.repository.UserRepository;
import com.api.crypto.dashboard.security.SecurityUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    @Test
    @DisplayName("Deve retornar UserDetails quando o usuário existir")
    void deveRetornarUserDetailsQuandoUsuarioExiste() {
        // ARRANGE
        User usuario = new User("João Silva", "joao123", "$2a$10$hashedpassword");
        when(userRepository.findByUserName("joao123")).thenReturn(Optional.of(usuario));

        // ACT
        UserDetails resultado = userDetailsService.loadUserByUsername("joao123");

        // ASSERT
        assertThat(resultado).isNotNull();
        assertThat(resultado.getUsername()).isEqualTo("joao123");
        verify(userRepository, times(1)).findByUserName("joao123");
    }

    @Test
    @DisplayName("Deve lançar UsernameNotFoundException quando o usuário não existir")
    void deveLancarExcecaoQuandoUsuarioNaoExiste() {
        // ARRANGE — repositório retorna vazio (usuário não cadastrado)
        when(userRepository.findByUserName("naoexiste")).thenReturn(Optional.empty());

        // ASSERT + ACT — verifica que a exceção correta é lançada
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("naoexiste"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("Usuário não encontrado");
    }
}
