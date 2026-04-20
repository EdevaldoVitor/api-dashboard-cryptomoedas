package com.api.crypto.dashboard.repository;

import com.api.crypto.dashboard.model.Coin;
import com.api.crypto.dashboard.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
class RepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CoinRepository coinRepository;

    private User usuario;

    @BeforeEach
    void setUp() {
        // Limpa o banco antes de cada teste (garantia extra)
        coinRepository.deleteAll();
        userRepository.deleteAll();

        // Persiste um usuário real no H2
        usuario = userRepository.save(
                new User("João Silva", "joao123", "$2a$10$hashfake")
        );
    }

    @Test
    @DisplayName("findByUserName — deve encontrar usuário pelo username")
    void deveBuscarUsuarioPorUsername() {
        Optional<User> resultado = userRepository.findByUserName("joao123");

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getFullName()).isEqualTo("João Silva");
        assertThat(resultado.get().getUserName()).isEqualTo("joao123");
    }

    @Test
    @DisplayName("findByUserName — deve retornar Optional vazio para username inexistente")
    void deveRetornarVazioParaUsernameInexistente() {
        Optional<User> resultado = userRepository.findByUserName("naoexiste");

        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("save — deve gerar ID automaticamente ao persistir um usuário")
    void devePersistirUsuarioComIdGerado() {
        User novoUsuario = userRepository.save(
                new User("Maria Souza", "maria456", "$2a$10$hashfake2")
        );

        assertThat(novoUsuario.getId()).isPositive();
        assertThat(novoUsuario.getUserName()).isEqualTo("maria456");
    }

    @Test
    @DisplayName("findByUser — deve retornar apenas as moedas do usuário informado")
    void deveRetornarMoedasDoUsuarioCorreto() {
        Coin btc = new Coin();
        btc.setCoinId("bitcoin");
        btc.setSymbol("btc");
        btc.setName("Bitcoin");
        btc.setCurrentPrice(95000.0);
        btc.setUser(usuario);
        coinRepository.save(btc);

        Coin eth = new Coin();
        eth.setCoinId("ethereum");
        eth.setSymbol("eth");
        eth.setName("Ethereum");
        eth.setCurrentPrice(3500.0);
        eth.setUser(usuario);
        coinRepository.save(eth);

        User outroUsuario = userRepository.save(
                new User("Carlos Lima", "carlos789", "$2a$10$hashfake3")
        );
        Coin sol = new Coin();
        sol.setCoinId("solana");
        sol.setSymbol("sol");
        sol.setName("Solana");
        sol.setCurrentPrice(200.0);
        sol.setUser(outroUsuario);
        coinRepository.save(sol);

        // Busca apenas as moedas do usuário de teste
        List<Coin> favoritos = coinRepository.findByUser(usuario);

        assertThat(favoritos)
                .hasSize(2)
                .extracting(Coin::getCoinId)
                .containsExactlyInAnyOrder("bitcoin", "ethereum")
                // Garante que a moeda do outro usuário NÃO está aqui
                .doesNotContain("solana");
    }

    @Test
    @DisplayName("findByUser — deve retornar lista vazia quando usuário não tem favoritos")
    void deveRetornarListaVaziaParaUsuarioSemFavoritos() {
        List<Coin> favoritos = coinRepository.findByUser(usuario);

        assertThat(favoritos).isEmpty();
    }

    @Test
    @DisplayName("existsById — deve retornar true para moeda existente")
    void deveRetornarTrueParaMoedaExistente() {
        Coin btc = new Coin();
        btc.setCoinId("bitcoin");
        btc.setName("Bitcoin");
        btc.setCurrentPrice(95000.0);
        btc.setUser(usuario);
        Coin salvo = coinRepository.save(btc);

        boolean existe = coinRepository.existsById(String.valueOf(salvo.getId()));

        assertThat(existe).isTrue();
    }

    @Test
    @DisplayName("existsById — deve retornar false para ID inexistente")
    void deveRetornarFalseParaIdInexistente() {
        boolean existe = coinRepository.existsById("99999");

        assertThat(existe).isFalse();
    }
}
