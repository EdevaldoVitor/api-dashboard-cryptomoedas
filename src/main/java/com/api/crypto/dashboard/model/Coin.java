package com.api.crypto.dashboard.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Coin {

    @Id
    private String id;

    private String symbol;
    private String name;
    private Double currentPrice;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

}
