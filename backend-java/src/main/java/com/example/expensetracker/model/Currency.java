package com.example.expensetracker.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder.Default;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "currency_rates")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Currency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty("_id")
    private Long id;

    @Column(nullable = false, unique = true)
    @Default
    private String baseCurrency = "BYN";

    @ElementCollection
    @CollectionTable(name = "currency_rate_values", joinColumns = @JoinColumn(name = "currency_id"))
    @MapKeyColumn(name = "code")
    @Column(name = "rate")
    @Default
    private Map<String, Double> rates = new HashMap<>();

    @Default
    private String source = "Manual Entry";

    @Column(nullable = false, updatable = false)
    @Default
    private Instant createdAt = Instant.now();

    @Default
    private Instant updatedAt = Instant.now();

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }
}