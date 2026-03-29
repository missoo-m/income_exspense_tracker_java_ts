package com.example.expensetracker.unit.service;

import com.example.expensetracker.service.CurrencyRateService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class CurrencyRateServiceTest {

    @InjectMocks
    private CurrencyRateService currencyRateService;

    @Test
    void getCurrentRatesCached_ShouldReturnRates() {
        Map<String, Object> result = currencyRateService.getCurrentRatesCached();

        assertThat(result).isNotNull();
        assertThat(result.get("baseCurrency")).isEqualTo("BYN");
        assertThat(result.get("source")).isEqualTo("NBRB");
        assertThat(result.get("rates")).isNotNull();
    }
}
