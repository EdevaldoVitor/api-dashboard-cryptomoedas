package com.api.crypto.dashboard.repository;

import com.api.crypto.dashboard.model.Coin;
import com.api.crypto.dashboard.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CoinRepository extends JpaRepository<Coin,String> {
    List<Coin> findByUser(User user);
}
