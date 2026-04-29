package com.api.crypto.dashboard.controller;

import com.api.crypto.dashboard.dto.AiResumoFavoritosResponseDTO;
import com.api.crypto.dashboard.model.Coin;
import com.api.crypto.dashboard.model.User;
import com.api.crypto.dashboard.repository.UserRepository;
import com.api.crypto.dashboard.service.CoinService;
import com.api.crypto.dashboard.service.OpenAIService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Coins", description = "Listagem de moedas e gerenciamento de favoritos do usuário")
@SecurityRequirement(name = "bearerAuth")
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

    @Operation(
            summary = "Listar moedas do mercado",
            description = "Busca a lista de criptomoedas da API externa (CoinGecko)."
    )
    @GetMapping("/markets")
    public ResponseEntity<List<Object>> getAllCoins() {
        return ResponseEntity.ok(coinService.getAllCoinsFromAPI());
    }

    @Operation(
            summary = "Salvar moeda favorita",
            description = "Adiciona uma moeda favorita para o usuário autenticado."
    )
    @PostMapping("/favorite")
    public ResponseEntity<Coin> addFavorite(@RequestBody Coin coin,
                                            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUserName(userDetails.getUsername()).orElseThrow();
        coin.setUser(user);
        Coin saved = coinService.saveFavoriteCoin(coin);
        return ResponseEntity.ok(saved);
    }

    @Operation(
            summary = "Listar moedas favoritas do usuário",
            description = "Retorna a lista de moedas favoritas do usuário autenticado."
    )
    @GetMapping("/favorites")
    public ResponseEntity<?> getFavorites(@AuthenticationPrincipal UserDetails userDetails) {

        User user = userRepository.findByUserName(userDetails.getUsername()).orElseThrow();
        List<Coin> favorites = coinService.getFavoriteCoins(user);

        return ResponseEntity.ok(favorites);
    }

    @Operation(
            summary = "Remover moeda favorita",
            description = "Remove uma moeda favorita do usuário autenticado."
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFavorite(@PathVariable Long id) {

        coinService.deleteById(id);

        return ResponseEntity.noContent().build();

    }

    @Operation(
            summary = "Resumo de IA das moedas favoritas",
            description = "Retorna um resumo das moedas favoritas do usuário autenticado."
    )
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
