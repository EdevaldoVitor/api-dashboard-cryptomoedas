package com.api.crypto.dashboard.controller;

import com.api.crypto.dashboard.dto.AiResumoFavoritosResponseDTO;
import com.api.crypto.dashboard.model.Coin;
import com.api.crypto.dashboard.model.User;
import com.api.crypto.dashboard.repository.UserRepository;
import com.api.crypto.dashboard.service.CoinService;
import com.api.crypto.dashboard.service.OpenAIService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/coins")
public class CoinController {

    private final CoinService coinService;
    private final UserRepository userRepository;
    private final OpenAIService openAIService;

    public CoinController(
            CoinService coinService,
            OpenAIService openAIService,
            UserRepository userRepository) {

        this.coinService = coinService;
        this.openAIService = openAIService;
        this.userRepository = userRepository;
    }

    // Buscar todas as moedas (da API externa)
    @GetMapping("/markets")
    public ResponseEntity<List<Object>> getAllCoins() {
        return ResponseEntity.ok(coinService.getAllCoinsFromAPI());
    }

    // Salvar moeda favorita
    @PostMapping("/favorite")
    public ResponseEntity<Coin> addFavorite(@RequestBody Coin coin,
                                            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUserName(userDetails.getUsername()).orElseThrow();
        coin.setUser(user);
        Coin saved = coinService.saveFavoriteCoin(coin);
        return ResponseEntity.ok(saved);
    }

    // Listar moedas favoritas do usuário
    @GetMapping("/favorites")
    public ResponseEntity<?> getFavorites(@AuthenticationPrincipal UserDetails userDetails) {

        User user = userRepository.findByUserName(userDetails.getUsername()).orElseThrow();
        List<Coin> favorites = coinService.getFavoriteCoins(user);

        return ResponseEntity.ok(favorites);
    }

    // Remover moeda favorita
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFavorite(@PathVariable Long id) {

        coinService.deleteById(id);

        return ResponseEntity.noContent().build();

    }

    @GetMapping("/favorites/resume")
    public ResponseEntity<AiResumoFavoritosResponseDTO> resumoFavoritos(
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userRepository
                .findByUserName(userDetails.getUsername())
                .orElseThrow();

        List<Coin> favoritos = coinService.getFavoriteCoins(user);

        if (favoritos.isEmpty()) {
            return ResponseEntity.ok(
                    new AiResumoFavoritosResponseDTO(
                            "Você ainda não possui moedas favoritas cadastradas.",
                            0
                    )
            );
        }

        String resumo = openAIService.gerarResumoFavoritos(favoritos);

        return ResponseEntity.ok(
                new AiResumoFavoritosResponseDTO(resumo, favoritos.size())
        );
    }

}
