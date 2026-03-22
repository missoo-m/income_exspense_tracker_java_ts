package com.example.expensetracker.repository;

import com.example.expensetracker.model.Currency;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CurrencyRepository extends JpaRepository<Currency, Long> {
    Optional<Currency> findByBaseCurrency(String baseCurrency);
}

