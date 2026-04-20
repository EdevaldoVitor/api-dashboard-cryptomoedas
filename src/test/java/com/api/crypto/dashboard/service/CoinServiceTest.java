package com.api.crypto.dashboard.service;

import com.api.crypto.dashboard.model.Coin;
import com.api.crypto.dashboard.model.User;
import com.api.crypto.dashboard.repository.CoinRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CoinServiceTest {

    @Mock
    private CoinRepository coinRepository;

    @InjectMocks
    private CoinService coinService;

    private User usuarioTeste;
    private Coin coinTeste;

    @BeforeEach
    void setUp() {
        // Monta um usuário e uma moeda reutilizáveis nos testes
        usuarioTeste = new User("João Silva", "joao123", "senha-hash");

        coinTeste = new Coin();
        coinTeste.setCoinId("bitcoin");
        coinTeste.setSymbol("btc");
        coinTeste.setName("Bitcoin");
        coinTeste.setCurrentPrice(95000.0);
        coinTeste.setUser(usuarioTeste);
    }

    @Test
    @DisplayName("Deve salvar e retornar a moeda favorita corretamente")
    void deveSalvarMoedaFavorita() {
        when(coinRepository.save(any(Coin.class))).thenReturn(coinTeste);

        // ACT
        Coin resultado = coinService.saveFavoriteCoin(coinTeste);

        // ASSERT
        assertThat(resultado).isNotNull();
        assertThat(resultado.getCoinId()).isEqualTo("bitcoin");
        assertThat(resultado.getName()).isEqualTo("Bitcoin");
        assertThat(resultado.getCurrentPrice()).isEqualTo(95000.0);

        // Verifica que o repositório foi chamado exatamente 1 vez
        verify(coinRepository, times(1)).save(coinTeste);
    }

    @Test
    @DisplayName("O ID do usuário associado à moeda deve ser preservado ao salvar")
    void deveMaterUsuarioAssociadoAoSalvar() {
        when(coinRepository.save(any(Coin.class))).thenReturn(coinTeste);

        Coin resultado = coinService.saveFavoriteCoin(coinTeste);

        assertThat(resultado.getUser()).isNotNull();
        assertThat(resultado.getUser().getUserName()).isEqualTo("joao123");
    }

    @Test
    @DisplayName("Deve retornar a lista de moedas favoritas de um usuário")
    void deveRetornarFavoritosDoUsuario() {
        Coin outraCoin = new Coin();
        outraCoin.setCoinId("ethereum");
        outraCoin.setName("Ethereum");
        outraCoin.setUser(usuarioTeste);

        when(coinRepository.findByUser(usuarioTeste))
                .thenReturn(List.of(coinTeste, outraCoin));

        List<Coin> favoritos = coinService.getFavoriteCoins(usuarioTeste);

        assertThat(favoritos)
                .hasSize(2)
                .extracting(Coin::getCoinId)
                .containsExactlyInAnyOrder("bitcoin", "ethereum");

        verify(coinRepository, times(1)).findByUser(usuarioTeste);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando usuário não tem favoritos")
    void deveRetornarListaVaziaQuandoNaoHaFavoritos() {
        when(coinRepository.findByUser(usuarioTeste)).thenReturn(List.of());

        List<Coin> favoritos = coinService.getFavoriteCoins(usuarioTeste);

        assertThat(favoritos).isEmpty();
    }

    @Test
    @DisplayName("Deve deletar a moeda quando o ID existir")
    void deveDeletarMoedaQuandoIdExiste() {
        Long id = 1L;
        when(coinRepository.existsById(String.valueOf(id))).thenReturn(true);

        assertThatCode(() -> coinService.deleteById(id))
                .doesNotThrowAnyException();

        verify(coinRepository, times(1)).deleteById(String.valueOf(id));
    }

    @Test
    @DisplayName("Deve lançar RuntimeException quando o ID não existir")
    void deveLancarExcecaoQuandoIdNaoExiste() {
        Long id = 99L;
        when(coinRepository.existsById(String.valueOf(id))).thenReturn(false);

        assertThatThrownBy(() -> coinService.deleteById(id))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Moeda não encontrada");

        verify(coinRepository, never()).deleteById(any());
    }
}
