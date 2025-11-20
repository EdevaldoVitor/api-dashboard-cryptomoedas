package com.api.crypto.dashboard.service;

import com.api.crypto.dashboard.model.Coin;
import com.api.crypto.dashboard.model.User;
import com.api.crypto.dashboard.repository.CoinRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Service
public class CoinService {

    private final CoinRepository coinRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    public CoinService(CoinRepository coinRepository) {
        this.coinRepository = coinRepository;
    }

    // Busca todas as moedas do CoinGecko (mercado USD)
    public List<Object> getAllCoinsFromAPI() {
        String url = "https://api.coingecko.com/api/v3/coins/markets?vs_currency=usd";
        Object[] response = restTemplate.getForObject(url, Object[].class);
        return Arrays.asList(response);
    }

    // Salvar moeda favorita do usuário
    public Coin saveFavoriteCoin(Coin coin) {
        return coinRepository.save(coin);
    }

    // Listar moedas favoritas de um usuário
    public List<Coin> getFavoriteCoins(User user) {
        return coinRepository.findByUser(user);
    }
}
